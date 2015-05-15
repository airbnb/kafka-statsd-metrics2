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

import org.junit.Test;

import java.util.EnumSet;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 *
 */
public class DimensionTest {

  @Test
  public void create_from_properties() {
    String prefix = "foo.";
    Properties p = new Properties();
    p.setProperty(prefix + "count", "true");
    p.setProperty(prefix + "meanRate", "false");
    EnumSet<Dimension> dimensions = Dimension.fromProperties(p, prefix);

    assertTrue(dimensions.contains(Dimension.count));
    assertFalse(dimensions.contains(Dimension.meanRate));
    assertEquals(Dimension.rate1m.displayName, "1MinuteRate");
  }

}
