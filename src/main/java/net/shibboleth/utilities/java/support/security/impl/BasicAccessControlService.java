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

package net.shibboleth.utilities.java.support.security.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.security.AccessControl;
import net.shibboleth.utilities.java.support.security.AccessControlService;

/** Simple implementation that uses an in-memory map of policies. */
public class BasicAccessControlService extends AbstractIdentifiableInitializableComponent
        implements AccessControlService {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BasicAccessControlService.class);
    
    /** Map of named policies. */
    @Nonnull @NonnullElements private Map<String,AccessControl> policyMap;
    
    /** Constructor. */
    public BasicAccessControlService() {
        policyMap = Collections.emptyMap();
    }
    
    /**
     * Set the policies to store.
     * 
     * @param map map of named policies
     */
    public void setPolicyMap(@Nonnull @NonnullElements final Map<String,AccessControl> map) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(map, "Policy map cannot be null");
        
        policyMap = new HashMap<>(map.size());
        
        for (final Map.Entry<String,AccessControl> entry : map.entrySet()) {
            final String trimmed = StringSupport.trimOrNull(entry.getKey());
            if (trimmed != null && entry.getValue() != null) {
                policyMap.put(trimmed, entry.getValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public AccessControl getInstance(@Nonnull final String name) {

        final AccessControl ac = policyMap.get(name);
        if (ac != null) {
            return ac;
        }
        
        log.warn("Access Control Service {}: No policy named '{}' found, returning default denial policy",
                getId(), name);
        
        return new AccessControl() {
            public boolean checkAccess(@Nonnull final ServletRequest request, @Nullable final String operation,
                    @Nullable final String resource) {
                return false;
            }
        };
    }

}