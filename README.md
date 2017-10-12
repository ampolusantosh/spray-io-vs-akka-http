# Akka Actors vs Streams for Rest APIs
A comparison of use cases for Spray IO (on Akka Actors) and Akka Http (on Akka Streams) for creating rest APIs

This repo has code meant for [this](https://anish749.github.io/scala/akka-actors-streams-rest-api/) blog post.

### Build this repo
This has two projects. One using Spray IO and another using Akka Http.
Both needs to be built individually, and can be done using the following:
```
sbt package
```

To edit the code / run, load the desired project in IntelliJ and run the appropriate object with a main method.

This is not readily packaged to be deployed in the cloud or things like that. This is only meant for learning and understanding of the frameworks underneath.
