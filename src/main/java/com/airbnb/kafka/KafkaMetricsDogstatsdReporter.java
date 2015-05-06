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

import com.airbnb.metrics.ExcludeMetricPredicate;
import com.airbnb.metrics.MetricDimensionOptions;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class KafkaMetricsDogstatsdReporter implements KafkaMetricsDogstatsdReporterMBean, KafkaMetricsReporter {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DatadogReporter.class);

    public static final String DEFAULT_REGEX_FILTER = "(kafka\\.consumer\\.FetchRequestAndResponseMetrics.*)|(.*ReplicaFetcherThread.*)|(kafka\\.server\\.FetcherLagMetrics\\..*)|(kafka\\.log\\.Log\\..*)|(kafka\\.cluster\\.Partition\\..*)";

    @Override
    public String getMBeanName() {
        return "kafka:type=" + getClass().getName();
    }

    private boolean enabled;
    private AtomicBoolean running = new AtomicBoolean(false);
    private String host;
    private int port;
    private String prefix;
    private long pollingPeriodInSeconds;
    private MetricDimensionOptions metricDimensionOptions;
    private MetricPredicate metricPredicate;
    private StatsDClient statsd;
    private boolean isTagSupported;
    private AbstractPollingReporter underlying = null;

    public boolean isRunning() {
        return running.get();
    }

    //try to make it compatible with kafka-statsd-metrics2
    @Override
    public synchronized void init(VerifiableProperties props) {
        loadConfig(props);
        if (enabled) {
            log.info("Reporter is enabled and starting...");
            startReporter(pollingPeriodInSeconds);
        } else {
            log.warn("Reporter is disabled");
        }
    }

    private void loadConfig(VerifiableProperties props) {
        enabled = props.getBoolean("external.kafka.statsd.reporter.enabled", false);
        host = props.getString("external.kafka.statsd.host", "localhost");
        port = props.getInt("external.kafka.statsd.port", 8125);
        prefix = props.getString("external.kafka.statsd.metrics.prefix", "");
        pollingPeriodInSeconds = props.getInt("kafka.metrics.polling.interval.secs", 10);
        metricDimensionOptions = MetricDimensionOptions.fromProperties(props.props()
                , "external.kafka.statsd.dimension.enabled.");

        String regexFilter = props.getString("external.kafka.statsd.metrics.exclude_regex", DEFAULT_REGEX_FILTER);
        if (regexFilter != null && regexFilter.length() != 0) {
            metricPredicate = new ExcludeMetricPredicate(regexFilter);
        }

        this.isTagSupported = props.getBoolean("external.kafka.statsd.support.tag", true);
    }

    @Override
    public void startReporter(long pollingPeriodInSeconds) {
        if (pollingPeriodInSeconds <= 0) {
            throw new IllegalArgumentException("Polling period must be greater than zero");
        }

        synchronized (running) {
            if (running.get()) {
                log.warn("Reporter is already running");
            } else {
                createStatsd();
                underlying = new DatadogReporter(Metrics.defaultRegistry()
                        , statsd
                        , metricPredicate
                        , metricDimensionOptions
                        , isTagSupported);
                underlying.start(pollingPeriodInSeconds, TimeUnit.SECONDS);
                log.info("Started Reporter with host={}, port={}, polling_period_secs={}, prefix={}",
                        host, port, pollingPeriodInSeconds, prefix);
                running.set(true);
            }
        }
    }

    private void createStatsd() {
        try {
            statsd = new NonBlockingStatsDClient(
                    prefix                                  /* prefix to any stats; may be null or empty string */
                    , host                                   /* common case: localhost */
                    , port                                   /* port */
            );
        } catch (StatsDClientException ex) {
            log.error("Reporter cannot be started");
            throw ex;
        }
    }

    @Override
    public void stopReporter() {
        if (!enabled) {
            log.warn("Reporter is disabled");
        } else {
            synchronized (running) {
                if (running.get() == false) {
                    log.warn("Reporter is not running");
                } else {
                    statsd.stop();
                    underlying.shutdown();
                    running.set(false);
                    log.info("Stopped Reporter with host={}, port={}", host, port);
                }
            }
        }
    }


}
