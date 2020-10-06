package com.airbnb.kafka.kafka09;

import com.airbnb.metrics.MetricInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.Collectors;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatsdMetricsReporterTest {
  private final String TEST_METRIC_NAME = "test-metric";
  private final String TEST_METRIC_GROUP = "test-group";
  private final String TEST_METRIC_DESCRIPTION = "This is a test metric.";

  private Map<String, String> configs;

  @Before
  public void init() {
    configs = new HashMap<String, String>();
    configs.put(StatsdMetricsReporter.STATSD_HOST, "127.0.0.1");
    configs.put(StatsdMetricsReporter.STATSD_PORT, "1234");
    configs.put(StatsdMetricsReporter.STATSD_METRICS_PREFIX, "foo");
    configs.put(StatsdMetricsReporter.STATSD_REPORTER_ENABLED, "false");
  }

  @Test
  public void init_should_start_reporter_when_enabled() {
    configs.put(StatsdMetricsReporter.STATSD_REPORTER_ENABLED, "true");
    StatsdMetricsReporter reporter = new StatsdMetricsReporter();
    assertFalse("reporter should not be running", reporter.isRunning());
    reporter.configure(configs);
    reporter.init(new ArrayList<KafkaMetric>());
    assertTrue("reporter should be running once #init has been invoked", reporter.isRunning());
  }

  @Test
  public void init_should_not_start_reporter_when_disabled() {
    configs.put(StatsdMetricsReporter.STATSD_REPORTER_ENABLED, "false");
    StatsdMetricsReporter reporter = new StatsdMetricsReporter();
    assertFalse("reporter should not be running", reporter.isRunning());
    reporter.configure(configs);
    reporter.init(new ArrayList<KafkaMetric>());
    assertFalse("reporter should NOT be running once #init has been invoked", reporter.isRunning());
  }

  @Test
  public void testMetricsReporter_sameMetricNamesWithDifferentTags() {
    StatsdMetricsReporter reporter = spy(new StatsdMetricsReporter());
    reporter.configure(ImmutableMap.of(StatsdMetricsReporter.STATSD_REPORTER_ENABLED, "true"));
    StatsDClient mockStatsDClient = mock(NonBlockingStatsDClient.class);
    when(reporter.createStatsd()).thenReturn(mockStatsDClient);

    KafkaMetric testMetricWithTag = generateMockKafkaMetric(TEST_METRIC_NAME, TEST_METRIC_GROUP, TEST_METRIC_DESCRIPTION, ImmutableMap.of("test-key", "test-value"));
    reporter.init(ImmutableList.of(testMetricWithTag));
    Assert.assertEquals(ImmutableSet.of(testMetricWithTag), getAllKafkaMetricsHelper(reporter));

    KafkaMetric otherTestMetricWithTag = generateMockKafkaMetric(TEST_METRIC_NAME, TEST_METRIC_GROUP, TEST_METRIC_DESCRIPTION, ImmutableMap.of("another-test-key", "another-test-value"));
    reporter.metricChange(otherTestMetricWithTag);
    Assert.assertEquals(ImmutableSet.of(testMetricWithTag, otherTestMetricWithTag), getAllKafkaMetricsHelper(reporter));

    reporter.underlying.run();
    reporter.registry.getAllMetricInfo().forEach(info -> verify(mockStatsDClient, atLeastOnce()).gauge(info.getName(), info.getMetric().value(), info.getTags()));
  }

  private KafkaMetric generateMockKafkaMetric(String name, String group, String description, Map<String, String> tags) {
    KafkaMetric mockMetric = mock(KafkaMetric.class);
    when(mockMetric.metricName()).thenReturn(new MetricName(name, group, description, tags));
    return mockMetric;
  }

  private static Collection<Metric> getAllKafkaMetricsHelper(StatsdMetricsReporter reporter) {
    return reporter.registry.getAllMetricInfo().stream().map(MetricInfo::getMetric).collect(Collectors.toSet());
  }
}
