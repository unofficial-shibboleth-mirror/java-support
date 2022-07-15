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

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Base class for things that implement {@link DestructableComponent} and {@link InitializableComponent}. */
@ThreadSafe
public abstract class AbstractInitializableComponent implements DestructableComponent,
        InitializableComponent {

    /** Whether this component has been destroyed. */
    @GuardedBy("this") private boolean isDestroyed;

    /** Whether this component has been initialized. */
    @GuardedBy("this") private boolean isInitialized;

    /** {@inheritDoc} */
    @Override
    public final synchronized boolean isDestroyed() {
        return isDestroyed;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isInitialized() {
        return isInitialized;
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized void destroy() {
        if (isDestroyed()) {
            return;
        }

        doDestroy();
        isDestroyed = true;
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized void initialize() throws ComponentInitializationException {
        ifDestroyedThrowDestroyedComponentException();
        if (isInitialized()) {
            return;
        }

        doInitialize();
        isInitialized = true;
    }
    
    /**
     * Checks if the component is destroyed and, if so, throws a {@link DestroyedComponentException}.
     */
    protected void ifDestroyedThrowDestroyedComponentException() {
        if (isDestroyed()) {
            throw new DestroyedComponentException(
                    "Unidentified Component has already been destroyed and can no longer be used.");
        }
    }

    /**
     * Checks if a component has not been initialized and, if so, throws a {@link UninitializedComponentException}.
     */
    protected void ifNotInitializedThrowUninitializedComponentException() {
        if (!isInitialized()) {
            throw new UninitializedComponentException(
                    "Unidentified Component has not yet been initialized and cannot be used.");
        }
    }

    /**
     * Checks if a component has been initialized and, if so, throws a {@link UnmodifiableComponentException}.
     */
    protected void ifInitializedThrowUnmodifiabledComponentException() {
        if (isInitialized()) {
            throw new UnmodifiableComponentException(
                    "Unidentified Component has already been initialized and can no longer be modified");
        }
    }

    /**
     * Helper for a setter method to check the standard preconditions.
     */
    protected final void checkSetterPreconditions() {
        ifDestroyedThrowDestroyedComponentException();
        ifInitializedThrowUnmodifiabledComponentException();
    }

    /**
     * Helper for any method to throw appropriate exceptions if we are either
     * not initialized, or have been destroyed.
     */
    protected final void checkComponentActive() {
        ifDestroyedThrowDestroyedComponentException();
        ifNotInitializedThrowUninitializedComponentException();
    }

    /**
     * Performs component specific destruction logic. This method is executed within the lock on the object being
     * destroyed. The default implementation of this method is a no-op.
     */
    protected void doDestroy() {

    }

    /**
     * Performs the initialization of the component. This method is executed within the lock on the object being
     * initialized.
     * 
     * The default implementation of this method is a no-op.
     * 
     * @throws ComponentInitializationException thrown if there is a problem initializing the component
     */
    protected void doInitialize() throws ComponentInitializationException {

    }
}
