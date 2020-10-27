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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;


/** Helper methods for reporting usage of deprecated features. */
public final class DeprecationSupport {

    /** Log category name for warnings. */
    @Nonnull @NotEmpty public static final String LOG_CATEGORY = "DEPRECATED";
    
    /** Class logger. */
    @Nonnull private static final Logger LOG = LoggerFactory.getLogger(LOG_CATEGORY);
    
    /** Tracks issued warnings. */
    @Nonnull @NonnullElements private static final Set<String> WARNED_SET = new HashSet<>();
    
    /** Constructor. */
    private DeprecationSupport() {
        
    }

    /**
     * Type of object, setting, feature, etc. being deprecated.
     */
    public enum ObjectType {
        
        /** Java class/type. */
        CLASS("Java class"),
        
        /** Java method. */
        METHOD("Java class method"),
        
        /** Key/value property. */
        PROPERTY("property"),
        
        /** Spring bean. */
        BEAN("Spring bean"),

        /** XML namespace. */
        NAMESPACE("XML Namespace"),

        /** XML type. */
        XSITYPE("xsi:type"),

        /** XML element. */
        ELEMENT("XML Element"),
        
        /** XML attribute. */
        ATTRIBUTE("XML Attribute"),
        
        /** Spring web flow. */
        WEBFLOW("Spring WebFlow"),

        /** Spring web flow. */
        ACTION("Spring WebFlow action"),
        
        /** Configuration approach. */
        CONFIGURATION("configuration"),
        
        /**
         * Command-line option.
         *
         * @since 8.1
         */
        CLI_OPTION("command-line option");
        
        /** Printed version. */
        @Nonnull @NotEmpty private final String text;

        /**
         * Constructor.
         *
         * @param s printed representation
         */
        private ObjectType(@Nonnull @NotEmpty final String s) {
            text = s;
        }
        
        /** {@inheritDoc} */
        @Override public String toString() {
            return text;
        }
    }
    
    /**
     * Emit a deprecation warning for an object or feature of the system.
     * 
     * @param type type of object or feature
     * @param name name of object or feature
     * @param context surrounding context for deprecation warning
     * @param replacement the replacement for the deprecated feature
     */
    public static void warn(@Nonnull final ObjectType type, @Nonnull @NotEmpty final String name,
            @Nullable final String context, @Nullable final String replacement) {
        
        if (context != null && replacement != null) {
            LOG.warn("{} '{}', ({}):"
                        + " This will be removed in the next major version of this software; replacement is {}",
                    type, name, context, replacement);
        } else if (context != null) {
            LOG.warn("{} '{}', ({}): This will be removed in the next major version of this software",
                type, name, context);
        } else if (replacement != null) {
            LOG.warn("{} '{}':"
                    + " This will be removed in the next major version of this software; replacement is {}",
                type, name, replacement);
        } else {
            LOG.warn("{} '{}': This will be removed in the next major version of this software.",
                    type, name);
        }
    }
    
    /**
     * Emit a deprecation warning for an object or feature of the system but limit to a single warning
     * for the life of the process or until state is cleared.
     * 
     * @param type type of object or feature
     * @param name name of object or feature
     * @param context surrounding context for deprecation warning
     * @param replacement the replacement for the deprecated feature
     */
    public static void warnOnce(@Nonnull final ObjectType type, @Nonnull @NotEmpty final String name,
            @Nullable final String context, @Nullable final String replacement) {
        
        synchronized(WARNED_SET) {
            if (!WARNED_SET.add(type.toString() + ':' + name)) {
                return;
            }
        }
        
        warn(type, name, context, replacement);
    }
    
    /**
     * Clear the previously warned state.
     */
    public static void clearWarningState() {
        
        synchronized(WARNED_SET) {
            WARNED_SET.clear();
        }
    }
    
}