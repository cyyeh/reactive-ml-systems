# [Reactive Machine Learning Systems](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems)

###### tags: `ml-systems`

## Overview

Machine Learning Systems: Designs that scale teaches you to design and implement production-ready ML systems. You'll learn the principles of reactive design as you build pipelines with Spark, create highly scalable services with Akka, and use powerful machine learning libraries like MLib on massive datasets. The examples use the Scala language, but the same ideas and tools work in Java, as well.

## Chapters

### PART 1 FUNDAMENTALS OF REACTIVE MACHINE LEARNING

- [Learning reactive machine learning](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch1)
    - Chapter 1 introduces machine learning, reactive systems, and the goals of reac- tive machine learning.
- [Using reactive tools](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch2)
    - Chapter 2 introduces three of the technologies the book uses: Scala, Spark, and Akka.

### PART 2 BUILDING A REACTIVE MACHINE LEARNING SYSTEM

- [Collecting data](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch3)
    - Chapter 3 discusses the challenges of collecting data and ingesting it into a machine learning system. As part of that, it introduces various concepts around handling uncertain data. It also goes into detail about how to persist data, focusing on properties of distributed databases.
- Generating features
    - Chapter 4 gets into how you can extract features from raw data and the various ways in which you can compose this functionality.
- Learning models
    - Chapter 5 covers model learning. You’ll implement your own model learning algorithms and use library implementations. It also covers how to work with model learning algorithms from other languages.
- Evaluating models
    - Chapter 6 covers a range of concerns related to evaluating models once they’ve been learned.
- Publishing models
    - Chapter 7 shows how to take learned models and make them available for use. In the service of this goal, this chapter introduces Akka HTTP, microservices, and containerization via Docker.
- Responding
    - Chapter 8 is all about using machine learned models to act on the real world. It also introduces an alternative to Akka HTTP for building services: http4s.


### PART 3 OPERATING A MACHINE LEARNING SYSTEM

- Delivering
    - Chapter 9 shows how to build Scala applications using SBT. It also introduces concepts from continuous delivery.
- Evolving intelligence
    - Chapter 10 shows how to build artificially intelligent agents of various levels of complexity as an example of system evolution. It also covers more techniques for analyzing the reactive properties of a machine learning system.