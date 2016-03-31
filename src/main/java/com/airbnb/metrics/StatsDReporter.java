/*
 * Copyright (c) 2015.  Airbnb.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.airbnb.metrics;

import com.timgroup.statsd.StatsDClient;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import static com.airbnb.metrics.Dimension.*;

/**
 *
 */
public class StatsDReporter extends AbstractPollingReporter implements MetricProcessor<Long> {
  static final Logger log = LoggerFactory.getLogger(StatsDReporter.class);
  public static final String REPORTER_NAME = "kafka-statsd-metrics";

  private final StatsDClient statsd;
  private final Clock clock;
  private final EnumSet<Dimension> dimensions;
  private MetricPredicate metricPredicate;
  private boolean isTagEnabled;

  private Parser parser;

  public StatsDReporter(MetricsRegistry metricsRegistry,
                        StatsDClient statsd,
                        EnumSet<Dimension> metricDimensions) {
    this(metricsRegistry, statsd, REPORTER_NAME, MetricPredicate.ALL, metricDimensions, true);
  }

  public StatsDReporter(MetricsRegistry metricsRegistry,
                        StatsDClient statsd,
                        MetricPredicate metricPredicate,
                        EnumSet<Dimension> metricDimensions,
                        boolean isTagEnabled) {
    this(metricsRegistry, statsd, REPORTER_NAME, metricPredicate, metricDimensions, isTagEnabled);
  }

  public StatsDReporter(MetricsRegistry metricsRegistry,
                        StatsDClient statsd,
                        String reporterName,
                        MetricPredicate metricPredicate,
                        EnumSet<Dimension> metricDimensions,
                        boolean isTagEnabled) {
    super(metricsRegistry, reporterName);
    this.statsd = statsd;               //exception in statsd is handled by default NO_OP_HANDLER (do nothing)
    this.clock = Clock.defaultClock();
    this.parser = null;          //postpone set it because kafka doesn't start reporting any metrics.
    this.dimensions = metricDimensions;
    this.metricPredicate = metricPredicate;
    this.isTagEnabled = isTagEnabled;
  }

  @Override
  public void run() {
    try {
      final long epoch = clock.time() / 1000;
      if (parser == null) {
        createParser(getMetricsRegistry());
      }
      sendAllKafkaMetrics(epoch);
    } catch (RuntimeException ex) {
      log.error("Failed to print metrics to statsd", ex);
    }
  }

  private void createParser(MetricsRegistry metricsRegistry) {
    if (isTagEnabled) {
      final boolean isMetricsTagged = isTagged(metricsRegistry.allMetrics());
      if (isMetricsTagged) {
        log.info("Kafka metrics are tagged");
        parser = new ParserForTagInMBeanName();
      } else {
        parser = new ParserForNoTag();
      }
    } else {
      parser = new ParserForNoTag();
    }
  }

  //kafka.common.AppInfo is not reliable, sometimes, not correctly loaded.
  public boolean isTagged(Map<MetricName, Metric> metrics) {
    for (MetricName metricName : metrics.keySet()) {
      if ("kafka.common:type=AppInfo,name=Version".equals(metricName.getMBeanName())
          || metricName.hasScope()) {
        return true;
      }
    }
    return false;
  }

  private void sendAllKafkaMetrics(long epoch) {
    final Map<MetricName, Metric> allMetrics = new TreeMap<MetricName, Metric>(getMetricsRegistry().allMetrics());
    for (Map.Entry<MetricName, Metric> entry : allMetrics.entrySet()) {
      sendAMetric(entry.getKey(), entry.getValue(), epoch);
    }
  }

  private void sendAMetric(MetricName metricName, Metric metric, long epoch) {
    log.debug("MBeanName[{}], Group[{}], Name[{}], Scope[{}], Type[{}]",
        metricName.getMBeanName(), metricName.getGroup(), metricName.getName(),
        metricName.getScope(), metricName.getType());

    if (metricPredicate.matches(metricName, metric) && metric != null) {
      try {
        parser.parse(metricName);
        metric.processWith(this, metricName, epoch);
      } catch (Exception ignored) {
        log.error("Error printing regular metrics:", ignored);
      }
    }
  }

  @Override
  public void processCounter(MetricName metricName, Counter counter, Long context) throws Exception {
    statsd.gauge(parser.getName(), counter.count(), parser.getTags());
  }

  @Override
  public void processMeter(MetricName metricName, Metered meter, Long epoch) {
    send(meter);
  }

  @Override
  public void processHistogram(MetricName metricName, Histogram histogram, Long context) throws Exception {
    send((Summarizable) histogram);
    send((Sampling) histogram);
  }

  @Override
  public void processTimer(MetricName metricName, Timer timer, Long context) throws Exception {
    send((Metered) timer);
    send((Summarizable) timer);
    send((Sampling) timer);
  }

  @Override
  public void processGauge(MetricName metricName, Gauge<?> gauge, Long context) throws Exception {
    final Object value = gauge.value();
    final Boolean flag = isDoubleParsable(value);
    if (flag == null) {
      log.debug("Gauge can only record long or double metric, it is " + value.getClass());
    } else if (flag.equals(true)) {
      statsd.gauge(parser.getName(), new Double(value.toString()), parser.getTags());
    } else {
      statsd.gauge(parser.getName(), new Long(value.toString()), parser.getTags());
    }
  }

  protected static final Dimension[] meterDims = {count, meanRate, rate1m, rate5m, rate15m};
  protected static final Dimension[] summarizableDims = {min, max, mean, stddev};
  protected static final Dimension[] SamplingDims = {median, p75, p95, p98, p99, p999};

  private void send(Metered metric) {
    double[] values = {metric.count(), metric.meanRate(), metric.oneMinuteRate(),
        metric.fiveMinuteRate(), metric.fifteenMinuteRate()};
    for (int i = 0; i < values.length; ++i) {
      sendDouble(meterDims[i], values[i]);
    }
  }

  protected void send(Summarizable metric) {
    double[] values = {metric.min(), metric.max(), metric.mean(), metric.stdDev()};
    for (int i = 0; i < values.length; ++i) {
      sendDouble(summarizableDims[i], values[i]);
    }
  }

  protected void send(Sampling metric) {
    final Snapshot snapshot = metric.getSnapshot();
    double[] values = {snapshot.getMedian(), snapshot.get75thPercentile(), snapshot.get95thPercentile(),
        snapshot.get98thPercentile(), snapshot.get99thPercentile(), snapshot.get999thPercentile()};
    for (int i = 0; i < values.length; ++i) {
      sendDouble(SamplingDims[i], values[i]);
    }
  }

  private void sendDouble(Dimension dim, double value) {
    if (dimensions.contains(dim)) {
      statsd.gauge(parser.getName() + "." + dim.getDisplayName(), value, parser.getTags());
    }
  }

  private Boolean isDoubleParsable(final Object o) {
    if (o instanceof Float) {
      return true;
    } else if (o instanceof Double) {
      return true;
    } else if (o instanceof Byte) {
      return false;
    } else if (o instanceof Short) {
      return false;
    } else if (o instanceof Integer) {
      return false;
    } else if (o instanceof Long) {
      return false;
    } else if (o instanceof BigInteger) {
      return false;
    } else if (o instanceof BigDecimal) {
      return true;
    }
    return null;
  }
}
