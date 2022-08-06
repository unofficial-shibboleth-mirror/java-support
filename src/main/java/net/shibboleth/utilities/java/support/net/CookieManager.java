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

import java.util.function.Supplier;

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
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;

/**
 * A helper class for managing one or more cookies on behalf of a component.
 * 
 * <p>This bean centralizes settings related to cookie creation and access,
 * and is parameterized by name so that multiple cookies may be managed with
 * common properties.</p>
 */
public final class CookieManager extends AbstractInitializableComponent {

    /** Log. */
    private final Logger log = LoggerFactory.getLogger(CookieManager.class);

    /** Path of cookie. */
    @Nullable private String cookiePath;

    /** Domain of cookie. */
    @Nullable private String cookieDomain;
    
    /** Supplier for the servlet request to read from. */
    @NonnullAfterInit private Supplier<HttpServletRequest> httpRequestSupplier;

    /** Supplier for the servlet response to write to. */
    @NonnullAfterInit private Supplier<HttpServletResponse> httpResponseSupplier;
    
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
     * Set the Supplier for the servlet request to read from.
     *
     * @param requestSupplier servlet request supplier
     */
    public void setHttpServletRequestSupplier(@Nonnull final Supplier<HttpServletRequest> requestSupplier) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        httpRequestSupplier = Constraint.isNotNull(requestSupplier, "HttpServletRequest cannot be null");
    }

    /**
     * Set the current HTTP request.
     *
     * @param request current HTTP request
     */
    @Deprecated(since = "4.3", forRemoval = true)
    public void setHttpServletRequest(@Nullable final HttpServletRequest request) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        DeprecationSupport.warnOnce(ObjectType.METHOD, "setHttpServletReqest",
                "CookieManager", "setHttpServletRequestSupplier");
        if (request != null && !(request instanceof ThreadLocalHttpServletRequestProxy)) {
            log.warn("Unsafe HttpServletRequest injected");
        }
        httpRequestSupplier = new Supplier<>() {
            public HttpServletRequest get() {
                return request;
            };
        };
    }

    /**
     * Get the current HTTP request if available.
     *
     * @return current HTTP request
     */
    @NonnullAfterInit private HttpServletRequest getHttpServletRequest() {
        if (httpRequestSupplier == null) {
            return null;
        }
        return httpRequestSupplier.get();
    }

    /**
     * Set the supplier for the servlet response to write to.
     *
     * @param responseSupplier servlet response
     */
    public void setHttpServletResponseSupplier(@Nonnull final Supplier<HttpServletResponse> responseSupplier) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        httpResponseSupplier = Constraint.isNotNull(responseSupplier, "HttpServletResponse cannot be null");
    }

    /**
     * Set the servlet response to write to.
     *
     * @param response current HTTP response
     */
    @Deprecated(since = "4.3", forRemoval = true)
    public void setHttpServletResponse(@Nullable final HttpServletResponse response) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        DeprecationSupport.warnOnce(ObjectType.METHOD, "setHttpServletResponse",
                "CookieManager", "setHttpServletResponseSupplier");
        if (response != null && !(response instanceof ThreadLocalHttpServletResponseProxy)) {
            log.warn("Unsafe HttpServletRequest injected");
        }
        httpResponseSupplier = new Supplier<>() {
            public HttpServletResponse get() {
                return response;
            };
        };
    }

    /**
     * Get the current HTTP response if available.
     *
     * @return current HTTP response or null
     */
    @NonnullAfterInit private HttpServletResponse getHttpServletResponse() {
        if (httpResponseSupplier == null) {
            return null;
        }
        return httpResponseSupplier.get();
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
        
        if (getHttpServletRequest() == null || getHttpServletResponse() == null) {
            throw new ComponentInitializationException("Servlet request and response must be set");
        }
    }

    /**
     * Add a cookie with the specified name and value.
     * 
     * @param name  name of cookie
     * @param value value of cookie
     */
    public void addCookie(@Nonnull @NotEmpty final String name, @Nonnull @NotEmpty final String value) {
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
        
        getHttpServletResponse().addCookie(cookie);
    }

    /**
     * Unsets a cookie with the specified name.
     * 
     * @param name  name of cookie
     */
    public void unsetCookie(@Nonnull @NotEmpty final String name) {
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
        
        getHttpServletResponse().addCookie(cookie);
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
        
        final Cookie[] cookies = getHttpServletRequest().getCookies();
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
        final  HttpServletRequest httpRequest = getHttpServletRequest();
        return "".equals(httpRequest.getContextPath()) ? "/" : httpRequest.getContextPath();
    }
    
}