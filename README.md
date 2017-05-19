disthene-reader: cassandra backed metric storage *reader*
=========================================================

## Motivation
This is a "dual" project to [disthene](https://github.com/EinsamHauer/disthene). Though the data written by **disthene** can still be read and plotted by combination of **cyanite** & **graphite-api**, this schema introduces quite some overhead at least caused by serializing/deserializing to/from json. In cases when rendering a graph involves 10s of millions of data points this overhead is quite noticable.
Besides that **graphite-api** as well as original **graphite** rendering could really be a bit faster.
All in all, this project is about read and rendering performance exactly like **disthene** is about write performance.

## What's in
The following APIs are supported:
* /paths API for backward compatibility with **graphite-api** and **cyanite**
* /metrics
* /render mostly as per Graphite specification version 0.10.0

The functions are mostly per Graphite specification version 0.10.0 with several exceptions below.

The following functions have a different implementation:
* stdev (see https://github.com/graphite-project/graphite-web/issues/986)
* holtWintersForecast
* holtWintersConfidenceBands
* holtWintersConfidenceArea
* holtWintersAberration

The following functions are not implemented:
* smartSummarize
* fallbackSeries
* removeBetweenPercentile
* useSeriesAbove
* removeEmptySeries
* map
* mapSeries
* reduce
* reduceSeries
* identity
* cumulative
* consolidateBy
* changed
* groupByNode
* substr
* time
* sin
* randomWalk
* timeFunction
* sinFunction
* randomWalkFunction
* events




## Compiling 

This is a standard Java Maven project. 

```
mvn package
```

will most probably do the trick.

## Running
There are a couple of things you will need in runtime, just the same set as for **cyanite** and/or **disthene**

* Cassandra
* Elasticsearch
* Graphite-web (probably a modified version like https://github.com/cybem/graphite-web-iow)
* [graphite-cyanite](https://github.com/brutasse/graphite-cyanite)

## Configuration
There several configuration files involved
* /etc/disthene-reader/disthene-reader.yaml (location can be changed with -c command line option if needed)
* /etc/disthene-reader/disthene-reader-log4j.xml (location can be changed with -l command line option if needed) 

##### Main configuration in disthene.yaml
```
reader:
# bind address and port
  bind: "0.0.0.0"
  port: 8080
# rollups - currently only "s" units supported  
  rollups:
    - 60s:5356800s
    - 900s:62208000s
store:
# C* contact points, port, keyspace and table
  cluster:
    - "cassandra-1"
    - "cassandra-2"
  port: 9042
  keyspace: 'metric'
  columnFamily: 'metric'
# maximum connections per host , timeouts in seconds, max requests per host - these are literally used in C* java driver settings
  maxConnections: 2048
  readTimeout: 10
  connectTimeout: 10
  maxRequests: 128
index:
# ES cluster name, contact points, native port, index name & type
  name: "disthene"
  cluster:
    - "es-1"
    - "es-2"
  port: 9300
  index: "disthene"
  type: "path"
# Maxim number paths allowed per one wildcard. This is just to prevent abuse
  maxPaths: 50000
stats:
# flush self metrics every 'interval' seconds
  interval: 60
# tenant to use for stats
  tenant: "graphite"
# hostname to use
  hostname: "disthene-reader"
# carbon server to send stats to
  carbonHost: "carbon.example.net"
# carbon port to send stats to
  carbonPort: 2003  
```

##### Logging configuration in disthene-reader-log4j.xml
Configuration is straight forward as per log4j

## License

The MIT License (MIT)

Copyright (C) 2015 Andrei Ivanov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
