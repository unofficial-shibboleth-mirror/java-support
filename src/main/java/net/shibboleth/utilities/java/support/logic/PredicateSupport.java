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

import java.util.ArrayList;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicates;

/**
 * Helper class for constructing predicates. Especially useful for creating internal DSLs via Java's static method
 * import mechanism.
 */
public final class PredicateSupport {

    /** Constructor. */
    private PredicateSupport() {
    }

    /**
     * Creates a predicate that checks that all elements of an {@link Iterable} matches a given target predicate.
     * 
     * @param <T> type of objects in the iterable and that the target operates upon
     * @param target predicate used to check each element in the iterable
     * 
     * @return the constructed predicate
     */
    @Nonnull public static <T> Predicate<Iterable<T>> allMatch(@Nonnull final java.util.function.Predicate<T> target) {
        return new AllMatchPredicate<>(target);
    }

    /**
     * Creates a predicate that checks that any element in an {@link Iterable} matches a given target predicate.
     * 
     * @param <T> type of objects in the iterable and that the target operates upon
     * @param target predicate used to check each element in the iterable
     * 
     * @return the constructed predicate
     */
    @Nonnull public static <T> Predicate<Iterable<T>> anyMatch(@Nonnull final java.util.function.Predicate<T> target) {
        return new AnyMatchPredicate<>(target);
    }

    /**
     * Creates a predicate that checks if a given {@link CharSequence} matches a target string while ignoring case.
     * 
     * @param target the target string to match against
     * 
     * @return the constructed predicate
     */
    @Nonnull public static Predicate<CharSequence> caseInsensitiveMatch(@Nonnull final String target) {
        return new CaseInsensitiveStringMatchPredicate(target);
    }
    
    /**
     * Creates a predicate that applies a function to an input and returns its result, or a default value
     * if null.
     * 
     * @param <T> type of function input
     * 
     * @param function function to apply to input
     * @param defValue default predicate to apply if function returns null
     * 
     * @return a predicate adapter
     * 
     *  @since 7.4.0
     */
    @Nonnull public static <T> Predicate<T> fromFunction(@Nonnull final Function<T,Boolean> function,
            @Nonnull final java.util.function.Predicate<? super T> defValue) {
        return new Predicate<>() {
            public boolean test(@Nullable final T input) {
                final Boolean result = function.apply(input);
                return result != null ? result : defValue.test(input);
            }
        };
    }
    
    /**
     * Returns a predicate that evaluates to {@code true} if the given predicate evaluates to {@code
     * false}.
     * 
     * @param <T> predicate input type
     * @param predicate the predicate to negate
     * 
     * @return the negated predicate
     */
    @Nonnull public static <T> Predicate<T> not(@Nonnull final java.util.function.Predicate<? super T> predicate) {
        return predicate.negate()::test;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param components the predicates to combine
     * 
     * @return the composite predicate
     */
    @Nonnull public static <T> Predicate<T> and(
            @Nonnull final Iterable<? extends java.util.function.Predicate<? super T>> components) {
        
        final ArrayList<com.google.common.base.Predicate<T>> copy = new ArrayList<>();
        for (final java.util.function.Predicate<? super T> p : components) {
            copy.add(p::test);
        }
        
        return Predicates.and(copy)::test;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param components the predicates to combine
     * 
     * @return the composite predicate
     */
    @SafeVarargs
    @Nonnull public static <T> Predicate<T> and(
            @Nonnull final java.util.function.Predicate<? super T>... components) {
        final ArrayList<com.google.common.base.Predicate<T>> copy = new ArrayList<>();
        for (final java.util.function.Predicate<? super T> p : components) {
            copy.add(p::test);
        }
        
        return Predicates.and(copy)::test;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param first the first predicate
     * @param second the second predicate
     * 
     * @return the composite predicate
     */
    @Nonnull public static <T> Predicate<T> and(@Nonnull final java.util.function.Predicate<? super T> first,
            @Nonnull final java.util.function.Predicate<? super T> second) {
        
        return t -> first.test(t) && second.test(t);
    }
    
    /**
     * Returns a predicate that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param components the predicates to combine
     * 
     * @return the composite predicate
     */
    @Nonnull public static <T> Predicate<T> or(
            @Nonnull final Iterable<? extends java.util.function.Predicate<? super T>> components) {
        
        final ArrayList<com.google.common.base.Predicate<T>> copy = new ArrayList<>();
        for (final java.util.function.Predicate<? super T> p : components) {
            copy.add(p::test);
        }
        
        return Predicates.or(copy)::test;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param components the predicates to combine
     * 
     * @return the composite predicate
     */
    @SafeVarargs
    @Nonnull public static <T> Predicate<T> or(
            @Nonnull final java.util.function.Predicate<? super T>... components) {
        
        final ArrayList<com.google.common.base.Predicate<T>> copy = new ArrayList<>();
        for (final java.util.function.Predicate<? super T> p : components) {
            copy.add(p::test);
        }
        
        return Predicates.or(copy)::test;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned predicate will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param first the first predicate
     * @param second the second predicate
     * 
     * @return the composite predicate
     */
    @Nonnull public static <T> Predicate<T> or(@Nonnull final java.util.function.Predicate<? super T> first,
            @Nonnull final java.util.function.Predicate<? super T> second) {
        
        return t -> first.test(t) || second.test(t);
    }
    
}