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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;


/**
 * Implementation of an HTTP servlet {@link Filter} which adds the SameSite attribute to cookies, until
 * the Java API supports it natively, if ever.
 * 
 * <p>Explicitly named cookies are configured and placed into a Map of cookie name to same-site attribute value.</p>
 * 
 * <p>All other cookies may be assigned a default value.</p>
 * 
 * <p>Cookies with an existing same-site cookie flag are  left unaltered - copied back into the response
 * without modification.</p>
 * 
 * <p>A single cookie can only have at most one same-site value set. Attempts in the configuration to 
 * give more than one same-site value to a cookie are caught during argument injection and throw an
 * {@link IllegalArgumentException}.</p>
 * 
 */
public class SameSiteCookieHeaderFilter implements Filter {
    
    /** The name of the same-site cookie attribute.*/
    @Nonnull @NotEmpty private static final String SAMESITE_ATTRIBITE_NAME="SameSite";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SameSiteCookieHeaderFilter.class);
      
    /** The allowed same-site cookie attribute values.*/
    public enum SameSiteValue{       
        
        /**
         * Send the cookie for 'same-site' requests only.
         */
        Strict("Strict"),
        /**
         * Send the cookie for 'same-site' requests along with 'cross-site' top 
         * level navigations using safe HTTP methods (GET, HEAD, OPTIONS, and TRACE).
         */
        Lax("Lax"),        
        /**
         * Send the cookie for 'same-site' and 'cross-site' requests.
         */
        None("None");
        
        /** The same-site attribute value.*/
        @Nonnull @NotEmpty private String value;
        
        /**
         * Constructor.
         *
         * @param attrValue the same-site attribute value.
         */
        private SameSiteValue(@Nonnull @NotEmpty final String attrValue) {
            value = Constraint.isNotEmpty(attrValue, "the same-site attribute value can not be empty");
         }

        /**
         * Get the same-site attribute value.
         * 
         * @return Returns the value.
         */
        public String getValue() {
            return value;
        }
        
    }
    
    /** Optional default value to apply. */
    @Nullable private SameSiteValue defaultValue;
    
    /** Map of cookie name to same-site attribute value.*/
    @Nonnull @NonnullElements private Map<String,SameSiteValue> sameSiteCookies;
    
    /** Constructor. */
    public SameSiteCookieHeaderFilter() {
        sameSiteCookies = Collections.emptyMap();
    }
    
    /**
     * Set an optional default value to apply to all unmapped cookies.
     * 
     * @param value default value
     */
    public void setDefaultValue(@Nullable final SameSiteValue value) {
        defaultValue = value;
    }
    
    /**
     * Set the names of cookies to add the same-site attribute to. 
     * 
     * <p>The argument map is flattened to remove the nested collection. The argument map allows duplicate 
     * cookie names to appear in order to detect configuration errors which would otherwise not be found during 
     * argument injection e.g. trying to set a session identifier cookie as both SameSite=Strict and SameSite=None. 
     * Instead, duplicates are detected here, throwing a terminating {@link IllegalArgumentException} if found.</p>
     * 
     * @param map the map of same-site attribute values to cookie names.
     */
    public void setSameSiteCookies(@Nullable @NonnullElements final Map<SameSiteValue,List<String>> map) {
        if (map != null) {
            sameSiteCookies = new HashMap<>(4);
            for (final Map.Entry<SameSiteValue,List<String>> entry : map.entrySet()) {
                
                for (final String cookieName : entry.getValue()) {
                   if (sameSiteCookies.get(cookieName) != null) {
                       log.error("Duplicate cookie name '{}' found in SameSite cookie map, "
                               + "please check configuration.",cookieName);
                       throw new IllegalArgumentException("Duplicate cookie name found in SameSite cookie map");
                   }  
                   final String trimmedName = StringSupport.trimOrNull(cookieName);
                    if (trimmedName != null) {
                        sameSiteCookies.put(cookieName, entry.getKey());
                    }
                }                
            }
        } else {
            sameSiteCookies = Collections.emptyMap();
        }
        
    }
    
    /** {@inheritDoc} */
    public void init(@Nonnull final FilterConfig filterConfig) throws ServletException {
    }
    
    /** {@inheritDoc} */
    public void destroy() {
    }
    
    /** {@inheritDoc} */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Response is not an instance of HttpServletResponse");
        }
        
        chain.doFilter(request, new SameSiteResponseProxy((HttpServletResponse)response));
        
    }
    
    /**
     * An implementation of the {@link HttpServletResponse} which adds the same-site flag to {@literal Set-Cookie}
     * headers for the set of configured cookies.
     */
    private class SameSiteResponseProxy extends HttpServletResponseWrapper{

        /** The response. */
        @Nonnull private final HttpServletResponse response;
        
        /**
         * Constructor.
         *
         * @param resp the response to delegate to
         */
        public SameSiteResponseProxy(@Nonnull final HttpServletResponse resp) {
            super(resp);
            response = resp;
        }
        
        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc) throws IOException {
            appendSameSite();
            super.sendError(sc);
        }
        
        /** {@inheritDoc} */
        @Override
        public PrintWriter getWriter() throws IOException {
            appendSameSite();
            return super.getWriter();
        }
        
        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc, final String msg) throws IOException {
            appendSameSite();
            super.sendError(sc, msg);
        }
        
        /** {@inheritDoc} */
        @Override
        public void sendRedirect(final String location) throws IOException {
            appendSameSite();
            super.sendRedirect(location);
        }
        
        /** {@inheritDoc} */
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            appendSameSite();
            return super.getOutputStream();
        }
        
        /** 
         * Add the SameSite attribute to those cookies configured in the {@code sameSiteCookies} map iff 
         * they do not already contain the same-site flag. All other cookies are copied over to the response
         * without modification.
         * */
        private void appendSameSite() {
            
            final Collection<String> cookieheaders = response.getHeaders(HttpHeaders.SET_COOKIE);
            
            boolean firstHeader = true;
            for (final String cookieHeader : cookieheaders) {
                
                if (StringSupport.trimOrNull(cookieHeader)==null) {
                    continue;
                }
                
                List<HttpCookie> parsedCookies = null;
                try {
                    //this parser only parses name and value, we only need the name.
                    parsedCookies = HttpCookie.parse(cookieHeader);
                } catch(final IllegalArgumentException e) {
                    // Should not get here
                   log.trace("Cookie header '{}' violates the cookie specification and will be ignored", cookieHeader);
                }
                
                if (parsedCookies == null || parsedCookies.size() != 1) {
                    // Should be one cookie since we're only looking at Set-Cookie, not Set-Cookie2.
                    continue;
                }
                
                final SameSiteValue sameSiteValue = sameSiteCookies.get(parsedCookies.get(0).getName());
                if (sameSiteValue != null) {
                    appendSameSiteAttribute(cookieHeader, sameSiteValue.getValue(), firstHeader);
                } else if (defaultValue != null) {
                    appendSameSiteAttribute(cookieHeader, defaultValue.getValue(), firstHeader);
                } else {
                    // Copy it over unaltered.
                    if (firstHeader) {                      
                        response.setHeader(HttpHeaders.SET_COOKIE, cookieHeader);                        
                    } else {
                        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
                    }
                }
                firstHeader=false;
                
            }
        }
        
        /**
         * Append the SameSite cookie attribute with the specified samesite-value to the {@code cookieHeader} 
         * iff it does not already have one set.           
         * 
         * @param cookieHeader the cookie header value
         * @param sameSiteValue the SameSite attribute value e.g. None, Lax, or Strict
         * @param first true iff this is the first Set-Cookie header
         */
        private void appendSameSiteAttribute(@Nonnull @NotEmpty final String cookieHeader,
                @Nonnull @NotEmpty final String sameSiteValue,
                @Nonnull final boolean first) {
            
            String sameSiteSetCookieValue =  cookieHeader;
            
            //only add if does not already exist, else leave
            if (!cookieHeader.contains(SAMESITE_ATTRIBITE_NAME)) {
                sameSiteSetCookieValue = String.format("%s; %s", cookieHeader,
                        SAMESITE_ATTRIBITE_NAME + "=" + sameSiteValue);                
            } 
            
            if (first) {                
                response.setHeader(HttpHeaders.SET_COOKIE,sameSiteSetCookieValue);
            } else {
                response.addHeader(HttpHeaders.SET_COOKIE, sameSiteSetCookieValue);
            }
        } 
    }
    
}