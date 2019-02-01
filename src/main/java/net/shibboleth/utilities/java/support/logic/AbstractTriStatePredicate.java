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

/**
 * A base {@link Predicate} implementation which provides support for cases where the
 * predicate can not meaningfully evaluate the input.  The flags on this class
 * allow translating these cases to a boolean <code>true</code> or <code>false</code>
 * as required by the predicate interface.
 *
 *@param <T> the type to which the predicate is applied
 */
public abstract class AbstractTriStatePredicate<T> implements Predicate<T> {
    
    /** Flag indicating whether the null input case is treated as satisfying the predicate. */
    private boolean nullInputSatisfies;
    
    /** Flag indicating whether the general unevaluable case is treated as satisfying the predicate. */
    private boolean unevaluableSatisfies;
    
    /**
     * Get the flag indicating whether a null input satisfies the predicate.
     * 
     * @return true if should satisfy, false otherwise
     */
    public boolean isNullInputSatisfies() {
        return nullInputSatisfies;
    }

    /**
     * Set the flag indicating whether a null input satisfies the predicate.
     * 
     * @param flag true if should satisfy, false otherwise
     */
    public void setNullInputSatisfies(final boolean flag) {
        nullInputSatisfies = flag;
    }

    /**
     * Get the flag indicating whether a general 'unevaluable' input satisfies the predicate.
     * 
     * @return true if should satisfy, false otherwise
     */
    public boolean isUnevaluableSatisfies() {
        return unevaluableSatisfies;
    }

    /**
     * Set the flag indicating whether a general 'unevaluable' input satisfies the predicate.
     * 
     * @param flag true if should satisfy, false otherwise
     */
    public void setUnevaluableSatisfies(final boolean flag) {
        unevaluableSatisfies = flag;
    }

}