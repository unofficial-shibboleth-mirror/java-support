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

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;

/** Generates a type 4 UUID as an identifier. */
@ThreadSafe
public class Type4UUIDIdentifierGenerationStrategy implements IdentifierGenerationStrategy {

    /** {@inheritDoc} */
    @Nonnull @NotEmpty public String generateIdentifier() {
        return generateIdentifier(true);
    }

    /** {@inheritDoc} */
    @Nonnull @NotEmpty public String generateIdentifier(final boolean xmlSafe) {
        if (xmlSafe) {
            return "_" + UUID.randomUUID().toString();
        } else {
            return UUID.randomUUID().toString();
        }
    }
}