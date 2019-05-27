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

package net.shibboleth.utilities.java.support.httpclient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A wrapper implementation of {@link HttpClient} which invokes supplied instances of {@link HttpClientContextHandler}
 * before and after request execution.
 * 
 * <p>
 * By definition the handlers will only be invoked for the {@link HttpClient} execute(...) method variants
 * which take an {@link HttpContext} argument.
 * </p>
 * 
 * <p>
 * The order of execution is:
 * <ol>
 * <li>Static handlers supplied via the constructor, in original list order</li>
 * <li>Dynamic handlers from the context attribute {@link HttpClientSupport#CONTEXT_KEY_DYNAMIC_CONTEXT_HANDLERS},
 *     in original list order</li>
 * <li>the wrapped client's corresponding execute(...) method</li>
 * <li>Dynamic handlers from the context attribute {@link HttpClientSupport#CONTEXT_KEY_DYNAMIC_CONTEXT_HANDLERS},
 *     in reverse list order</li>
 * <li>Static handlers supplied via the constructor, in reverse list order</li>
 * </ol>
 * </p>
 */
class ContextHandlingHttpClient extends CloseableHttpClient {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(ContextHandlingHttpClient.class);
    
    /** The wrapped client instance. */
    @Nonnull private CloseableHttpClient httpClient;
    
    /** Optional list of static handlers supplied to this class instance. */
    @Nonnull private List<HttpClientContextHandler> handlers;
    
    /**
     * Constructor.
     *
     * @param client the wrapped client instance
     */
    public ContextHandlingHttpClient(@Nonnull final CloseableHttpClient client) {
        this(client, null);
    }

    /**
     * Constructor.
     *
     * @param client the wrapped client instance
     * @param staticHandlers the list of static handlers
     */
    public ContextHandlingHttpClient(@Nonnull final CloseableHttpClient client, 
            @Nonnull final List<HttpClientContextHandler> staticHandlers) {
        httpClient = Constraint.isNotNull(client, "HttpClient was null");
        handlers = staticHandlers != null ? staticHandlers : Collections.emptyList();
    }

    /** {@inheritDoc} */
    public HttpParams getParams() {
        return httpClient.getParams();
    }

    /** {@inheritDoc} */
    public ClientConnectionManager getConnectionManager() {
        return httpClient.getConnectionManager();
    }
    
    /** {@inheritDoc} */
    public void close() throws IOException {
        httpClient.close();
    }

    /** {@inheritDoc} */
    protected CloseableHttpResponse doExecute(final HttpHost target, final HttpRequest request, 
            final HttpContext context) throws IOException, ClientProtocolException {
        
        Throwable error = null;
        
        final HttpClientContext clientContext = 
                HttpClientContext.adapt(context != null ? context : new BasicHttpContext());
        final HttpUriRequest uriRequest = HttpUriRequest.class.isInstance(request) 
                ? (HttpUriRequest)request : HttpRequestWrapper.wrap(request, target);
        try {
            invokeBefore(uriRequest, clientContext);
            return httpClient.execute(target, request, clientContext);
        } catch (final Throwable t) {
            error = t;
            throw t;
        } finally {
            invokeAfter(uriRequest, clientContext, error);
        }
    }

    /**
     * Invoke {@link HttpClientContextHandler#invokeBefore(HttpClientContext, HttpUriRequest)}
     * for supplied handlers.
     * 
     * @param request the HTTP request
     * @param context the HTTP context
     * @throws IOException if any handler throws an error
     */
    private void invokeBefore(final HttpUriRequest request, final HttpClientContext context) throws IOException {
        log.trace("In invokeBefore");
        
        final List<Throwable> errors = new LazyList<>();

        for (final HttpClientContextHandler handler : handlers) {
            try {
                if (handler != null) {
                    log.trace("Invoking static handler invokeBefore: {}", handler.getClass().getName());
                    handler.invokeBefore(context, request);
                }
            } catch (final Throwable t) {
                log.warn("Static handler invokeBefore threw: {}", handler.getClass().getName(), t);
                errors.add(t);
            }
        }

        for (final HttpClientContextHandler handler 
                : HttpClientSupport.getDynamicContextHandlerList(context)) {
            try {
                if (handler != null) {
                    log.trace("Invoking dynamic handler invokeBefore: {}", handler.getClass().getName());
                    handler.invokeBefore(context, request);
                }
            } catch (final Throwable t) {
                log.warn("Dynamic handler invokeBefore threw: {}", handler.getClass().getName(), t);
                errors.add(t);
            }
        }
        
        final IOException exception = processHandlerErrors("Invoke Before", errors);
        if (exception != null) {
            throw exception;
        }
        
    }

    /**
     * Invoke {@link HttpClientContextHandler#invokeAfter(HttpClientContext, HttpUriRequest)}
     * for all supplied handlers.
     * 
     * @param request the HTTP request
     * @param context the HTTP context
     * @param priorError an error thrown by by either {@link #invokeBefore(HttpUriRequest, HttpClientContext)}
     *          or by HttpClient execute(...).
     * 
     * @throws IOException if any handler throws an error, or if priorError is an IOException. If priorError
     *                     is a type of unchecked error (RuntimeException or Error) that will be propagated out
     *                     here as well.
     */
    private void invokeAfter(final HttpUriRequest request, final HttpClientContext context, 
            final Throwable priorError) throws IOException {
        log.trace("In invokeAfter");
        
        final List<Throwable> errors = new LazyList<>();
            
        for (final HttpClientContextHandler handler 
                : Lists.reverse(HttpClientSupport.getDynamicContextHandlerList(context))) {
            try {
                if (handler != null) {
                    log.trace("Invoking dynamic handler invokeAfter: {}", handler.getClass().getName());
                    handler.invokeAfter(context, request);
                }
            } catch (final Throwable t) {
                log.warn("Dynamic handler invokeAfter threw: {}", handler.getClass().getName(), t);
                errors.add(t);
            }
        }

        for (final HttpClientContextHandler handler : Lists.reverse(handlers)) {
            try {
                if (handler != null) {
                    log.trace("Invoking static handler invokeAfter: {}", handler.getClass().getName());
                    handler.invokeAfter(context, request);
                }
            } catch (final Throwable t) {
                log.warn("Static handler invokeAfter threw: {}", handler.getClass().getName(), t);
                errors.add(t);
            }
        }
        
        final IOException exception = processHandlerErrors("Invoke After", errors);
        processErrorsForInvokeAfter(exception, priorError);
            
    }
    
    /**
     * Process the error(s) seen during {@link #invokeBefore(HttpUriRequest, HttpClientContext)} 
     * or {@link #invokeAfter(HttpUriRequest, HttpClientContext, Throwable)}
     * into a single {@link IOException} that will be propagated out of that method.
     * 
     * @param stage the name of the stage, for reporting purposes
     * @param errors all errors seen during the method execution
     * 
     * @return the single exception to be propagated out, will be null if no errors present
     */
    private IOException processHandlerErrors(final String stage, final List<Throwable> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        
        if (errors.size() == 1) {
            final Throwable t = errors.get(0);
            if (IOException.class.isInstance(t)) {
                return IOException.class.cast(t);
            } else {
                return new IOException(
                        String.format("Context handler threw non-IOException Throwable in stage '%s'", stage), t);
            }
        } else {
            final IOException e = new IOException(
                    String.format("Multiple context handlers in stage '%s' reported error, see suppressed list", 
                            stage));
            for (final Throwable t : errors) {
                e.addSuppressed(t);
            }
            return e;
        }
    }
    
    /**
     * Process errors for
     * {@link #invokeAfter(HttpUriRequest, HttpClientContext, Throwable)}.
     * 
     * @param invokeAfterException the exception thrown by invokeAfter handlers, if any
     * @param priorError an error thrown by by either {@link #invokeBefore(HttpUriRequest, HttpClientContext)}
     *          or by HttpClient execute(...), if any.
     * 
     * @throws IOException if invokeAfterException is non-null, or if priorError is an IOException. If priorError
     *                     is a type of unchecked error (RuntimeException or Error) that will be propagated out
     *                     here as well. 
     */
    private void processErrorsForInvokeAfter(final IOException invokeAfterException, final Throwable priorError)
            throws IOException {
        
        if (priorError != null) {
            if (invokeAfterException != null) {
                priorError.addSuppressed(invokeAfterException);
            }
            // Note: The RuntimeException and Error cases below can only occur from the HttpClient execute() method,
            // since a priorError from invokeBefore() is always processed into a checked IOException, and in that case
            // HttpClient execute() wasn't called at all.
            if (IOException.class.isInstance(priorError)) {
                throw IOException.class.cast(priorError);
            } else if (RuntimeException.class.isInstance(priorError)) {
                throw RuntimeException.class.cast(priorError);
            } else if (Error.class.isInstance(priorError)) {
                throw Error.class.cast(priorError);
            } else {
                // This would either be an actual instance of java.lang.Throwable itself (not a subclass), 
                // which really shouldn't ever happen,
                // or some unaccounted case in a future version of Java which adds additional unchecked base types.
                // For safety handle by converting to a RuntimeException.
                throw new RuntimeException(priorError);
            }
        } else if (invokeAfterException != null) {
            throw invokeAfterException;
        }
    }



}
