package com.airbnb.metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.MetricName;

public class StatsDMetricsRegistry {
  private final Map<MetricName, MetricInfo> metrics;

  public StatsDMetricsRegistry() {
    metrics = new HashMap<>();
  }

  public void register(MetricName metricName, MetricInfo metricInfo) {
    metrics.put(metricName, metricInfo);
  }

  public void unregister(MetricName metricName) {
    metrics.remove(metricName);
  }

  public Collection<MetricInfo> getAllMetricInfo() {
      return metrics.values();
  }
}
