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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link Predicate} that checks that any item in an {@link Iterable} matches a given target predicate. If the given
 * {@link Iterable} is null or contains no items this method will return <code>false</code>, otherwise it passes each
 * {@link Iterable} value to the target predicate, even if those values are <code>null</code>. The first
 * <code>true</code> returned by the target predicate stops evaluation and causes this predicate to return (i.e., it
 * short-circuits the evaluation).
 * 
 * @param <T> type of object upon which this predicate operates
 */
public class AnyMatchPredicate<T> implements Predicate<Iterable<T>> {

    /** The predicate applied to each value of the {@link Iterable}. */
    @Nonnull private final java.util.function.Predicate<T> predicate;

    /**
     * Constructor.
     * 
     * @param target the target predicate against which all {@link Iterable} elements are evaluated
     */
    public AnyMatchPredicate(@Nonnull final java.util.function.Predicate<T> target) {
        predicate = Constraint.isNotNull(target, "Target predicate cannot be null");
    }

    /** {@inheritDoc} */
    public boolean test(@Nullable final Iterable<T> inputs) {
        if (inputs == null) {
            return false;
        }

        for (final T input : inputs) {
            if (predicate.test(input)) {
                return true;
            }
        }

        return false;
    }
    
}