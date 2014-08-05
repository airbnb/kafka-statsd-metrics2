/**
 *
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbnb.kafka;

import com.airbnb.metrics.ExcludeMetricPredicate;
import com.airbnb.metrics.MetricDimensionOptions;
import com.airbnb.metrics.StatsDReporter;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Kafka uses Yammer Metrics for metrics reporting in both the server and the client.
 * This can be configured to report stats using pluggable stats reporters to hook up to your monitoring system.
 *
 * This class allows to report Kafka Metrics to StatsD.
 *
 * The reporter is enabled and configured from the Kafka config file, server.properties.
 *
 * @see kafka.metrics.KafkaMetricsReporter
 */
public class KafkaStatsdMetricsReporter implements KafkaStatsdMetricsReporterMBean, KafkaMetricsReporter {


    public static final String DEFAULT_EXCLUDE_REGEX = "(kafka\\.consumer\\.FetchRequestAndResponseMetrics.*)|(.*ReplicaFetcherThread.*)|(kafka\\.server\\.FetcherLagMetrics\\..*)|(kafka\\.log\\.Log\\..*)|(kafka\\.cluster\\.Partition\\..*)";

    private AbstractPollingReporter underlying;
    private boolean enabled = false;
    private boolean running = false;
    private final Logger logger = Logger.getLogger(getClass());
    private String host;
    private int port;
    private String prefix;
    private MetricDimensionOptions metricDimensionOptions;
    private MetricPredicate metricPredicate;


    @Override
    public String getMBeanName() {
        return "kafka:type="+getClass().getName();
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void init(VerifiableProperties props) {
        enabled = props.getBoolean("external.kafka.statsd.reporter.enabled", false);
        int pollingIntervalSecs = props.getInt("kafka.metrics.polling.interval.secs", 10);
        host = props.getString("external.kafka.statsd.host", "localhost");
        port = props.getInt("external.kafka.statsd.port", 8125);
        prefix = props.getString("external.kafka.statsd.metrics.prefix", null);
        metricPredicate = MetricPredicate.ALL;
        metricDimensionOptions = MetricDimensionOptions.fromProperties(props.props(), "external.kafka.statsd.dimension.enabled.");

        String exclude_regex = props.getString("external.kafka.statsd.metrics.exclude_regex", DEFAULT_EXCLUDE_REGEX);
        if (exclude_regex.length() != 0){
            metricPredicate = new ExcludeMetricPredicate(exclude_regex);
        }


        underlying = new StatsDReporter(Metrics.defaultRegistry(), host, port, prefix, metricPredicate, metricDimensionOptions);

        if (enabled) {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is enabled");

            startReporter(pollingIntervalSecs);
        } else {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is disabled");

        }
    }


    @Override
    public synchronized void startReporter(long pollingPeriodSecs) {
        if (!enabled) {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is disabled");
            throw new UnsupportedOperationException("Reported is disabled. See external.kafka.statsd.reporter.enabled property");
        }
        if (pollingPeriodSecs <= 0)
            throw new IllegalArgumentException("Polling period must be greater than zero");

        if (!running) {
            underlying.start(pollingPeriodSecs, TimeUnit.SECONDS);
            running = true;
            if (logger.isInfoEnabled())
                logger.info(String.format("Started Kafka Statsd metrics reporter with host=%s, port=%d, polling_period_secs=%d, prefix=%s", host, port, pollingPeriodSecs, prefix == null ? "" : prefix));
        } else {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is already running");
        }
    }


    @Override
    public synchronized void stopReporter() {
        if (!enabled) {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is disabled");
            return;
        }
        if (running) {
            underlying.shutdown();
            running = false;
            if (logger.isInfoEnabled())
                logger.info("Stopped Kafka Statsd metrics reporter");
            underlying = new StatsDReporter(Metrics.defaultRegistry(), host, port, prefix, metricPredicate, metricDimensionOptions);
        } else {
            if (logger.isInfoEnabled())
                logger.info("Kafka Statsd metrics reporter is not running");
        }
    }


}
