# 0.6.0
- Remove support for Kafka v0.8.

# 0.5.2
- Convert INFINITY values to 0.

# 0.5.1
- Fix metrics change log level

# 0.5.0
 - `0.5.0` add support to report new producer/consumer metrics in kafka-0.9
 - Compatible with Kafka 0.8
 - A complete list of all the metrics supported in the metrics reporter can be found [here](http://docs.confluent.io/2.0.1/kafka/monitoring.html)

# 0.4.0
 - `0.4.0` adds support for tags on metrics. See [dogstatsd extensions](http://docs.datadoghq.com/guides/dogstatsd/#tags). If your statsd server does not support tags, you can disable them in the Kafka configuration. See property `external.kafka.statsd.tag.enabled` below.

 - The statsd client is [`com.indeed:java-dogstatsd-client:2.0.11`](https://github.com/indeedeng/java-dogstatsd-client/tree/java-dogstatsd-client-2.0.11).
 - support new `MetricNames` introduced by kafka `0.8.2.x`
 - remove JVM metrics. Only the metrics from Kafka `MetricRegistry` are sent.

# 0.3.0
- send JVM metrics