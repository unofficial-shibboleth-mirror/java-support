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

package net.shibboleth.utilities.java.support.velocity;

import java.util.Properties;

import javax.annotation.Nonnull;

import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This is a helper class for creating velocity engines.
 */
public final class VelocityEngine {

    /**
     * Default constructor.
     */
    private VelocityEngine() {
    }

    /**
     * Creates a new velocity engine with default properties. See {@link #getDefaultProperties()}.
     * 
     * @return velocity engine
     */
    @Nonnull public static org.apache.velocity.app.VelocityEngine newVelocityEngine() {
        return newVelocityEngine(getDefaultProperties());
    }

    /**
     * Creates a new velocity engine with the supplied properties.
     * 
     * @param props velocity engine properties
     * 
     * @return velocity engine
     */
    @Nonnull public static org.apache.velocity.app.VelocityEngine newVelocityEngine(@Nonnull final Properties props) {
        final org.apache.velocity.app.VelocityEngine engine = new org.apache.velocity.app.VelocityEngine();
        engine.init(props);
        return engine;
    }

    /**
     * Returns the default velocity engine properties. Default properties include:
     * <ul>
     * <li>"resource.loader.string.class","org.apache.velocity.runtime.resource.loader.StringResourceLoader"</li>
     * <li>"resource.loader.classpath.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"</li>
     * <li>"resource.loaders", "classpath, string"</li>
     * </ul>
     * 
     * @return velocity engine properties
     */
    @Nonnull public static Properties getDefaultProperties() {
        final Properties props = new Properties();
        props.setProperty("resource.loader.string.class",
                "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
        props.setProperty("resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath, string");
        return props;
    }
}