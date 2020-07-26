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

package net.shibboleth.utilities.java.support.collection;

import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support functions for Collection and Map Management.
 */
public final class CollectionSupport {
    
    /** logger. */
    private static Logger log = LoggerFactory.getLogger(CollectionSupport.class);
    
    /** Constructor. */
    private CollectionSupport() {
    }

    /** Build something we can plug in into {@link 
     * Collectors#toMap(java.util.function.Function, java.util.function.Function, BinaryOperator)}.
     * @param <T> the type we'll be looking at
     * @param what What we are building (i.e. IdpUI) 
     * @param takeFirst do we want the first of the last to win.
     * @return an appropriate {@link BinaryOperator}
     */
    public static <T> BinaryOperator<T> warningMergeFunction(final String what, final boolean takeFirst) {
        
        return new BinaryOperator<>() {

            public T apply(final T current, final T lookingAt) {
                log.warn("Duplicate detected building {}", what);
                log.debug("Values provided are {} and {} taking {}", current, lookingAt,
                        takeFirst ?"first":"last");
                return takeFirst? current:lookingAt ;
            }
            
        };        
    }
}
