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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.utilities.java.support.net.SameSiteCookieHeaderFilter.SameSiteValue;

/**
 * Tests for {@link SameSiteCookieHeaderFilter}.
 */
public class SameSiteCookieHeaderFilterTest {

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeMethod public void setUp() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("POST");
        mockRequest.setRequestURI("/foo");
        request = mockRequest;

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponse.addHeader(HttpHeaders.SET_COOKIE,
                "JSESSIONID=jyohu8ttc3dp1g3yqe8g8ff7y;Path=/idp;Secure;HttpOnly");
        mockResponse.addHeader(HttpHeaders.SET_COOKIE,
                "shib_idp_session_ss=AAdzZWNyZXQyzL1Rzi9ROe3%2BGk%2B6%2B;Path=/idp;HttpOnly");
        mockResponse.addHeader(HttpHeaders.SET_COOKIE,
                "shib_idp_session=8ee460bc0b3695c477b2b5f3e192ddf7297baa7ee01bd2bcf24695f8c21cb3a2;Path=/idp;HttpOnly");
        //add a cookie with existing SameSite value - should ignore and copy over.
        mockResponse.addHeader(HttpHeaders.SET_COOKIE,
                "existing_same_site=already-same-site;Path=/idp;HttpOnly;SameSite=None");
        //ignore this, copy it over as is.
        mockResponse.addHeader(HttpHeaders.SET_COOKIE,
                "ignore_copy_over=copy-over;Path=/idp;HttpOnly");

