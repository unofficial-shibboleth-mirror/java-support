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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Class which holds and makes available the current HTTP servlet request and response via ThreadLocal storage.
 * 
 * <p>
 * See also {@link RequestResponseContextFilter}, which is a Java Servlet {@link javax.servlet.Filter}-based way
 * to populate and clean up this context in a servlet container.
 * </p>
 */
public final class HttpServletRequestResponseContext {

    /** ThreadLocal storage for request. */
    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();

    /** ThreadLocal storage for response. */
    private static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<>();

    /** Constructor. */
    private HttpServletRequestResponseContext() {
    };

    /**
     * Load the thread-local storage with the current request and response.
     * 
     * @param request the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     */
    public static void loadCurrent(@Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response) {
        Constraint.isNotNull(request, "HttpServletRequest may not be null");
        Constraint.isNotNull(response, "HttpServletResponse may not be null");

        currentRequest.set(request);
        currentResponse.set(response);
    }

    /**
     * Clear the current thread-local context instances.
     */
    public static void clearCurrent() {
        currentRequest.remove();
        currentResponse.remove();
    }

    /**
     * Get the current {@link HttpServletRequest} being serviced by the current thread.
     * 
     * @return the current request
     */
    @Nullable public static HttpServletRequest getRequest() {
        return currentRequest.get();
    }

    /**
     * Get the current {@link HttpServletResponse} being serviced by the current thread.
     * 
     * @return the current response
     */
    @Nullable public static HttpServletResponse getResponse() {
        return currentResponse.get();
    }

}
