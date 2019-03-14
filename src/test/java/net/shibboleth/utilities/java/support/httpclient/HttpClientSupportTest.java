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

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class HttpClientSupportTest {

    @Test
    public void testAddDynamicContextHandlerFirst() {
        HttpClientContext context = HttpClientContext.create();
        HttpClientContextHandler one = new TestContextHandler();
        HttpClientContextHandler two = new TestContextHandler();
        HttpClientContextHandler three = new TestContextHandler();
        
        HttpClientSupport.addDynamicContextHandlerFirst(context, one);
        HttpClientSupport.addDynamicContextHandlerFirst(context, two);
        HttpClientSupport.addDynamicContextHandlerFirst(context, three);
        
        Assert.assertEquals(HttpClientSupport.getDynamicContextHandlerList(context), Lists.newArrayList(three, two, one));
    }
     
    @Test
    public void testAddDynamicContextHandlerLast() {
        HttpClientContext context = HttpClientContext.create();
        HttpClientContextHandler one = new TestContextHandler();
        HttpClientContextHandler two = new TestContextHandler();
        HttpClientContextHandler three = new TestContextHandler();
        
        HttpClientSupport.addDynamicContextHandlerLast(context, one);
        HttpClientSupport.addDynamicContextHandlerLast(context, two);
        HttpClientSupport.addDynamicContextHandlerLast(context, three);
        
        Assert.assertEquals(HttpClientSupport.getDynamicContextHandlerList(context), Lists.newArrayList(one, two, three));
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
