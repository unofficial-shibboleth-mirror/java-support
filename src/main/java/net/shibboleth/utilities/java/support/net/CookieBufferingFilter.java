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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * Implementation of an HTTP servlet {@link Filter} which wraps the {@link HttpServletResponse} via
 * {@link CookieBufferingHttpServletResponseProxy} to ensure that only a single cookie of a given name is set.
 */
public class CookieBufferingFilter implements Filter {

    /** {@inheritDoc} */
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /** {@inheritDoc} */
    public void destroy() {
    }

    /** {@inheritDoc} */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException,
            ServletException {

        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Request is not an instance of HttpServletRequest");
        }

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Response is not an instance of HttpServletResponse");
        }

        chain.doFilter(request, new CookieBufferingHttpServletResponseProxy((HttpServletResponse) response));
    }

    /**
     * An implementation of {@link HttpServletResponse} which buffers added cookies to
     * ensure only a single cookie of a given name is eventually set.
     */
    private class CookieBufferingHttpServletResponseProxy extends HttpServletResponseWrapper {

        /** Map of delayed cookie additions. */
        @Nonnull @NonnullElements private Map<String,Cookie> cookieMap;
        
        /**
         * Constructor.
         *
         * @param response the response to delegate to
         */
        public CookieBufferingHttpServletResponseProxy(@Nonnull final HttpServletResponse response) {
            super(response);
            cookieMap = new HashMap<>();
        }
    
        /** {@inheritDoc} */
        @Override
        public void addCookie(final Cookie cookie) {
            // Guarantees any existing cookie by this name is replaced.
            cookieMap.put(cookie.getName(), cookie);
        }
    
        /**
         * Get the map of cookies that will be set.
         * 
         * @return map of cookies to be set
         */
        @Nonnull @NonnullElements @Live protected Map<String,Cookie> getCookies() {
            return cookieMap;
        }

        /** {@inheritDoc} */
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            dumpCookies();
            return super.getOutputStream();
        }

        /** {@inheritDoc} */
        @Override
        public PrintWriter getWriter() throws IOException {
            dumpCookies();
            return super.getWriter();
        }

        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc, final String msg) throws IOException {
            dumpCookies();
            super.sendError(sc, msg);
        }

        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc) throws IOException {
            dumpCookies();
            super.sendError(sc);
        }

        /** {@inheritDoc} */
        @Override
        public void sendRedirect(final String location) throws IOException {
            dumpCookies();
            super.sendRedirect(location);
        }
        
        /**
         * Transfer cookies added into the real response.
         */
        protected void dumpCookies() {
            for (final Cookie cookie : cookieMap.values()) {
                ((HttpServletResponse) getResponse()).addCookie(cookie);
            }
            cookieMap.clear();
        }
    }
    
}
