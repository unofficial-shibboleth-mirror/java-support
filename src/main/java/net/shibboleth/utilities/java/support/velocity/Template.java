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

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;

/**
 * This is a helper class that wraps a velocity engine and template information into a single object. It provides
 * methods, {@link #fromTemplate(VelocityEngine, String)} and {@link #fromTemplate(VelocityEngine, String, Charset)},
 * for evaluating literal templates and {@link #fromTemplateName(VelocityEngine, String)} and
 * {@link #fromTemplateName(VelocityEngine, String, Charset)} for evaluating templates referenced by name. It also
 * ensures that the given {@link VelocityEngine} is configured in such a way as to be able to use the literal or named
 * template. Note, this check occurs only at {@link Template} construction time so, if you're loading a named template
 * from a file and that file disappears before calling {@link #merge(Context)} or {@link #merge(Context, Writer)} you'll
 * still end up getting a {@link org.apache.velocity.exception.ResourceNotFoundException}.
 * 
 * Many methods throw an {@link org.apache.velocity.exception.VelocityException} to report template or argument errors,
 * which is an unchecked exception type.
 */
public final class Template {

    /** The {@link VelocityEngine} used when evaluating the template. */
    @Nonnull private final VelocityEngine engine;

    /** The name of the template to be evaluated. */
    @Nonnull @NotEmpty private final String templateName;

    /** The character encoding of the template. */
    @Nonnull @NotEmpty private final String templateEncoding;

    /**
     * Constructor.
     * 
     * @param velocityEngine engine used to evaluate the template
     * @param velocityTemplateName name of the template to be evaluated
     * @param velocityTemplateEncoding encoding used by the template
     */
    private Template(@Nonnull final VelocityEngine velocityEngine, @Nonnull @NotEmpty final String velocityTemplateName,
            @Nonnull final String velocityTemplateEncoding) {
        engine = Constraint.isNotNull(velocityEngine, "Velocity engine can not be null");
        templateName =
                Constraint.isNotNull(StringSupport.trimOrNull(velocityTemplateName),
                        "Velocity template name can not be null or empty");
        templateEncoding =
                Constraint.isNotNull(StringSupport.trimOrNull(velocityTemplateEncoding),
                        "Velocity template encoding name can not be null or empty");
    }

    /**
     * A convenience method that invoked {@link #fromTemplate(VelocityEngine, String, Charset)} and assumes the given
     * template is US ASCII encoded.
     * 
     * <p>
     * See {@link #fromTemplate(VelocityEngine, String, Charset)} for full details.
     * </p>
     * 
     * @param engine engine that will be used to evaluate the template
     * @param template the literal Velocity template, <strong>NOT</strong> a template name see
     *            {@link #fromTemplateName(VelocityEngine, String)} and
     *            {@link #fromTemplateName(VelocityEngine, String, Charset)} for that
     * 
     * @return an instance of this class that can be used to evaluate the given template using the given engine
     */
    @Nonnull public static Template fromTemplate(@Nonnull final VelocityEngine engine,
            @Nonnull @NotEmpty final String template) {
        return fromTemplate(engine, template, Charsets.US_ASCII);
    }

