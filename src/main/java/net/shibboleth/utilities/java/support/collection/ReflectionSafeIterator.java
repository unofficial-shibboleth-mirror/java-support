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

import java.util.Iterator;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 *  Wrapper class for delegating publically to {@link Iterator} implementations that
 *  may themselves be private.
 *  
 *  <p>This addresses bugs in Java that result from old implementations of collection classes
 *  that were never cleaned up to work properly in Java 17+ after access to JDK internals was
 *  clossed off.</p>
 *  
 *  @param <T> iterator type
 *  
 *  @since 8.2.0
 */
public class ReflectionSafeIterator<T> implements Iterator<T> {

    /** Wrapped iterator. */
    @Nonnull private final Iterator<T> iter;
    
    /**
     * Constructor.
     *
     * @param i iterator to wrap
     */
    public ReflectionSafeIterator(@Nonnull final Iterator<T> i) {
        iter = Constraint.isNotNull(i, "Wrapped Iterator cannot be null");
    }
    
    /** {@inheritDoc} */
    public boolean hasNext() {
        return iter.hasNext();
    }

    /** {@inheritDoc} */
    public T next() {
        return iter.next();
    }

}