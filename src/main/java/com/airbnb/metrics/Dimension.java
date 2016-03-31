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

import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public enum Dimension {    //use name itself as suffix
  count("count"),
  meanRate("meanRate"),
  rate1m("1MinuteRate"),
  rate5m("5MinuteRate"),
  rate15m("15MinuteRate"),
  min("min"),
  max("max"),
  mean("mean"),
  stddev("stddev"),
  median("median"),
  p75("p75"),
  p95("p95"),
  p98("p98"),
  p99("p99"),
  p999("p999");

  final String displayName;

  public String getDisplayName() {
    return displayName;
  }

  Dimension(String defaultValue) {
    this.displayName = defaultValue;
  }

  public static EnumSet<Dimension> fromProperties(Properties p, String prefix) {
    EnumSet<Dimension> df = EnumSet.allOf(Dimension.class);
    for (Dimension k : Dimension.values()) {
      String key = prefix + k.toString();
      if (p.containsKey(key)) {
        Boolean value = Boolean.parseBoolean(p.getProperty(key));
        if (!value) {
          df.remove(k);
        }
      }
    }
    return df;
  }

  public static EnumSet<Dimension> fromConfigs(Map<String, ?> configs, String prefix) {
    EnumSet<Dimension> df = EnumSet.allOf(Dimension.class);
    for (Dimension k : Dimension.values()) {
      String key = prefix + k.toString();
      if (configs.containsKey(key)) {
        Boolean value = (Boolean) configs.get(key);
        if (!value) {
          df.remove(k);
        }
      }
    }
    return df;
  }
}
