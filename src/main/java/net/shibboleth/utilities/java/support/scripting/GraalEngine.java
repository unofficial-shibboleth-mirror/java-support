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

import java.io.Reader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * A subset of {@link ScriptEngine} implemented with GraaVm with enough function that 
 * {@link EvaluableScript} can work with it. 
 * 
 * NOTE that this does not (currently) implement {@link javax.script.Compilable}.
 */
public class GraalEngine extends AbstractScriptEngine implements ScriptEngine, Compilable {

    /** {@inheritDoc} */
    public Object eval(final String script, final Bindings scriptBindings) throws ScriptException {
        try (final Context context = Context.newBuilder().allowExperimentalOptions(true).
                option("js.nashorn-compat", "true").
                allowAllAccess(true).
                build()) {
            
            final Value bindings  = context.getBindings("js");
            for (final String name: scriptBindings.keySet()) {
                bindings.putMember(name, scriptBindings.get(name));
            }
            final Source s = Source.newBuilder("js", script, "embedded").buildLiteral();
            final Value v = context.eval(s);
            return processValue(v);
        }  catch (final RuntimeException e) {
            throw new ScriptException(e);
        }
    }

    /** {@inheritDoc} */
    public CompiledScript compile(final String script) throws ScriptException {

        return new GraalVMCompiledScript(script);
    }

    /** Convert the output from {@link Context#eval(String, CharSequence)} or
     * {@link Context#eval(String, CharSequence)} into a java {@link Object}.
     * @param value what to consider
     * @return The output
     */
    @Nullable protected static Object processValue(@Nonnull final Value value) {
        if (value.isNull()) {
            return null;
        } else if (value.isHostObject()) {
            return value.asHostObject();
        } else if (value.isProxyObject()) {
            return value.asProxyObject();
        } else {
            return  value.as(Object.class);
        }
    }

    private class GraalVMCompiledScript extends CompiledScript {
        
        /** The compiled source. */
        private final Source source;

        /**
         * Constructor.
         *
         * @param script the script (as text)
         */
        protected GraalVMCompiledScript(final String script) {
            source = Source.newBuilder("js", script, "embedded").buildLiteral();
        }

        /** {@inheritDoc} */
        public ScriptEngine getEngine() {
            return GraalEngine.this;
        }

        /** {@inheritDoc} */
        public Object eval(final ScriptContext scriptContext) throws ScriptException {

            try (final Context context = Context.newBuilder().allowExperimentalOptions(true).
                    option("js.nashorn-compat", "true").
                    allowAllAccess(true).
                    build()) {

                final Bindings scriptBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
                final Value bindings  = context.getBindings("js");
                for (final String name: scriptBindings.keySet()) {
                    bindings.putMember(name, scriptBindings.get(name));
                }
                final Value v = context.eval(source);
                return processValue(v);
            }  catch (final RuntimeException e) {
                throw new ScriptException(e);
            }
        }
    }

    /** {@inheritDoc} */
    public CompiledScript compile(final Reader script) throws ScriptException {
       throw new ScriptException("Cannot compile from a reader");
    }
}
