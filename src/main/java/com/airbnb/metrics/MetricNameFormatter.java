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


  public static String formatWithScope(MetricName metricName) {
    StringBuilder sb = new StringBuilder(128)
        .append(metricName.getGroup())
        .append('.')
        .append(metricName.getType())
        .append('.');
    if (metricName.hasScope() && !metricName.getScope().isEmpty()) {
      sb.append(metricName.getScope())
          .append(".");
    }
    sb.append(sanitizeName(metricName.getName()));
    return sb.toString();
  }

  public static String format(MetricName metricName) {
    return format(metricName, "");
  }

  public static String format(MetricName metricName, String suffix) {
    return new StringBuilder(128)
        .append(metricName.getGroup())
        .append('.')
        .append(metricName.getType())
        .append('.')
        .append(sanitizeName(metricName.getName()))
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
