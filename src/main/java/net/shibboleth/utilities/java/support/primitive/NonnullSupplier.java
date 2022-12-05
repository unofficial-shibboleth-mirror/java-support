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

package net.shibboleth.utilities.java.support.primitive;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link Supplier} that carries the nonnull annotation
 * on the {@link #get()} method.
 * 
 * @param <T> type of supplier output
 * 
 * @since 8.4.0
 */
public interface NonnullSupplier<T> extends Supplier<T> {

    /** {@inheritDoc} */
    @Nonnull T get();


    /**
     * Return a {@link NonnullSupplier} that returns the input argument.
     * 
     * @param <T> argument type
     * @param input input argument to return
     * 
     * @return the input argument
     */
    @Nonnull static public <T> NonnullSupplier<T> of(@Nonnull final T input) {

        return new NonnullSupplier<T>() {
            @Override
            @Nonnull public T get() {
                return input;
            }
        };
    }

}