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

package net.shibboleth.utilities.java.support.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.servlet.ServletRequest;

/**
 * A policy that evaluates a {@link ServletRequest} and determines whether access to a named resource
 * should be granted.
 */
public interface AccessControl {

    /**
     * Determine whether the request to the resource should be granted.
     * 
     * @param request   request to check
     * @param operation operation being performed
     * @param resource  target resource
     * 
     * @return true iff access should be granted
     */
    boolean checkAccess(@Nonnull final ServletRequest request, @Nullable final String operation,
            @Nullable final String resource);
    
}
