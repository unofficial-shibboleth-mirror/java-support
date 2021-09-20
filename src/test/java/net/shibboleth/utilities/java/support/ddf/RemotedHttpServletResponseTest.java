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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link RemotedHttpServletResponse}.
 */
public class RemotedHttpServletResponseTest {
    
    private DDF obj;
    private RemotedHttpServletResponse resp;
    
    @BeforeMethod
    public void setUp() {
        obj = new DDF();
        resp = new RemotedHttpServletResponse(obj);
    }
    
    @Test
    public void testBasics() throws IOException {
        
        resp.setContentType("text/xml");
        resp.setHeader("Cache-Control", "private");
        resp.addHeader("foo", "foo");
        resp.setHeader("Foo", "bar");
        resp.addDateHeader("Foo", Instant.now().toEpochMilli());
        resp.addIntHeader("Bar", 42);
        resp.setContentLength(42);
        
        final Cookie cookie = new Cookie("cookie1", "value1");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/idp");
        resp.addCookie(cookie);
        
        assertEquals(resp.getContentType(), "text/xml");
        assertEquals((Set<String>) resp.getHeaderNames(), Set.of("Content-Type", "Cache-Control", "Foo", "Bar", "Content-Length", "Set-Cookie"));
        assertTrue(resp.getHeaders("foo").contains("bar"));
        assertEquals(resp.getHeader("Bar"), "42");
        
        final DDF headers = obj.getmember("headers");
        assertTrue(headers.islist());
        final DDF cheader = headers.asList().stream().filter(ddf -> "Set-Cookie".equalsIgnoreCase(ddf.name())).findFirst().orElseThrow();
        assertEquals(cheader.string(), "cookie1=value1; Path=/idp; Secure; HttpOnly");
    }

    @Test
    public void testRedirect() throws IOException {
        resp.sendRedirect("http://localhost");

        assertTrue(resp.isCommitted());
        assertEquals(obj.getmember("redirect").string(), "http://localhost");
        
        try {
            resp.getOutputStream();
            fail("Response should have been committed.");
        } catch (final IllegalStateException e) {
            
        }
    }
    
    @Test
    public void testResponseStream() throws IOException {
        resp.setBufferSize(3);
        resp.setStatus(200);
        try (final OutputStream os = resp.getOutputStream()) {
            os.write("zorkmid".getBytes("UTF-8"));
        }
        
        assertTrue(resp.isCommitted());
        assertEquals(obj.getmember("response.status").integer(), Integer.valueOf(200));
        assertEquals(obj.getmember("response.data").unsafe_string(), "zorkmid".getBytes("UTF-8"));
    }
    
    @Test
    public void testResponseStream2() throws IOException {
        resp.setBufferSize(3);
        resp.setStatus(200);
        try (final OutputStream os = resp.getOutputStream()) {
            os.write("zorkmid☯️".getBytes("ISO-8859-1"));
        }
        
        assertTrue(resp.isCommitted());
        assertEquals(obj.getmember("response.status").integer(), Integer.valueOf(200));
        assertEquals(obj.getmember("response.data").unsafe_string(), "zorkmid☯️".getBytes("ISO-8859-1"));
    }
    
    @Test
    public void testWriter() throws IOException {
        resp.setBufferSize(3);
        resp.setStatus(200);
        try (final PrintWriter pw = resp.getWriter()) {
            pw.print("zorkmid");
        }
        
        assertTrue(resp.isCommitted());
        assertEquals(obj.getmember("response.status").integer(), Integer.valueOf(200));
        assertEquals(obj.getmember("response.data").unsafe_string(), "zorkmid".getBytes("UTF-8"));
    }

    @Test
    public void testWriter2() throws IOException {
        resp.setBufferSize(3);
        resp.setStatus(200);
        try (final PrintWriter pw = resp.getWriter()) {
            pw.print("zorkmid☯️");
        }
        
        assertTrue(resp.isCommitted());
        assertEquals(obj.getmember("response.status").integer(), Integer.valueOf(200));
        assertEquals(obj.getmember("response.data").unsafe_string(), "zorkmid☯️".getBytes("UTF-8"));
    }
}