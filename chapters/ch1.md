# [Chapter 1: Learning reactive machine learning](https://hackmd.io/@distributed-systems-engineering/reactive-ml-sytems-ch1)

###### tags: `ml-systems`

## Outline

- Introducing the components of machine learning systems
- Understanding the reactive systems design paradigm
- The reactive approach to building machine learning systems

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

- Reactive systems are defined by four traits and three strategies. The paradigm as a whole is a way of codify- ing an approach to building systems that can serve modern user expectations for things like interactivity and availability.
- Traits of Reactive Systems
![](https://i.imgur.com/tZDj9JW.png)
- Reactive Strategies
![](https://i.imgur.com/BbqxhGU.png)
