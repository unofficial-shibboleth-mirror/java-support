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

import java.util.Collections;
import java.util.Set;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.net.MediaType;

/** {@link HttpServletSupport} unit test. */
public class HttpServletSupportTest {

    @Test public void testAddNoCacheHeaders(){
        MockHttpServletResponse response = new MockHttpServletResponse();
        Assert.assertNull(response.getHeaderValue("Cache-control"));
        Assert.assertNull(response.getHeaderValue("Pragma"));
        
        HttpServletSupport.addNoCacheHeaders(response);
        Assert.assertEquals(response.getHeaderValue("Cache-control"), "no-cache, no-store");
        Assert.assertEquals(response.getHeaderValue("Pragma"), "no-cache");
    }
    
    @Test public void testGetFullRequestURI(){
//        mock request doesn't do what we want, need to figure out something better
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        
//        request.setScheme("http");
//        request.setServerName("example.org");
//        request.setRequestURI("/foo/bar");
//        request.setQueryString("baz=true");        
//        Assert.assertEquals(HttpServletSupport.getFullRequestUri(request), "http://example.org/foo/bar?baz=true");
//        
//        request.setScheme("https");
//        request.setServerPort(8443);
//        request.setQueryString(null);
//        Assert.assertEquals(HttpServletSupport.getFullRequestUri(request), "https://example.org:8443/foo/bar");
    }
    
    @Test public void testGetRequestPathWithoutContext(){
        
    }
    
    @Test public void testSetContentType(){
        
    }
    
    @Test public void testSetUTF8Encoding(){
        
    }
    
    @Test public void testValidateContentType() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // No Content-type
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Collections.singleton(MediaType.XML_UTF_8), 
                true, 
                false));
        
        Assert.assertFalse(HttpServletSupport.validateContentType(request, 
                Collections.singleton(MediaType.XML_UTF_8), 
                false, 
                false));
        
        // With charset parameter
        request.setContentType("text/xml; charset=utf-8");
        
        Assert.assertFalse(HttpServletSupport.validateContentType(request, 
                Collections.singleton(MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8.withoutParameters(), MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.ANY_TEXT_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.ANY_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        // No parameters
        request.setContentType("text/xml");
        
        Assert.assertFalse(HttpServletSupport.validateContentType(request, 
                Collections.singleton(MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                false));
        
        // Not valid, because the text/xml valid type includes parameters
        Assert.assertFalse(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.XML_UTF_8.withoutParameters(), MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.ANY_TEXT_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(HttpServletSupport.validateContentType(request, 
                Set.of(MediaType.ANY_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
    }
}