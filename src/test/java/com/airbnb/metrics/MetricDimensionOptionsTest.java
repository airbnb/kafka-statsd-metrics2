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

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 *
 */
public class MetricDimensionOptionsTest {

    @Test
    public void default_values() {
        for (MetricDimensionOptions.Dimension d : MetricDimensionOptions.Dimension.values()) {
            assertEquals("unexpected default value", d.defaultValue, MetricDimensionOptions.DEFAULT.isEnabled(d));
        }
    }

    @Test
    public void create_from_properties() {
        String prefix = "foo.";
        Properties p = new Properties();
        p.setProperty(prefix + "count", "true");
        p.setProperty(prefix + "meanRate", "false");
        MetricDimensionOptions options = MetricDimensionOptions.fromProperties(p, prefix);

        assertTrue(options.isEnabled(MetricDimensionOptions.Dimension.count));
        assertFalse(options.isEnabled(MetricDimensionOptions.Dimension.meanRate));
        assertEquals(MetricDimensionOptions.Dimension.rate1m.defaultValue, options.isEnabled(MetricDimensionOptions.Dimension.rate1m));
    }

}
