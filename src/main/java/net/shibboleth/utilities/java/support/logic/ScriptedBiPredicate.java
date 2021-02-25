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
import java.util.function.BiPredicate;

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
 * A {@link BiPredicate} which calls out to a supplied script.
 *
 * @param <T> first input type
 * @param <U> second input type
 * @since 8.2.0
 */
public class ScriptedBiPredicate<T,U> extends AbstractScriptEvaluator implements BiPredicate<T,U> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedBiPredicate.class);

    /** Input type 1. */
    @Nullable private Class<T> inputTypeClass1;

    /** Input type 2. */
    @Nullable private Class<U> inputTypeClass2;

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedBiPredicate(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript,
            @Nullable @NotEmpty @ParameterName(name="extraInfo") final String extraInfo) {
        super(theScript);
        setOutputType(Boolean.class);
        setReturnOnError(false);
        setLogPrefix("Scripted BiPredicate from " + extraInfo + ":");
    }

    /**
     * Constructor.
     *
     * @param theScript the script we will evaluate.
     */
    protected ScriptedBiPredicate(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript) {
        super(theScript);
        setOutputType(Boolean.class);
        setReturnOnError(false);
        setLogPrefix("Anonymous BiPredicate:");
    }

    /**
     * Get the input type to be enforced.
     *
     * @return input type
     */
    @Nullable public Pair<Class<T>,Class<U>> getInputTypes() {
        return new Pair<>(inputTypeClass1, inputTypeClass2);
    }

    /**
     * Set the input type to be enforced.
     *
     * @param type1 first input type
     * @param type2 second input type
     */
    public void setInputTypes(@Nullable final Class<T> type1, @Nullable final Class<U> type2) {
        inputTypeClass1 = type1;
        inputTypeClass2 = type2;
    }

    /**
     * Set value to return if an error occurs.
     * 
     * @param flag value to return
     */
    public void setReturnOnError(final boolean flag) {
        setReturnOnError(Boolean.valueOf(flag));
    }

    /** {@inheritDoc} */
    public boolean test(@Nullable final T first, @Nullable final U second) {
        
        final Pair<Class<T>,Class<U>> types = getInputTypes();
        if (null != types) {
            if (null != first && !types.getFirst().isInstance(first)) {
                log.error("{} Input of type {} was not of type {}", getLogPrefix(), first.getClass(), types.getFirst());
                return (boolean) getReturnOnError();
            }
            if (null != second && !types.getSecond().isInstance(second)) {
                log.error("{} Input of type {} was not of type {}", getLogPrefix(), second.getClass(),
                        types.getSecond());
                return (boolean) getReturnOnError();
            }
        }

        final Object result = evaluate(first, second);
        return (boolean) (result != null ? result : getReturnOnError());
    }

    /** {@inheritDoc} */
    @Override
    protected void prepareContext(@Nonnull final ScriptContext scriptContext, @Nullable final Object... input) {
        scriptContext.setAttribute("input1", input[0], ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("input2", input[1], ScriptContext.ENGINE_SCOPE);
    }

    /**
     * Factory to create {@link ScriptedBiPredicate} from a {@link Resource}.
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
    public static <T,U> ScriptedBiPredicate<T,U> resourceScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull final Resource resource) throws ScriptException, IOException {
        try (final InputStream is = resource.getInputStream()) {
            final EvaluableScript script = new EvaluableScript();
            script.setEngineName(engineName);
            script.setScript(is);
            script.initializeWithScriptException();
            return new ScriptedBiPredicate<>(script, resource.getDescription());
        }
    }

    /**
     * Factory to create {@link ScriptedBiPredicate} from a {@link Resource}.
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
    public static <T,U> ScriptedBiPredicate<T,U> resourceScript(final Resource resource)
            throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedBiPredicate} from inline data.
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
    public static <T,U> ScriptedBiPredicate<T,U> inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript();
        script.setEngineName(engineName);
        script.setScript(scriptSource);
        script.initializeWithScriptException();
        return new ScriptedBiPredicate<>(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedBiPredicate} from inline data.
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
    public static <T,U> ScriptedBiPredicate<T,U> inlineScript(@Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        final EvaluableScript script = new EvaluableScript();
        script.setScript(scriptSource);
        script.initializeWithScriptException();
        return new ScriptedBiPredicate<>(script, "Inline");
    }
}