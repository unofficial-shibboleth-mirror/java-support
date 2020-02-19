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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class ContextHandlingHttpClientTest {
    
    public static final CloseableHttpResponse STATIC_RESPONSE_HTTP = new MockCloseableHttpResponse(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
        
    public static final Object STATIC_RESPONSE_HANDLER = new Object();
    
    private ContextHandlingHttpClient client;
    
    private TestContextHandler staticOne, staticTwo, staticThree;
    private TestContextHandler dynamicOne, dynamicTwo, dynamicThree;
    
    private HttpClientContext context;
    
    private HttpUriRequest request;
    private HttpHost target;
    private ResponseHandler<Object> responseHandler = new MockResponseHandler();
    
    @BeforeClass
    public void setupClass() {
        staticOne = new TestContextHandler("static-1");
        staticTwo = new TestContextHandler("static-2");
        staticThree = new TestContextHandler("static-3");
        dynamicOne = new TestContextHandler("dynamic-1");
        dynamicTwo = new TestContextHandler("dynamic-2");
        dynamicThree = new TestContextHandler("dynamic-3");
        
    }
    
    @BeforeMethod
    public void setupMethod() {
        request = new HttpGet("/test");
        target = new HttpHost("test.example.edu");
    }
    
    @Test
    public void testNoHandlers() throws ClientProtocolException, IOException {
        client = new ContextHandlingHttpClient(new MockHttpClient());
        context = HttpClientContext.create();
        
        //Non-context execute methods
        Assert.assertSame(client.execute(request), STATIC_RESPONSE_HTTP);
        Assert.assertSame(client.execute(request, responseHandler), STATIC_RESPONSE_HANDLER);
        Assert.assertSame(client.execute(target, request), STATIC_RESPONSE_HTTP);
        Assert.assertSame(client.execute(target, request, responseHandler), STATIC_RESPONSE_HANDLER);
        
        //Context execute methods
        Assert.assertSame(client.execute(request, context), STATIC_RESPONSE_HTTP);
        Assert.assertSame(client.execute(request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertSame(client.execute(target, request, context), STATIC_RESPONSE_HTTP);
        Assert.assertSame(client.execute(target, request, responseHandler, context), STATIC_RESPONSE_HANDLER);
    }
    
    @Test
    public void testStaticOnly() throws ClientProtocolException, IOException {
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        context = HttpClientContext.create();
        Assert.assertSame(client.execute(request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        Assert.assertSame(client.execute(request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        Assert.assertSame(client.execute(target, request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        Assert.assertSame(client.execute(target, request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
    }
    
    @Test
    public void testDynamicOnly() throws ClientProtocolException, IOException {
        client = new ContextHandlingHttpClient(new MockHttpClient());
        
        List<String> control = Lists.newArrayList(
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(target, request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(target, request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
    }
    
    @Test
    public void testStaticAndDynamic() throws ClientProtocolException, IOException {
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(target, request, context), STATIC_RESPONSE_HTTP);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        
        context = HttpClientContext.create();
        HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
        Assert.assertSame(client.execute(target, request, responseHandler, context), STATIC_RESPONSE_HANDLER);
        Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
    }
    
    @Test
    public void testWrappedClientThrowsIOException() throws ClientProtocolException, IOException {
        IOException error = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(error), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testWrappedClientThrowsRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(error), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (RuntimeException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (RuntimeException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (RuntimeException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (RuntimeException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testWrappedClientThrowsError() throws ClientProtocolException, IOException {
        Error error = new Error();
        client = new ContextHandlingHttpClient(new MockHttpClient(error), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (Error e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (Error e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (Error e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (Error e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
     
    @Test
    public void testSingleStaticHandlerInvokeBeforeThrowsIOException() throws ClientProtocolException, IOException {
        IOException error = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", error, null), staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testSingleStaticHandlerInvokeAfterThrowsIOException() throws ClientProtocolException, IOException {
        IOException error = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", null, error), staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testSingleStaticHandlerInvokeBeforeThrowsRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", error, null), staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testSingleStaticHandlerInvokeAfterThrowsRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", null, error), staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleStaticHandlersInvokeBeforeThrowIOException() throws ClientProtocolException, IOException {
        IOException error1 = new IOException();
        IOException error3 = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList( new TestContextHandler("static-1", error1, null), staticTwo, new TestContextHandler("static-3", error3, null)));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleStaticHandlersInvokeBeforeThrowRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error1 = new RuntimeException();
        RuntimeException error3 = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList( new TestContextHandler("static-1", error1, null), staticTwo, new TestContextHandler("static-3", error3, null)));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    
    @Test
    public void testMultipleStaticHandlersInvokeAfterThrowIOException() throws ClientProtocolException, IOException {
        IOException error1 = new IOException();
        IOException error3 = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList( new TestContextHandler("static-1", null, error1), staticTwo, new TestContextHandler("static-3", null, error3)));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleStaticHandlersInvokeAfterThrowRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error1 = new RuntimeException();
        RuntimeException error3 = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList( new TestContextHandler("static-1", null, error1), staticTwo, new TestContextHandler("static-3", null, error3)));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, dynamicTwo, dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    
    @Test
    public void testSingleDynamicHandlerInvokeBeforeThrowsIOException() throws ClientProtocolException, IOException {
        IOException error = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", error, null), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testSingleDynamicHandlerInvokeAfterThrowsIOException() throws ClientProtocolException, IOException {
        IOException error = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, error), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    
    @Test
    public void testSingleDynamicHandlerInvokeBeforeThrowsRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", error, null), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testSingleDynamicHandlerInvokeAfterThrowsRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, error), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), error);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    
    @Test
    public void testMultipleDynamicHandlersInvokeBeforeThrowIOException() throws ClientProtocolException, IOException {
        IOException error1 = new IOException();
        IOException error3 = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = 
                Lists.newArrayList(new TestContextHandler("dynamic-1", error1, null), dynamicTwo, new TestContextHandler("dynamic-3", error3, null));
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleDynamicHandlersInvokeBeforeThrowRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error1 = new RuntimeException();
        RuntimeException error3 = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = 
                Lists.newArrayList(new TestContextHandler("dynamic-1", error1, null), dynamicTwo, new TestContextHandler("dynamic-3", error3, null));
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleDynamicHandlersInvokeAfterThrowIOException() throws ClientProtocolException, IOException {
        IOException error1 = new IOException();
        IOException error3 = new IOException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = 
                Lists.newArrayList(new TestContextHandler("dynamic-1", null, error1), dynamicTwo, new TestContextHandler("dynamic-3", null, error3));
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testMultipleDynamicHandlersInvokeAfterThrowRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException error1 = new RuntimeException();
        RuntimeException error3 = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), Lists.newArrayList(staticOne, staticTwo, staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = 
                Lists.newArrayList(new TestContextHandler("dynamic-1", null, error1), dynamicTwo, new TestContextHandler("dynamic-3", null, error3));
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertNotSame(e, error1);
            Assert.assertNotSame(e, error3);
            Assert.assertEquals(e.getSuppressed().length, 2);
            Assert.assertTrue(Arrays.asList(e.getSuppressed()).containsAll(Lists.newArrayList(error1, error3)));
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    
    @Test
    public void testStaticAndDynamicHandlersThrowIOException() throws ClientProtocolException, IOException {
        IOException staticBeforeError = null;
        IOException dynamicAfterError = null;
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> dynamicHandlers = null;
        
        staticBeforeError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", staticBeforeError, null), staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        staticBeforeError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", staticBeforeError, null), staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        staticBeforeError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", staticBeforeError, null), staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        staticBeforeError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", staticBeforeError, null), staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testStaticAndDynamicHandlersThrowRuntimeException() throws ClientProtocolException, IOException {
        RuntimeException staticBeforeError = new RuntimeException();
        RuntimeException dynamicAfterError = new RuntimeException();
        client = new ContextHandlingHttpClient(new MockHttpClient(), 
                Lists.newArrayList(staticOne, new TestContextHandler("static-2", staticBeforeError, null), staticThree));
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> handlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0].getCause(), dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0].getCause(), dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0].getCause(), dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(handlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e.getCause(), staticBeforeError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0].getCause(), dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
    
    @Test
    public void testWrappedClientThrowsIOExceptionInvokeAfterThrowsIOException() throws ClientProtocolException, IOException {
        IOException clientError = null;
        IOException dynamicAfterError = null;
        
        List<String> control = Lists.newArrayList(
                "before-static-1",
                "before-static-2",
                "before-static-3",
                "before-dynamic-1",
                "before-dynamic-2",
                "before-dynamic-3",
                "after-dynamic-3",
                "after-dynamic-2",
                "after-dynamic-1",
                "after-static-3",
                "after-static-2",
                "after-static-1"
                );
        
        List<HttpClientContextHandler> dynamicHandlers = null;
        
        clientError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(clientError), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, clientError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        clientError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(clientError), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, clientError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        clientError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(clientError), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(target, request, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, clientError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
        
        clientError = new IOException();
        dynamicAfterError = new IOException();
        
        client = new ContextHandlingHttpClient(new MockHttpClient(clientError), 
                Lists.newArrayList(staticOne, staticTwo, staticThree));
        dynamicHandlers = Lists.newArrayList(dynamicOne, new TestContextHandler("dynamic-2", null, dynamicAfterError), dynamicThree);
        
        try {
            context = HttpClientContext.create();
            HttpClientSupport.getDynamicContextHandlerList(context).addAll(dynamicHandlers);
            client.execute(target, request, responseHandler, context);
            Assert.fail("Wrapped client should have thrown");
        } catch (IOException e) {
            Assert.assertSame(e, clientError);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], dynamicAfterError);
            Assert.assertEquals(context.getAttribute(TestContextHandler.TEST_KEY), control);
        }
    }
     
    
    
    
    
    // Helpers
    
    private class TestContextHandler implements HttpClientContextHandler {
        
        public static final String TEST_KEY = "testKey";
        
        private String name;
        
        private Throwable invokeBeforeError;
        private Throwable invokeAfterError;
        
        public TestContextHandler(String instanceName) {
            name = instanceName;
        }
        
        public TestContextHandler(Throwable beforeError, Throwable afterError) {
            invokeBeforeError = beforeError;
            invokeAfterError = afterError;
        }
        
        public TestContextHandler(String instanceName, Throwable beforeError, Throwable afterError) {
            name = instanceName;
            invokeBeforeError = beforeError;
            invokeAfterError = afterError;
        }
        
        /** {@inheritDoc} */
        public void invokeBefore(HttpClientContext context, HttpUriRequest request) throws IOException {
            if (name != null) {
                addValue(context, "before-" + name);
            }
            ThrowableHelper.checkAndThrowError(invokeBeforeError);
        }

        /** {@inheritDoc} */
        public void invokeAfter(HttpClientContext context, HttpUriRequest request) throws IOException {
            if (name != null) {
                addValue(context, "after-" + name);
            }
            ThrowableHelper.checkAndThrowError(invokeAfterError);
        }
        
        private void addValue(HttpClientContext context, String value) {
            List<String> attrib = context.getAttribute(TEST_KEY, List.class);
            if (attrib == null) {
                attrib = new ArrayList<>();
                context.setAttribute(TEST_KEY, attrib);
            }
            attrib.add(value);
        }
        
    }
    
    private static class MockHttpClient extends CloseableHttpClient {
        
        private Throwable error;
        
        public MockHttpClient() {
        }
        
        public MockHttpClient(final Throwable throwable) {
            error = throwable;
        }

        /** {@inheritDoc} */
        @Deprecated
        @Override
        public org.apache.http.params.HttpParams getParams() {
            return null;
        }

        /** {@inheritDoc} */
        @Deprecated
        @Override
        public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
            return null;
        }
        
        /** {@inheritDoc} */
        public void close() throws IOException {
            // nothing to do
        }

        /** {@inheritDoc} */
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
                throws IOException, ClientProtocolException {
            ThrowableHelper.checkAndThrowError(error);
            return STATIC_RESPONSE_HTTP;
        }
        
    }

    public static class MockResponseHandler implements ResponseHandler<Object> {

        public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            return STATIC_RESPONSE_HANDLER;
        }

    }
    
    public static class MockCloseableHttpResponse extends BasicHttpResponse implements CloseableHttpResponse {

        public MockCloseableHttpResponse(ProtocolVersion ver, int code, String reason) {
            super(ver, code, reason);
        }

        public void close() throws IOException {
            
        }
        
    }
    
    public static class ThrowableHelper {
        
        public static void checkAndThrowError(Throwable t) throws IOException {
            if (t != null) {
                if (IOException.class.isInstance(t)) {
                    throw IOException.class.cast(t);
                } 
                if (RuntimeException.class.isInstance(t)) {
                    throw RuntimeException.class.cast(t);
                } 
                if (Error.class.isInstance(t)) {
                    throw Error.class.cast(t);
                } 
            }
        }
        
    }

}
