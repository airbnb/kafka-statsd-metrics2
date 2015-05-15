
# 0.4.0

 - `0.4.0` adds support for tags on metrics. See [dogstatsd extensions](http://docs.datadoghq.com/guides/dogstatsd/#tags). If your statsd server does not support tags, you can disable them in the Kafka configuration. See property `external.kafka.statsd.tag.enabled` below.

 - The statsd client is [`com.indeed:java-dogstatsd-client:2.0.11`](https://github.com/indeedeng/java-dogstatsd-client/tree/java-dogstatsd-client-2.0.11).
 - support new `MetricNames` introduced by kafka `0.8.2.x`
 - remove JVM metrics. Only the metrics from Kafka `MetricRegistry` are sent.

# 0.3.0
- send JVM metrics

