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

package net.shibboleth.utilities.java.support.primitive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An object that represents a string containing a {@link java.util.Formatter} string and a set of values. When
 * {@link #toString()} is called the format string is filled in with the given values. This allows for lazy evaluation
 * of the value objects formatting function which may be expensive.
 */
public class LazilyFormattedString {

    /** The template that is filled in with the values. */
    private final String template;

    /** The values that are filled in to the template string. */
    private final Object[] arguments;

    /**
     * Constructor.
     * 
     * @param stringTemplate the {@link java.util.Formatter} template string
     * @param templateArguments the arguments to the template
     */
    public LazilyFormattedString(@Nonnull final String stringTemplate, @Nullable final Object... templateArguments) {
        template = Constraint.isNotNull(stringTemplate, "String template can not be null");
        arguments = templateArguments;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(template, arguments);
    }
    
}