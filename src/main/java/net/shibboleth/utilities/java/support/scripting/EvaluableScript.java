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
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resource.Resource;

import com.google.common.io.Files;

/** This is a helper class that takes care of reading in, optionally compiling, and evaluating a script. */
public class EvaluableScript {

    /** The scripting language. */
    @Nonnull @NotEmpty private final String scriptLanguage;

    /** The script to execute. */
    @Nonnull @NotEmpty private final String script;

    /** The script engine to execute the script. */
    @Nullable private ScriptEngine scriptEngine;

    /** The compiled form of the script, if the script engine supports compiling. */
    @Nullable private CompiledScript compiledScript;

    /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the scripting engine supports compilation and the script does not compile
     */
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        scriptLanguage =
                Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                        "Scripting language can not be null or empty");
        script = Constraint.isNotNull(StringSupport.trimOrNull(scriptSource), "Script source can not be null or empty");

        initialize();
    }

    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the scripting engine supports compilation and the script does not compile
     */
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }

    /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     *             
     * @since 8.0.0
     */
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final Resource scriptSource)
            throws ScriptException {
        scriptLanguage = Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                "Scripting language can not be null or empty");
        
        try (final InputStream in =
                Constraint.isNotNull(scriptSource, "Script source can not be null or empty").getInputStream()) {
            script = StringSupport.inputStreamToString(in, null);
        } catch (final IOException e) {
            throw new ScriptException(e);
        }

        initialize();
    }

    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     *             
     * @since 8.0.0
     */
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull final Resource scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }
    
    /**
     * Constructor. The provided stream is <strong>not</strong> closed.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     */
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final InputStream scriptSource)
            throws ScriptException {
        scriptLanguage =
                Constraint.isNotNull(StringSupport.trimOrNull(engineName),
                        "Scripting language can not be null or empty");
        try {
            script = StringSupport.inputStreamToString(
                            Constraint.isNotNull(scriptSource, "Script source can not be null or empty"), null);
        } catch (final IOException e) {
            throw new ScriptException(e);
        }

        initialize();
    }
    
    /**
     * Constructor. The provided stream is <strong>not</strong> closed.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     * 
     * @since 8.0.0
     */
    public EvaluableScript(@ParameterName(name="scriptSource") @Nonnull final InputStream scriptSource)
            throws ScriptException {
        this("javascript", scriptSource);
    }

    /**
     * Constructor.
     * 
     * @param engineName the JSR-223 scripting engine name
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     */
    public EvaluableScript(@ParameterName(name="engineName") @Nonnull @NotEmpty final String engineName,
            @ParameterName(name="scriptSource") @Nonnull final File scriptSource)
            throws ScriptException {
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

        initialize();
    }
    
    /**
     * Constructor.
     * 
     * @param scriptSource the script source
     * 
     * @throws ScriptException thrown if the script source file can not be read or the scripting engine supports
     *             compilation and the script does not compile
     *             
     * @since 8.0.0
     */
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
     * Gets the script language.
     * 
     * @return the script source
     */
    @Nonnull @NotEmpty public String getScriptLanguage() {
        return scriptLanguage;
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
        if (compiledScript != null) {
            return compiledScript.eval(scriptContext);
        }
        return scriptEngine.eval(script, scriptContext);
    }

    /**
     * Initializes the scripting engine and compiles the script, if possible.
     * 
     * @throws ScriptException thrown if the scripting engine supports compilation and the script does not compile
     */
    private void initialize() throws ScriptException {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        scriptEngine = engineManager.getEngineByName(scriptLanguage);
        Constraint.isNotNull(scriptEngine, "No scripting engine associated with scripting language " + scriptLanguage);

        if (scriptEngine instanceof Compilable) {
            compiledScript = ((Compilable) scriptEngine).compile(script);
        } else {
            compiledScript = null;
        }
    }
    
}