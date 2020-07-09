# [Chapter 3: Collecting Data](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch3)

###### tags: `ml-systems`

![](https://i.imgur.com/StCu3bp.png)

## Outline

- Collecting inherently uncertain data
- Handling data collection at scale
- Querying aggregates of uncertain data
- Avoiding updating data after it’s been written to a database

## Brief Summary

- Facts are immutable records of something that happened and the time that it happened:
    - Transforming facts during data collection results in information loss and should never be done.
    - Facts should encode any uncertainty about that information.
- Data collection can’t work at scale with shared mutable state and locks.
- Fact databases solve the problems of collecting data at scale:
    - Facts can always be written without blocking or using locks.
    - Facts can be written in any order.
- Futures-based programming handles the possibility that operations can take time and even fail.

## Key Points

- This chapter will show you a much better way to collect data, one based on recording immutable facts. The approach in this chapter also assumes that the data being collected is intrinsically uncertain and effectively infinite.

### Sensing uncertain data

- comparing different kinds of data
    - raw sensor data
        ![](https://i.imgur.com/zflHQJY.png)
    - transformed data
        ![](https://i.imgur.com/avOHCz9.png)
    - heavily transformed data
        ![](https://i.imgur.com/1m0lqQy.png)
- As you saw, transforming this raw data caused you to lose information about the intrinsic uncertainty in the sensor readings. This is a common mistake that people make when implementing data-processing systems: people destroy valuable data by only persisting some derived version of the raw data.
- This is an illustration of a fundamental strategy in data collection. You should always collect data as immutable facts.
    - A fact is merely a true record about something that happened.
    - Immutable facts are an important part of building a reactive machine learning system. Not only do they help you build a system that can manage uncertainty, they also form the foundation for strategies that deal with data-collection problems that only emerge once your system gets to a certain scale.

### Collecting data at scale

