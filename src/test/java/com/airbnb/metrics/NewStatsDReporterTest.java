package com.airbnb.metrics;

import java.util.concurrent.Callable;

import com.timgroup.statsd.StatsDClient;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NewStatsDReporterTest {
  @Mock
  private Clock clock;
  @Mock
  private StatsDClient statsD;
  private NewStatsDReporter reporter;
  private StatsDMetricsRegistry registry;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
    registry = new StatsDMetricsRegistry();
    reporter = new NewStatsDReporter(
      statsD,
      registry
    );
  }

  protected void addMetricAndRunReporter(
    String metricName,
    Gauge<?> gauge,
    String tag
  ) throws Exception {
    try {
      registry.register(metricName, gauge, tag);
      reporter.run();
    } finally {
      reporter.shutdown();
    }
  }

  @Test
  public final void sendDoubleGauge() throws Exception {
    final double value = 10.11;
    Gauge<Double> gauge = new Gauge<Double>() {
      @Override
      public Double value() {
        return value;
      }
    };

    addMetricAndRunReporter("foo", gauge, "bar");
    verify(statsD).gauge(Matchers.eq("foo"), Matchers.eq(value), Matchers.eq("bar"));
  }
}
