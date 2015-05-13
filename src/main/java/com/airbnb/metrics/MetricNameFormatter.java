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


import com.yammer.metrics.core.MetricName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MetricNameFormatter {
  static final Pattern whitespaceRegex = Pattern.compile("\\s+");

  public static String format(MetricName name) {
    final StringBuilder sb = new StringBuilder(128)
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

  public static String sanitizeName(MetricName metricName) {
    return sanitizeName(metricName, metricName.getName(), "");
  }

  public static String sanitizeName(MetricName metricName, String suffix) {
    return sanitizeName(metricName, metricName.getName(), suffix);
  }

  //keep it similar as those in com.airbnb.kafka.KafkaStatsdMetricsReporter
  public static String sanitizeName(MetricName metricName, String name, String suffix) {
    return new StringBuilder(128)
        .append(metricName.getGroup())
        .append('.')
        .append(metricName.getType())
        .append('.')
        .append(sanitizeName(name))
        .append(suffix)
        .toString();
  }

  public static String sanitizeName(String name) {
    Matcher m = whitespaceRegex.matcher(name);
    if (m.find())
      return m.replaceAll("_");
    else
      return name;
  }
}
