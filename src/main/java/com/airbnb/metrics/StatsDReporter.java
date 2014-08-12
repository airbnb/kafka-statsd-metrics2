package com.airbnb.metrics;

/**
 * Copyright (C) 2014-2015 Alexis Midon alexis.midon@airbnb.com
 * Copyright (C) 2012-2013 Sean Laurent
 * Copyright (C) 2013 metrics-statsd contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airbnb.metrics.MetricDimensionOptions.Dimension.*;

public class StatsDReporter extends AbstractPollingReporter implements MetricProcessor<Long> {
    private final Logger logger = Logger.getLogger(getClass());

    protected final String prefix;
    protected final MetricPredicate predicate;
    protected final Clock clock;
    protected final VirtualMachineMetrics vm;

    private final StatsD statsD;
    private MetricDimensionOptions dimensionOptions = MetricDimensionOptions.ALL_ENABLED;
    private final Pattern whitespaceRegex = Pattern.compile("\\s+");


    public StatsDReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix, MetricPredicate predicate, MetricDimensionOptions dimensionOptions) {
        this(metricsRegistry,
                prefix,
                predicate,
                Clock.defaultClock(),
                new StatsD(host, port));
        this.dimensionOptions = dimensionOptions;
    }

    public StatsDReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Clock clock, StatsD statsD) {
        this(metricsRegistry, prefix, predicate, clock, VirtualMachineMetrics.getInstance(), statsD);
    }

    public StatsDReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Clock clock, VirtualMachineMetrics vm, StatsD statsD) {
        this(metricsRegistry, prefix, predicate, clock, vm, "statsd-reporter", statsD);
    }

    public StatsDReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Clock clock, VirtualMachineMetrics vm, String name, StatsD statsD) {
        super(metricsRegistry, name);

        this.vm = vm;

        this.clock = clock;

        if (prefix != null) {
            // Pre-append the "." so that we don't need to make anything conditional later.
            this.prefix = prefix + ".";
        } else {
            this.prefix = "";
        }
        this.predicate = predicate;
        this.statsD = statsD;
    }

    @Override
    public void run() {
        try {
            statsD.connect();
            final long epoch = clock.time() / 1000;
            printJvmMetrics(epoch);
            printRegularMetrics(epoch);
        } catch (IOException e) {
            logger.info("Failed to connect or print metrics to statsd", e);
        } finally {
            try {
                statsD.close();
            } catch (IOException e) {
                logger.info("Failure when closing statsd connection", e);
            }
        }

    }


    protected void printJvmMetrics(long epoch) {
        processGauge("jvm.memory.heap.usage", vm.heapUsage(), epoch);
        processGauge("jvm.memory.heap.used", vm.heapUsed(), epoch);
        processGauge("jvm.memory.non_heap.usage", vm.nonHeapUsage(), epoch);
        for (Map.Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
            String gaugeName = String.format("jvm.memory.pool.%s.usage", pool.getKey());
            processGauge(gaugeName, pool.getValue(), epoch);
        }

        processGauge("jvm.daemon_threads.count", vm.daemonThreadCount(), epoch);
        processGauge("jvm.threads.count", vm.threadCount(), epoch);
        processGauge("jvm.uptime", vm.uptime(), epoch);
        processGauge("jvm.fd_usage", vm.fileDescriptorUsage(), epoch);

        for (Map.Entry<Thread.State, Double> entry : vm.threadStatePercentages()
                .entrySet()) {
            String gaugeName = String.format("jvm.threads.state.%s",
                    entry.getKey());
            processGauge(gaugeName, entry.getValue(), epoch);
        }

        for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm
                .garbageCollectors().entrySet()) {

            String name = entry.getKey();
            String p = String.format("jvm.gc.%s", name);
            processGauge(p + ".time", entry.getValue().getTime(TimeUnit.MILLISECONDS), epoch);
            processGauge(p + ".runs", entry.getValue().getRuns(), epoch);
        }
    }



    protected void printRegularMetrics(long epoch) {
        for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry().groupedMetrics(predicate).entrySet()) {
            for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                final Metric metric = subEntry.getValue();
                if (metric != null) {
                    try {
                        metric.processWith(this, subEntry.getKey(), epoch);
                    } catch (Exception ignored) {
                        logger.error("Error printing regular metrics:", ignored);
                    }
                }
            }
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Long epoch) {
        final String sanitizedName = sanitizeName(name);
        if(dimensionOptions.isEnabled(count)) sendToStatsD(sanitizedName + ".samples", formatNumber(meter.count()));
        if(dimensionOptions.isEnabled(meanRate)) sendToStatsD(sanitizedName + ".meanRate", formatNumber(meter.meanRate()));
        if(dimensionOptions.isEnabled(rate1m)) sendToStatsD(sanitizedName + ".1MinuteRate", formatNumber(meter.oneMinuteRate()));
        if(dimensionOptions.isEnabled(rate5m)) sendToStatsD(sanitizedName + ".5MinuteRate", formatNumber(meter.fiveMinuteRate()));
        if(dimensionOptions.isEnabled(rate15m)) sendToStatsD(sanitizedName + ".15MinuteRate", formatNumber(meter.fifteenMinuteRate()));
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Long epoch) {
        sendToStatsD(sanitizeName(name), formatNumber(counter.count()));
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Long epoch) {
        final String sanitizedName = sanitizeName(name);
        sendSummarizable(sanitizedName, histogram);
        sendSampling(sanitizedName, histogram);
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Long epoch) {
        processMeter(name, timer, epoch);
        final String sanitizedName = sanitizeName(name);
        sendSummarizable(sanitizedName, timer);
        sendSampling(sanitizedName, timer);
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Long epoch) {
        String stringValue = format(gauge.value());
        if (stringValue != null) {
            sendToStatsD(sanitizeName(name), stringValue);
        }
    }

    public void processGauge(String name, Object value, Long epoch) {
        String stringValue = format(value);
        if (stringValue != null) {
            sendToStatsD(sanitizeName(name), stringValue);
        }
    }

    protected void sendSummarizable(String sanitizedName, Summarizable metric) {
        if(dimensionOptions.isEnabled(min)) sendToStatsD(sanitizedName + ".min", formatNumber(metric.min()));
        if(dimensionOptions.isEnabled(max)) sendToStatsD(sanitizedName + ".max", formatNumber(metric.max()));
        if(dimensionOptions.isEnabled(mean)) sendToStatsD(sanitizedName + ".mean", formatNumber(metric.mean()));
        if(dimensionOptions.isEnabled(stddev)) sendToStatsD(sanitizedName + ".stddev", formatNumber(metric.stdDev()));
    }

    protected void sendSampling(String sanitizedName, Sampling metric) {
        final Snapshot snapshot = metric.getSnapshot();
        if(dimensionOptions.isEnabled(median)) sendToStatsD(sanitizedName + ".median", formatNumber(snapshot.getMedian()));
        if(dimensionOptions.isEnabled(p75)) sendToStatsD(sanitizedName + ".75percentile", formatNumber(snapshot.get75thPercentile()));
        if(dimensionOptions.isEnabled(p95))sendToStatsD(sanitizedName + ".95percentile", formatNumber(snapshot.get95thPercentile()));
        if(dimensionOptions.isEnabled(p98))sendToStatsD(sanitizedName + ".98percentile", formatNumber(snapshot.get98thPercentile()));
        if(dimensionOptions.isEnabled(p99))sendToStatsD(sanitizedName + ".99percentile", formatNumber(snapshot.get99thPercentile()));
        if(dimensionOptions.isEnabled(p999))sendToStatsD(sanitizedName + ".999percentile", formatNumber(snapshot.get999thPercentile()));
    }

    protected String sanitizeName(MetricName name) {
        final StringBuilder sb = new StringBuilder()
                .append(name.getGroup())
                .append('.')
                .append(name.getType())
                .append('.');
        if (name.hasScope()) {
            sb.append(name.getScope())
                    .append('.');
        }
        return sb.append(name.getName()).toString();
    }

    protected String sanitizeName(String name) {
        Matcher m = whitespaceRegex.matcher(name);
        if (m.find())
            return m.replaceAll("_");
        else
            return name;
    }

    private void sendToStatsD(String metricName,  String metricValue) {
        statsD.send(prefix + metricName, metricValue);
    }

    private String format(final Object o) {
        if (o instanceof Float) {
            return formatNumber(((Float) o).doubleValue());
        } else if (o instanceof Double) {
            return formatNumber((Double) o);
        } else if (o instanceof Byte) {
            return formatNumber(((Byte) o).longValue());
        } else if (o instanceof Short) {
            return formatNumber(((Short) o).longValue());
        } else if (o instanceof Integer) {
            return formatNumber(((Integer) o).longValue());
        } else if (o instanceof Long) {
            return formatNumber((Long) o);
        } else if (o instanceof BigInteger) {
            return formatNumber((BigInteger) o);
        } else if (o instanceof BigDecimal) {
            return formatNumber(((BigDecimal) o).doubleValue());
        }
        return null;
    }

    private String formatNumber(final BigInteger n) {
        return String.valueOf(n);
    }

    private String formatNumber(final long n) {
        return Long.toString(n);
    }

    private String formatNumber(final double v) {
        return String.format(Locale.US, "%2.2f", v);
    }
}

