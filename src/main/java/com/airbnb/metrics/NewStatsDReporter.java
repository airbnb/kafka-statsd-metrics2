package com.airbnb.metrics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.timgroup.statsd.StatsDClient;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewStatsDReporter implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(NewStatsDReporter.class);
  private final ScheduledExecutorService executor;

  private final StatsDClient statsDClient;
  private final StatsDMetricsRegistry registry;

  public NewStatsDReporter(
    StatsDClient statsDClient,
    StatsDMetricsRegistry registry
  ) {
    this.statsDClient = statsDClient;
    this.registry = registry;
    this.executor = new ScheduledThreadPoolExecutor(1);
  }

  public void start(
    long period,
    TimeUnit unit
  ) {
    executor.scheduleWithFixedDelay(this, period, period, unit);
  }

  public void shutdown() throws InterruptedException {
    executor.shutdown();
  }

  private void sendAllKafkaMetrics() {
    for (String metricName : registry.getMetricsName()) {
      sendAMetric(metricName);
    }
  }

  private void sendAMetric(
    String metricName
  ) {
    Gauge<?> gauge = registry.getGauge(metricName);
    String tag = registry.getTag(metricName);

    final Object value = gauge.value();
    if (tag != null) {
      statsDClient.gauge(metricName, new Double(value.toString()), tag);
    } else {
      statsDClient.gauge(metricName, new Double(value.toString()));
    }
  }

  @Override
  public void run() {
    sendAllKafkaMetrics();
  }
}
