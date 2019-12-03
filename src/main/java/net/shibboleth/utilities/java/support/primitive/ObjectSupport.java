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

import javax.annotation.Nullable;


/** Helper methods for working with Objects. */
public final class ObjectSupport {

    /** Constructor. */
    private ObjectSupport() {
    }

    /**
     * Performs a safe (null-aware) {@link Object#hashCode()}.
     * 
     * @param o object for which to get the hash code, may be null
     * 
     * @return the hash code for the object of 0 if the given object is null
     */
    @Deprecated(forRemoval=true)
    public static int hashCode(@Nullable final Object o) {
        if (o == null) {
            return 0;
        }

        return o.hashCode();
    }
    
    /**
     * Return the first from a list of arguments that is non-null, or null if all arguments 
     * are null.
     * 
     * <p>
     * This is similar to Guava's {@link com.google.common.base.MoreObjects#firstNonNull(Object, Object)},
     * except it takes more than 2 arguments, and also doesn't throw a null pointer exception if 
     * all arguments are null.
     * </p>
     * 
     * @param <T> the type of arguments being evaluated
     * 
     * @param objects the list of object references to evaluate
     * 
     * @return the first non-null argument, or null if all arguments are null
     * 
     */
    @SafeVarargs
    @Nullable public static <T> T firstNonNull(@Nullable final T ... objects) {
        if (objects == null) {
            return null;
        }
        
        for (final T obj : objects) {
            if (obj != null) {
                return obj;
            }
        }
        
        return null;
    }
}