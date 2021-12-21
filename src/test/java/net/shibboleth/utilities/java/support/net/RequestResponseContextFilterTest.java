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

import java.io.IOException;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests for {@link RequestResponseContextFilter}.
 */
public class RequestResponseContextFilterTest {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private FilterChain filterChain;
    private Servlet servlet;
    
    private RequestResponseContextFilter filter;
    
    @BeforeMethod
    public void setUp() throws ServletException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("GET");
        mockRequest.setRequestURI("/foo");
        mockRequest.addHeader("MyRequestHeader", "MyRequestHeaderValue");
        mockRequest.addParameter("MyParam", "MyParamValue");
        request = mockRequest;
        
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponse.setHeader("MyResponseHeader", "MyResponseHeaderValue");
        response = mockResponse;
        
        filter = new RequestResponseContextFilter();
        filter.init(new MockFilterConfig());
        
        servlet = new TestServlet();
        servlet.init(new MockServletConfig());
        
        filterChain = new MockFilterChain(servlet, filter);
    }
    
    @AfterMethod
    public void tearDown()  {
        HttpServletRequestResponseContext.clearCurrent();
    }
    
    @Test
    public void testFilter() throws IOException, ServletException {
        Assert.assertNull(HttpServletRequestResponseContext.getRequest()); 
        Assert.assertNull(HttpServletRequestResponseContext.getResponse()); 
        
        filterChain.doFilter(request, response);
        
        Assert.assertNull(HttpServletRequestResponseContext.getRequest()); 
        Assert.assertNull(HttpServletRequestResponseContext.getResponse()); 
    }
    
    // Helper classes
    public class TestServlet implements Servlet {

        /** {@inheritDoc} */
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            Assert.assertNotNull(HttpServletRequestResponseContext.getRequest(), "HttpServletRequest was null");
            Assert.assertNotNull(HttpServletRequestResponseContext.getResponse(), "HttpServletResponse was null");
        }

        /** {@inheritDoc} */
        public void init(ServletConfig config) throws ServletException { }

        /** {@inheritDoc} */
        public ServletConfig getServletConfig() { return null; }

        /** {@inheritDoc} */
        public String getServletInfo() { return null; }

        /** {@inheritDoc} */
        public void destroy() { }
        
    }

}
