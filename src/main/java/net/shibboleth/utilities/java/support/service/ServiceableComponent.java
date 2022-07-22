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

/**
 * Any component that wants to be reloaded via the Service interface and Spring implements this interface.
 * 
 * The idea is that the attribute resolver will be
 * <code>
 * public class AttributeResolver extends AbstractServiceableComponent&lt;AttributeResolver&gt; implements
 *  AttributeResolver, ServiceableComponent&lt;ServiceableComponent&gt;.
 *  </code>
 *  AbstractServiceableComponent will do all the work around reload and synchronization.
 *  
 * @param <T> The underlying type of the component.
 */
public interface ServiceableComponent<T> extends AutoCloseable {

    /**
     * Extract the component that does the actual work.  Callers <em>MUST</em> have the ServiceableComponent
     * pinned at this stage.
     *
     * @return the component.
     */
    @Nonnull T getComponent();
    
    /**
     * This function takes a lock on the component which guarantees that it will not be disposed until the unpin call
     * is made.
     * 
     * <p>This method is typically <em>only</em> used during initialization of the component.</p>
     * 
     * <p><em>Every call to {@link #pinComponent()} must be matched by a call to {@link #unpinComponent()}</em>.</p>
     */
    void pinComponent();

    /**
     * This undoes the work that is done by {@link #pinComponent()}.
     */
    void unpinComponent();

    /**
     * This call will wait for all transient operations to complete and then
     * calls dispose/destroy on the component.
     *
     * <p>Implementations should avoid calling this with locks held.</p>
     */
    void unloadComponent();

    /** {@inheritDoc}
     * Although this method is the same as {@link #unpinComponent()} this is targeted at places where
     * {@link ReloadableService#getServiceableComponent()} was called.
     */
     default void close() {
        unpinComponent();
     }
}
