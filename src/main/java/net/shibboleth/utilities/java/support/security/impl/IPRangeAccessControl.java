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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.net.IPRange;
import net.shibboleth.utilities.java.support.security.AccessControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.net.InetAddresses;

/**
 * Simple access control implementation based on IP address checking.
 */
public class IPRangeAccessControl extends AbstractIdentifiableInitializableComponent implements AccessControl {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(IPRangeAccessControl.class);

    /** List of CIDR blocks allowed to access this servlet. */
    @Nonnull @NonnullElements private Collection<IPRange> allowedRanges;
    
    /**
     * Constructor.
     *
     */
    public IPRangeAccessControl() {
        allowedRanges = Collections.emptyList();
    }
    
    /**
     * Set the CIDR address ranges to allow.
     * 
     * @param ranges ranges to allow
     */
    public void setAllowedRanges(@Nonnull @NonnullElements final Collection<IPRange> ranges) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(ranges, "IPRange collection cannot be null");
        
        allowedRanges = new ArrayList<>(Collections2.filter(ranges, Predicates.notNull()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean checkAccess(@Nonnull final ServletRequest request, @Nullable final String operation,
            @Nullable final String resource) {
        
        Constraint.isNotNull(request, "ServletRequest cannot be null");

        final String addr = request.getRemoteAddr();
        if (addr != null) {
            log.debug("{} Evaluating client address '{}'", getLogPrefix(), addr);
            
            try {
                final byte[] resolvedAddress = InetAddresses.forString(addr).getAddress();
                for (final IPRange range : allowedRanges) {
                    if (range.contains(resolvedAddress)) {
                        log.debug("{} Granted access to client address '{}' (Operation: {}, Resource: {})",
                                new Object[] {getLogPrefix(), addr, operation, resource});
                        return true;
                    }
                }
            } catch (final IllegalArgumentException e) {
                log.warn("{} Error translating client address", getLogPrefix(), e);
            }
            
            log.warn("{} Denied request from client address '{}' (Operation: {}, Resource: {})",
                    new Object[] {getLogPrefix(), addr, operation, resource});
        } else {
            log.warn("{} Denied request from client address 'unknown' (Operation: {}, Resource: {})",
                    new Object[] {getLogPrefix(), operation, resource});
        }
        
        return false;
    }

    
    /**
     * Get logging prefix.
     * 
     * @return  prefix
     */
    @Nonnull private String getLogPrefix() {
        return "Policy " + getId() + ":";
    }

}