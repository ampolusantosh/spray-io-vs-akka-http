package org.anish.akka

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.collection.mutable.{HashMap => mHashMap}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

/**
  * A simple server which can create tasks when requested and stores the state of each task created.
  * We can query the status of the Tasks as well. The state of the task is store in memory in a
  * Task Manager Actor.
  *
  * Created by anish on 12/10/17.
  */
object TaskManagerServer {

  object TaskStatuses extends Enumeration with Serializable {
    type TaskStatus = Value
    val CREATED = Value("created")
    val RUNNING = Value("running")
    val COMPLETED = Value("completed")
    val UNDEFINED = Value("undefined") // Task doesn't exist
  }

  // Spray Json Formats for TaskStatus Enum
  class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
    override def write(obj: T#Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): T#Value = {
      json match {
        case JsString(txt) => enu.withName(txt)
        case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
      }
    }
  }

  // Commands to the Server
  case object CreateTask
  case class GetTaskStatus(taskId:Int)
  case object GetRunningTasks
  case object GetCompletedTasks

  // Server response objects
  case class TaskStatusResponse(taskId:Int, taskStatus: TaskStatuses.TaskStatus)
  case class ManagedTasks(tasks: List[TaskStatusResponse]) // This is for response of running / completed tasks

  // json marshallers from spray-json
  implicit val taskStatusEnumFormat = new EnumJsonConverter(TaskStatuses)
  implicit val getTaskStatusResponseFormat = jsonFormat2(TaskStatusResponse)
  implicit val managedTasksFormat = jsonFormat1(ManagedTasks)

  /**
    * Our actual task. This is just dummy task which sleeps
    * @param taskId
    * @param taskStatus
    */
  class Task(val taskId: Int, var taskStatus: TaskStatuses.TaskStatus){
    def start() = {
      taskStatus = TaskStatuses.RUNNING
      Thread.sleep(3000)
      taskStatus = TaskStatuses.COMPLETED
    }
  }

  /**
    * Task Manager Actor running inside our Server
    */
  class TaskManager extends Actor with ActorLogging {
    implicit val executionContext = context.dispatcher // Used for starting task in Future

    val monitoredTasks = mHashMap[Int, Task]() // mutable HashMap - state store in Actor
    // We should probably use a Cache to that very old task status are no longer stored
    var tasksCreated = 0 // Initial value

    def receive = {
      case CreateTask =>
        tasksCreated += 1
        val task = new Task(tasksCreated, TaskStatuses.CREATED)
        monitoredTasks.put(tasksCreated, task) // tasksCreated is also the taskId for the currently task
        Future(task.start()) // Start the task in a separate thread, so that the task manager is not blocked.
      case GetTaskStatus(taskId) =>
        val taskStatusForId = TaskStatusResponse(taskId, monitoredTasks.get(taskId).map(_.taskStatus).getOrElse(TaskStatuses.UNDEFINED))
        sender() ! taskStatusForId
      case GetRunningTasks =>
        val running = ManagedTasks(monitoredTasks.valuesIterator.filter(_.taskStatus == TaskStatuses.RUNNING).map(task => TaskStatusResponse(task.taskId, task.taskStatus)).toList)
        sender() ! running
      case GetCompletedTasks =>
        val completed = ManagedTasks(monitoredTasks.valuesIterator.filter(_.taskStatus == TaskStatuses.COMPLETED).map(task => TaskStatusResponse(task.taskId, task.taskStatus)).toList)
        sender() ! completed
      case _ => log.info("Invalid message")
    }

  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val taskManager = system.actorOf(Props[TaskManager], "task_manager")

    implicit val timeout: Timeout = 5.seconds // Request time out.

    val route =
      path("create") {
        put {
          // create a new task, fire-and-forget
          taskManager ! CreateTask
          complete((StatusCodes.Accepted, "Task Created"))
        }
      } ~ path("running") {
        get {
          val runningTasks = taskManager ? GetRunningTasks // This returns a future
          complete(runningTasks.mapTo[ManagedTasks])
        }
      } ~ path("completed") {
        get {
          val completedTasks = taskManager ? GetCompletedTasks // This returns a future
          complete(completedTasks.mapTo[ManagedTasks]) // We complete the request with a Future here
        }
      } ~ path("status") {
        get {
          parameter("taskId".as[Int]) { (taskId) =>
            val taskStatus = taskManager ? GetTaskStatus(taskId) // This returns a future
            complete(taskStatus.mapTo[TaskStatusResponse])
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

