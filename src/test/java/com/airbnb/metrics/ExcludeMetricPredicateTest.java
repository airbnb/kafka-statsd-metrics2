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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ExcludeMetricPredicateTest {

  @Test
  public void exclude() {
    ExcludeMetricPredicate predicate = new ExcludeMetricPredicate("my\\.package\\.MyClass.*");
    // String group, String type, String name, String scope
    assertFalse(predicate.matches(new MetricName("my.package", "MyClass", "some_name", "some_scope"), null));
    assertTrue(predicate.matches(new MetricName("another.package", "MyClass", "some_name", "some_scope"), null));
  }

  @Test
  public void exclude2() {
    ExcludeMetricPredicate predicate = new ExcludeMetricPredicate("(kafka\\.consumer\\.FetchRequestAndResponseMetrics.*)|(.*ReplicaFetcherThread.*)|(kafka\\.server\\.FetcherLagMetrics\\..*)|(kafka\\.log\\.Log\\..*)|(kafka\\.cluster\\.Partition\\..*)");
    assertFalse(predicate.matches(new MetricName("kafka.consumer", "FetchRequestAndResponseMetrics", "some_name", "some_scope"), null));
    assertFalse(predicate.matches(new MetricName("kafka.server", "FetcherStats", "ReplicaFetcherThread", "some_scope"), null));
    assertTrue(predicate.matches(new MetricName("kafka.server", "ReplicaManager", "IsrExpandsPerSec", "some_scope"), null));

  }
}
