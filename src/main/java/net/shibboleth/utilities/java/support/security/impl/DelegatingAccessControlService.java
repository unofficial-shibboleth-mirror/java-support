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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletRequest;
import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.AccessControl;
import net.shibboleth.utilities.java.support.security.AccessControlService;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

/**
 * This class uses the {@link ReloadableService} concept to implement {@link AccessControlService}
 * to hide the details of pinning and unpinning the underlying service.
 */
public class DelegatingAccessControlService extends AbstractIdentifiableInitializableComponent
    implements AccessControlService {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DelegatingAccessControlService.class);

    /** The service which manages the reloading. */
    private final ReloadableService<AccessControlService> service;

    /**
     * Constructor.
     * 
     * @param acService the service which will manage the loading.
     */
    public DelegatingAccessControlService(
            @Nonnull @ParameterName(name="acService") final ReloadableService<AccessControlService> acService) {
        service = Constraint.isNotNull(acService, "AccessControlService cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public AccessControl getInstance(@Nonnull final String name) {
        checkComponentActive();
        ServiceableComponent<AccessControlService> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("AccessControlService '{}': Error accessing underlying component: Invalid configuration.",
                        getId());
            } else {
                final AccessControlService svc = component.getComponent();
                return svc.getInstance(name);
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }

        return new AccessControl() {
            public boolean checkAccess(@Nonnull final ServletRequest request, @Nullable final String operation,
                    @Nullable final String resource) {
                return false;
            }
        };
    }
    
}
