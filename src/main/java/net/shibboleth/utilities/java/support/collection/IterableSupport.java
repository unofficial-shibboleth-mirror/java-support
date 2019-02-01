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

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/** Helper methods for working with {@link Iterable} instances. */
public final class IterableSupport {
    
    /** Constructor. */
    private IterableSupport() { }
    
    /**
     * Checks whether the {@link Iterable} contains at least one instance of the supplied class.
     * 
     * @param target iterable to evaluate
     * @param clazz the class to evaluate
     * 
     * @return true if an instance of the given class is present in the iterable
     */
    public static boolean containsInstance(@Nonnull final Iterable<?> target, @Nonnull final Class<?> clazz) {
        Constraint.isNotNull(target, "Target collection can not be null");
        Constraint.isNotNull(clazz, "Class can not be null");
        
        final Predicate<Object> instanceOf = Predicates.instanceOf(clazz);
        final Optional<?> result = Iterables.tryFind(target, instanceOf::test);
        return result.isPresent();
    }

}
