# [Chapter 3: Collecting Data](https://hackmd.io/@distributed-systems-engineering/reactive-ml-systems-ch3)

###### tags: `ml-systems`

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