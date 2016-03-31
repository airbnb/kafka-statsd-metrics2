package com.airbnb.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yammer.metrics.core.Gauge;

public class StatsDMetricsRegistry {
  private final Map<String, Gauge<?>> gauges;
  private final Map<String, String> tags;

  public StatsDMetricsRegistry() {
    gauges = new HashMap<String, Gauge<?>>();
    tags = new HashMap<String, String>();
  }

  public void register(
    String metricName,
    Gauge<?> gauge,
    String tag
  ) {
    gauges.put(metricName, gauge);
    tags.put(metricName, tag);
  }

  public void unregister(String metricName) {
    gauges.remove(metricName);
    tags.remove(metricName);
  }

  public List<String> getMetricsName() {
    return new ArrayList<String>(gauges.keySet());
  }

  public Gauge<?> getGauge(String metricName) {
    return gauges.get(metricName);
  }

  public String getTag(String metricName) {
    return tags.get(metricName);
  }
}