        response = mockResponse;
    }

    @AfterMethod public void tearDown() {
        HttpServletRequestResponseContext.clearCurrent();
    }
    
    /** Test a null init value, which should not trigger an exception.*/
    @Test public void testNullInitValues() {
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        filter.setSameSiteCookies(null);
        filter.setDefaultValue(null);
    }
    
    /** Test an empty cookie name is not added to the internal map.*/
    @Test public void testEmptyCookieNameInitValue() {
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {""});
        cookies.put(SameSiteValue.None, noneCookies);
        filter.setSameSiteCookies(cookies);
        
        testSameSiteMapSize("sameSiteCookies", 0, filter);
    }
   
    /** Test the correct number of cookies are added to the internal filter cookie map.*/
    @Test public void testInitValues() {
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"});
        List<String> laxCookies = List.of(new String[] {"another-cookie-lax"});
        List<String> strictCookies = List.of(new String[] {"another-cookie-strict"});
        cookies.put(SameSiteValue.None, noneCookies);
        cookies.put(SameSiteValue.Lax, laxCookies);
        cookies.put(SameSiteValue.Strict, strictCookies);
        filter.setSameSiteCookies(cookies);
        
        testSameSiteMapSize("sameSiteCookies", 6, filter);
    }
    
    /** Test failure on duplicated cookie names*/
    @Test(expectedExceptions=IllegalArgumentException.class) public void testDuplicateInitValues() {
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"});
        List<String> laxCookies = List.of(new String[] {"JSESSIONID"});
        cookies.put(SameSiteValue.None, noneCookies);
        cookies.put(SameSiteValue.Lax, laxCookies);
        filter.setSameSiteCookies(cookies);
    }
    
    /**
     * Test empty SameSite cookie map, which should not trigger an exception, and just copy over the
     * existing cookies.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testEmptySameSiteCookieMap() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        filter.setSameSiteCookies(null);
        
        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        final Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE); 
        
        Assert.assertEquals(headers.size(), 5);
    }

    /**
     * Test empty SameSite cookie map and Null default, which should not trigger an exception, and just copy over the
     * existing cookies.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testEmptySameSiteCookieMapAndNullDefault() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        filter.setSameSiteCookies(null);
        filter.setDefaultValue(SameSiteValue.Null);
        
        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        final Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE); 
        
        Assert.assertEquals(headers.size(), 5);
    }

    /**
     * Test empty SameSite cookie map, which should not trigger an exception, and should apply
     * a default.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testEmptySameSiteCookieMapWithDefault() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        filter.setSameSiteCookies(null);
        filter.setDefaultValue(SameSiteValue.Strict);
        
        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        final Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE); 
        
        Assert.assertEquals(headers.size(), 5);
        testExpectedHeadersInResponse(SameSiteValue.Strict.getValue(),
                (MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss", "ignore_copy_over"}),
                Collections.emptyList(), 5);
    }

    /**
     * Test the samesite filter works correctly with None values when a redirect response is issued.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testRedirectResponseSameSiteNone() throws IOException, ServletException {
       
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"});
        cookies.put(SameSiteValue.None, noneCookies);
        filter.setSameSiteCookies(cookies);

        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"}),
                List.of(new String[] {"ignore_copy_over"}),5);
    }

    /**
     * Test the samesite filter works correctly with None values when a redirect response is issued.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testRedirectResponseSameSiteNoneWithDefault() throws IOException, ServletException {
       
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"shib_idp_session","shib_idp_session_ss","existing_same_site"});
        cookies.put(SameSiteValue.None, noneCookies);
        filter.setSameSiteCookies(cookies);
        filter.setDefaultValue(SameSiteValue.None);

        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site","ignore_copy_over"}),
                Collections.emptyList(), 5);
    }
    
    /**
     * Test the samesite filter works correctly with Lax values when a redirect response is issued.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testRedirectResponseSameSiteLax() throws IOException, ServletException {
       
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss"});
        cookies.put(SameSiteValue.Lax, noneCookies);
        filter.setSameSiteCookies(cookies);
        
        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        //as "existing_same_site" is None, ignore it here.
        testExpectedHeadersInResponse("Lax",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss"}),
                List.of(new String[] {"ignore_copy_over"}),5);
    }
    
    /**
     * Test the samesite filter works correctly with Strict values when a redirect response is issued.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testRedirectResponseSameSiteStrict() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss"});
        cookies.put(SameSiteValue.Strict, noneCookies);
        filter.setSameSiteCookies(cookies);

        Servlet redirectServlet = new TestRedirectServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(redirectServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        //as "existing_same_site" is None, ignore it here.
        testExpectedHeadersInResponse("Strict",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss"}),
                List.of(new String[] {"ignore_copy_over"}),5);
    }

    /**
     * Test the samesite filter works correctly when an output stream is written to and flushed.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testGetOutputStreamResponse() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"});
        cookies.put(SameSiteValue.None, noneCookies);
        filter.setSameSiteCookies(cookies);

        Servlet outputStreamServlet = new TestOutputStreamServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(outputStreamServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"}),
                List.of(new String[] {"ignore_copy_over"}),5);
    }
    
    /**
     * Test the samesite filter works correctly when the response print writer is written to and closed.
     * 
     * @throws IOException if something bad happens
     * @throws ServletException if something bad happens
     */
    @Test public void testPrintWriterResponse() throws IOException, ServletException {
        
        SameSiteCookieHeaderFilter filter = new SameSiteCookieHeaderFilter();
        Map<SameSiteValue,List<String>> cookies = new HashMap<>();
        List<String> noneCookies = List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"});
        cookies.put(SameSiteValue.None, noneCookies);
        filter.setSameSiteCookies(cookies);

        Servlet printWriterServlet = new TestPrintWriterServlet();
        MockFilterChain mockRedirectChain = new MockFilterChain(printWriterServlet, filter);

        mockRedirectChain.doFilter(request, response);

        Assert.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);
        
        testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(), 
                List.of(new String[] {"JSESSIONID","shib_idp_session","shib_idp_session_ss","existing_same_site"}),
                List.of(new String[] {"ignore_copy_over"}),5);
    }
    
    /**
     * Get the field from the filter (even if private), check the field is of type {@link Set}, and compare
     * the size of the set to the expected size.
     * 
     * @param fieldName the name of the field on the object of type {@link Map}.
     * @param expectedSize the expected size of the map.
     * @param filter the filter with the field to get.
     */
    private void testSameSiteMapSize(String fieldName, int expectedSize, Filter filter) {
        
        Object sameSiteSet = ReflectionTestUtils.getField(filter, fieldName);
        Assert.assertNotNull(sameSiteSet);
        Assert.assertTrue(sameSiteSet instanceof Map);
        Assert.assertEquals(((Map<?,?>)sameSiteSet).size(),expectedSize);
    }
    
    /**
     * Test the Set-Cookie headers in the response contain the {@literal SameSite=<sameSiteValue>} attribute if they are named
     * in the {@code cookiesWithSamesite} list, and do not if named in the {@code cookiesWithoutSameSite} list.
     * <p>
     * Also checks the number of Set-Cookie headers matches {@code numberOfHeaders}. This makes sure the filter
     * is not adding or removing headers during operation - it should only ever append the SameSite attribute
     * to existing cookies.
     * </p>
     * 
     * @param sameSiteValue the value of samesite to check for.
     * @param response the http servlet response.
     * @param cookiesWithSamesite the list of cookies that should have the {@literal SameSite=None} attribute set.
     * @param cookiesWithoutSameSite the list of cookies that should not have the {@literal SameSite} attribute set.
     * @param numberOfHeaders the number of Set-Cookie headers expected in the response.
     */
    private void testExpectedHeadersInResponse(final String sameSiteValue, final MockHttpServletResponse response, 
            final List<String> cookiesWithSamesite, final List<String> cookiesWithoutSameSite, final int numberOfHeaders) {
        
        final Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE); 
        
        Assert.assertEquals(headers.size(), numberOfHeaders);       
        
        for (String header : headers) {
            
            List<HttpCookie> cookies = HttpCookie.parse(header);
            Assert.assertNotNull(cookies);
            Assert.assertTrue(cookies.size()==1);
            Cookie cookie = response.getCookie(cookies.get(0).getName());
            Assert.assertNotNull(cookie);
            Assert.assertTrue(cookie instanceof MockCookie);
            MockCookie mockCookie = (MockCookie)cookie;
                      
            if (cookiesWithSamesite.contains(mockCookie.getName())) {
                Assert.assertNotNull(mockCookie.getSameSite());
                Assert.assertEquals(mockCookie.getSameSite(),sameSiteValue);                           
                      
            }
            else if (cookiesWithoutSameSite.contains(mockCookie.getName())) {
                Assert.assertNull(mockCookie.getSameSite());
                           
            }
        }
    }

    /**
     * Servlet that initiates a redirect on the response.
     */
    public class TestRedirectServlet implements Servlet {

        /** {@inheritDoc} */
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            Assert.assertNotNull(req, "HttpServletRequest was null");
            Assert.assertNotNull(res, "HttpServletResponse was null");
            ((HttpServletResponse) res).sendRedirect("/redirect");
        }

        /** {@inheritDoc} */
        public void init(ServletConfig config) throws ServletException {
        }

        /** {@inheritDoc} */
        public ServletConfig getServletConfig() {
            return null;
        }

        /** {@inheritDoc} */
        public String getServletInfo() {
            return null;
        }

        /** {@inheritDoc} */
        public void destroy() {
        }

    }

    /**
     * Servlet that opens an output stream on the response.
     */
    public class TestOutputStreamServlet implements Servlet {

        /** {@inheritDoc} */
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            Assert.assertNotNull(req, "HttpServletRequest was null");
            Assert.assertNotNull(res, "HttpServletResponse was null");

            // write nothing to the output stream.
            final Writer out = new OutputStreamWriter(((HttpServletResponse) res).getOutputStream(), "UTF-8");
            out.flush();

        }

        /** {@inheritDoc} */
        public void init(ServletConfig config) throws ServletException {
        }

        /** {@inheritDoc} */
        public ServletConfig getServletConfig() {
            return null;
        }

        /** {@inheritDoc} */
        public String getServletInfo() {
            return null;
        }

        /** {@inheritDoc} */
        public void destroy() {
        }

    }
    
    /**
     * Servlet that opens a print writer on the response.
     */
    public class TestPrintWriterServlet implements Servlet {

        /** {@inheritDoc} */
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            Assert.assertNotNull(req, "HttpServletRequest was null");
            Assert.assertNotNull(res, "HttpServletResponse was null");

            // write nothing to the print writer.
            PrintWriter writer = ((HttpServletResponse) res).getWriter();
            writer.flush();

        }

        /** {@inheritDoc} */
        public void init(ServletConfig config) throws ServletException {
        }

        /** {@inheritDoc} */
        public ServletConfig getServletConfig() {
            return null;
        }

        /** {@inheritDoc} */
        public String getServletInfo() {
            return null;
        }

        /** {@inheritDoc} */
        public void destroy() {
        }

    }

}
