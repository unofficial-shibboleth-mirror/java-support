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

import java.lang.ref.Cleaner;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with {@link Cleaner}.
 */
public final class CleanerSupport {
    
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CleanerSupport.class);
    
    /** Constructor. */
    private CleanerSupport() {}
    
    /**
     * Obtain a new {@link Cleaner} instance.
     * 
     * @param requester the class which requests the cleaner instance
     * 
     * @return the cleaner instance
     */
    public static Cleaner getInstance(@Nonnull final Class<?> requester) {
        // Current approach here is to create a new Cleaner on each call. A given class requester/owner
        // is assumed to call only once and store in static storage.
        LOG.debug("Creating new java.lang.ref.Cleaner instance requested by class:  {}", requester.getName());
        return Cleaner.create();
    }

}
