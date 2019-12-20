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

import java.net.MalformedURLException;

import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Tests building and parsing URLs with the builder.
 */
public class URLBuilderTest {
    
    /**
     * Test with scheme and host.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder1() throws MalformedURLException{
        String url = "http://www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), null);
        Assert.assertEquals(builder1.getQueryParams().size(), 0);
        Assert.assertEquals(builder1.getFragment(), null);
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
    
    /**
     * Test with scheme, host, and path.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder2() throws MalformedURLException{
        String url = "https://www.example.com/foo/index.html";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "https");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), "/foo/index.html");
        Assert.assertEquals(builder1.getQueryParams().size(), 0);
        Assert.assertEquals(builder1.getFragment(), null);
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
    
    /**
     * Test with scheme, host, port, path, and query params.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder3() throws MalformedURLException{
        String url = "http://www.example.com:8080/index.html?attrib1=value1&attrib2=value&attrib3";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), Integer.valueOf(8080));
        Assert.assertEquals(builder1.getPath(), "/index.html");
        Assert.assertEquals(builder1.getQueryParams().size(), 3);
        Assert.assertEquals(builder1.getFragment(), null);
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
    
    /**
     * Test with some odd values.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testValues() throws MalformedURLException{
        String url = "http://www.example.com/index.html?=value1&attrib2=&attrib3=val3=val3b";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getQueryParams().size(), 3);
        Assert.assertEquals(builder1.getQueryParams().get(0), new Pair<>((String)null, "value1"));
        Assert.assertEquals(builder1.getQueryParams().get(1), new Pair<>("attrib2", (String)null));
        Assert.assertEquals(builder1.getQueryParams().get(2), new Pair<>("attrib3", "val3=val3b"));
        Assert.assertEquals(builder1.getFragment(), null);
        
        // NOTE that we drop the name-less value and properly encode the third value 
        
        Assert.assertEquals(builder1.buildURL(), "http://www.example.com/index.html?attrib2&attrib3=val3%3Dval3b");
    }

    
    /**
     * Test with scheme, host, and fragment.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder4() throws MalformedURLException{
        String url = "https://www.example.com#anchor";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "https");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), null);
        Assert.assertEquals(builder1.getQueryParams().size(), 0);
        Assert.assertEquals(builder1.getFragment(), "anchor");
        
        Assert.assertEquals(builder1.buildURL(), url);
    }

    /**
     * Test with scheme, host, port, path, query params, and anchor.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder5() throws MalformedURLException{
        String url = "http://www.example.com/index.html?attrib1=value1&attrib2=value&attrib3#anchor";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), null);
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), "/index.html");
        Assert.assertEquals(builder1.getQueryParams().size(), 3);
        Assert.assertEquals(builder1.getFragment(), "anchor");
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
    
    /**
     * Test with scheme, username, password, and host.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder6() throws MalformedURLException{
        String url = "http://user:pass@www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), "user");
        Assert.assertEquals(builder1.getPassword(), "pass");
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), null);
        Assert.assertEquals(builder1.getQueryParams().size(), 0);
        Assert.assertEquals(builder1.getFragment(), null);
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
    
    /**
     * Test with scheme, username, and host.
     * @throws MalformedURLException if the URL is malformed
     */
    @Test
    public void testURLBuilder7() throws MalformedURLException{
        String url = "http://user@www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        Assert.assertEquals(builder1.getScheme(), "http");
        Assert.assertEquals(builder1.getUsername(), "user");
        Assert.assertEquals(builder1.getPassword(), null);
        Assert.assertEquals(builder1.getHost(), "www.example.com");
        Assert.assertEquals(builder1.getPort(), null);
        Assert.assertEquals(builder1.getPath(), null);
        Assert.assertEquals(builder1.getQueryParams().size(), 0);
        Assert.assertEquals(builder1.getFragment(), null);
        
        Assert.assertEquals(builder1.buildURL(), url);
    }
}