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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class ParserTest {

    @Test
    public void testParseTagInMBeanName() throws Exception {
        MetricName name = new MetricName("kafka.producer",
                "ProducerRequestMetrics", "ProducerRequestSize",
                "clientId.group7", "kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestSize,clientId=group7");
        Parser p = new ParserForTagInMBeanName();
        p.parse(name);
        assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.ProducerRequestSize_all");
        assertArrayEquals(p.getTags(), new String[]{"clientId:group7"});
    }

    @Test
    public void testParseTagInName() throws Exception {

    }

    @Test
    public void testParseNoTag() throws Exception {
        MetricName name = new MetricName("kafka.producer",
                "ProducerRequestMetrics", "group7-AllBrokersProducerRequestSize");
        Parser p = new ParserForNoTag();
        p.parse(name);
        assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.group7-AllBrokersProducerRequestSize");
        assertNull(p.getTags());
    }

}
