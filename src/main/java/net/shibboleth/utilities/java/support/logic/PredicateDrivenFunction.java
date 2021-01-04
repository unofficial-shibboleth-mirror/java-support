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

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.ParameterName;

/**
 * A {@link Function} that runs a {@link Predicate} and then runs one of two other possible
 * functions based on the result.
 * 
 * <p>If either function is null, the result will be null if that function were needed.</p>
 * 
 * @param <T> type of input accepted by this function
 * @param <U> type of output produced by this function
 * 
 * @since 8.2.0
 */
@ThreadSafe
public class PredicateDrivenFunction<T,U> implements Function<T,U> {

    /** A predicate to apply. */
    @Nonnull private final Predicate<? super T> predicate;

    /** Function to apply if predicate is true. */
    @Nullable private final Function<? super T,U> trueFunction;

    /** Function to apply if predicate is false. */
    @Nullable private final Function<? super T,U> falseFunction;

    /**
     * Constructor.
     * 
     * @param condition     predicate to apply
     * @param whenTrue      function to apply if predicate evaluates to true
     * @param whenFalse     function to apply if predicate evaluates to false
     */
    public PredicateDrivenFunction(@Nonnull @ParameterName(name="condition") final Predicate<? super T> condition,
            @Nullable @ParameterName(name="whenTrue") final Function<? super T,U> whenTrue,
            @Nullable @ParameterName(name="whenFalse") final Function<? super T,U> whenFalse) {
        
        predicate = Constraint.isNotNull(condition, "Input predicate cannot be null");
        trueFunction = whenTrue != null ? whenTrue : FunctionSupport.constant(null);
        falseFunction = whenFalse != null ? whenFalse : FunctionSupport.constant(null);
    }

    /** {@inheritDoc} */
    public U apply(@Nullable final T input) {
        return predicate.test(input) ? trueFunction.apply(input) : falseFunction.apply(input);
    }

}