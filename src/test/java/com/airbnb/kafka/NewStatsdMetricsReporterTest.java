package com.airbnb.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.metrics.KafkaMetric;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewStatsdMetricsReporterTest {
  private Map<String, String> configs;

  @Before
  public void init() {
    configs = new HashMap<String, String>();
    configs.put(NewStatsdMetricsReporter.STATSD_HOST, "127.0.0.1");
    configs.put(NewStatsdMetricsReporter.STATSD_PORT, "1234");
    configs.put(NewStatsdMetricsReporter.STATSD_METRICS_PREFIX, "foo");
    configs.put(NewStatsdMetricsReporter.STATSD_REPORTER_ENABLED, "false");
  }

  @Test
  public void init_should_start_reporter_when_enabled() {
    configs.put(NewStatsdMetricsReporter.STATSD_REPORTER_ENABLED, "true");
    NewStatsdMetricsReporter reporter = new NewStatsdMetricsReporter();
    assertFalse("reporter should not be running", reporter.isRunning());
    reporter.configure(configs);
    reporter.init(new ArrayList<KafkaMetric>());
    assertTrue("reporter should be running once #init has been invoked", reporter.isRunning());
  }

  @Test
  public void init_should_not_start_reporter_when_disabled() {
    configs.put(NewStatsdMetricsReporter.STATSD_REPORTER_ENABLED, "false");
    NewStatsdMetricsReporter reporter = new NewStatsdMetricsReporter();
    assertFalse("reporter should not be running", reporter.isRunning());
    reporter.configure(configs);
    reporter.init(new ArrayList<KafkaMetric>());
    assertFalse("reporter should NOT be running once #init has been invoked", reporter.isRunning());
  }
}
