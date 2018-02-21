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

package net.shibboleth.utilities.java.support.collection;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Test for {@link Pair} */
public class PairTest {

    private final static String STRING_1 = "StringOne";

    private final static String STRING_2 = "StringTwo";
    
    private final static Integer INTEGER_1 = -6; 

    private final static Integer INTEGER_2 = 0; 
    
    @Test
    public void testPair() {
        Pair <Integer, String> pair1;
        Pair <Integer, String> pair2;
        Pair <String, Integer> pair3;
        Pair <Object, Object> pair4;
        
        pair1 = new Pair<>(INTEGER_1, STRING_1);
        pair2 = new Pair<>(null, null);
        pair3 = new Pair<>(null, null);
        pair4 = new Pair<Object, Object>(INTEGER_2, STRING_2);
        
        Assert.assertEquals(pair1.getFirst(), INTEGER_1, "Should find " + INTEGER_1);
        Assert.assertNotSame(pair1.getSecond(), INTEGER_1, "Should not find " + INTEGER_1);
        Assert.assertEquals(pair1.getSecond(), STRING_1, "Should find " + STRING_1);
        Assert.assertNotSame(pair1, pair4, "different pairs");
        pair1.setSecond(STRING_2);
        pair1.setFirst(INTEGER_2);
        Assert.assertNotSame(pair1.getFirst(), INTEGER_1, "Should not find " + INTEGER_1);
        Assert.assertNotSame(pair1.getSecond(), STRING_1, "Should not find " + STRING_1);
        Assert.assertEquals(pair1.getFirst(), INTEGER_2, "Should find " + INTEGER_2);
        Assert.assertEquals(pair1.getSecond(), STRING_2, "Should find " + STRING_2);
        
        Assert.assertEquals(pair1, pair4, "Same contents");
        Assert.assertEquals(pair1.hashCode(), pair4.hashCode(), "Same contents, same hashcode");
        Assert.assertEquals(pair2, pair3, "null pairs are equal");
        Assert.assertEquals(pair2.hashCode(), pair3.hashCode(), "null pairs have equal hashcode");
        Assert.assertNotSame(pair1, pair3);
        
        pair3.setFirst(STRING_2);pair3.setSecond(INTEGER_2); 
        Assert.assertNotSame(pair1, pair3);
        Assert.assertNotSame(pair1.hashCode(), pair3.hashCode());
    }
}
