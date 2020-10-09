package com.airbnb.metrics;

import org.apache.kafka.common.Metric;

public class MetricInfo {
  private final Metric metric;
  private final String name;
  private final String tags;

  public MetricInfo(Metric metric, String name, String tags) {
    this.metric = metric;
    this.name = name;
    this.tags = tags;
  }

  public Metric getMetric() {
    return metric;
  }

  public String getName() {
    return name;
  }

  public String getTags() {
    return tags;
  }
}
