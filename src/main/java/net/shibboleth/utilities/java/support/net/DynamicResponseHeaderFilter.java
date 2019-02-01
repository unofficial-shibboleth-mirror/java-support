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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Implementation of an HTTP servlet {@link Filter} which supports configurable response header
 * injection, including via injected functions that can conditionally attach headers.
 */
public class DynamicResponseHeaderFilter implements Filter {
    
    /** Statically defined headers to return. */
    @Nonnull @NonnullElements private Map<String,String> headers;

    /** Callbacks to add headers dynamically. */
    @Nonnull @NonnullElements
    private Collection<Function<Pair<HttpServletRequest,HttpServletResponse>,Boolean>> callbacks;
    
    /** Constructor. */
    public DynamicResponseHeaderFilter() {
        headers = Collections.emptyMap();
        callbacks = Collections.emptyList();
    }
    
    /**
     * Set the headers to statically attach to all responses.
     * 
     * @param map   header map
     */
    public void setHeaders(@Nullable @NonnullElements final Map<String,String> map) {
        if (map != null) {
            headers = new HashMap<>(map.size());
            for (final Map.Entry<String,String> entry : map.entrySet()) {
                final String trimmedName = StringSupport.trimOrNull(entry.getKey());
                final String trimmedValue = StringSupport.trimOrNull(entry.getValue());
                if (trimmedName != null && trimmedValue != null) {
                    headers.put(trimmedName, trimmedValue);
                }
            }
        } else {
            headers = Collections.emptyMap();
        }
    }
    
    /**
     * Set the callbacks to invoke to dynamically attach headers.
     * 
     * @param theCallbacks callback collection
     */
    public void setCallbacks(@Nullable @NonnullElements
            final Collection<Function<Pair<HttpServletRequest,HttpServletResponse>,Boolean>> theCallbacks) {
        if (theCallbacks != null) {
            callbacks = new ArrayList<>(Collections2.filter(theCallbacks, Predicates.notNull()));
        } else {
            callbacks = Collections.emptyList();
        }
    }
    
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
        
        if (headers.isEmpty() && callbacks.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Request is not an instance of HttpServletRequest");
        }

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Response is not an instance of HttpServletResponse");
        }

        chain.doFilter(request, new ResponseProxy((HttpServletRequest) request, (HttpServletResponse) response));
    }

    /**
     * An implementation of {@link HttpServletResponse} which adds the response headers supplied by the outer class.
     */
    private class ResponseProxy extends HttpServletResponseWrapper {
        
        /** Request. */
        @Nonnull private final HttpServletRequest request;
        
        /**
         * Constructor.
         *
         * @param req the request
         * @param response the response to delegate to
         */
        public ResponseProxy(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse response) {
            super(response);
            
            request = req;
        }
    
        /** {@inheritDoc} */
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            addHeaders();
            return super.getOutputStream();
        }

        /** {@inheritDoc} */
        @Override
        public PrintWriter getWriter() throws IOException {
            addHeaders();
            return super.getWriter();
        }

        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc, final String msg) throws IOException {
            addHeaders();
            super.sendError(sc, msg);
        }

        /** {@inheritDoc} */
        @Override
        public void sendError(final int sc) throws IOException {
            addHeaders();
            super.sendError(sc);
        }

        /** {@inheritDoc} */
        @Override
        public void sendRedirect(final String location) throws IOException {
            addHeaders();
            super.sendRedirect(location);
        }
        
        /** Add headers to response. */
        private void addHeaders() {
            for (final Map.Entry<String, String> header : headers.entrySet()) {
                ((HttpServletResponse) getResponse()).addHeader(header.getKey(), header.getValue());
            }
            
            if (!callbacks.isEmpty()) {
                final Pair<HttpServletRequest,HttpServletResponse> p =
                        new Pair<>(request, (HttpServletResponse) getResponse());
                for (final Function<Pair<HttpServletRequest,HttpServletResponse>,Boolean> callback : callbacks) {
                    callback.apply(p);
                }
            }
        }
    }
    
}