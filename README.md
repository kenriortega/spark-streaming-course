# Repository for the Rock the JVM Spark Streaming with Scala course

### Tech used in this project

- apache kakfa
- apache cassandra
- postgresql
- netcat

### Spark Streaming principles

> Declarative API

- write `what` needs to be computed, let the library decide `how`
- alternative: *RAAT* (record-at-a-time)
  - set of APIs to process each incoming elements as it arrives
  - low-level: maintaining state & resource usage is your responsibility
  - hard to develop

> Event time vs Processing time API

- event time = when the event was produced
- processing time = when the event arrives
- event time is critical: allow detection of late data points

> Continuous vs micro-batch execution

- continuous = include each data point as it arrives `lower latency`
- micro-batch = wait for a few data points, process them all in the new result `higher throughput`

> Low-level (`DStreams`) vs High-level API (`Structured Streaming`)

Spark streaming operates on micro-batches
`continuous executions is experimental` [2020]

### Structured Streaming Principles

> Lazy evaluation

Transformation and Action

- transformations describe of how new DFs are obtained
- actions start executing/running spark code

Input sources e.g:

- kafka, flume
- a distributed file sustem
- sockets

Output sinks e.g:

- a distributed file systemd
- databases
- kafka
- testing sinks e.g: console, memory

> Streaming I/O

Outputs modes
- append = only add new records
- update = modify records in place `if query has no aggregations, eqquivalent with append`
- complete = rewrite everything

Not all queries and sinks support all output modes
- e.g: aggregations and append mode

Triggers = when new data is written
- default: write as soon as the current micro-batch has been processed
- once: write a single micro-batch and stop
- processing-time: lok for new data at fixed intervals
- continuous (currently experimental `2020`)