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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.testng.annotations.Test;

/** Minimal tests for our two JSR-223 engine plug ins. */
@SuppressWarnings({"javadoc", "rawtypes", })
public class EngineTests {
    
    @Test public void testRhino() throws ScriptException {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine scriptEngine = engineManager.getEngineByName("shibboleth-rhino");
        final ScriptContext ctx = new SimpleScriptContext();
        final String script = "var s = new java.util.HashMap(2); s.put('a',b); s";

        ctx.setAttribute("b", this, ScriptContext.ENGINE_SCOPE);
        
        final Object o = scriptEngine.eval(script, ctx);
        
        assertTrue(o instanceof Map);
        final Map map = (Map) o;
        assertEquals(map.size(), 1);
        assertEquals(map.get("a"), this);
    }

    @Test public void testGraal() throws ScriptException {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine scriptEngine = engineManager.getEngineByName("shibboleth-nashorn");
        final ScriptContext ctx = new SimpleScriptContext();
        final String script = "var map = Java.type('java.util.HashMap');"
                + "var s = new map(2); s.put('a',b); s";

        ctx.setAttribute("b", this, ScriptContext.ENGINE_SCOPE);
        
        final Object o = scriptEngine.eval(script, ctx);
        
        assertTrue(o instanceof Map);
        final Map map = (Map) o;
        assertEquals(map.size(), 1);
        assertEquals(map.get("a"), this);
    }

}
