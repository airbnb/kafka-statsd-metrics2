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

package com.airbnb.kafka.kafka08;

import com.airbnb.metrics.Dimension;
import com.airbnb.metrics.ExcludeMetricPredicate;
import com.airbnb.metrics.StatsDReporter;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StatsdMetricsReporter implements StatsdMetricsReporterMBean, KafkaMetricsReporter {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(StatsDReporter.class);

  public static final String DEFAULT_EXCLUDE_REGEX = "(kafka\\.server\\.FetcherStats.*ConsumerFetcherThread.*)|(kafka\\.consumer\\.FetchRequestAndResponseMetrics.*)|(.*ReplicaFetcherThread.*)|(kafka\\.server\\.FetcherLagMetrics\\..*)|(kafka\\.log\\.Log\\..*)|(kafka\\.cluster\\.Partition\\..*)";

  private boolean enabled;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private String host;
  private int port;
  private String prefix;
  private long pollingPeriodInSeconds;
  private EnumSet<Dimension> metricDimensions;
  private MetricPredicate metricPredicate;
  private StatsDClient statsd;
  private boolean isTagEnabled;
  private AbstractPollingReporter underlying = null;

  @Override
  public String getMBeanName() {
    return "kafka:type=" + getClass().getName();
  }

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
    metricDimensions = Dimension.fromProperties(props.props(), "external.kafka.statsd.dimension.enabled.");

    String excludeRegex = props.getString("external.kafka.statsd.metrics.exclude_regex", DEFAULT_EXCLUDE_REGEX);
    if (excludeRegex != null && excludeRegex.length() != 0) {
      metricPredicate = new ExcludeMetricPredicate(excludeRegex);
    } else {
      metricPredicate = MetricPredicate.ALL;
    }

    this.isTagEnabled = props.getBoolean("external.kafka.statsd.tag.enabled", true);
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
        statsd = createStatsd();
        underlying = new StatsDReporter(
            Metrics.defaultRegistry(),
            statsd,
            metricPredicate,
            metricDimensions,
            isTagEnabled);
        underlying.start(pollingPeriodInSeconds, TimeUnit.SECONDS);
        log.info("Started Reporter with host={}, port={}, polling_period_secs={}, prefix={}",
            host, port, pollingPeriodInSeconds, prefix);
        running.set(true);
      }
    }
  }

  private StatsDClient createStatsd() {
    try {
      return new NonBlockingStatsDClient(
          prefix,                                  /* prefix to any stats; may be null or empty string */
          host,                                   /* common case: localhost */
          port                                   /* port */
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
        if (running.get()) {
          underlying.shutdown();
          statsd.stop();
          running.set(false);
          log.info("Stopped Reporter with host={}, port={}", host, port);
        } else {
          log.warn("Reporter is not running");
        }
      }
    }
  }

}
