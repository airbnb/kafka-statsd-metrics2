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

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 *
 */
public class ExcludeMetricPredicate implements MetricPredicate {
  private final Logger logger = Logger.getLogger(getClass());

  final String excludeRegex;
  final Pattern pattern;

  public ExcludeMetricPredicate(String excludeRegex) {
    this.excludeRegex = excludeRegex;
    this.pattern = Pattern.compile(excludeRegex);
  }

  @Override
  public boolean matches(MetricName name, Metric metric) {
    String n = MetricNameFormatter.format(name);
    boolean excluded = pattern.matcher(n).matches();
    if (excluded) {
      if (logger.isTraceEnabled()) {
        logger.trace("Metric " + n + " is excluded");
      }
    }
    return !excluded;
  }
}
