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

package net.shibboleth.utilities.java.support.logic;


import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.scripting.AbstractScriptEvaluator;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Function} which calls out to a supplied script.
 *
 * @param <T> input type
 * @param <U> output type
 * @since 7.4.0
 */
public class ScriptedFunction<T, U> extends AbstractScriptEvaluator implements Function<T,U> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedFunction.class);

    /** Input Type.*/
    @Nullable private Class<T> inputTypeClass;

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedFunction(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript,
            @Nullable @NotEmpty @ParameterName(name="extraInfo") final String extraInfo) {
        super(theScript);
        setLogPrefix("Scripted Function from " + extraInfo + ":");
    }

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     */
    protected ScriptedFunction(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript) {
        super(theScript);
        setLogPrefix("Anonymous Function:");
    }

    /**
     * Set the output type to be enforced.
     *
     * @param type output type
     */
    @Override public void setOutputType(@Nullable final Class<?> type) {
        super.setOutputType(type);
    }

    /**
     * Get the input type to be enforced.
     *
     * @return input type
     */
    @Nullable public  Class<T> getInputType() {
        return inputTypeClass;
    }

    /**
     * Set the input type to be enforced.
     *
     * @param type input type
     */
    public void setInputType(@Nullable final Class<T> type) {
        inputTypeClass = type;
    }

    /**
     * Set value to return if an error occurs.
     *
     * @param value value to return
     */
    @Override public void setReturnOnError(@Nullable final Object value) {
        super.setReturnOnError(value);
    }

    /** {@inheritDoc} */
    public U apply(@Nullable final T input) {

        if (null != getInputType() && null != input && !getInputType().isInstance(input)) {
            log.error("{} Input of type {} was not of type {}", getLogPrefix(), input.getClass(),
                    getInputType());
            return (U) getReturnOnError();
        }

        return (U) evaluate(input);
    }

    /** {@inheritDoc} */
    @Override
    protected void prepareContext(@Nonnull final ScriptContext scriptContext, @Nullable final Object... input) {
        scriptContext.setAttribute("input", input[0], ScriptContext.ENGINE_SCOPE);
    }

    /**
     * Factory to create {@link ScriptedFunction} from a {@link Resource}.
     *
     * @param <T> input type
     * @param <U> output type
     * @param resource the resource to look at
     * @param engineName the language
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static <T,U> ScriptedFunction<T,U> resourceScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull final Resource resource) throws ScriptException, IOException {
        try (final InputStream is = resource.getInputStream()) {
            final EvaluableScript script = new EvaluableScript(engineName, is);
            return new ScriptedFunction<>(script, resource.getDescription());
        }
    }

    /**
     * Factory to create {@link ScriptedFunction} from a {@link Resource}.
     *
     * @param <T> input type
     * @param <U> output type
     * @param resource the resource to look at
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static <T,U> ScriptedFunction<T,U> resourceScript(final Resource resource) throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedFunction} from inline data.
     *
     * @param <T> input type
     * @param <U> output type
     * @param scriptSource the script, as a string
     * @param engineName the language
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     */
    static <T,U> ScriptedFunction<T,U> inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedFunction<>(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedFunction} from inline data.
     *
     * @param <T> input type
     * @param <U> output type
     * @param scriptSource the script, as a string
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     */
    static <T,U> ScriptedFunction<T,U> inlineScript(@Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedFunction<>(script, "Inline");
    }
}