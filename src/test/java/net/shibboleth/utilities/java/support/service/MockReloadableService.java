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

package net.shibboleth.utilities.java.support.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Utility class for wrapping a serviceable component in a dummy reloadable service.
 * 
 * @param <T> type of component
 */
public class MockReloadableService<T> extends AbstractReloadableService<T> {

    @Nonnull private final ServiceableComponent<T> component;

    public MockReloadableService(@Nonnull final ServiceableComponent<T> what) {
        component = Constraint.isNotNull(what, "Component cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public ServiceableComponent<T> getServiceableComponent() {
        if (null == component) {
            return null;
        }
        component.pinComponent();
        return component;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldReload() {
        return false;
    }

}