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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

/**
 * A {@link Predicate} that always throws a {@link ComponentInitializationException} when {@link #initialize()} is
 * called.
 */
public class ComponentInitializationExceptionPredicate<T> implements InitializableComponent, Predicate<T> {

    /** {@inheritDoc} */
    public boolean test(T arg0) {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isInitialized() {
        return false;
    }

    /** {@inheritDoc} */
    public void initialize() throws ComponentInitializationException {
        throw new ComponentInitializationException();
    }
}