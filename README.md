[![Build Status](https://travis-ci.org/airbnb/kafka-statsd-metrics2.png?branch=master)](https://travis-ci.org/airbnb/kafka-statsd-metrics2)

# kafka-statsd-metrics2

Send Kafka Metrics to StatsD.

## Contact 
**Let us know!** If you fork this, or if you use it, or if it helps in anyway, we'd love to hear from you! opensource@airbnb.com

## What is it about?
Kafka uses [Yammer Metrics](http://metrics.codahale.com/getting-started/) (now part of the [Dropwizard project](http://metrics.codahale.com/about/)) for [metrics reporting](https://kafka.apache.org/documentation.html#monitoring)
in both the server and the client.
This can be configured to report stats using pluggable stats reporters to hook up to your monitoring system.

This project provides a simple integration between Kafka and a StatsD reporter for Metrics.

Metrics can be filtered based on the metric name and the metric dimensions (min, max, percentiles, etc).

## Supported Kafka versions

- For Kafka `0.8.2.0` or later use `kafka-statsd-metrics2-0.4.0`
- For Kafka `0.8.1.1` or prior use `kafka-statsd-metrics2-0.3.0`


## Releases
 
### 0.4.0

 - `0.4.0` adds support for tags on metrics. See [dogstatsd extensions](http://docs.datadoghq.com/guides/dogstatsd/#tags). If your statsd server does not support tags, you can disable them in the Kafka configuration. See property `external.kafka.statsd.tag.enabled` below.

 - The statsd client is [`com.indeed:java-dogstatsd-client:2.0.11`](https://github.com/indeedeng/java-dogstatsd-client/tree/java-dogstatsd-client-2.0.11).
 - support new `MetricNames` introduced by kafka 0.8.2.x

## 0.3.0

- initial release
        
## How to install?

- Download or build the shadow jar for `kafka-statsd-metrics`.
- Install the jar in Kafka classpath, typically `./kafka_2.9.2-0.8.2.1/libs/`
- In the Kafka config file, `server.properties`, add the following properties. Default values are in parenthesis.



```bash

    # declare the reporter
    kafka.metrics.reporters=com.airbnb.kafka.KafkaStatsdMetricsReporter

    # enable the reporter, (false)
    external.kafka.statsd.reporter.enabled=true

    # the host of the StatsD server (localhost)
    external.kafka.statsd.host=localhost

    # the port of the StatsD server (8125)
    external.kafka.statsd.port=8125

    # enable the support of statsd tag extension, e.g. datadog statsd (true)
    external.kafka.statsd.tag.enabled=true

    # a prefix for all metrics names (empty)
    external.kafka.statsd.metrics.prefix=
    
    # note that the StatsD reporter follows the global polling interval (10)
    # kafka.metrics.polling.interval.secs=10


    
    # A regex to exclude some metrics
    # Default is: (kafka\.consumer\.FetchRequestAndResponseMetrics.*)|(.*ReplicaFetcherThread.*)|(kafka\.server\.FetcherLagMetrics\..*)|(kafka\.log\.Log\..*)|(kafka\.cluster\.Partition\..*)
    #
    # The metric name is formatted with this template: group.type.scope.name
    #
    # external.kafka.statsd.metrics.exclude_regex=
    
    #
    # Each metric provides multiple dimensions: min, max, meanRate, etc
    # This might be too much data.
    # It is possible to disable some metric dimensions with the following properties:
    # By default all dimenstions are enabled.
    #
    # external.kafka.statsd.dimension.enabled.count=true
    # external.kafka.statsd.dimension.enabled.meanRate=true
    # external.kafka.statsd.dimension.enabled.rate1m=true
    # external.kafka.statsd.dimension.enabled.rate5m=true
    # external.kafka.statsd.dimension.enabled.rate15m=true
    # external.kafka.statsd.dimension.enabled.min=true
    # external.kafka.statsd.dimension.enabled.max=true
    # external.kafka.statsd.dimension.enabled.mean=true
    # external.kafka.statsd.dimension.enabled.stddev=true
    # external.kafka.statsd.dimension.enabled.median=true
    # external.kafka.statsd.dimension.enabled.p75=true
    # external.kafka.statsd.dimension.enabled.p95=true
    # external.kafka.statsd.dimension.enabled.p98=true
    # external.kafka.statsd.dimension.enabled.p99=true
    # external.kafka.statsd.dimension.enabled.p999=true
```

- finally restart the Kafka server

## How to test your configuration?

You can check your configuration in different ways:

- During Kafka startup, the reporter class will be instantiated and initialized. The logs should contain a message similar to:
`"Kafka Statsd metrics reporter is enabled"`
- A JMX MBean named `kafka:type=com.airbnb.kafka.KafkaStatsdMetricsReporter` should also exist.
- Check the logs of your StatsD server
- Finally, on the configured StatsD host, you could listen on the configured port and check for incoming data:
 
```bash
    # assuming the Statsd server has been stopped...
    $ nc -ul 8125
    
    kafka.controller.ControllerStats.LeaderElectionRateAndTimeMs.samples:1|gkafka.controller.ControllerStats
    .LeaderElectionRateAndTimeMs.meanRate:0.05|gkafka.controller.ControllerStats.LeaderElectionRateAndTimeMs.
    1MinuteRate:0.17|gkafka.controller.ControllerStats.LeaderElectionRateAndTimeMs.5MinuteRate:0.19|g....
```

## List of metrics for Kafka 0.8.1 and 0.8.2

Below are the metrics in Kafka 0.8.1 or 0.8.2

```bash
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Metrics kind | Group (pkg)          | Type (class)                   | Name in Kafka 0.8.1                                       | Name Diff in Kafka 0.8.2                                  |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ReplicaManager                 | "LeaderCount"                                             | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ReplicaManager                 | "PartitionCount"                                          | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ReplicaManager                 | "UnderReplicatedPartitions"                               | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.controller     | KafkaController                | "ActiveControllerCount"                                   | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.controller     | KafkaController                | "OfflinePartitionsCount"                                  | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.controller     | KafkaController                | "PreferredReplicaImbalanceCount"                          | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.network        | RequestChannel                 | "RequestQueueSize"                                        | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ReplicaFetcherManager          | "Replica_MaxLag"                                          | tag: {"clientId" -> clientId}                             |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ReplicaFetcherManager          | "Replica_MinFetchRate"                                    | tag: {"clientId" -> clientId}                             |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | FetchRequestPurgatory          | "PurgatorySize"                                           | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | FetchRequestPurgatory          | "NumDelayedRequests"                                      | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ProducerRequestPurgatory       | "PurgatorySize"                                           | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | ProducerRequestPurgatory       | "NumDelayedRequests"                                      | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.consumer       | ConsumerFetcherManager         | config.clientId + "-MaxLag"                               | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.consumer       | ConsumerFetcherManager         | config.clientId + "-MinFetchRate"                         | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.consumer       | ZookeeperConsumerConnector     | config.clientId + "-" + config.groupId + "-" +            | with tag {"clientId" -> config.clientId,                  |
    |              |                      |                                | topic + "-" + threadId + "-FetchQueueSize"                | "topic" -> topic, "threadId" -> threadId}                 |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.network        | RequestChannel                 | "Processor-" + i + "-ResponseQueueSize"                   | i is in tag at 0.8.2                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Timer        | kafka.log            | LogFlushStats                  | "LogFlushRateAndTimeMs"                                   | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | ReplicaManager                 | "IsrExpandsPerSec"                                        | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | ReplicaManager                 | "IsrShrinksPerSec"                                        | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | DelayedFetchRequestMetrics     | "FollowerExpiresPerSecond"                                | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | DelayedFetchRequestMetrics     | "ConsumerExpiresPerSecond"                                | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.controller     | ControllerStats                | "UncleanLeaderElectionsPerSec"                            | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Timer        | kafka.controller     | ControllerStats                | "LeaderElectionRateAndTimeMs"                             | same                                                      |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerStats                  | clientId + "-SerializationErrorsPerSec"                   | tag is clientId                                           |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerStats                  | clientId + "-ResendsPerSec"                               | tag is clientId                                           |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerStats                  | clientId + "-FailedSendsPerSec"                           | tag is clientId                                           |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerTopicMetrics           | metricId or "AllTopics" + "MessagesPerSec" or             | tag is either clientId or {"clientId" -> clientId,        |
    |              |                      |                                |                                                           | "topic" -> topic}                                         |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerTopicMetrics           | metricId or "AllTopics" + "BytesPerSec"                   | tag is either clientId or {"clientId" -> clientId,        |
    |              |                      |                                |                                                           | "topic" -> topic}                                         |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.producer       | ProducerTopicMetrics           | metricId or "AllTopics" + "DroppedMessagesPerSec"         | tag is either clientId or {"clientId" -> clientId,        |
    |              |                      |                                |                                                           | "topic" -> topic}                                         |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | FetcherStats                   | metricId + "-RequestsPerSec"                              | tag is {"clientId" -> metricId.clientId, "brokerHost" ->  |
    |              |                      |                                |                                                           | metricId.brokerHost, "brokerPort" -> metricId.brokerPort} |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | FetcherStats                   | metricId + "-BytesPerSec"                                 | same tag as above                                         |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "MessagesInPerSec"                  | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "BytesInPerSec"                     | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "BytesOutPerSec"                    | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "LogBytesAppendedPerSec"            | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "FailedProduceRequestsPerSec"       | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | BrokerTopicMetrics             | name or "AllTopics" + "FailedFetchRequestsPerSec"         | tag is topic or Empty                                     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.server         | DelayedProducerRequestMetrics  | keyLabel or "All" + "ExpiresPerSecond"                    | tag includes "topic" -> topicAndPartition.topic,          |
    |              |                      |                                |                                                           | "partition" -> topicAndPartition.partition or it is Empty |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Timer        | kafka.producer       | ProducerRequestMetrics         | metricId or "AllBrokers" + "ProducerRequestRateAndTimeMs" | tag is either clientId or {"clientId" -> clientId,        |
    |              |                      |                                |                                                           | "brokerHost", "brokerPort"}                               |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.producer       | ProducerRequestMetrics         | metricId or "AllBrokers" + "ProducerRequestSize"          | metricId is in tag                                        |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Timer        | kafka.consumer       | FetchRequestAndResponseMetrics | metricId or "AllBrokers" + "FetchRequestRateAndTimeMs"    | metricId is in tag                                        |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.consumer       | FetchRequestAndResponseMetrics | metricId or "AllBrokers" + "FetchResponseSize"            | metricId is in tag                                        |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.consumer       | ConsumerTopicMetrics           | metricId or "AllTopics" + "MessagesPerSec"                | tag is either clientId or                                 |
    |              |                      |                                |                                                           | {"clientId" -> clientId, "topic" -> topic}                |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.consumer       | ConsumerTopicMetrics           | metricId or "AllTopics" + "BytesPerSec"                   | rtopicmetr                                                |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Meter        | kafka.network        | RequestMetrics                 | name + "-RequestsPerSec"                                  | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-RequestQueueTimeMs"                              | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-LocalTimeMs"                                     | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-RemoteTimeMs"                                    | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-ResponseQueueTimeMs"                             | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-ResponseSendTimeMs"                              | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Histogram    | kafka.network        | RequestMetrics                 | name + "-TotalTimeMs"                                     | name is now in the tag!                                   |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.server         | FetcherLagMetrics              | metricId + "-ConsumerLag"                                 | tag includes clientId, topic, and partition               |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.producer.async | ProducerSendThread             | clientId + "-ProducerQueueSize"                           | tag is clientId                                           |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.log            | Log                            | name + "-" + "NumLogSegments"                             | tag is {"topic" -> topicAndPartition.topic,               |
    |              |                      |                                |                                                           | "partition" -> topicAndPartition.partition}               |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.log            | Log                            | name + "-" + "LogEndOffset"                               | same as above                                             |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
    | Gauge        | kafka.cluster        | Partition                      | topic + "-" + partitionId + "-UnderReplicated"            | tag is {"topic" -> topic, "partition" -> partitionId}     |
    +--------------+----------------------+--------------------------------+-----------------------------------------------------------+-----------------------------------------------------------+
```

Below are the new metrics in Kafka 0.8.2

```bash
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Metrics kind | Group (pkg)    | Type (class)               | Name in Kafka 0.8.2                       | Tags in Kafka 0.8.2                   |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.network  | SocketServer               | "ResponsesBeingSent"                      | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.network  | SocketServer               | "NetworkProcessorAvgIdlePercent"          | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.network  | SocketServer               | "IdlePercent"                             | with tag: {"networkProcessor" -> i}   |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.server   | OffsetManager              | "NumOffsets"                              | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.server   | OffsetManager              | "NumGroups"                               | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.consumer | ZookeeperConsumerConnector | "OwnedPartitionsCount"                    | tag: {"clientId" -> config.clientId,  |
    |              |                |                            |                                           | "groupId" -> config.groupId}          |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.consumer | ZookeeperConsumerConnector | "OwnedPartitionsCount"                    | tag: {"clientId" -> config.clientId,  |
    |              |                |                            |                                           | "groupId" -> config.groupId,          |
    |              |                |                            |                                           | "topic" -> topic}                     |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.consumer | ZookeeperConsumerConnector | "KafkaCommitsPerSec"                      | tag: clientId                         |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.consumer | ZookeeperConsumerConnector | "ZooKeeperCommitsPerSec"                  | tag: clientId                         |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.consumer | ZookeeperConsumerConnector | "RebalanceRateAndTime"                    | tag: clientId                         |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.tools    | DataChannel                | "MirrorMaker-DataChannel-WaitOnPut"       | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.tools    | DataChannel                | "MirrorMaker-DataChannel-WaitOnTake"      | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Histogram    | kafka.tools    | DataChannel                | "MirrorMaker-DataChannel-Size"            | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.common   | AppInfo                    | "Version"                                 | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.server   | KafkaRequestHandlerPool    | "RequestHandlerAvgIdlePercent"            | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Meter        | kafka.util     | Throttler                  | a input string not with small cardinality | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | LogCleaner                 | "max-buffer-utilization-percent"          | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | LogCleaner                 | "cleaner-recopy-percent"                  | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | LogCleaner                 | "max-clean-time-secs"                     | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Timer        | other.kafka    | FetchThread                | "fetch-thread"                            | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Timer        | other.kafka    | CommitThread               | "commit-thread"                           | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | Log                        | "LogStartOffset"                          | tags = {"topic" -> topic,             |
    |              |                |                            |                                           | "partition" -> partition}             |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | Log                        | "Size"                                    | tags = {"topic" -> topic,             |
    |              |                |                            |                                           | "partition" -> partition)             |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.server   | KafkaServer                | "BrokerState"                             | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
    | Gauge        | kafka.log      | LogCleanerManager          | "max-dirty-percent"                       | no tag                                |
    +--------------+----------------+----------------------------+-------------------------------------------+---------------------------------------+
```


## Metrics-2.x vs Metrics-3.x
The metrics project has two main versions: v2 and v3. Version 3 is not backward compatible.
 
Version [0.8.1.1](https://github.com/apache/kafka/blob/0.8.1.1/build.gradle#L217) and [0.8.2.1](https://github.com/apache/kafka/blob/0.8.2.1/build.gradle#L209), Kafka depends on [metrics-2.2.0](http://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.2.0). 

*Note:*<br/>
In a future release, Kafka [might upgrade](https://issues.apache.org/jira/browse/KAFKA-960) to Metrics-3.x.
Due to the incompatibilities between Metrics versions, a new Statsd reporter for metrics-3 will be required.<br/>
All contributions welcome!


## How to build

After cloning the repo, type

```bash
    ./gradlew shadowJar
```

This produces a jar file in `build/libs/`. 

The shallow jar is a standalone jar.


# License & Attributions

This project is released under the Apache License Version 2.0 (APLv2).

