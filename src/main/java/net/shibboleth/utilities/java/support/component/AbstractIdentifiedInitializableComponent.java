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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Simple implementation of {@link InitializableComponent} and {@link IdentifiedComponent}.
 */
@ThreadSafe
public abstract class AbstractIdentifiedInitializableComponent extends AbstractInitializableComponent implements
        IdentifiedComponent {

    /** The unique identifier for this component. */
    @Nullable @NonnullAfterInit @GuardedBy("this") private String id;

    /** {@inheritDoc} */
    @Nullable @NonnullAfterInit public synchronized String getId() {
        return id;
    }
    
    /**
     * Checks if the component is destroyed and, if so, throws a {@link DestroyedComponentException}.
     */
    protected final void ifDestroyedThrowDestroyedComponentException() {
        if (isDestroyed()) {
            throw new DestroyedComponentException("Component '"
                    + StringSupport.trimOrNull(getId())
                    + "' has already been destroyed and can no longer be used.");
        }
    }

    /**
     * Checks if a component has not been initialized and, if so, throws a {@link UninitializedComponentException}.
     */
    protected final void ifNotInitializedThrowUninitializedComponentException() {
        if (!isInitialized()) {
            throw new UninitializedComponentException("Component '"
                    + StringSupport.trimOrNull(getId())
                    + "' has not yet been initialized and cannot be used.");
        }
    }

    /**
     * Checks if a component has been initialized and, if so, throws a {@link UnmodifiableComponentException}.
     */
    protected final void ifInitializedThrowUnmodifiabledComponentException() {
        if (isInitialized()) {
            throw new UnmodifiableComponentException("Component '"
                    + StringSupport.trimOrNull(getId())
                    + "' has already been initialized and can no longer be modified");
        }
    }


    /**
     * Sets the ID of this component. The component must not yet be initialized.
     * 
     * @param componentId ID of the component
     */
    protected synchronized void setId(@Nonnull @NotEmpty final String componentId) {
        checkSetterPreconditions();

        id = Constraint.isNotNull(StringSupport.trimOrNull(componentId), "Component ID can not be null or empty");
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