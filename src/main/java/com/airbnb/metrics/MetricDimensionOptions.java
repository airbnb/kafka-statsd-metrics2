/**
 * Copyright (C) 2014-2015 Alexis Midon alexis.midon@airbnb.com
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


package com.airbnb.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class MetricDimensionOptions {

    public static final MetricDimensionOptions DEFAULT = new MetricDimensionOptions();
    public static final MetricDimensionOptions ALL_ENABLED = new MetricDimensionOptions(Boolean.TRUE);

    public enum Dimension {    //use name itself as suffix
        count(false),
        meanRate(true),
        rate1m(true),
        rate5m(true),
        rate15m(true),
        min(false),
        max(false),
        mean(true),
        stddev(false),
        median(true),
        p75(false),
        p95(false),
        p98(false),
        p99(true),
        p999(false);

        final Boolean defaultValue;

        final String[] sanitizeNames = {".samples", ".meanRate", ".1MinuteRate", ".5MinuteRate"
                , ".15MinuteRate", ".min", ".max", ".mean", ".stddev", ".median", ".75percentile"
                , ".95percentile", ".98percentile", ".99percentile", ".999percentile"};

        public String toNameString() {
            return sanitizeNames[ordinal()];
        }

        Dimension(Boolean defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private final Map<Dimension, Boolean> options = new HashMap<Dimension, Boolean>();

    public Boolean isEnabled(Dimension dimension) {
        return options.get(dimension);
    }

    public MetricDimensionOptions(Boolean enabled) {
        for (Dimension k : Dimension.values()) {
            options.put(k, enabled);
        }

    }

    public MetricDimensionOptions() {
        for (Dimension k : Dimension.values()) {
            options.put(k, k.defaultValue);
        }
    }

    public static MetricDimensionOptions fromProperties(Properties p, String prefix) {
        MetricDimensionOptions df = new MetricDimensionOptions();
        for (Dimension k : Dimension.values()) {
            String key = prefix + k.toString();
            if (p.containsKey(key)) {
                Boolean value = Boolean.parseBoolean(p.getProperty(key));
                df.options.put(k, value);
            }

        }
        return df;
    }
}
