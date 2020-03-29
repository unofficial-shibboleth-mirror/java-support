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

package net.shibboleth.utilities.java.support.scripting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.utilities.java.support.resource.Resource;

import com.google.common.io.Files;

/** This is a helper class that takes care of reading in, optionally compiling, and evaluating a script. */
public final class EvaluableScript extends AbstractInitializableComponent {

    /** The scripting language. */
    @Nonnull @NotEmpty private String scriptLanguage = "javascript";

    /** The script to execute. */
    @NonnullAfterInit @NotEmpty private String script;

    /** The script engine to execute the script. */
    @Nullable private ScriptEngine scriptEngine;

    /** The compiled form of the script, if the script engine supports compiling. */
    @Nullable private CompiledScript compiledScript;

    /**
     * Constructor.
     */
    public EvaluableScript() {
    }

   /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * @deprecated in 8.1
     * @throws ScriptException thrown if the scripting engine supports compilation and the script does not compile
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "EvaluableScript(parameters...)",
                null, "by using the setters");
        scriptLanguage =
                Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                        "Scripting language can not be null or empty");
        script = Constraint.isNotNull(StringSupport.trimOrNull(scriptSource), "Script source can not be null or empty");

        initializeWithScriptException();
    }

    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * @deprecated in 8.1
     * @throws ScriptException thrown if the scripting engine supports compilation and the script does not compile
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }

    /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * @deprecated in 8.1
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     *             
     * @since 8.0.0
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final Resource scriptSource)
            throws ScriptException {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "EvaluableScript(parameters...)",
                null, "by using the setters");
        scriptLanguage = Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                "Scripting language can not be null or empty");
        
        try (final InputStream in =
                Constraint.isNotNull(scriptSource, "Script source can not be null or empty").getInputStream()) {
            script = StringSupport.inputStreamToString(in, null);
        } catch (final IOException e) {
            throw new ScriptException(e);
        }

        initializeWithScriptException();
    }

    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     * @deprecated in 8.1
     * @since 8.0.0
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull final Resource scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }
    
    /**
     * Constructor. The provided stream is <strong>not</strong> closed.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * @deprecated in 8.1
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final InputStream scriptSource)
            throws ScriptException {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "EvaluableScript(parameters...)",
                null, "by using the setters");
        scriptLanguage =
                Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                        "Scripting language can not be null or empty");
        try {
            script = StringSupport.inputStreamToString(
                            Constraint.isNotNull(scriptSource, "Script source can not be null or empty"), null);
        } catch (final IOException e) {
            throw new ScriptException(e);
        }

        initializeWithScriptException();
    }
    
    /**
     * Constructor. The provided stream is <strong>not</strong> closed.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     * @deprecated in 8.1
     * @since 8.0.0
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull final InputStream scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }

    /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * @deprecated in 8.1
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final File scriptSource)
            throws ScriptException {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "EvaluableScript(parameters...)",
                null, "by using the setters");
        scriptLanguage =
                Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                        "Scripting language can not be null or empty");

        Constraint.isNotNull(scriptSource, "Script source file can not be null");

        if (!scriptSource.exists()) {
            throw new ScriptException("Script source file " + scriptSource.getAbsolutePath() + " does not exist");
        }

        if (!scriptSource.canRead()) {
            throw new ScriptException("Script source file " + scriptSource.getAbsolutePath()
                    + " exists but is not readable");
        }

        try {
            script =
                    Constraint.isNotNull(
                            StringSupport.trimOrNull(Files.asCharSource(scriptSource, Charset.defaultCharset()).read()),
                            "Script source cannot be empty");
        } catch (final IOException e) {
            throw new ScriptException("Unable to read data from source file " + scriptSource.getAbsolutePath());
        }

        initializeWithScriptException();
    }
    
    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     * @deprecated in 8.1
     * @since 8.0.0
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull final File scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }
    
    /**
     * Gets the script source.
     * 
     * @return the script source
     */
    @Nonnull @NotEmpty public String getScript() {
        return script;
    }

    /**
     * Sets the script source.
     *
     * @param what the script source
     */
    @Nonnull @NotEmpty public void setScript(@Nonnull @NotEmpty final String what) {
        script = Constraint.isNotNull(StringSupport.trimOrNull(what), "Script must not be null");
        if ("".equals(script)) {
            throw new ConstraintViolationException("Script must be non-empty");
        }
    }

    /**
     * Sets the script source.
     *
     * @param scriptSource how to get the script source
     * @throws IOException if there were issues reading the script
     */
    @Nonnull @NotEmpty public void setScript(@Nonnull final InputStream scriptSource) throws IOException {

        Constraint.isNotNull(scriptSource, "Script source should not be null");

        script = StringSupport.inputStreamToString(
                Constraint.isNotNull(scriptSource, "Script source can not be null or empty"), null);
    }

    /**
     * Sets the script source.
     * 
     * @param scriptSource how to get the script source
     * @throws IOException if there were issues reading the script
     */
    @Nonnull @NotEmpty public void setScript(@Nonnull final File scriptSource) throws IOException {

        Constraint.isNotNull(scriptSource, "Script source should not be null");

        if (!scriptSource.exists()) {
            throw new IOException("Script source file " + scriptSource.getAbsolutePath() + " does not exist");
        }

        if (!scriptSource.canRead()) {
            throw new IOException("Script source file " + scriptSource.getAbsolutePath()
                    + " exists but is not readable");
        }

        script = Constraint.isNotNull(
                        StringSupport.trimOrNull(Files.asCharSource(scriptSource, Charset.defaultCharset()).read()),
                        "Script source cannot be empty");
    }

    /**
     * Sets the script source.
     *
     * @param scriptSource how to get the script source
     * @throws IOException if there were issues reading the script
     */
    @Nonnull @NotEmpty public void setScript(@Nonnull final Resource scriptSource) throws IOException {

        Constraint.isNotNull(scriptSource, "Script source should not be null");

        setScript(Constraint.isNotNull(scriptSource, "Script source can not be null or empty").getInputStream());
    }

    /**
     * Gets the script language.
     *
     * @return the script language
     */
    @Nonnull @NotEmpty public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * Sets the script language.
     *
     * @param what the script language
     */
    @Nonnull @NotEmpty public void setEngineName(@Nonnull @NotEmpty final String what) {
        scriptLanguage = Constraint.isNotNull(StringSupport.trimOrNull(what),
                "Language must not be null");
    }

    /**
     * Evaluates this script against the given bindings.
     * 
     * @param scriptBindings the script bindings
     * 
     * @return the result of the script or null if the script did not return a result
     * 
     * @throws ScriptException thrown if there was a problem evaluating the script
     */
    @Nullable public Object eval(@Nonnull final Bindings scriptBindings) throws ScriptException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        if (compiledScript != null) {
            return compiledScript.eval(scriptBindings);
        }
        return scriptEngine.eval(script, scriptBindings);
    }

    /**
     * Evaluates this script against the given context.
     * 
     * @param scriptContext the script context
     * 
     * @return the result of the script or null if the script did not return a result
     * 
     * @throws ScriptException thrown if there was a problem evaluating the script
     */
    @Nullable public Object eval(@Nonnull final ScriptContext scriptContext) throws ScriptException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        if (compiledScript != null) {
            return compiledScript.eval(scriptContext);
        }
        return scriptEngine.eval(script, scriptContext);
    }

    /** {@inheritDoc}
     * Initializes the scripting engine and compiles the script, if possible.
     *
     * @throws ComponentInitializationException if the scripting engine supports 
     * compilation and the script does not compile
     */
    protected void doInitialize() throws ComponentInitializationException {

        if ("".equals(scriptLanguage)) {
            throw new ComponentInitializationException("Language must be non-empty");
        }

        if ("".equals(script)) {
            throw new ComponentInitializationException("Sanguage must be non-empty");
        }

        final ScriptEngineManager engineManager = new ScriptEngineManager();
        scriptEngine = engineManager.getEngineByName(scriptLanguage);
        Constraint.isNotNull(scriptEngine, "No scripting engine associated with scripting language " + scriptLanguage);

        if (scriptEngine instanceof Compilable) {
            try {
                compiledScript = ((Compilable) scriptEngine).compile(script);
            } catch (final ScriptException e) {
                throw new ComponentInitializationException(e);
            }
        } else {
            compiledScript = null;
        }
    }

    /**
     * Internal method to wrap {@link #initialize()}.  This allows backwards compatibility with
     * respect to the exception handling.
     * 
     * We extract the cause from the Component Initialization and if it is a {@link ScriptException}
     * throw that, otherwise we throw a new one which encapsulates the exception.
     * 
     * Deprecation note.  In most non-test cases the was to resolve this deprecation is to
     * remove the method call (since most use is in bean generation and the initialize will be
     * called).  In every other case the answer is to use {@link #initialize()} and change the callers
     * signature. Or just remove the whole thing.
     *
     * @throws ScriptException if there is a compilation issue.
     * @deprecated Remove in V9.0.0 
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public void initializeWithScriptException() throws ScriptException {

        try {
            initialize();
        } catch (final ComponentInitializationException e) {
            final Throwable cause = e.getCause();

            if (cause != null && cause instanceof ScriptException) {
                throw (ScriptException) cause;
            }
            throw new ScriptException(e);
        }
    }
}
