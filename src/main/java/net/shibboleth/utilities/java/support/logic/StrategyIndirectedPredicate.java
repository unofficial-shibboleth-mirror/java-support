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

import java.util.Collection;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicates;

/**
 * Generic predicate that checks a candidate {@link Object} returned by a lookup function
 * against an injected predicate.
 * 
 * @param <T1> type of object used as the source of the data to compare
 * @param <T2> type of object being compared
 */
public class StrategyIndirectedPredicate<T1,T2> implements Predicate<T1> {

    /** Lookup strategy for object. */
    @Nonnull private final Function<T1,T2> objectLookupStrategy;
    
    /** Predicate to apply to indirected object. */
    @Nonnull private final java.util.function.Predicate<T2> predicate;
    
    /**
     * Constructor.
     * 
     * @param objectStrategy  lookup strategy for object
     * @param pred the predicate to apply
     */
    public StrategyIndirectedPredicate(@Nonnull final Function<T1,T2> objectStrategy,
            @Nonnull final java.util.function.Predicate<T2> pred) {
        objectLookupStrategy = Constraint.isNotNull(objectStrategy, "Object lookup strategy cannot be null");
        predicate = Constraint.isNotNull(pred, "Predicate cannot be null");
    }

    /**
     * Constructor that simplifies constructing a test for containment in a collection, which
     * is a common use case.
     * 
     * @param objectStrategy  lookup strategy for object
     * @param collection a collection to test for containment
     */
    public StrategyIndirectedPredicate(@Nonnull final Function<T1,T2> objectStrategy,
            @Nonnull final Collection<T2> collection) {
        objectLookupStrategy = Constraint.isNotNull(objectStrategy, "Object lookup strategy cannot be null");
        predicate = Predicates.in(collection);
    }
    
    /** {@inheritDoc} */
    public boolean test(@Nullable final T1 input) {
        return predicate.test(objectLookupStrategy.apply(input));
    }
    
    /**
     * Factory method for predicate-based constructor.
     * 
     * @param <T1> type of object used as the source of the data to compare
     * @param <T2> type of object being compared
     * 
     * @param objectStrategy the lookup strategy for object
     * @param pred the predicate to apply
     * 
     * @return a suitably constructed predicate
     * 
     * @since 7.3.0
     */
    @Nonnull public static <T1,T2> StrategyIndirectedPredicate<T1,T2> forPredicate(
            @Nonnull final Function<T1,T2> objectStrategy, @Nonnull final java.util.function.Predicate<T2> pred) {
        return new StrategyIndirectedPredicate<>(objectStrategy, pred);
    }

    /**
     * Factory method for collection-based constructor.
     * 
     * @param <T1> type of object used as the source of the data to compare
     * @param <T2> type of object being compared
     * 
     * @param objectStrategy the lookup strategy for object
     * @param collection a collection to test for containment
     * 
     * @return a suitably constructed predicate
     * 
     * @since 7.3.0
     */
    @Nonnull public static <T1,T2> StrategyIndirectedPredicate<T1,T2> forCollection(
            @Nonnull final Function<T1,T2> objectStrategy, @Nonnull final Collection<T2> collection) {
        return new StrategyIndirectedPredicate<>(objectStrategy, collection);
    }

}