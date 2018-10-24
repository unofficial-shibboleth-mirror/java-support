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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Container for a pair of objects.
 * 
 * @param <T1> type of the first object in the pair
 * @param <T2> type of the second object in the pair
 */
public class Pair<T1, T2> {

    /** First object in pair. */
    private T1 first;

    /** Second object in pair. */
    private T2 second;

    /** Constructor. */
    public Pair() {

    }

    /**
     * Constructor.
     * 
     * @param newFirst first object in the pair
     * @param newSecond second object in the pair
     */
    public Pair(@Nullable final T1 newFirst, @Nullable final T2 newSecond) {
        first = newFirst;
        second = newSecond;
    }

    /**
     * Copy constructor.
     * 
     * @param pair pair to be copied
     */
    public Pair(@Nonnull final Pair<? extends T1, ? extends T2> pair) {
        Constraint.isNotNull(pair, "Pair to be copied can not be null");
        first = pair.getFirst();
        second = pair.getSecond();
    }

    /**
     * Gets the first object in the pair.
     * 
     * @return first object in the pair
     */
    @Nullable public T1 getFirst() {
        return first;
    }

    /**
     * Sets the first object in the pair.
     * 
     * @param newFirst first object in the pair
     */
    public void setFirst(@Nullable final T1 newFirst) {
        first = newFirst;
    }

    /**
     * Gets the second object in the pair.
     * 
     * @return second object in the pair
     */
    @Nullable public T2 getSecond() {
        return second;
    }

    /**
     * Sets the second object in the pair.
     * 
     * @param newSecond second object in the pair
     */
    public void setSecond(@Nullable final T2 newSecond) {
        second = newSecond;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Pair) {
            final Pair<?,?> otherPair = (Pair<?,?>) o;
            return java.util.Objects.equals(getFirst(), otherPair.getFirst())
                    && java.util.Objects.equals(getSecond(), otherPair.getSecond());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(first, second);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("first", first).add("second", second).toString();
    }
}