/*
 *
 * Copyright (c) 2015. Jun He jun.he@airbnb.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.airbnb.kafka;

import com.airbnb.metrics.MetricDimensionOptions;
import com.airbnb.metrics.Parser;
import com.airbnb.metrics.ParserForNoTag;
import com.airbnb.metrics.ParserForTagInMBeanName;
import com.timgroup.statsd.StatsDClient;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import static com.airbnb.metrics.MetricDimensionOptions.Dimension.*;

/**
 * todo implement MetricPredicate
 */
public class DatadogReporter extends AbstractPollingReporter implements MetricProcessor<Long> {
    static final Logger log = LoggerFactory.getLogger(DatadogReporter.class);
    public static final String REPORTER_NAME = "kafka-metrics-dogstatsd";

    private final StatsDClient statsd;
    private final Clock clock;
    private final MetricDimensionOptions dimensionOptions;
    private MetricPredicate metricPredicate;
    private boolean isTagSupported;

    private Parser nameTagParser;

    public DatadogReporter(MetricsRegistry metricsRegistry
            , StatsDClient statsd
            , MetricDimensionOptions metricDimensionOptions) {
        this(metricsRegistry, statsd, REPORTER_NAME, MetricPredicate.ALL, metricDimensionOptions, true);
    }

    public DatadogReporter(MetricsRegistry metricsRegistry
            , StatsDClient statsd
            , MetricPredicate metricPredicate
            , MetricDimensionOptions metricDimensionOptions
            , boolean isTagSupported) {
        this(metricsRegistry, statsd, REPORTER_NAME, metricPredicate, metricDimensionOptions, isTagSupported);
    }

    public DatadogReporter(MetricsRegistry metricsRegistry
            , StatsDClient statsd
            , String reporterName
            , MetricPredicate metricPredicate
            , MetricDimensionOptions metricDimensionOptions
            , boolean isTagSupported) {
        super(metricsRegistry, reporterName);
        this.statsd = statsd;               //exception in statsd is handled by default NO_OP_HANDLER (do nothing)
        this.clock = Clock.defaultClock();
        this.nameTagParser = null;          //postpone set it because kafka doesn't start reporting any metrics.
        this.dimensionOptions = metricDimensionOptions;
        this.metricPredicate = metricPredicate;
        this.isTagSupported = isTagSupported;
    }

    @Override
    public void run() {
        try {
            final long epoch = clock.time() / 1000;
            if (nameTagParser == null) {
                createParser(getMetricsRegistry());
            }
            sendAllKafkaMetrics(epoch);
        } catch (RuntimeException ex) {
            log.error("Failed to print metrics to statsd", ex);
        }
    }

    private void createParser(MetricsRegistry metricsRegistry) {
        if (isTagSupported) {
            final boolean isMetricsTagged = isTagged(metricsRegistry.allMetrics());
            log.info("Kafka metrics {} tagged.", isMetricsTagged ? "is already" : "is not");
            if (isMetricsTagged) {
                nameTagParser = new ParserForTagInMBeanName();
            } else {
                nameTagParser = new ParserForNoTag();   //todo ParserForTagInName class
            }
        } else {
            nameTagParser = new ParserForNoTag();
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
        final Map<MetricName, Metric> allMetrics = new TreeMap<>(getMetricsRegistry().allMetrics());
        for (Map.Entry<MetricName, Metric> entry : allMetrics.entrySet()) {
            sendAMetric(entry.getKey(), entry.getValue(), epoch);
        }
    }

    private void sendAMetric(MetricName metricName, Metric metric, long epoch) {
        log.debug(String.format("  MBeanName[%s], Group[%s], Name[%s], Scope[%s], Type[%s]",
                metricName.getMBeanName(), metricName.getGroup(), metricName.getName(),
                metricName.getScope(), metricName.getType()));

        if (metricPredicate.matches(metricName, metric) && metric != null) {
            try {
                nameTagParser.parse(metricName);
                metric.processWith(this, metricName, epoch);
            } catch (Exception ignored) {
                log.error("Error printing regular metrics:", ignored);
            }
        }
    }

    @Override
    public void processCounter(MetricName metricName, Counter counter, Long context) throws Exception {
        statsd.gauge(nameTagParser.getName(), counter.count(), nameTagParser.getTags());
    }

    @Override
    public void processMeter(MetricName metricName, Metered meter, Long epoch) {
        send(meter);
    }

    private void sendDouble(MetricDimensionOptions.Dimension dim, double value) {
        if (dimensionOptions.isEnabled(dim)) {
            statsd.gauge(nameTagParser.getName() + dim.toNameString(), value, nameTagParser.getTags());
        }
    }

    protected static MetricDimensionOptions.Dimension[] meterDims = {count, meanRate, rate1m, rate5m, rate15m};
    protected static MetricDimensionOptions.Dimension[] summarizableDims = {min, max, mean, stddev};
    protected static MetricDimensionOptions.Dimension[] SamplingDims = {median, p75, p95, p98, p99, p999};

    private void send(Metered metric) {
        double[] values = {metric.count(), metric.meanRate(), metric.oneMinuteRate()
                , metric.fiveMinuteRate(), metric.fifteenMinuteRate()};
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
        double[] values = {snapshot.getMedian(), snapshot.get75thPercentile(), snapshot.get95thPercentile()
                , snapshot.get98thPercentile(), snapshot.get99thPercentile(), snapshot.get999thPercentile()};
        for (int i = 0; i < values.length; ++i) {
            sendDouble(SamplingDims[i], values[i]);
        }
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
            statsd.gauge(nameTagParser.getName(), new Double(value.toString()), nameTagParser.getTags());
        } else {
            statsd.gauge(nameTagParser.getName(), new Long(value.toString()), nameTagParser.getTags());
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
