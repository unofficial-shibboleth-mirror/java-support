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

package net.shibboleth.utilities.java.support.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resource.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * A class for building a {@link Schema} from a set of inputs, allowing for
 * manipulation of the underlying factory.
 */
@NotThreadSafe
public class SchemaBuilder {
    
    /** Language of the schema files. */
    public static enum SchemaLanguage {

        /** W3 XML Schema. */
        XML(XMLConstants.W3C_XML_SCHEMA_NS_URI),

        /** OASIS RELAX NG Schema. */
        RELAX(XMLConstants.RELAXNG_NS_URI);

        /** Constant for use with {@link SchemaFactory#newInstance(String)}. */
        @Nonnull private String schemaFactoryURI;
        
        /**
         * Constructor.
         * 
         * @param uri schema factory identifier
         */
        private SchemaLanguage(@Nonnull @NotEmpty final String uri) {
            schemaFactoryURI = Constraint.isNotNull(StringSupport.trimOrNull(uri), "URI cannot be null or empty");
        }

        /**
         * Get a {@link SchemaFactory} instance for a schema language.
         * 
         * @return  a factory instance
         */
        @Nonnull public SchemaFactory getSchemaFactory() {
            return SchemaFactory.newInstance(schemaFactoryURI);
        }
    };

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SchemaBuilder.class);
    
    /** Language of schemas. */
    @Nonnull private SchemaLanguage schemaLang;

    /** Sources of schema material compatible with JAXP. */
    @Nonnull @NonnullElements private List<Source> sources;
    
    /** Mechanism for resolving nested resources like included/imported schemas. */
    @Nullable private LSResourceResolver resourceResolver;
    
    /** Custom error handler. */
    @Nullable private ErrorHandler errorHandler;
    
    /** Features to set on factory. */
    @Nonnull private Map<String,Boolean> features;
    
    /** Properties to set on factory. */
    @Nonnull private Map<String,Object> properties;
    
    /** One time-init flag. */
    private boolean alreadyBuilt;
    
    /** Constructor. */
    public SchemaBuilder() {
        schemaLang = SchemaLanguage.XML;
        sources = new ArrayList<>();
        features = new HashMap<>();
        properties = new HashMap<>();
        errorHandler = new LoggingErrorHandler(log);
        alreadyBuilt = false;
    }
    
    /**
     * Set the schema language.
     * 
     * @param lang  the schema language
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder setSchemaLanguage(@Nonnull final SchemaLanguage lang) {
        schemaLang = Constraint.isNotNull(lang, "SchemaLanguage cannot be null");
        return this;
    }
    
    /**
     * Set the resource resolver to use for included/imported schemas.
     * 
     * <p>If not set, the default resolver will prevent lookup of any
     * schemas not explicitly added.</p>
     * 
     * @param resolver resource resolver
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder setResourceResolver(@Nullable final LSResourceResolver resolver) {
        resourceResolver = resolver;
        return this;
    }
    
    /**
     * Set a custom error handler to use.
     * 
     * <p>If not set, a default handler will be used that logs errors before throwing exceptions.</p>
     * 
     * @param handler   error handler
     * @return this builder
     */
    @Nonnull public SchemaBuilder setErrorHandler(@Nullable final ErrorHandler handler) {
        errorHandler = handler;
        return this;
    }
    
    /**
     * Set a feature for the {@link SchemaFactory} to be built.
     * 
     * @param name The feature name, which is a non-null fully-qualified URI.
     * @param value The requested value of the feature (true or false).
     *
     * @see SchemaFactory#setFeature(String, boolean)
     */
    public void setFeature(@Nonnull @NotEmpty final String name, final boolean value) {
        features.put(name, value);
    }

    /**
     * Set the value of a property for the {@link SchemaFactory} to be built.
     * 
     * @param name The property name, which is a non-null fully-qualified URI.
     * @param object The requested value for the property.
     * 
     * @see SchemaFactory#setProperty(String, Object)
     */
    public void setProperty(@Nonnull @NotEmpty final String name, @Nullable final Object object) {
        properties.put(name, object);
    }
    
    /**
     * Clear the schemas to be included.
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder resetSchemas() {
        sources.clear();
        return this;
    }
    
    /**
     * Set the schemas to load from the given schema sources (replaces any previously added).
     * 
     * <p>If the caller wishes to ensure the schemas are loaded in a particular order,
     * the {@link Collection} implementation provided must be one that preserves order.
     * The method will add the sources in the order returned by the collection.</p>
     * 
     * @param schemaSources schema sources
     */
    public void setSchemas(@Nonnull @NullableElements final Collection<Source> schemaSources) {
        Constraint.isNotNull(schemaSources, "Schema source file paths cannot be null");

        resetSchemas();
        for (final Source schemaSource : schemaSources) {
            if (schemaSource != null) {
                addSchema(schemaSource);
            }
        }
    }
    
    /**
     * Set the schemas to load from the given schema resources (replaces any previously added).
     * 
     * <p>If the caller wishes to ensure the schemas are loaded in a particular order,
     * the {@link Collection} implementation provided must be one that preserves order.
     * The method will add the sources in the order returned by the collection.</p>
     * 
     * @param schemaResources schema resources
     */
    public void setSchemaResources(@Nonnull @NullableElements final Collection<Resource> schemaResources) {
        Constraint.isNotNull(schemaResources, "Schema resources cannot be null");

        resetSchemas();
        for (final Resource schemaResource : schemaResources) {
            if (schemaResource != null) {
                addSchema(schemaResource);
            }
        }
    }

    /**
     * Add schemas from the given schema input streams.
     * 
     * @param schemaSource schema input stream
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder addSchema(@Nonnull final InputStream schemaSource) {
        Constraint.isNotNull(schemaSource, "Schema source input stream cannot be null");

        addSchema(new StreamSource(schemaSource));

        return this;
    }
    
    /**
     * Add schemas from the given schema sources.
     * 
     * @param schemaSource schema source
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder addSchema(@Nonnull final Source schemaSource) {
        Constraint.isNotNull(schemaSource, "Schema source inputstreams can not be null");

        sources.add(schemaSource);

        return this;
    }
    
    /**
     * Add schemas from the given schema resource.
     * 
     * @param resource schema input resource
     * 
     * @return this builder
     */
    @Nonnull public SchemaBuilder addSchema(@Nonnull final Resource resource) {
        Constraint.isNotNull(resource, "Schema resource cannot be null");

        try {
            addSchema(resource.getInputStream());
        } catch (final IOException e) {
            log.error("IO error adding schema from resource: {}", resource.getDescription(), e);
        }

        return this;
    }

    /**
     * Build a schema from the given schema sources.
     * 
     * <p>This method is thread-safe, although the various mutating methods to establish the state of
     * the object are not.</p>
     * 
     * @return the constructed schema
     * @throws SAXException thrown if there is a problem converting the schema sources into a schema
     */
    @Nonnull public synchronized Schema buildSchema() throws SAXException {
        if (alreadyBuilt) {
            throw new IllegalStateException("Schema already built, cannot build a second time");
        }
        
        Constraint.isNotEmpty(sources, "No schema sources specified");

        final SchemaFactory schemaFactory = schemaLang.getSchemaFactory();
        
        if (features.isEmpty()) {
            log.debug("No SchemaFactory features set, setting FEATURE_SECURE_PROCESSING by default");
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            if (resourceResolver != null && !(resourceResolver instanceof ClasspathResolver)) {
                log.warn("Custom LSResourceResolver supplied, may interact badly with secure processing mode");
            } else {
                try {
                    schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "all");
                    if (resourceResolver == null) {
                        log.info("Allowing schema and DTD access to non-remote resources (LSResourceResolver unset)");
                    } else {
                        log.debug("Allowing schema and DTD access to non-remote resources (ClasspathResolver set)");
                    }
                } catch (final SAXException e) {
                    log.info("Unable to set ACCESS_EXTERNAL_SCHEMA property, classpath-based schema lookup might fail");
                }
            }
        } else {
            for (final Map.Entry<String, Boolean> entry : features.entrySet()) {
                schemaFactory.setFeature(entry.getKey(), entry.getValue());
            }
        }

        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            schemaFactory.setProperty(entry.getKey(), entry.getValue());
        }
        
        schemaFactory.setErrorHandler(errorHandler);
        if (resourceResolver != null) {
            schemaFactory.setResourceResolver(resourceResolver);
        }
        alreadyBuilt = true;
        return schemaFactory.newSchema(sources.toArray(new Source[sources.size()]));
    }

}