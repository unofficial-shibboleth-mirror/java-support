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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Objects;

/**
 * Test for the remaining method in {@link ObjectSupport}
 */
public class ObjectSupportTest {
    
    @Test public void testFirstNonNull() {
       Object foo = new Object();
       Object bar = new Object();
       Object baz = new Object();
       
       Assert.assertSame(ObjectSupport.firstNonNull(foo, bar), foo);
       Assert.assertSame(ObjectSupport.firstNonNull(foo, bar, baz), foo);
       Assert.assertSame(ObjectSupport.firstNonNull(null, bar, baz), bar);
       Assert.assertSame(ObjectSupport.firstNonNull(null, null, baz), baz);
       
       Assert.assertNull(ObjectSupport.firstNonNull(null, null, null, null));
       Assert.assertNull(ObjectSupport.firstNonNull());
    }

    @Test public void testHashCode() {
        ComplexClass c1 = new ComplexClass(Integer.valueOf(1), Double.valueOf(3.4), "String!");
        ComplexClass c2 = new ComplexClass("String3", Integer.valueOf(1243), Boolean.valueOf(false));

        Assert.assertEquals(ObjectSupport.hashCode(null), 0, "Hashcode of null is 0");
        Assert.assertEquals(ObjectSupport.hashCode(c1), c1.hashCode(),
                "Hashcode of should be the same no matter how called");
        Assert.assertEquals(ObjectSupport.hashCode(c2), c2.hashCode(),
                "Hashcode of should be the same no matter how called");
        Assert.assertNotSame(ObjectSupport.hashCode(c1), ObjectSupport.hashCode(c2),
                "Hashcode of different objects should differ");
        Assert.assertEquals(c2.getHashCalls(), 3, "Hashcode should have been called three times");

    }

    private class ComplexClass {

        private Object object1;

        private Object object2;

        private Object object3;

        private int hashCalls;

        public int getHashCalls() {
            return hashCalls;
        }

        ComplexClass(Object o1, Object o2, Object o3) {
            object1 = o1;
            object2 = o2;
            object3 = o3;
        }

        public int hashCode() {
            hashCalls++;
            return Objects.hashCode(object1, object2, object3);
        }
    }
}
