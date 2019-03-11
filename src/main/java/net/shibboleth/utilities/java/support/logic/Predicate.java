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

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;

/**
 * A convenience interface to allow our own classes to implement the Java
 * {@link java.util.function.Predicate} and log any calls to the
 * {@link #apply(Object)} method as deprecated.
 * 
 * @param <T> type of object upon which this predicate operates
 */
public interface Predicate<T> extends java.util.function.Predicate<T> {

    /**
     * Default method to log deprecated use of Guava's apply() signature.
     * 
     * @param input input to predicate
     * 
     * @return the result of the {@link #test(Object)} method
     * @deprecated
     */
    @Deprecated
    default boolean apply(@Nullable final T input) {
        DeprecationSupport.warn(ObjectType.METHOD, "apply", "on Predicate objects", "test");
        
        return test(input);
    }
    
}