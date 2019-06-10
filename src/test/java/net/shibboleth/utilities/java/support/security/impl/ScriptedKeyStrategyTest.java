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

package net.shibboleth.utilities.java.support.security.impl;

import java.io.File;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.script.ScriptException;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for {@link ScriptedKeyStrategy}.
 */
public class ScriptedKeyStrategyTest {

    final String scriptPath = "src/test/resources/net/shibboleth/utilities/java/support/security/keyStrategyScript.js";
    
    private Map<String,Object> customMap;
    
    private KeyGenerator keyGenerator;
    
    private ScriptedKeyStrategy strategy;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException, NoSuchAlgorithmException, ScriptException {

        final SecureRandom random = new SecureRandom(); 
        keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(random); 
        customMap = new HashMap<>();
        customMap.put("secret1", keyGenerator.generateKey());
        customMap.put("default", "secret1");
        
        strategy = new ScriptedKeyStrategy();
        strategy.setUpdateInterval(Duration.ofSeconds(1));
        strategy.setKeyScript(new EvaluableScript("javascript", new File(scriptPath)));
        strategy.setCustomObject(customMap);
        strategy.initialize();
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testNoScript() throws ComponentInitializationException {
        final ScriptedKeyStrategy strategy = new ScriptedKeyStrategy();
        strategy.initialize();
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testScriptFailure() throws ComponentInitializationException, ScriptException {
        final ScriptedKeyStrategy strategy = new ScriptedKeyStrategy();
        strategy.setKeyScript(new EvaluableScript("null"));
        strategy.initialize();
    }
    
    @Test public void testScriptedKeystoreKeyStrategy() throws Exception {

        
        Assert.assertEquals(strategy.getDefaultKey().getFirst(), "secret1");
        try {
            strategy.getKey("secret2");
            Assert.fail("secret2 should not exist");
        } catch (final KeyException e) {

        }

        customMap.put("secret2", keyGenerator.generateKey());
        customMap.put("default", "secret2");
        Thread.sleep(5000);
        Assert.assertEquals(strategy.getDefaultKey().getFirst(), "secret2");
        Assert.assertNotNull(strategy.getKey("secret1"));
    }
    
}