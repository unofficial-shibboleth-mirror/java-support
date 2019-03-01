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

import java.time.Instant;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.component.InitializableComponent;

/**
 * A service that supports reloading its configuration.
 *  
 * @param <T> The sort of service that this implements
 */
public interface ReloadableService<T> extends InitializableComponent {
    
    /**
     * Gets the time when the service was last successfully reloaded.
     * Returns null if the service has never reloaded. 
     * 
     * @return time when the service was last successfully reloaded
     */
    @Nullable Instant getLastSuccessfulReloadInstant();

    /**
     * Gets the time when the service last attempted to reload. If the reload was successful this time should match the
     * time given by {@link #getLastSuccessfulReloadInstant()}.
     * 
     * @return time when the service last attempted to reload
     */
    @Nullable Instant getLastReloadAttemptInstant();

    /**
     * Gets the reason the last reload failed.
     * 
     * @return reason the last reload failed or null if the last reload was successful
     */
    @Nullable Throwable getReloadFailureCause();

    /**
     * Reloads the configuration of the service. Whether internal state is maintained between reloads is implementation
     * dependent.
     * 
     * @throws ServiceException thrown if there is a problem reloading the service
     */
    void reload() ;
    
    /**
     * Get the serviceable component that this service supports. If the component hasn't been successfully
     * loaded yet or if this service does not support a ServiceableComponent, null is returned. On a non-null
     * value, the returned component will be pinned and <em>MUST</em> be unpinned by a call to
     * {@link ServiceableComponent#unpinComponent()}.
     * 
     * @return the component, if appropriate.
     */
    @Nullable ServiceableComponent<T> getServiceableComponent();

}