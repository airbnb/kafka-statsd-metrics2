package com.airbnb.metrics;

import com.yammer.metrics.core.MetricName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MetricNameFormatterTest {

    @Test
    public void testFormat() throws Exception {
        MetricName name = new MetricName("kafka.common", "AppInfo", "Version",
                null, "kafka.common:type=AppInfo,name=Version");
        assertEquals(MetricNameFormatter.format(name), "kafka.common.AppInfo.Version");
    }
}