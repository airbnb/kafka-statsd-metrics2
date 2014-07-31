[![Build Status](https://travis-ci.org/airbnb/kafka-statsd-metrics2.png?branch=master)](https://travis-ci.org/airbnb/kafka-statsd-metrics2)

# kafka-statsd-metrics2

Send Kafka Metrics to your StatsD server.

## What is it about?
Kafka uses [Yammer Metrics](http://metrics.codahale.com/getting-started/) (now part of the [Dropwizard project](http://metrics.codahale.com/about/)) for [metrics reporting](https://kafka.apache.org/documentation.html#monitoring)
in both the server and the client.
This can be configured to report stats using pluggable stats reporters to hook up to your monitoring system.

This project provides a simple integration between Kafka and a StatsD reporter for Metrics.

        
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


## Dependency Management

### metrics2-statsd
[metrics2-statsd](https://github.com/ReadyTalk/metrics-statsd) provides a the StatsD reporter for Metrics2. The jar spec is `com.readytalk:metrics2-statsd:4.1.0`.

`kafka-statsd-metrics2` simply provides the integration between Kafka and this reporter. 


### Metrics-2.x vs Metrics-3.x
The metrics project has two main versions: v2 and v3. Version 3 is not backward compatible.
 
As of [version 0.8.1.1](https://github.com/apache/kafka/blob/0.8.1.1/build.gradle#L217), Kafka depends on [metrics-2.2.0](http://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.2.0). 

*Note:*_<br/>
In a future release, Kafka [might upgrade](https://issues.apache.org/jira/browse/KAFKA-960) to Metrics-3.x.
Due to the incompatibilities between Metrics versions, a new Statsd reporter for metrics-3 will be required.<br/>
All contributions welcome!


## How to build

After cloning the repo, type

```bash
    ./gradlew shadowJar
```

This produces a jar file in `build/libs/`. 

The shallow jar contains all the `kafka-statsd-metrics2` classes as well as the 