    /**
     * Constructs a {@link Template} from a given template. This template is loaded in to the singleton
     * {@link StringResourceRepository} used by the {@link StringResourceLoader} under a randomly generated ID.
     * Therefore, calling this method multiple times with the same template will result in multiple instances of the
     * template string being loaded in to the {@link StringResourceRepository} (each under its own unique ID).
     * 
     * <p>
     * <strong>NOTE</strong>, in oder for subsequent calls to {@link #merge(Context)} or {@link #merge(Context, Writer)}
     * to be successful, the given {@link VelocityEngine} must be configured to look up templates from the
     * {@link StringResourceLoader}.
     * </p>
     * 
     * @param engine engine that will be used to evaluate the template
     * @param template the literal Velocity template, <strong>NOT</strong> a template name see
     *            {@link #fromTemplateName(VelocityEngine, String)} or
     *            {@link #fromTemplateName(VelocityEngine, String, Charset)} for that
     * @param encoding the encoding used by the template
     * 
     * @return an instance of this class that can be used to evaluate the given template using the given engine
     */
    @Nonnull public static Template fromTemplate(@Nonnull final VelocityEngine engine,
            @Nonnull @NotEmpty final String template,
            @Nonnull final Charset encoding) {
        final String trimmedTemplate =
                Constraint.isNotNull(StringSupport.trimOrNull(template), "Velocity template can not be null or empty");
        Constraint.isNotNull(encoding, "Template encoding character set can not be null");

        final StringResourceRepository templateRepo = StringResourceLoader.getRepository();

        String templateName;
        do {
            // keep generating a random name until we find one not already in use
            // in theory it should be the first one, but just in case...
            templateName = UUID.randomUUID().toString();
        } while (templateRepo.getStringResource(templateName) != null);

        templateRepo.putStringResource(templateName, trimmedTemplate, encoding.name());

        if (!engine.resourceExists(templateName)) {
            throw new VelocityException(
                    "Velocity engine is not configured to load templates from the default StringResourceRepository");
        }

        try {
            engine.getTemplate(templateName);
        } catch (final VelocityException e) {
            throw new VelocityException("The following template is not valid:\n" + trimmedTemplate, e);
        }

        return new Template(engine, templateName, encoding.name());
    }

    /**
     * A convenience method that invoked {@link #fromTemplateName(VelocityEngine, String, Charset)} and assumes the
     * named template is US ASCII encoded.
     * 
     * <p>
     * See {@link #fromTemplateName(VelocityEngine, String, Charset)} for full details.
     * </p>
     * 
     * @param engine engine that will be used to evaluate the template
     * @param templateName the name, as known to the given engine, of a velocity template
     * 
     * @return an instance of this class that can be used to evaluate the named template using the given engine
     */
    public static Template fromTemplateName(@Nonnull final VelocityEngine engine,
            @Nonnull @NotEmpty final String templateName) {
        return fromTemplateName(engine, templateName, Charsets.US_ASCII);
    }

    /**
     * Constructs a {@link Template} that evaluates a named velocity template with a using the given velocity engine.
     * 
     * @param engine the engine used to evaluate the template
     * @param name the name of the template
     * @param encoding the template encoding
     * 
     * @return an instance of this class that can be used to evaluate the named template using the given engine
     */
    public static Template fromTemplateName(@Nonnull final VelocityEngine engine, @Nonnull @NotEmpty final String name,
            @Nonnull final Charset encoding) {
        final String trimmedName =
                Constraint.isNotNull(StringSupport.trimOrNull(name), "Velocity template name can not be null or empty");
        Constraint.isNotNull(encoding, "Template encoding character set can not be null");

        if (!engine.resourceExists(name)) {
            throw new VelocityException("No template with the name " + trimmedName
                    + " is available to the velocity engine");
        }

        try {
            engine.getTemplate(trimmedName);
        } catch (final VelocityException e) {
            throw new VelocityException("Template '" + trimmedName + "' is not a valid template", e);
        }

        return new Template(engine, trimmedName, encoding.name());
    }

    /**
     * Gets the name of the template.
     * 
     * @return name of the template
     */
    @Nonnull public String getTemplateName() {
        return templateName;
    }

    /**
     * Evaluates the template using the given context and returns the result as a string.
     * 
     * @param templateContext current template context
     * 
     * @return the generated output of the template
     */
    public String merge(final Context templateContext) {
        final StringWriter output = new StringWriter();
        merge(templateContext, output);
        return output.toString();
    }

    /**
     * Evaluates the template using the given context and sends the result to a Writer.
     * 
     * @param templateContext current template context
     * @param output writer that will receive the template output
     */
    public void merge(final Context templateContext, final Writer output) {
        try {
            engine.mergeTemplate(templateName, templateEncoding, templateContext, output);
        } catch (final ResourceNotFoundException e) {
            throw new VelocityException("Velocity template " + templateName
                    + " has been removed since this object was constructed");
        } catch (final Exception e) {
            throw new VelocityException("Velocity template " + templateName + " threw an exception", e);
        }
    }

    /** {@inheritDoc} */
    @Override public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Template)) {
            return false;
        }

        final Template otherTemplate = (Template) obj;
        return engine.equals(otherTemplate.engine) && templateName.equals(otherTemplate.templateName);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hash(engine, templateName);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("templateName", templateName).toString();
    }
}