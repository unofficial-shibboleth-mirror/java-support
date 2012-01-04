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

import net.shibboleth.utilities.java.support.logic.Assert;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Simple implementation of {@link InitializableComponent} and {@link IdentifiableComponent}.
 * 
 * Note, this class synchronizes the {@link #setId(String)} method and, if the component is already initialized this
 * method is treated as no-op.
 */
public abstract class AbstractIdentifiableInitializableComponent extends AbstractInitializableComponent implements
        IdentifiableComponent {

    /** The unique identifier for this component. */
    private String id;

    /** {@inheritDoc} */
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    protected synchronized void setId(final String componentId) {
        if (isInitialized()) {
            return;
        }

        id = Assert.isNotNull(StringSupport.trimOrNull(componentId), "Component ID can not be null or empty");
    }

    /**
     * This method checks to ensure that the component ID is not null.
     * 
     * {@inheritDoc}
     */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (getId() == null) {
            throw new ComponentInitializationException("Component identifier can not be null");
        }
    }
}