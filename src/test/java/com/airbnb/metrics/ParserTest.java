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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ParserTest {

  @Test
  public void testParseTagInMBeanNameWithSuffix() throws Exception {
    MetricName name = new MetricName("kafka.producer",
        "ProducerRequestMetrics", "ProducerRequestSize",
        "clientId.group7", "kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestSize,clientId=group7");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.ProducerRequestSize_all");
    assertArrayEquals(p.getTags(), new String[]{"clientId:group7"});
  }

  @Test
  public void testParseTagInMBeanNameWithSuffixWithoutClientId() throws Exception {
    MetricName name = new MetricName("kafka.producer",
        "ProducerRequestMetrics", "ProducerRequestSize",
        null, "kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestSize");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.ProducerRequestSize_all");
    assertArrayEquals(p.getTags(), new String[]{"clientId:unknown"});
  }

  @Test
  public void testParseTagInMBeanNameWithoutSuffix() throws Exception {
    MetricName name = new MetricName("kafka.producer",
        "ProducerRequestMetrics", "ProducerRequestSize",
        "clientId.group7.brokerPort.9092.brokerHost.10_1_152_206",
        "kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestSize,clientId=group7,brokerPort=9092,brokerHost=10.1.152.206");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.ProducerRequestSize");
    assertArrayEquals(p.getTags(), new String[]{"clientId:group7", "brokerPort:9092", "brokerHost:10.1.152.206"});
  }

  @Test
  public void testParseTagInMBeanNameWithoutClientId() throws Exception {
    MetricName name = new MetricName("kafka.producer",
        "ProducerRequestMetrics", "ProducerRequestSize",
        "brokerPort.9092.brokerHost.10_1_152_206", "kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestSize,brokerPort=9092,brokerHost=10.1.152.206");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.ProducerRequestSize");
    assertArrayEquals(p.getTags(), new String[]{"clientId:unknown", "brokerPort:9092", "brokerHost:10.1.152.206"});
  }

  @Test
  public void testParseTagInMBeanNameWithoutSuffixForConsumer() throws Exception {
    MetricName name = new MetricName("kafka.consumer",
        "ZookeeperConsumerConnector", "ZooKeeperCommitsPerSec",
        "clientId.group7",
        "kafka.consumer:type=ZookeeperConsumerConnector,name=ZooKeeperCommitsPerSec,clientId=group7");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.consumer.ZookeeperConsumerConnector.ZooKeeperCommitsPerSec");
    assertArrayEquals(p.getTags(), new String[]{"clientId:group7"});
  }

  @Test
  public void testParseTagInMBeanNameNoTag() throws Exception {
    MetricName name = new MetricName("kafka.server",
        "ReplicaManager", "LeaderCount",
        null, "kafka.server:type=ReplicaManager,name=LeaderCount");
    Parser p = new ParserForTagInMBeanName();
    p.parse(name);
    assertEquals(p.getName(), "kafka.server.ReplicaManager.LeaderCount");
    assertArrayEquals(p.getTags(), new String[]{});
  }

  @Test
  public void testParseNoTag() throws Exception {
    MetricName name = new MetricName("kafka.producer",
        "ProducerRequestMetrics", "group7-AllBrokersProducerRequestSize");
    Parser p = new ParserForNoTag();
    p.parse(name);
    assertEquals(p.getName(), "kafka.producer.ProducerRequestMetrics.group7-AllBrokersProducerRequestSize");
    assertArrayEquals(p.getTags(), new String[]{});
  }

}
