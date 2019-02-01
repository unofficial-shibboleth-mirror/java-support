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

package net.shibboleth.utilities.java.support.logic;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.testng.annotations.Test;

import com.google.common.base.Optional;

/**
 * Test for {@link TransformAndCheckFunction}.
 */
public class TransformAndCheckFunctionTest {

    private final List<String> excludes = Arrays.asList("one", "2", "iii");

    @Test public void testConstructorFails() {
        Function<String, Optional<? extends String>> f = null;
        boolean thrown = false;
        try {
            f = new TransformAndCheckFunction<>(nullValue(), new MyPredicate(), true);
        } catch (final ConstraintViolationException e) {
            thrown = true;
        }
        org.testng.Assert.assertTrue(thrown, "Null function should throw");

        thrown = false;
        try {
            f = new TransformAndCheckFunction<>(TrimOrNullStringFunction.INSTANCE, nullValue(), true);
        } catch (final ConstraintViolationException e) {
            thrown = true;
        }
        org.testng.Assert.assertTrue(thrown, "Null predicate should throw");
        org.testng.Assert.assertNull(f, "silence compiler warning");
    }

    @Test(expectedExceptions=IllegalArgumentException.class) public void testApply() {
        Function<String, Optional<? extends String>> f =
                new TransformAndCheckFunction<>(TrimOrNullStringFunction.INSTANCE, new MyPredicate(), false);

        org.testng.Assert.assertFalse(f.apply(" two").isPresent(), "Should not be present since the predicate failed");
        org.testng.Assert.assertEquals(f.apply(" iii ").get(), "iii", "present and trimmed");

        f = new TransformAndCheckFunction<>(TrimOrNullStringFunction.INSTANCE, new MyPredicate(), true);
        org.testng.Assert.assertEquals(f.apply(" iii ").get(), "iii", "present and trimmed");
        f.apply(" two");
    }

    private <T> T nullValue() {
        return null;
    }
    
    private class MyPredicate implements Predicate<String> {
        /** {@inheritDoc} */
        public boolean test(String input) {
            for (String s : excludes) {
                if (s.equals(input)) {
                    return true;
                }
            }
            return false;
        }
    }
}
