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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Parser for kafka 0.8.2 or later version
 * where the MBeanName contains tags and
 * Scope will store tags as well.
 */
public class ParserForTagInMBeanName extends Parser {

    public static final String SUFFIX_FOR_ALL = "_all";

    @Override
    public void parse(MetricName metricName) {
        Pattern p = tagRegexMap.get(metricName.getType());
        if (p != null && !p.matcher(metricName.getMBeanName()).matches()) {
            name = sanitizeName(metricName, SUFFIX_FOR_ALL);
        } else {
            name = sanitizeName(metricName);
        }
        tags = getTags(metricName);
    }

    String[] getTags(MetricName metricName) {
        if (metricName.hasScope()) {
            final String name = metricName.getName();
            final String mBeanName = metricName.getMBeanName();
            final int idx = mBeanName.indexOf(name);
            if (idx < 0) {
                log.error("Cannot find name[{}] in MBeanName[{}]", name, mBeanName);
            } else {
                String tags = mBeanName.substring(idx + name.length() + 1);
                if ("kafka.producer".equals(metricName.getGroup())) {
                    if (tags == null || tags.length() <= 0) {
                        tags = "clientId=unknown";
                    } else if (!tags.contains("clientId")) {
                        tags = "clientId=unknown,".concat(tags);
                    }
                }
                if (tags != null && tags.length() > 0) {
                    return tags.replace('=', ':').split(",");
                }
            }
        } else if ("kafka.producer".equals(metricName.getGroup())) {
            return new String[]{"clientId:unknown"};
        }
        return null;    //for the case without tag
    }

    public static final Map<String, Pattern> tagRegexMap = new ConcurrentHashMap<>();

    static {
        tagRegexMap.put("BrokerTopicMetrics", Pattern.compile(".*topic=.*"));
        tagRegexMap.put("DelayedProducerRequestMetrics", Pattern.compile(".*topic=.*"));

        tagRegexMap.put("ProducerTopicMetrics", Pattern.compile(".*topic=.*"));
        tagRegexMap.put("ProducerRequestMetrics", Pattern.compile(".*brokerHost=.*"));

        tagRegexMap.put("ConsumerTopicMetrics", Pattern.compile(".*topic=.*"));
        tagRegexMap.put("FetchRequestAndResponseMetrics", Pattern.compile(".*brokerHost=.*"));
        tagRegexMap.put("ZookeeperConsumerConnector", Pattern.compile(".*name=OwnedPartitionsCount,.*topic=.*|^((?!name=OwnedPartitionsCount).)*$"));
    }
}
