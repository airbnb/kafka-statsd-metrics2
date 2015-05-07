/*
 *
 * Copyright (c) 2015. Jun He jun.he@airbnb.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.airbnb.metrics;

import com.yammer.metrics.core.MetricName;

/**
 * Parser for statsd not supporting tags
 */
public class ParserForNoTag extends Parser {

    @Override
    public void parse(MetricName metricName) {
        name = sanitizeName(metricName);
        tags = null;
    }
}
