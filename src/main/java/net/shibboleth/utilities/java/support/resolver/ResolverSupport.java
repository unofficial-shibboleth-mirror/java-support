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

package net.shibboleth.utilities.java.support.resolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.shibboleth.utilities.java.support.logic.PredicateSupport;

/**
 * Support class for resolver implementations.
 */
public final class ResolverSupport {
    
    /** Constructor. */
    private ResolverSupport() {}
    
    /**
     * Obtain a set of {@link Predicate} based on a {@link CriteriaSet}.
     * 
     * @param criteriaSet the criteria set to evaluate
     * @param predicateCriterionType the optional type to evaluate and extract directly from the criteria set
     * @param registry the optional registry of mappings from {@link Criterion} to {@link Predicate}
     * 
     * @return a set of predicates, possibly empty
     * 
     * @throws ResolverException if there is a fatal error evaluating the criteria set
     * 
     * @param <T> the type of target which the returned predicates can evaluate
     * @param <E> the type of criterion predicates to extract directly from the criteria set
     */
    @Nonnull
    public static <T, E extends Predicate<T>> Set<Predicate<T>> getPredicates(@Nullable final CriteriaSet criteriaSet, 
            @Nullable final Class<E> predicateCriterionType, @Nullable final CriterionPredicateRegistry<T> registry) 
                    throws ResolverException {
        
        if (criteriaSet == null) {
            return Collections.emptySet();
        }
        
        final Set<Predicate<T>> predicates = new HashSet<>(criteriaSet.size());
        
        for (final Criterion criterion : criteriaSet) {
            if (predicateCriterionType != null && predicateCriterionType.isInstance(criterion)) {
                predicates.add(predicateCriterionType.cast(criterion));
            } else if (registry != null) {
                final Predicate<T> predicate = registry.getPredicate(criterion);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
        }
        
        return predicates;
    }
    
    /**
     * Return a filtered {@link Iterable} of the specified candidates based on the supplied set of {@link Predicate}
     * and the satisfyAny flag.
     * 
     * @param candidates the candidates to filter
     * @param predicates the predicates with which to filter
     * @param satisfyAny if true the predicates will be logically OR-ed, otherwise they are logically AND-ed
     * @param onEmptyPredicatesReturnEmpty if true and no predicates are supplied, then return an empty iterable;
     *          otherwise return the original input candidates
     * 
     * @return the filtered iteration of the candidates
     * 
     * @param <T> the type of target candidates
     */
    @Nonnull
    public static <T> Iterable<T> getFilteredIterable(@Nullable final Iterable<T> candidates, 
            @Nullable final Set<Predicate<T>> predicates, final boolean satisfyAny, 
            final boolean onEmptyPredicatesReturnEmpty) {
        
        if (candidates == null || !candidates.iterator().hasNext()) {
            return Collections.emptySet();
        }
        
        if (predicates == null || predicates.isEmpty()) {
            if (onEmptyPredicatesReturnEmpty) {
                return Collections.emptySet();
            } else {
                return candidates;
            }
        }
        
        final Predicate<T> predicate;
        if (satisfyAny) {
            predicate = PredicateSupport.or(predicates);
        } else {
            predicate = PredicateSupport.and(predicates);
        }
        
        return Iterables.filter(candidates, predicate::test);
    }
    
}