/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.utilities.java.support.primitive;

import java.util.Formatter;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Test for {@link LazilyFormattedString}. */
public class LazilyFormattedStringTest {

    private final static String FORMAT = "%d--%o++%x";

    @Test public void testFormat() {
        Formatter formatter = new Formatter();
        Object value = Integer.valueOf(1234567);
        formatter.format(FORMAT, value, value, value);
        LazilyFormattedString testString = new LazilyFormattedString(FORMAT, value, value, value);

        Assert.assertEquals(testString.toString(), formatter.out().toString(),
                "Should be the same result regardless of whether lazily or actively formatted");
        formatter.close();
    }

    @Test public void testNullFormat() {
        Formatter formatter = new Formatter();
        Object value = Integer.valueOf(78654321);
        formatter.format(FORMAT, null, value, null, value);
        LazilyFormattedString testString = new LazilyFormattedString(FORMAT, null, value, null, value);

        Assert.assertEquals(testString.toString(), formatter.out().toString(),
                "Should be the same result regardless of whether lazily or actively formatted");
        formatter.close();
    }
}
