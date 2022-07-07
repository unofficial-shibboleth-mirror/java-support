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

package net.shibboleth.utilities.java.support.ddf;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.Cookie;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * Unit test for {@link RemotedHttpServletRequest}.
 */
@Ignore
public class RemotedHttpServletRequestTest {
    
    private DDF obj;
    private RemotedHttpServletRequest req;
    
    @BeforeMethod
    public void setUp() {
        obj = new DDF();
        req = new RemotedHttpServletRequest(obj);
    }
    
    @Test
    public void testEmpty() throws IOException {
        assertEquals(req.getContentLength(), -1);
        assertEquals(req.getContentType(), null);
        assertEquals(req.getCookies(), null);
        assertEquals(req.getHeader("foo"), null);
        assertEquals(req.getInputStream().read(), -1);
        assertEquals(req.getMethod(), null);
        assertEquals(req.getParameter("foo"), null);
        assertEquals(req.getQueryString(), null);
        assertEquals(req.getRemoteAddr(), null);
        assertEquals(req.getRemoteUser(), null);
        assertEquals(req.getRequestURI(), null);
        assertEquals(req.getRequestURL().toString(), "");
        assertEquals(req.getScheme(), null);
        assertFalse(req.isSecure());
        assertEquals(req.getServerPort(), -1);
    }

    @Test
    public void testBasics() throws IOException {
        obj.structure();
        obj.addmember("content_length").integer(100);
        obj.addmember("content_type").string("text/xml");
        obj.addmember("method").string("POST");
        obj.addmember("body").string("<foo/>");
        obj.addmember("port").integer(80);
        obj.addmember("client_addr").string("127.0.0.1");
        obj.addmember("remote_user").string("jdoe");
        obj.addmember("hostname").unsafe_string("localhost".getBytes("UTF-8"));
        obj.addmember("uri").unsafe_string("/endpoint".getBytes("UTF-8"));
        obj.addmember("url").unsafe_string("http://localhost/endpoint".getBytes("UTF-8"));
        obj.addmember("scheme").string("http");
        
        assertEquals(req.getContentLength(), 100);
        assertEquals(req.getContentType(), "text/xml");
        assertEquals(req.getCookies(), null);
        assertEquals(req.getHeader("foo"), null);
        assertEquals(Files.streamToString(req.getInputStream()), "<foo/>");
        assertEquals(req.getMethod(), "POST");
        assertEquals(req.getParameter("foo"), null);
        assertEquals(req.getQueryString(), null);
        assertEquals(req.getRemoteAddr(), "127.0.0.1");
        assertEquals(req.getRemoteUser(), "jdoe");
        assertEquals(req.getServerName(), "localhost");
        assertEquals(req.getRequestURI(), "/endpoint");
        assertEquals(req.getRequestURL().toString(), "http://localhost/endpoint");
        assertEquals(req.getScheme(), "http");
        assertFalse(req.isSecure());
        assertEquals(req.getServerPort(), 80);        
    }
    
    @Test
    public void testOneQueryParameter() throws IOException {
        obj.structure();
        obj.addmember("content_type").string("text/xml");
        obj.addmember("body").string("<foo/>");
        obj.addmember("query").string("foo=bar+baz");
        
        assertEquals(req.getParameterNames().nextElement(), "foo");
        assertEquals(req.getParameter("foo"), "bar baz");
    }

    @Test
    public void testMultiQueryParameters() throws IOException {
        obj.structure();
        obj.addmember("content_type").string("text/xml");
        obj.addmember("body").string("<foo/>");
        obj.addmember("query").string("foo=bar+baz&zork=grue&foo=baf");
        
        assertEquals(req.getParameter("foo"), "bar baz");
        assertEquals(req.getParameterValues("foo"), List.of("bar baz", "baf").toArray());
        assertEquals(req.getParameter("zork"), "grue");
    }

    @Test
    public void testFormParameters() throws IOException {
        obj.structure();
        obj.addmember("content_type").string("application/x-www-form-urlencoded");
        obj.addmember("body").string("foo=baf");
        obj.addmember("query").string("foo=bar+baz&zork=grue");
        
        assertEquals(req.getParameter("foo"), "bar baz");
        assertEquals(req.getParameterValues("foo"), List.of("bar baz", "baf").toArray());
        assertEquals(req.getParameter("zork"), "grue");
    }

    @Test
    public void testHeaders() throws IOException {
        obj.structure();
        obj.addmember("headers.foo").unsafe_string("bar".getBytes("UTF-8"));
        obj.addmember("headers.zork").unsafe_string("grue".getBytes("UTF-8"));
        
        assertEquals(req.getHeaders("foo").nextElement(), "bar");
        assertEquals(req.getHeader("zork"), "grue");
        assertNull(req.getHeader("baz"));
    }

    @Test
    public void testCookie() throws IOException {
        obj.structure();
        obj.addmember("headers.Cookie").unsafe_string("foo=bar;".getBytes("UTF-8"));
        
        final Cookie[] cookies = req.getCookies();
        
        assertEquals(cookies.length, 1);
        assertEquals(cookies[0].getName(), "foo");
        assertEquals(cookies[0].getValue(), "bar");
    }

    @Test
    public void testCookies() throws IOException {
        obj.structure();
        obj.addmember("headers.Cookie").unsafe_string("foo=bar; zork=grue".getBytes("UTF-8"));
        
        final Cookie[] cookies = req.getCookies();
        
        assertEquals(cookies.length, 2);
        assertEquals(cookies[0].getName(), "foo");
        assertEquals(cookies[0].getValue(), "bar");
        assertEquals(cookies[1].getName(), "zork");
        assertEquals(cookies[1].getValue(), "grue");
    }

}