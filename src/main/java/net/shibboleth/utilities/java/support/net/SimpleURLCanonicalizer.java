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

package net.shibboleth.utilities.java.support.net;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * This class performs simple canonicalization of a URL as follows:
 * 
 * <ul>
 *   <li>The scheme is lower-cased.</li>
 *   <li>The hostname is lower-cased</li>
 *   <li>The port is removed if it is the default port registered for the scheme</li>
 * </ul>
 */
public final class SimpleURLCanonicalizer {
    
    /** The scheme-to-port mapping data. */
    private static Map<String, Integer> schemePortMap = new HashMap<>();
    
    /** Constructor to prevent instantiation.  */
    private SimpleURLCanonicalizer() {}

    /**
     * Register a new scheme-to-port mapping.
     * 
     * @param scheme the scheme to register
     * @param port the default port for that scheme
     */
    public static void registerSchemePortMapping(@Nonnull @NotEmpty final String scheme, @Nonnull final Integer port) {
        final String trimmedScheme = Constraint.isNotNull(StringSupport.trimOrNull(scheme), "Scheme cannot be null");
        Constraint.isNotNull(port, "Port cannot be null");
        
        schemePortMap.put(trimmedScheme.toLowerCase(), port);
    }
    
    /**
     * Deregister a scheme-to-port mapping.
     * 
     * @param scheme the scheme to deregister
     */
    public static void deregisterSchemePortMapping(@Nonnull final String scheme) {
        final String trimmedScheme = Constraint.isNotNull(StringSupport.trimOrNull(scheme), "Scheme cannot be null");
        
        schemePortMap.remove(trimmedScheme.toLowerCase());
    }
    
    /**
     * Obtain the default port registered for a scheme.
     * 
     * @param scheme the scheme to look up
     * @return the default port registered for the scheme, or null if none registered
     */
    @Nullable public static Integer getRegisteredPort(@Nonnull @NotEmpty final String scheme) {
        final String trimmedScheme = Constraint.isNotNull(StringSupport.trimOrNull(scheme), "Scheme cannot be null");
        
        return schemePortMap.get(trimmedScheme.toLowerCase());
    }
    
    /**
     * Canonicalize the supplied URL.
     * 
     * @param url the URL to canonicalize
     * @return the canonicalized URL
     * @throws MalformedURLException  if the URL is not a valid URL
     */
    @Nonnull @NotEmpty public static String canonicalize(@Nonnull @NotEmpty final String url)
            throws MalformedURLException {
        Constraint.isFalse(Strings.isNullOrEmpty(url), "URL was null or empty");
        final URLBuilder urlBuilder = new URLBuilder(url);
        canonicalize(urlBuilder);
        return Constraint.isNotEmpty(urlBuilder.buildURL(), "Canonical URL was null");
    }
    
    /**
     * Canonicalize the supplied URLBuilder data.
     * 
     * @param url the URLBuilder to canonicalize
     */
    private static void canonicalize(@Nonnull final URLBuilder url) {
        
        // Lower case the scheme.
        if (url.getScheme() != null) {
            url.setScheme(url.getScheme().toLowerCase());
        }
        
        final String scheme = url.getScheme();
        if (scheme != null) {
            final Integer port = getRegisteredPort(scheme);
            if (port != null && port.equals(url.getPort())) {
                url.setPort(null);
            }
        }
        
        if (url.getHost() != null) {
            url.setHost(url.getHost().toLowerCase());
        }
    }
    
    static {
        registerSchemePortMapping("ftp", 23);
        registerSchemePortMapping("http", 80);
        registerSchemePortMapping("https", 443);
        registerSchemePortMapping("ldap", 389);
        registerSchemePortMapping("ldaps", 636);
    }

}