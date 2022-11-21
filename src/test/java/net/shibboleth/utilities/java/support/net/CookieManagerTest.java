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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.NonnullSupplier;

/** {@link CookieManager} unit test. */
public class CookieManagerTest {

    @Test public void testInitFailure() {
        CookieManager cm = new CookieManager();
        try {
            cm.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            
        }
    }

    @Test public void testInitSuccess() throws ComponentInitializationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        CookieManager cm = new CookieManager();
        cm.setHttpServletRequestSupplier(new NonnullSupplier<>() { public HttpServletRequest get() {return request;}});
        cm.setHttpServletResponseSupplier(new NonnullSupplier<>() { public HttpServletResponse get() {return response;}});
        cm.initialize();
    }

    @Test public void testCookieWithPath() throws ComponentInitializationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        CookieManager cm = new CookieManager();
        cm.setHttpServletRequestSupplier(new NonnullSupplier<>() { public HttpServletRequest get() {return request;}});
        cm.setHttpServletResponseSupplier(new NonnullSupplier<>() { public HttpServletResponse get() {return response;}});
        cm.setCookiePath("/idp");
        cm.initialize();

        cm.addCookie("foo", "bar");

        Cookie cookie = response.getCookie("foo");
        Assert.assertNotNull(cookie);
        Assert.assertEquals(cookie.getValue(), "bar");
        Assert.assertEquals(cookie.getPath(), "/idp");
        Assert.assertNull(cookie.getDomain());
        Assert.assertTrue(cookie.getSecure());
        Assert.assertEquals(cookie.getMaxAge(), -1);
    }
    
    @Test public void testCookieWithPathOldSetters() throws ComponentInitializationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        CookieManager cm = new CookieManager();
        cm.setHttpServletRequest(request);
        cm.setHttpServletResponse(response);
        cm.setCookiePath("/idp");
        cm.initialize();

        cm.addCookie("foo", "bar");

        Cookie cookie = response.getCookie("foo");
        Assert.assertNotNull(cookie);
        Assert.assertEquals(cookie.getValue(), "bar");
        Assert.assertEquals(cookie.getPath(), "/idp");
        Assert.assertNull(cookie.getDomain());
        Assert.assertTrue(cookie.getSecure());
        Assert.assertEquals(cookie.getMaxAge(), -1);
    }


    @Test public void testCookieNoPath() throws ComponentInitializationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/idp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        CookieManager cm = new CookieManager();
        cm.setHttpServletRequestSupplier(new NonnullSupplier<>() { public HttpServletRequest get() {return request;}});
        cm.setHttpServletResponseSupplier(new NonnullSupplier<>() { public HttpServletResponse get() {return response;}});
        cm.initialize();
        
        cm.addCookie("foo", "bar");
        
        Cookie cookie = response.getCookie("foo");
        Assert.assertNotNull(cookie);
        Assert.assertEquals(cookie.getValue(), "bar");
        Assert.assertEquals(cookie.getPath(), "/idp");
        Assert.assertNull(cookie.getDomain());
        Assert.assertTrue(cookie.getSecure());
        Assert.assertEquals(cookie.getMaxAge(), -1);
    }

    @Test public void testCookieUnset() throws ComponentInitializationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/idp");
        request.setCookies(new Cookie("foo", "bar"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        CookieManager cm = new CookieManager();
        cm.setHttpServletRequestSupplier(new NonnullSupplier<>() { public HttpServletRequest get() {return request;}});
        cm.setHttpServletResponseSupplier(new NonnullSupplier<>() { public HttpServletResponse get() {return response;}});
        cm.initialize();
        
        cm.unsetCookie("foo");
        
        Cookie cookie = response.getCookie("foo");
        Assert.assertNotNull(cookie);
        Assert.assertNull(cookie.getValue());
        Assert.assertEquals(cookie.getPath(), "/idp");
        Assert.assertNull(cookie.getDomain());
        Assert.assertTrue(cookie.getSecure());
        Assert.assertEquals(cookie.getMaxAge(), 0);
    }
}