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
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.scripting.AbstractScriptEvaluator;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link BiConsumer} which calls out to a supplied script.
 *
 * @param <T> first input type
 * @param <U> second input type
 * @since 8.2.0
 */
public class ScriptedBiConsumer<T,U> extends AbstractScriptEvaluator implements BiConsumer<T,U> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedBiConsumer.class);

    /** Input types. */
    @Nullable private Pair<Class<T>,Class<U>> inputTypes;

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedBiConsumer(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript,
            @Nullable @NotEmpty @ParameterName(name="extraInfo") final String extraInfo) {
        super(theScript);
        setLogPrefix("Scripted BiConsumer from " + extraInfo + ":");
    }

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     */
    protected ScriptedBiConsumer(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript) {
        super(theScript);
        setLogPrefix("Anonymous BiConsumer:");
    }

    /**
     * Get the input type to be enforced.
     *
     * @return input type
     */
    @Nullable public Pair<Class<T>,Class<U>> getInputTypes() {
        return inputTypes;
    }

    /**
     * Set the input types to be enforced.
     *
     * @param types the input types
     */
    public void setInputTypes(@Nullable final Pair<Class<T>,Class<U>> types) {
        if (types != null && types.getFirst() != null && types.getSecond() != null) {
            inputTypes = types;
        } else {
            inputTypes = null;
        }
    }

    /** {@inheritDoc} */
    public void accept(@Nullable final T first, @Nullable final U second) {
        
        final Pair<Class<T>,Class<U>> types = getInputTypes();
        if (null != types) {
            if (null != first && !types.getFirst().isInstance(first)) {
                log.error("{} Input of type {} was not of type {}", getLogPrefix(), first.getClass(), types.getFirst());
                return;
            }
            if (null != second && !types.getSecond().isInstance(second)) {
                log.error("{} Input of type {} was not of type {}", getLogPrefix(), second.getClass(),
                        types.getSecond());
                return;
            }
        }

        evaluate(first, second);
    }

    /** {@inheritDoc} */
    @Override
    protected void prepareContext(@Nonnull final ScriptContext scriptContext, @Nullable final Object... input) {
        scriptContext.setAttribute("input1", input[0], ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("input2", input[1], ScriptContext.ENGINE_SCOPE);
    }

    /**
     * Factory to create {@link ScriptedBiConsumer} from a {@link Resource}.
     *
     * @param <T> first input type
     * @param <U> second input type
     * @param resource the resource to look at
     * @param engineName the language
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    @SuppressWarnings("removal")
    public static <T,U> ScriptedBiConsumer<T,U> resourceScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull final Resource resource) throws ScriptException, IOException {
        try (final InputStream is = resource.getInputStream()) {
            final EvaluableScript script = new EvaluableScript();
            script.setEngineName(engineName);
            script.setScript(is);
            script.initializeWithScriptException();
            return new ScriptedBiConsumer<>(script, resource.getDescription());
        }
    }

    /**
     * Factory to create {@link ScriptedBiConsumer} from a {@link Resource}.
     *
     * @param <T> first input type
     * @param <U> second input type
     * @param resource the resource to look at
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    public static <T,U> ScriptedBiConsumer<T,U> resourceScript(final Resource resource)
            throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedBiConsumer} from inline data.
     *
     * @param <T> first input type
     * @param <U> second input type
     * @param scriptSource the script, as a string
     * @param engineName the language
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     */
    @SuppressWarnings("removal")
    public static <T,U> ScriptedBiConsumer<T,U> inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript();
        script.setEngineName(engineName);
        script.setScript(scriptSource);
        script.initializeWithScriptException();
        return new ScriptedBiConsumer<>(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedBiConsumer} from inline data.
     *
     * @param <T> first input type
     * @param <U> second input type
     * @param scriptSource the script, as a string
     * 
     * @return the function
     * 
     * @throws ScriptException if the compile fails
     */
    @SuppressWarnings("removal")
    public static <T,U> ScriptedBiConsumer<T,U> inlineScript(@Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        final EvaluableScript script = new EvaluableScript();
        script.setScript(scriptSource);
        script.initializeWithScriptException();
        return new ScriptedBiConsumer<>(script, "Inline");
    }
}