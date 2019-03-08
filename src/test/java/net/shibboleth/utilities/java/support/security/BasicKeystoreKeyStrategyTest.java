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

package net.shibboleth.utilities.java.support.security;

import java.io.File;
import java.security.KeyException;
import java.time.Duration;

import net.shibboleth.utilities.java.support.resource.TestResourceConverter;

import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link BasicKeystoreKeyStrategy}.
 */
public class BasicKeystoreKeyStrategyTest {
    
    @Test public void testBasicKeystoreKeyStrategy() throws Exception {
        final File keystoreFile = new File("src/test/resources/net/shibboleth/utilities/java/support/security/Temp.jks");
        final File versionFile = new File("src/test/resources/net/shibboleth/utilities/java/support/security/Temp.kver");
        keystoreFile.deleteOnExit();
        versionFile.deleteOnExit();
        
        final BasicKeystoreKeyStrategyTool tool = new BasicKeystoreKeyStrategyTool();
        tool.setKeyAlias("secret");
        tool.setKeystorePassword("password");
        tool.setKeystoreFile(keystoreFile);
        tool.setVersionFile(versionFile);
        tool.setKeyCount(3);
        tool.changeKey();
        
        final BasicKeystoreKeyStrategy strategy = new BasicKeystoreKeyStrategy();
        strategy.setKeyAlias("secret");
        strategy.setKeyPassword("password");
        strategy.setKeystorePassword("password");
        strategy.setKeystoreResource(TestResourceConverter.of(new FileSystemResource(keystoreFile)));
        strategy.setKeyVersionResource(TestResourceConverter.of(new FileSystemResource(versionFile)));
        strategy.setUpdateInterval(Duration.ofSeconds(1));
        strategy.initialize();
        
        Assert.assertEquals(strategy.getDefaultKey().getFirst(), "secret1");
        try {
            strategy.getKey("secret2");
            Assert.fail();
        } catch (final KeyException e) {

        }
        
        tool.changeKey();
        Thread.sleep(2000);
        Assert.assertEquals(strategy.getDefaultKey().getFirst(), "secret2");
        Assert.assertNotNull(strategy.getKey("secret1"));

        tool.changeKey();
        tool.changeKey();
        Thread.sleep(2000);
        Assert.assertEquals(strategy.getDefaultKey().getFirst(), "secret4");
        Assert.assertNotNull(strategy.getKey("secret2"));
        Assert.assertNotNull(strategy.getKey("secret3"));
        try {
            strategy.getKey("secret1");
            Assert.fail();
        } catch (final KeyException e) {

        }
    }
    
}