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

import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * A JSR-223 factory for {@link RhinoEngine}.
 */
public class RhinoFactory implements ScriptEngineFactory {
    
    /** {@inheritDoc} */
    public String getEngineName() {
        
        return this.getClass().getCanonicalName();
    }

    /** {@inheritDoc} */
    public String getEngineVersion() {
        return "1";
    }

    /** {@inheritDoc} */
    public List<String> getExtensions() {
        
        return List.of("js", "rhino-js");
    }

    /** {@inheritDoc} */
    public List<String> getMimeTypes() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    public List<String> getNames() {
        return List.of("shibboleth-rhino", "shibboleth-Rhino", 
                "shibboleth-js", "shibboleth-JS", "shibboleth-JavaScript", "shibboleth-javascript", 
                "shibboleth-ECMAScript", "shibboleth-ecmascript");
    }

    /** {@inheritDoc} */
    public String getLanguageName() {
        return "ECMAScript";
    }

    /** {@inheritDoc} */
    public String getLanguageVersion() {
        throw new RuntimeScriptingException("Unsupported method getMethodCallSyntax");
    }

    /** {@inheritDoc} */
    public Object getParameter(String key) {
        throw new RuntimeScriptingException("Unsupported method getMethodCallSyntax");
    }

    /** {@inheritDoc} */
    public String getMethodCallSyntax(String obj, String m, String... args) {
        throw new RuntimeScriptingException("Unsupported method getMethodCallSyntax");
    }

    /** {@inheritDoc} */
    public String getOutputStatement(String toDisplay) {
        throw new RuntimeScriptingException("Unsupported method getMethodCallSyntax");
    }

    /** {@inheritDoc} */
    public String getProgram(String... statements) {
        throw new RuntimeScriptingException("Unsupported method getMethodCallSyntax");
    }

    /** {@inheritDoc} */
    public ScriptEngine getScriptEngine() {
       
        return new RhinoEngine();
    }

}
