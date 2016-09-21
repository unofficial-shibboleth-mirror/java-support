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

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Helper class for constructing functions that are fully generic, in contrast to the broken,
 * Object-bound types Guava can build.
 */
public final class FunctionSupport {

    /** Constructor. */
    private FunctionSupport() {
        
    }

    /**
     * Creates a function that returns a constant value, like {@link Functions#constant(Object)}, but
     * with the type of input parameterized as well as the output.
     * 
     * @param <T1> type of object the function needs to act on
     * @param <T2> type of object being returned
     * @param target the value to return from the function
     * 
     * @return the constructed function
     */
    @Nonnull public static <T1,T2> Function<T1,T2> constant(@Nonnull final T2 target) {
        return (Function<T1, T2>) Functions.constant(target);
    }

}