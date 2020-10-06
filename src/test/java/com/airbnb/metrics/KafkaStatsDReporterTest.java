package com.airbnb.metrics;

import com.timgroup.statsd.StatsDClient;
import com.yammer.metrics.core.Clock;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class KafkaStatsDReporterTest {
  @Mock
  private Clock clock;
  @Mock
  private StatsDClient statsD;
  private KafkaStatsDReporter reporter;
  private StatsDMetricsRegistry registry;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
    registry = new StatsDMetricsRegistry();
    reporter = new KafkaStatsDReporter(
        statsD,
        registry
    );
  }

  protected void addMetricAndRunReporter(
    Metric metric,
    String metricName,
    String tag
  ) throws Exception {
    try {
      registry.register(metric.metricName(), new MetricInfo(metric, metricName, tag));
      reporter.run();
    } finally {
      reporter.shutdown();
    }
  }

  @Test
  public final void sendDoubleGauge() throws Exception {
    final double value = 10.11;
    Metric metric = new Metric() {
      @Override
      public MetricName metricName() {
        return new MetricName("test-metric", "group");
      }

      @Override
      public double value() {
        return value;
      }
    };

    addMetricAndRunReporter(metric, "foo", "bar");
    verify(statsD).gauge(Matchers.eq("foo"), Matchers.eq(value), Matchers.eq("bar"));
  }
}
