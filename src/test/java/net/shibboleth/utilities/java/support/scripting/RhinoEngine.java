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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A subset of {@link ScriptEngine} implemented using Mozilla rhino with enough function 
 * that {@link EvaluableScript} can work with it. 
 * 
 * NOTE that this does not (currently) implement {@link javax.script.Compilable}.
 */
public class RhinoEngine extends AbstractScriptEngine implements ScriptEngine, Compilable {

    /** {@inheritDoc} */
    public Object eval(final String script, final Bindings bindings) throws ScriptException {
        final Context ctx = Context.enter();
        try {
            final Scriptable scope = new ImporterTopLevel(ctx);
            
            for (final String name: bindings.keySet()) {
                final Object jsObj = Context.javaToJS(bindings.get(name), scope);
                ScriptableObject.putProperty(scope, name, jsObj);
            }
            final Object o = ctx.evaluateString(scope, script, "rhino source", 1, null);
            return Context.jsToJava(o, Object.class);
            
        } catch (final RuntimeException e) {
            throw new ScriptException(e);
        } finally {
            Context.exit();
        }
    }

    /** {@inheritDoc} */
    public CompiledScript compile(final String script) throws ScriptException {
        return new CompiledScriptImpl(script);
    }
    
    /** Rhino {@link CompiledScript}. */
    private class CompiledScriptImpl extends CompiledScript {

        /** The compiled script. */
        private final Script script;
        
        /** Constructor.
         *
         * @param source what to compile up.
         */
        public CompiledScriptImpl(final String source) {
            final Context ctx = Context.enter();
            try {
                script = ctx.compileString(source, "Script", 1, null);  
            } finally {
                Context.exit();
            }
        }

        /** {@inheritDoc} */
        public Object eval(final ScriptContext context) throws ScriptException {
            final Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
            final Context ctx = Context.enter();
            try {
                final Scriptable scope = new ImporterTopLevel(ctx);
                
                for (final String name: bindings.keySet()) {
                    final Object jsObj = Context.javaToJS(bindings.get(name), scope);
                    ScriptableObject.putProperty(scope, name, jsObj);
                }
                final Object o = script.exec(ctx, scope);
                return Context.jsToJava(o, Object.class);
            } 
            finally {
                Context.exit();
            }

        }

        /** {@inheritDoc} */
        public ScriptEngine getEngine() {
            return RhinoEngine.this;
        }
    }
}
