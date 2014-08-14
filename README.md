[![Build Status](https://travis-ci.org/airbnb/kafka-statsd-metrics2.png?branch=master)](https://travis-ci.org/airbnb/kafka-statsd-metrics2)

# kafka-statsd-metrics2

Send Kafka Metrics to your StatsD server.

## Contact 
**Let us know!** If you fork this, or if you use it, or if it helps in anyway, we'd love to hear from you! opensource@airbnb.com

## What is it about?
Kafka uses [Yammer Metrics](http://metrics.codahale.com/getting-started/) (now part of the [Dropwizard project](http://metrics.codahale.com/about/)) for [metrics reporting](https://kafka.apache.org/documentation.html#monitoring)
in both the server and the client.
This can be configured to report stats using pluggable stats reporters to hook up to your monitoring system.

This project provides a simple integration between Kafka and a StatsD reporter for Metrics.

Metrics can be filtered based on the metric name and the metric dimensions (min, max, percentiles, etc).
        
## How to install?

- Download or build the shadow jar for `kafka-statsd-metrics`.
- Install the jar in Kafka classpath, typically `./kafka_2.9.2-0.8.1.1/libs/`
- In the Kafka config file, `server.properties`, add the following properties:


```bash
    # declare the reporter
    kafka.metrics.reporters=com.airbnb.kafka.KafkaStatsdMetricsReporter

    # enable the reporter, (false)
    external.kafka.statsd.reporter.enabled=true

    # the host of the StatsD server (localhost)
    external.kafka.statsd.host=localhost

    # the port of the StatsD server (8125)
    external.kafka.statsd.port=8125

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
    # This is typically too much data.
    # It is possible to enable/disable some metric dimensions with the following properties:
    # (default values are shown)
    #
    # external.kafka.statsd.dimension.enabled.count=false
    # external.kafka.statsd.dimension.enabled.meanRate=true
    # external.kafka.statsd.dimension.enabled.rate1m=true
    # external.kafka.statsd.dimension.enabled.rate5m=true
    # external.kafka.statsd.dimension.enabled.rate15m=true
    # external.kafka.statsd.dimension.enabled.min=false
    # external.kafka.statsd.dimension.enabled.max=false
    # external.kafka.statsd.dimension.enabled.mean=true
    # external.kafka.statsd.dimension.enabled.stddev=false
    # external.kafka.statsd.dimension.enabled.median=true
    # external.kafka.statsd.dimension.enabled.p75=false
    # external.kafka.statsd.dimension.enabled.p95=false
    # external.kafka.statsd.dimension.enabled.p98=false
    # external.kafka.statsd.dimension.enabled.p99=true
    # external.kafka.statsd.dimension.enabled.p999=false
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


## Metrics-2.x vs Metrics-3.x
The metrics project has two main versions: v2 and v3. Version 3 is not backward compatible.
 
As of [version 0.8.1.1](https://github.com/apache/kafka/blob/0.8.1.1/build.gradle#L217), Kafka depends on [metrics-2.2.0](http://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.2.0). 

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

Part of the code shipped with this project comes from [ReadyTalk Metrics](https://github.com/ReadyTalk/metrics-statsd),
and has been modified.
