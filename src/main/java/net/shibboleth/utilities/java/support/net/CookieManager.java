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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A helper class for managing one or more cookies on behalf of a component.
 * 
 * <p>This bean centralizes settings related to cookie creation and access,
 * and is parametrized by name so that multiple cookies may be managed with
 * common properties.</p>
 */
public final class CookieManager extends AbstractInitializableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CookieManager.class);

    /** Path of cookie. */
    @Nullable private String cookiePath;

    /** Domain of cookie. */
    @Nullable private String cookieDomain;
    
    /** Servlet request to read from. */
    @NonnullAfterInit private HttpServletRequest httpRequest;

    /** Servlet response to write to. */
    @NonnullAfterInit private HttpServletResponse httpResponse;
    
    /** Is cookie secure? */
    private boolean secure;

    /** Is cookie marked HttpOnly? */
    private boolean httpOnly;
    
    /** Maximum age in seconds, or -1 for session. */
    private int maxAge;
    
    /** Constructor. */
    public CookieManager() {
        httpOnly = true;
        secure = true;
        maxAge = -1;
    }

    /**
     * Set the cookie path to use for session tracking.
     * 
     * <p>Defaults to the servlet context path.</p>
     * 
     * @param path cookie path to use, or null for the default
     */
    public void setCookiePath(@Nullable final String path) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        cookiePath = StringSupport.trimOrNull(path);
    }

    /**
     * Set the cookie domain to use for session tracking.
     * 
     * @param domain the cookie domain to use, or null for the default
     */
    public void setCookieDomain(@Nullable final String domain) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        cookieDomain = StringSupport.trimOrNull(domain);
    }

    /**
     * Set the servlet request to read from.
     * 
     * @param request servlet request
     */
    public void setHttpServletRequest(@Nonnull final HttpServletRequest request) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        httpRequest = Constraint.isNotNull(request, "HttpServletRequest cannot be null");
    }

    /**
     * Set the servlet response to write to.
     * 
     * @param response servlet response
     */
    public void setHttpServletResponse(@Nonnull final HttpServletResponse response) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        httpResponse = Constraint.isNotNull(response, "HttpServletResponse cannot be null");
    }

    /**
     * Set the SSL-only flag.
     * 
     * @param flag flag to set
     */
    public void setSecure(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        secure = flag;
    }


    /**
     * Set the HttpOnly flag.
     * 
     * @param flag flag to set
     */
    public void setHttpOnly(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        httpOnly = flag;
    }
    
    /**
     * Maximum age in seconds, or -1 for per-session.
     * 
     * @param age max age to set
     */
    public void setMaxAge(final int age) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        maxAge = age;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (httpRequest == null || httpResponse == null) {
            throw new ComponentInitializationException("Servlet request and response must be set");
        } else if (!secure || !httpOnly) {
            log.warn("Use of secure and httpOnly properties are strongly advisable, currently one or both are false");
        }
    }

    /**
     * Add a cookie with the specified name and value.
     * 
     * @param name  name of cookie
     * @param value value of cookie
     */
    @Nullable public void addCookie(@Nonnull @NotEmpty final String name, @Nonnull @NotEmpty final String value) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        final Cookie cookie = new Cookie(name, value);
        cookie.setPath(cookiePath != null ? cookiePath : contextPathToCookiePath());
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        
        httpResponse.addCookie(cookie);
    }

    /**
     * Unsets a cookie with the specified name.
     * 
     * @param name  name of cookie
     */
    @Nullable public void unsetCookie(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        final Cookie cookie = new Cookie(name, null);
        cookie.setPath(cookiePath != null ? cookiePath : contextPathToCookiePath());
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(0);
        
        httpResponse.addCookie(cookie);
    }

    /**
     * Check whether a cookie has a certain value.
     * 
     * @param name name of cookie
     * @param expectedValue expected value of cookie
     * 
     * @return true iff the cookie exists and has the expected value
     */
    public boolean cookieHasValue(@Nonnull @NotEmpty final String name, @Nonnull @NotEmpty final String expectedValue) {
        
        final String realValue =  getCookieValue(name, null);
        if (realValue == null) {
            return false;
        }
        
        return realValue.equals(expectedValue);
    }
    
    /**
     * Return the first matching cookie's value.
     * 
     * @param name cookie name
     * @param defValue default value to return if the cookie isn't found
     * 
     * @return cookie value
     */
    @Nullable public String getCookieValue(@Nonnull @NotEmpty final String name, @Nullable final String defValue) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        final Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        
        return defValue;
    }
    
    /**
     * Turn the servlet context path into an appropriate cookie path.
     * 
     * @return  the cookie path
     */
    @Nonnull @NotEmpty private String contextPathToCookiePath() {
        return "".equals(httpRequest.getContextPath()) ? "/" : httpRequest.getContextPath();
    }
    
}