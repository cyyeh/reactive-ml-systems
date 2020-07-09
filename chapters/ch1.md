---
title: "Chapter 1: Learning reactive machine learning"
description: Understanding the reactive systems design paradigm and the reactive approach to build machine learning systems
image: https://i.imgur.com/l5oZ7l6.png
---

# [Chapter 1: Learning reactive machine learning](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch1)

###### tags: `ml-systems`

![](https://i.imgur.com/l5oZ7l6.png)

## Outline

- Introducing the components of machine learning systems
- Understanding the reactive systems design paradigm
- The reactive approach to building machine learning systems

## Brief Summary

- Even simple machine learning systems can fail.
- Machine learning should be viewed as an application, not as a technique.
- A machine learning system is composed of five components, or phases:
    - The data-collection component ingests data from the outside world into the machine learning system
    - The data-transformation component transforms raw data into useful derived representations of that data: features and concepts.
    - The model-learning component learns models from the features and concepts.
    - The model-publishing component makes a model available to make predictions.
    - The model-serving component connects models to requests for predictions.
- The reactive systems design paradigm is a coherent approach to building better systems:
    - Reactive systems are responsive, resilient, elastic, and message-driven.
    - Reactive systems use the strategies of replication, containment, and supervision as concrete approaches for maintaining the reactive traits.

## Key Points

### An example machine learning system

- This book is all about how to build machine learning systems, which are sets of soft- ware components capable of learning from data and making predictions about the future.
- I’ll introduce you to a fundamentally new and better way of building machine learning systems called reactive machine learning. Reactive machine learning represents the marriage of ideas from reactive systems and the unique challenges of machine learning.

#### Building a prototype system

- Context
![](https://i.imgur.com/6IwCJqi.png)
- Pooch Predictor 1.0 architecture
![](https://i.imgur.com/fTHSldc.png)
    - Problems
        - The model couldn't be updated in scheduled time
- Pooch Predictor 1.1 architecture
![](https://i.imgur.com/0ixAKxz.png)
    - Description
        - Learned models were inserted directly into database, since the backend server was responsible for producing the predictions displayed in the app
        - With cron jobs, now models could be updated in scheduled time
    - Problems
        - Often something would change in the data- base, and one of the queries would fail
        - Other times there would be high load on the server, and the modeling job would fail
        - One time, the server that was supposed to be running the data- processing job failed, and all the relevant data was lost
        - Once the data scientist got at the data, he realized that some of the features weren’t being correctly extracted from the raw data
        - It was also really hard to understand how a change to the features that were being extracted would impact modeling performance
        - Sending the data from the app back to the server required a transaction. When the data scientist and engineer added more data to the total amount of data being collected for modeling, this transaction took way too long to maintain reasonable responsiveness within the app
        - The prediction functionality within the server that supported the app didn’t handle the new features properly. The server would throw an exception every time the prediction functionality saw any of the new features that had been added in another part of the application
        
#### Building a better system

- The Sniffable app must remain responsive, regardless of any other problems with the predictive system.
- The predictive system must be considerably less tightly coupled to the rest of the systems.
- The predictive system must behave predictably regardless of high load or errors in the system itself.
- It should be easier for different developers to make changes to the predictive system without breaking things.
- The code must use different programming idioms that ensure better performance when used consistently.
- The predictive system must measure its modeling performance better.
- The predictive system should support evolution and change.
- The predictive system should support online experimentation.
- It should be easy for humans to supervise the predictive system and rapidly correct any rogue behavior.

### Reactive machine learning

![](https://i.imgur.com/MjAlyTm.png)

#### Reactive systems

- Reactive systems are defined by four traits and three strategies. The paradigm as a whole is a way of codify- ing an approach to building systems that can serve modern user expectations for things like interactivity and availability.
- Traits of Reactive Systems
![](https://i.imgur.com/tZDj9JW.png)
    - **responsive**: they consistently return timely responses to users
    - **resilient**: they need to maintain responsiveness in the face of failure
        - Providing some sort of acceptable response even when things don’t go as planned is a key part of ensuring that users view a system as being responsive
    - **elastic**: they need to remain responsive despite varying loads
        - Elastic systems should respond to increases or decreases in load
    - **message-driven**: they communicate via asynchronous, non-blocking message passing
        - A loosely coupled system organized around message passing can make it easier to detect failure or issues with load
        - A design with this trait helps contain any of the effects of errors to just messages about bad news, rather than flaming production issues that need to be immediately addressed
- Reactive Strategies
![](https://i.imgur.com/BbqxhGU.png)
    - **replication**: they have the same component executing in more than one place at the same time
        - More generally, this means that data, whether at rest or in motion, should be redundantly stored or processed
        - Spark gives you automatic, fine-grained replication so that the system can recover from failure
    - **containment**: to prevent the failure of any single component of the system from affecting any other component
        - The term containment might get you thinking about specific technologies like Docker and rkt, but this strategy isn’t about any one implementation
    - **supervision**: to organize components, you explicitly identify the components that could fail and make sure that some other component is responsible for their lifecycles
        - Should their behavior deviate from acceptable bounds, the supervisor would stop sending them messages requesting predictions. In fact, the model supervisor could even completely destroy a model it knows to be bad, making the system potentially self-healing

#### Making machine learning systems reactive

![](https://i.imgur.com/Qcy0tHL.png)

- Ultimately, a reactive machine learning system gives you the ability to deliver value through ever better predictions. That’s why reactive machine learning is worth understanding and applying.
- The reactive machine learning approach is based on two key insights into the characteristics of data in a machine learning system: it is uncertain, and it is effectively infinite.
- data characteristics: **infinite data**
    - strategy 1: **laziness**
        - delay of execution
    - strategy 2: **pure functions**
        - evaluating the function must not result in some sort of side effect, such as changing the state of a variable or performing I/O
        - referential transparency: the function must always return the same value when given the same arguments
        - pure functions are a foundational concept in a style of programming known as functional programming, which we’ll use throughout this book
- data characteristics: **uncertain data**
    - strategy 1: **immutable facts**
        - this data can be written once and never modified; it is written in stone. The use of immutable facts allows us to reason about uncertain views of the world at specific points in time
    - strategy 2: **possible worlds**
        - by building our data models and queries with the concept of possible alternative worlds, we’ll be able to more effectively reason about the real range of potential outcomes in our system

#### When not to use reactive machine learning

- A research prototype
- A temporary system
    - [guide to building machine learning systems at hackathons](https://medium.com/data-engineering/modeling-madly-8b2c72eb52be)