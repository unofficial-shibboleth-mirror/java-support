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

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.resource.TestResourceConverter;
import net.shibboleth.utilities.java.support.security.impl.BasicKeystoreKeyStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for {@link DataSealer}.
 */
public class DataSealerTest {

    private Resource keystoreResource;
    private Resource versionResource;
    private Resource version2Resource;

    @Nonnull @NotEmpty final private String THE_DATA =
            "THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA"
            + "THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA"
            + "THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA"
            + "THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA THIS IS SOME TEST DATA";
    final private Duration THE_DELAY = Duration.ofMillis(500);

    @BeforeClass public void initializeKeystoreResource() {
        ClassPathResource resource =
                new ClassPathResource("/net/shibboleth/utilities/java/support/security/SealerKeyStore.jks");
        Assert.assertTrue(resource.exists());
        keystoreResource = TestResourceConverter.of(resource);

        resource =
                new ClassPathResource("/net/shibboleth/utilities/java/support/security/SealerKeyStore.kver");
        Assert.assertTrue(resource.exists());
        versionResource = TestResourceConverter.of(resource);

        resource =
                new ClassPathResource("/net/shibboleth/utilities/java/support/security/SealerKeyStore.kver2");
        Assert.assertTrue(resource.exists());
        version2Resource = TestResourceConverter.of(resource);
    }

    private DataSealer createDataSealer() throws DataSealerException, ComponentInitializationException {
        final BasicKeystoreKeyStrategy strategy = new BasicKeystoreKeyStrategy();
        
        strategy.setKeyAlias("secret");
        strategy.setKeyPassword("kpassword");

        strategy.setKeystorePassword("password");
        strategy.setKeystoreResource(keystoreResource);
        
        strategy.setKeyVersionResource(versionResource);

        strategy.initialize();
        
        final DataSealer sealer = new DataSealer();
        sealer.setKeyStrategy(strategy);
        sealer.initialize();
        return sealer;
    }

    private DataSealer createDataSealer2() throws DataSealerException, ComponentInitializationException {
        final BasicKeystoreKeyStrategy strategy = new BasicKeystoreKeyStrategy();
        
        strategy.setKeyAlias("secret");
        strategy.setKeyPassword("kpassword");

        strategy.setKeystorePassword("password");
        strategy.setKeystoreResource(keystoreResource);
        
        strategy.setKeyVersionResource(version2Resource);

        strategy.initialize();
        
        final DataSealer sealer = new DataSealer();
        sealer.setKeyStrategy(strategy);
        sealer.initialize();
        return sealer;
    }
    
    @Test public void encodeDecode() throws DataSealerException, ComponentInitializationException {
        final DataSealer sealer = createDataSealer();

        final String encoded = sealer.wrap(THE_DATA, Instant.now().plusSeconds(50));
        final StringBuffer alias = new StringBuffer(); 
        Assert.assertEquals(sealer.unwrap(encoded, alias), THE_DATA);
        Assert.assertEquals(alias.toString(), "secret1");
    }

    @Test public void encodeDecodeSecondKey() throws DataSealerException, ComponentInitializationException {
        final DataSealer sealer = createDataSealer();
        final DataSealer sealer2 = createDataSealer2();

        final StringBuffer alias = new StringBuffer(); 
        final String encoded = sealer.wrap(THE_DATA, Instant.now().plusSeconds(50));
        Assert.assertEquals(sealer.unwrap(encoded, alias), THE_DATA);
        Assert.assertEquals(alias.toString(), "secret1");
        alias.setLength(0);
        Assert.assertEquals(sealer2.unwrap(encoded, alias), THE_DATA);
        Assert.assertEquals(alias.toString(), "secret1");
    }
    
    @Test public void timeOut() throws DataSealerException, InterruptedException, ComponentInitializationException {
        final DataSealer sealer = createDataSealer();

        String encoded = sealer.wrap(THE_DATA, Instant.now().plus(THE_DELAY));
        Thread.sleep(THE_DELAY.toMillis() + 150);
        try {
            sealer.unwrap(encoded);
            Assert.fail("Should have timed out");
        } catch (DataExpiredException ex) {
            // OK
        }
    }

    @Test public void encodeDecodeLong() throws DataSealerException, ComponentInitializationException {
        final DataSealer sealer = createDataSealer();
        
        char[] buffer = new char[1000000];
        Arrays.fill(buffer, 'x');
        final String longData = new String(buffer);
        final String encoded = sealer.wrap(longData, Instant.now().plusSeconds(50));
        final StringBuffer alias = new StringBuffer(); 
        Assert.assertEquals(sealer.unwrap(encoded, alias), longData);
        Assert.assertEquals(alias.toString(), "secret1");
    }
    
    @Test public void badValues() throws DataSealerException, ComponentInitializationException {
        DataSealer sealer = new DataSealer();

        try {
            sealer.initialize();
            Assert.fail("no strategy");
        } catch (ComponentInitializationException e) {

        }

        sealer = createDataSealer();

        try {
            sealer.unwrap("");
            Assert.fail("no data");
        } catch (DataSealerException e) {
            // OK
        }

        try {
            sealer.unwrap("RandomGarbage");
            Assert.fail("random data");
        } catch (DataSealerException e) {
            // OK
        }

        final String wrapped = sealer.wrap(THE_DATA, Instant.now().plusSeconds(3600));

        final String corrupted = wrapped.substring(0, 25) + "A" + wrapped.substring(27);

        try {
            sealer.unwrap(corrupted);
            Assert.fail("corrupted data");
        } catch (DataSealerException e) {
            // OK
        }

        try {
            sealer.wrap(nullValue(), Instant.ofEpochMilli(10));
            Assert.fail("no data");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    private <T> T nullValue() {
        return null;
    }

}