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

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.ParameterName;

/**
 * Helper class for constructing {@link BiFunction}s in a Spring-friendly manner.
 * 
 * @since 8.1.0
 */
public final class BiFunctionSupport {

    /** Constructor. */
    private BiFunctionSupport() {
        
    }

    /**
     * Creates a {@link BiFunction} that returns a constant value.
     * 
     * @param <T> type of object the function needs to act on
     * @param <U> type of object the function needs to act on
     * @param <V> type of object being returned
     * @param target the value to return from the function
     * 
     * @return the constructed function
     */
    @Nonnull public static <T,U,V> BiFunction<T,U,V> constant(@Nonnull @ParameterName(name="target") final V target) {
        return (t,u) -> {
            return target;
            };
    }

    /**
     * A static version of {@link BiFunction#andThen(Function)}.
     *
     * @param <A> input to composed {@link BiFunction}
     * @param <B> input to composed {@link BiFunction}
     * @param <C> output of input {@link BiFunction}
     * @param <D> output of composed {@link BiFunction}
     *
     * @param g the second function to apply
     * @param f the first {@link BiFunction} to apply
     * 
     * @return the composition of {@code f} and {@code g}
     * @see <a href="//en.wikipedia.org/wiki/Function_composition">function composition</a>
     */
    @Nonnull public static <A,B,C,D> BiFunction<A,B,D> compose(
            @Nonnull @ParameterName(name="g") final Function<? super C,? extends D> g,
            @Nonnull @ParameterName(name="f") final BiFunction<A,B,? extends C> f) {
        return f.andThen(g);
    }

    /**
     * Creates a {@link BiFunction} that returns the same boolean output as the given {@link BiPredicate} for all
     * inputs.
     * 
     * @param <T> input type
     * @param <U> input type
     * @param predicate input {@link BiPredicate}
     * 
     * @return a corresponding {@link BiFunction} 
     */
    @Nonnull public static <T,U> BiFunction<T,U,Boolean> forBiPredicate(
            @Nonnull @ParameterName(name="predicate") final BiPredicate<? super T,? super U> predicate) {
        return predicate::test;
    }
    
    /**
     * Adapts a {@link Function} into a {BiFunction} that ignores the second argument.
     * 
     * @param <A> input type of function
     * @param <B> ignored argument type
     * @param <C> return type
     * @param function the function to apply
     * 
     * @return the adapted object
     */
    @Nonnull public static <A,B,C> BiFunction<A,B,C> forFunctionOfFirstArg(
            @Nonnull @ParameterName(name="function") final Function<? super A,? extends C> function) {
        return (a,b) -> function.apply(a);
    }

    /**
     * Adapts a {@link Function} into a {BiFunction} that ignores the first argument.
     * 
     * @param <A> ignored argument type
     * @param <B> input type of function
     * @param <C> return type
     * @param function the function to apply
     * 
     * @return the adapted object
     */
    @Nonnull public static <A,B,C> BiFunction<A,B,C> forFunctionOfSecondArg(
            @Nonnull @ParameterName(name="function") final Function<? super B,? extends C> function) {
        return (a,b) -> function.apply(b);
    }

}