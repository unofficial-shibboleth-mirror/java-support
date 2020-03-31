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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper class for constructing BiPredicates in a Spring-friendly manner.
 * 
 *  @since 8.1.0
 */
public final class BiPredicateSupport {

    /** Constructor. */
    private BiPredicateSupport() {
    }
    
    /**
     * Creates a {@link BiPredicate} that applies a {@link BiFunction} to inputs and returns its result,
     * or a default value if null.
     * 
     * @param <T> type of function input
     * @param <U> type of function input
     * 
     * @param function function to apply to inputs
     * @param defValue default predicate to apply if function returns null
     * 
     * @return a {@link BiPredicate} adapter
     */
    @Nonnull public static <T,U> BiPredicate<T,U> fromBiFunction(
            @Nonnull final BiFunction<? super T, ? super U,Boolean> function,
            @Nonnull final BiPredicate<T,U> defValue) {
        return new BiPredicate<>() {
            public boolean test(@Nullable final T input1, @Nullable final U input2) {
                final Boolean result = function.apply(input1, input2);
                return result != null ? result : defValue.test(input1, input2);
            }
        };
    }
    
    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if the given {@link BiPredicate} evaluates to {@code
     * false}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param predicate the predicate to negate
     * 
     * @return the negated {@link BiPredicate}
     */
    @Nonnull public static <T,U> BiPredicate<T,U> not(@Nonnull final BiPredicate<? super T, ? super U> predicate) {
        return predicate.negate()::test;
    }

    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this {@link BiPredicate}. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param components the {@link BiPredicate}s to combine
     * 
     * @return the composite {@link BiPredicate}
     */
    @Nonnull public static <T,U> BiPredicate<T,U> and(
            @Nonnull final Iterable<? extends BiPredicate<? super T, ? super U>> components) {
        
        final ArrayList<BiPredicate<? super T, ? super U>> copy = new ArrayList<>();
        for (final BiPredicate<? super T,? super U> p : components) {
            copy.add(p);
        }

        return (t, u) -> {
            for (final BiPredicate<? super T, ? super U> p : copy) {
                if (!p.test(t, u)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param components the {@link BiPredicate}s to combine
     * 
     * @return the composite {@link BiPredicate}
     */
    @SafeVarargs
    @Nonnull public static <T,U> BiPredicate<T,U> and(
            @Nonnull final BiPredicate<? super T,? super U>... components) {
        final ArrayList<BiPredicate<? super T,? super U>> copy = new ArrayList<>();
        for (final BiPredicate<? super T,? super U> p : components) {
            copy.add(p);
        }
        
        return (t, u) -> {
            for (final BiPredicate<? super T, ? super U> p : copy) {
                if (!p.test(t, u)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this {@link BiPredicate}. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code true}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param first the first {@link BiPredicate}
     * @param second the second {@link BiPredicate}
     * 
     * @return the composite {@link BiPredicate}
     */
    @Nonnull public static <T,U> BiPredicate<T,U> and(@Nonnull final BiPredicate<T,U> first,
            @Nonnull final BiPredicate<? super T,? super U> second) {
        
        return first.and(second);
    }
    
    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this {@link BiPredicate}. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param components the {@link BiPredicate}s to combine
     * 
     * @return the composite {@link BiPredicate}
     */
    @Nonnull public static <T,U> BiPredicate<T,U> or(
            @Nonnull final Iterable<? extends BiPredicate<? super T,? super U>> components) {
        
        final ArrayList<BiPredicate<? super T,? super U>> copy = new ArrayList<>();
        for (final BiPredicate<? super T,? super U> p : components) {
            copy.add(p);
        }
        
        return (t, u) -> {
            for (final BiPredicate<? super T, ? super U> p : copy) {
                if (p.test(t, u)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this {@link BiPredicate}. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param components the {@link BiPredicate}s to combine
     * 
     * @return the composite {@link BiPredicate}
     */
    @SafeVarargs
    @Nonnull public static <T,U> BiPredicate<T,U> or(
            @Nonnull final BiPredicate<? super T,? super U>... components) {
        
        final ArrayList<BiPredicate<? super T,? super U>> copy = new ArrayList<>();
        for (final BiPredicate<? super T,? super U> p : components) {
            copy.add(p);
        }
        
        return (t, u) -> {
            for (final BiPredicate<? super T, ? super U> p : copy) {
                if (p.test(t, u)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Returns a {@link BiPredicate} that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true {@link BiPredicate} is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this {@link BiPredicate}. If {@code components} is empty, the
     * returned {@link BiPredicate} will always evaluate to {@code false}.
     * 
     * @param <T> predicate input type
     * @param <U> predicate input type
     * @param first the first {@link BiPredicate}
     * @param second the second {@link BiPredicate}
     * 
     * @return the composite predicate
     */
    @Nonnull public static <T,U> BiPredicate<T,U> or(@Nonnull final BiPredicate<T,U> first,
            @Nonnull final BiPredicate<? super T,? super U> second) {
        
        return first.or(second);
    }
    
}