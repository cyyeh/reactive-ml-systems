# [Chapter 2: Using reactive tools](https://hackmd.io/@distributed-systems-engineering/reactive-ml-sytems-ch2)

###### tags: `ml-systems`

## Outline

- Managing uncertainty with Scala
- Implementing supervision and fault tolerance with Akka
- Using Spark and MLlib as frameworks for distributed machine learning pipelines

## Brief Summary

- Scala gives you constructs to help you reason about uncertainty:
    - `Options` abstract over the uncertainty of something being present or not.
    - `Futures` abstract over the uncertainty of actions, which take time.
    - `Futures` give you the ability to implement timeouts, which help ensure responsiveness through bounding response times.
- With Akka, you can build protections against failure into the structure of your application using the power of the actor model:
    - Communication via message passing helps you keep system components contained.
    - Supervisory hierarchies can help ensure resilience of components.
    - One of the best ways to use the power of the actor model is in libraries that use it behind the scenes, instead of doing much of the definition of the actor system directly in your code.
- Spark gives you reasonable components to build data-processing pipelines:
    - Spark pipelines are constructed using pure functions and immutable transformations.
    - Spark uses laziness to ensure efficient, reliable execution.
    - MLlib provides useful tools for building and evaluating models with a minimum of code.

## Key Points