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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * Enough boiler plate to allow us to implement a {@link ScriptEngine} sufficient for our own use.
 */
public abstract class AbstractScriptEngine implements ScriptEngine {

    /** {@inheritDoc} */
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        final Bindings globals = context.getBindings(ScriptContext.GLOBAL_SCOPE); 
        if (globals != null && !globals.isEmpty()) {
            throw new ScriptException("Non empty GLOBAL_SCOPE");
        }
        return eval(script, context.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    /** {@inheritDoc} */
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        throw new ScriptException("Unsupported method eval(Reader, ScriptContext)");
    }

    /** {@inheritDoc} */
    public Object eval(final String script) throws ScriptException {
        throw new ScriptException("Unsupported method eval(String)");
    }

    /** {@inheritDoc} */
    public Object eval(final Reader reader) throws ScriptException {
        throw new ScriptException("Unsupported method eval(Reader, ScriptContext)");
    }

    /** {@inheritDoc} */
    public Object eval(final Reader reader, final Bindings n) throws ScriptException {
        throw new ScriptException("Unsupported method eval(Reader, Bindings)");
    }

    /** {@inheritDoc} */
    public void put(final String key, final Object value) {
        throw new RuntimeScriptingException("Unsupported method put(String, Object)");
    }

    /** {@inheritDoc} */
    public Object get(final String key) {
        throw new RuntimeScriptingException("Unsupported method get(String)");
    }

    /** {@inheritDoc} */
    public Bindings getBindings(final int scope) {
        throw new RuntimeScriptingException("Unsupported method getBindings(String)");
    }

    /** {@inheritDoc} */
    public void setBindings(final Bindings bindings, final int scope) {
        if (!bindings.isEmpty()) {
            throw new RuntimeScriptingException("non empty bindings set");            
        }
    }

    /** {@inheritDoc} */
    public Bindings createBindings() {
        throw new RuntimeScriptingException("Unsupported method createBindings");
    }

    /** {@inheritDoc} */
    public ScriptContext getContext() {
        throw new RuntimeScriptingException("Unsupported method getContext");
    }

    /** {@inheritDoc} */
    public void setContext(ScriptContext context) {
        throw new RuntimeScriptingException("Unsupported method setContext");
    }

    /** {@inheritDoc} */
    public ScriptEngineFactory getFactory() {
        throw new RuntimeScriptingException("Unsupported method getFactory");
    }

}
