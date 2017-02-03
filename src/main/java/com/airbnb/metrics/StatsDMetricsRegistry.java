package com.airbnb.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.Metric;

public class StatsDMetricsRegistry {
  private final Map<String, Metric> metrics;
  private final Map<String, String> tags;

  public StatsDMetricsRegistry() {
    metrics= new HashMap<String, Metric>();
    tags = new HashMap<String, String>();
  }

  public void register(
    String metricName,
    Metric metric,
    String tag
  ) {
    metrics.put(metricName, metric);
    tags.put(metricName, tag);
  }

  public void unregister(String metricName) {
    metrics.remove(metricName);
    tags.remove(metricName);
  }

  public List<String> getMetricsName() {
    return new ArrayList<String>(metrics.keySet());
  }

  public Metric getMetric(String metricName) {
    return metrics.get(metricName);
  }

  public String getTag(String metricName) {
    return tags.get(metricName);
  }
}
