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
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * A subset of {@link ScriptEngine} implemented with GraaVm with enough function that 
 * {@link EvaluableScript} can work with it. 
 * 
 * NOTE that this does not (currently) implement {@link javax.script.Compilable}.
 */
public class GraalEngine extends AbstractScriptEngine implements ScriptEngine {

    /** {@inheritDoc} */
    public Object eval(final String script, final Bindings binding) throws ScriptException {
        try (final Context context = Context.newBuilder().allowExperimentalOptions(true).
                option("js.nashorn-compat", "true").
                allowAllAccess(true).
                build()) {
            
            final Value bindings  = context.getBindings("js");
            for (final String name: binding.keySet()) {
                bindings.putMember(name, binding.get(name));
            }
            final Value v = context.eval("js", script);
            if (v.isHostObject()) {
                return v.asHostObject();
            } else if (v.isProxyObject()) { 
                return v.asProxyObject();
            }
            return v.as(Object.class);
        }  catch (final RuntimeException e) {
            throw new ScriptException(e);
        }
    }

}
