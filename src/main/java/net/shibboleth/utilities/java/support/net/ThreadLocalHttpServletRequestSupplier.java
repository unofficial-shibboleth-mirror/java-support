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
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.NonnullSupplier;

/**
 * An implementation of {@link NonnullSupplier} of {@link HttpServletRequest}s which looks up the current thread-local
 * servlet request obtained from {@link HttpServletRequestResponseContext}.
 */
public class ThreadLocalHttpServletRequestSupplier implements NonnullSupplier<HttpServletRequest> {

    /**
     * {@inheritDoc}
     * Get the current HttpServletRequest from ThreadLocal storage.
     *
     * @return the current request
     */
    @Nonnull
    public HttpServletRequest get() {
        return Constraint.isNotNull(HttpServletRequestResponseContext.getRequest(),
                "Current HttpServletRequest has not been loaded via HttpServletRequestResponseContext");
    }
}
