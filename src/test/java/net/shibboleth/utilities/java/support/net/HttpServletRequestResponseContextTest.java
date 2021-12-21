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

package net.shibboleth.utilities.java.support.net;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests for {@link HttpServletRequestResponseContext}.
 */
public class HttpServletRequestResponseContextTest {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    @BeforeMethod
    public void setUp() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("GET");
        mockRequest.setRequestURI("/foo");
        mockRequest.addHeader("MyRequestHeader", "MyRequestHeaderValue");
        mockRequest.addParameter("MyParam", "MyParamValue");
        request = mockRequest;
        
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponse.setHeader("MyResponseHeader", "MyResponseHeaderValue");
        response = mockResponse;
    }
    
    @AfterMethod
    public void tearDown()  {
        HttpServletRequestResponseContext.clearCurrent();
    }
    
    @Test
    public void testLoadAndClear() {
       Assert.assertNull(HttpServletRequestResponseContext.getRequest()); 
       Assert.assertNull(HttpServletRequestResponseContext.getResponse()); 
       
       HttpServletRequestResponseContext.loadCurrent(request, response);
       
       Assert.assertNotNull(HttpServletRequestResponseContext.getRequest()); 
       Assert.assertNotNull(HttpServletRequestResponseContext.getResponse()); 
       
       Assert.assertEquals(HttpServletRequestResponseContext.getRequest().getMethod(), "GET");
       Assert.assertEquals(HttpServletRequestResponseContext.getRequest().getRequestURI(), "/foo");
       Assert.assertEquals(HttpServletRequestResponseContext.getRequest().getHeader("MyRequestHeader"), "MyRequestHeaderValue");
       Assert.assertEquals(HttpServletRequestResponseContext.getRequest().getParameter("MyParam"), "MyParamValue");
       
       Assert.assertTrue(HttpServletRequestResponseContext.getResponse().containsHeader("MyResponseHeader"));
       
       HttpServletRequestResponseContext.clearCurrent();
       
       Assert.assertNull(HttpServletRequestResponseContext.getRequest()); 
       Assert.assertNull(HttpServletRequestResponseContext.getResponse()); 
    }

}
