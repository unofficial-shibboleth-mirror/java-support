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

package net.shibboleth.utilities.java.support.component;

import javax.annotation.Nullable;

/** Exception thrown if a component has not been initialized and needs to be in order to perform the operation. */
public class UninitializedComponentException extends RuntimeException {

    /** Serial version UID. */
    private static final long serialVersionUID = -3451363632449131551L;

    /** Constructor. */
    public UninitializedComponentException() {
        super();
    }
    
    /**
     * Constructor. This method constructs the exception message by prepending the output of {@link Object#toString()}
     * to the string <code>has not been initialized</code>.
     * 
     * @param uninitializedComponent the component that was not initialzied
     */
    public UninitializedComponentException(final Object uninitializedComponent) {
        super(uninitializedComponent.toString() + " has not been initialized");
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public UninitializedComponentException(@Nullable final String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param wrappedException exception to be wrapped by this one
     */
    public UninitializedComponentException(@Nullable final Exception wrappedException) {
        super(wrappedException);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param wrappedException exception to be wrapped by this one
     */
    public UninitializedComponentException(@Nullable final String message, @Nullable final Exception wrappedException) {
        super(message, wrappedException);
    }
}