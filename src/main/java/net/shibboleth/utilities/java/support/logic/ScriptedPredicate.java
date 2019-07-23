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
 * A {@link Predicate} which calls out to a supplied script.
 * 
 * @param <T> input type
 * 
 * @since 7.4.0
 */
public class ScriptedPredicate<T> extends AbstractScriptEvaluator implements Predicate<T> {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedPredicate.class);
    
    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedPredicate(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript,
            @Nullable @NotEmpty @ParameterName(name="extraInfo") final String extraInfo) {
        super(theScript);
        setOutputType(Boolean.class);
        setReturnOnError(false);
        setLogPrefix("Scripted Predicate from " + extraInfo + ":");
    }

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     */
    protected ScriptedPredicate(@Nonnull @NotEmpty @ParameterName(name="theScript") final EvaluableScript theScript) {
        super(theScript);
        setLogPrefix("Anonymous Scripted Predicate:");
        setOutputType(Boolean.class);
        setReturnOnError(false);
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
    public boolean test(@Nullable final T input) {
        
        final Object result = evaluate(input);
        return (boolean) (result != null ? result : getReturnOnError());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void prepareContext(@Nonnull final ScriptContext scriptContext, @Nullable final Object... input) {
        scriptContext.setAttribute("input", input[0], ScriptContext.ENGINE_SCOPE);
    }
    
    /**
     * Factory to create {@link ScriptedPredicate} from a {@link Resource}.
     * 
     * @param <T> input type
     * @param resource the resource to look at
     * @param engineName the language
     * 
     * @return the predicate
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static <T> ScriptedPredicate<T> resourceScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull final Resource resource) throws ScriptException, IOException {
        try (final InputStream is = resource.getInputStream()) {
            final EvaluableScript script = new EvaluableScript(engineName, is);
            return new ScriptedPredicate<>(script, resource.getDescription());
        }
    }

    /**
     * Factory to create {@link ScriptedPredicate} from a {@link Resource}.
     * 
     * @param <T> input type
     * @param resource the resource to look at
     * 
     * @return the predicate
     * 
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static <T> ScriptedPredicate<T> resourceScript(final Resource resource) throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedPredicate} from inline data.
     *
     * @param <T> input type
     * @param scriptSource the script, as a string
     * @param engineName the language
     * 
     * @return the predicate
     * 
     * @throws ScriptException if the compile fails
     */
    static <T> ScriptedPredicate<T> inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedPredicate<>(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedPredicate} from inline data.
     *
     * @param <T> input type
     * @param scriptSource the script, as a string
     * 
     * @return the predicate
     * 
     * @throws ScriptException if the compile fails
     */
    static <T> ScriptedPredicate<T> inlineScript(@Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedPredicate<>(script, "Inline");
    }

}