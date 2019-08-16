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

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** Support class for working with {@link Component} objects. */
public final class ComponentSupport {

    /** Constructor. */
    private ComponentSupport() {
    }

    /**
     * If the given object is not null and an instance of {@link DestructableComponent}, then this method calls the
     * given object's {@link DestructableComponent#destroy()} method.
     * 
     * @param obj object to destroy, may be null
     */
    public static void destroy(@Nullable final Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof DestructableComponent) {
            final DestructableComponent destructable = (DestructableComponent) obj;
            if (!destructable.isDestroyed()) {
                destructable.destroy();
            }
        }
    }

    /**
     * If the given object is not null and an instance of {@link InitializableComponent}, then this method calls the
     * given object's {@link InitializableComponent#initialize()} method.
     * 
     * @param obj object to initialize, may be null
     * 
     * @throws ComponentInitializationException thrown if there is a problem initializing the object
     */
    public static void initialize(@Nullable final Object obj) throws ComponentInitializationException {
        if (obj == null) {
            return;
        }

        if (obj instanceof InitializableComponent) {
            final InitializableComponent initializable = (InitializableComponent) obj;
            if (!initializable.isInitialized()) {
                initializable.initialize();
            }
        }
    }

    /**
     * Checks if a component is destroyed and, if so, throws a {@link DestroyedComponentException}. If the component is
     * also an instance of {@link IdentifiedComponent}, the component's ID is included in the error message.
     * 
     * @param component component to check
     */
    public static void ifDestroyedThrowDestroyedComponentException(@Nonnull final DestructableComponent component) {
        Constraint.isNotNull(component, "Component cannot be null");

        if (component.isDestroyed()) {
            if (component instanceof IdentifiedComponent) {
                throw new DestroyedComponentException("Component '"
                        + StringSupport.trimOrNull(((IdentifiedComponent) component).getId())
                        + "' has already been destroyed and can no longer be used.");
            }
            throw new DestroyedComponentException("Component has already been destroyed and can no longer be used");
        }

    }

    /**
     * Checks if a component has not been initialized and, if so, throw)s a {@link UninitializedComponentException}. If
     * the component is also an instance of {@link IdentifiedComponent}, the component's ID is included in the error
     * message.
     * 
     * @param component component to check
     */
    public static void
            ifNotInitializedThrowUninitializedComponentException(@Nonnull final InitializableComponent component) {
        Constraint.isNotNull(component, "Component cannot be null");

        if (!component.isInitialized()) {
            if (component instanceof IdentifiedComponent) {
                throw new UninitializedComponentException("Component '"
                        + StringSupport.trimOrNull(((IdentifiedComponent) component).getId())
                        + "' has not yet been initialized and cannot be used.");
            }
            throw new UninitializedComponentException("Component has not yet been initialized and cannot be used.");
        }
    }

    /**
     * Checks if a component has been initialized and, if so, throws a {@link UnmodifiableComponentException}. If the
     * component is also an instance of {@link IdentifiedComponent}, the component's ID is included in the error
     * message.
     * 
     * @param component component to check
     */
    public static void
            ifInitializedThrowUnmodifiabledComponentException(@Nonnull final InitializableComponent component) {
        Constraint.isNotNull(component, "Component cannot be null");

        if (component.isInitialized()) {
            if (component instanceof IdentifiedComponent) {
                throw new UnmodifiableComponentException("Component '"
                        + StringSupport.trimOrNull(((IdentifiedComponent) component).getId())
                        + "' has already been initialized and can no longer be modified");
            }
            throw new UnmodifiableComponentException(
                    "Component has already been initialized and can no longer be modified");
        }
    }
}