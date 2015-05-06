package com.airbnb.kafka;

import kafka.metrics.KafkaMetricsReporterMBean;

/**
 * @see kafka.metrics.KafkaMetricsReporterMBean: the custom reporter needs to
 * additionally implement an MBean trait that extends kafka.metrics.KafkaMetricsReporterMBean
 * so that the registered MBean is compliant with the standard MBean convention.
 */
public interface KafkaMetricsDogstatsdReporterMBean extends KafkaMetricsReporterMBean {
}
