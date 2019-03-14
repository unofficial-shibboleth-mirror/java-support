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


package net.shibboleth.utilities.java.support.httpclient;

import java.io.IOException;
import java.time.Duration;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

public class HttpClientBuilderTest {
    
    // Default timeouts
    @Test public void JSPT66() throws Exception {
        final HttpClientBuilder builder = new HttpClientBuilder();
        
        // Check the defaults at the builder level
        Assert.assertEquals(builder.getConnectionTimeout(), Duration.ofSeconds(60));
        Assert.assertEquals(builder.getSocketTimeout(), Duration.ofSeconds(60));
        Assert.assertEquals(builder.getConnectionRequestTimeout(), Duration.ofSeconds(60));
        
        try {
            builder.setSocketTimeout(Duration.ofMillis(Long.MAX_VALUE));
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            
        }
        
        // Just make sure we can create a client, too
        final HttpClient client = builder.buildClient();
        Assert.assertNotNull(client);
    }
    
    @Test
    public void testContextHandlingSupport() throws Exception {
        
        HttpClientContextHandler handler1 = new TestContextHandler();
        HttpClientContextHandler handler2 = new TestContextHandler();
        HttpClientContextHandler handler3 = new TestContextHandler();
        
        final HttpClientBuilder builder = new HttpClientBuilder();
        
        Assert.assertNotNull(builder.getStaticContextHandlers());
        Assert.assertTrue(builder.getStaticContextHandlers().isEmpty());
        
        builder.setStaticContextHandlers(Lists.newArrayList(null, handler1, null, handler2, null, handler3));
        Assert.assertEquals(builder.getStaticContextHandlers(), Lists.newArrayList(handler1, handler2, handler3));
        
        try {
            builder.getStaticContextHandlers().add(new TestContextHandler());
            Assert.fail("List should have been unmodifaible");
        } catch (UnsupportedOperationException e) {
            //expected
        }
        
        builder.resetDefaults();
        
        Assert.assertNotNull(builder.getStaticContextHandlers());
        Assert.assertTrue(builder.getStaticContextHandlers().isEmpty());
        
        final HttpClient client = builder.buildClient();
        Assert.assertNotNull(client);
        Assert.assertTrue(ContextHandlingHttpClient.class.isInstance(client));
        
    }
    
    //Helpers 
    
    public class TestContextHandler implements HttpClientContextHandler {

        /** {@inheritDoc} */
        public void invokeBefore(HttpClientContext context, HttpUriRequest request) throws IOException {
            
        }

        /** {@inheritDoc} */
        public void invokeAfter(HttpClientContext context, HttpUriRequest request) throws IOException {
            
        }
        
    }
     
    
}
