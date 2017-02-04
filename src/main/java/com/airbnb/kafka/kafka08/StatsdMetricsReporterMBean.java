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

package com.airbnb.kafka.kafka08;

import kafka.metrics.KafkaMetricsReporterMBean;

/**
 * @see kafka.metrics.KafkaMetricsReporterMBean: the custom reporter needs to
 * additionally implement an MBean trait that extends kafka.metrics.KafkaMetricsReporterMBean
 * so that the registered MBean is compliant with the standard MBean convention.
 */
public interface StatsdMetricsReporterMBean extends KafkaMetricsReporterMBean {
}
