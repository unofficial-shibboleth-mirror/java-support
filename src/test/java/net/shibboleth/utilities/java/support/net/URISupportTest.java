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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.utilities.java.support.collection.Pair;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link URISupport}. */
public class URISupportTest {
    
    @Test public void testBuildQuery(){
        
    }
    
    @Test public void testSetFragment(){
        
    }
    
    @Test public void testSetHost(){
        
    }
    
    @Test public void testSetPath(){
        
    }
    
    @Test public void testSetPort(){
        
    }
    
    @Test public void testSetQuery(){
        
    }
    
    @Test public void testSetScheme(){
        
    }
    
    @Test public void testTrimOrNullFragment(){
        
    }
    
    @Test public void testTrimOrNullPath(){
        
    }
    
    @Test public void testTrimOrNullQuery(){
        
    }
    
    @Test public void testBuildQueryMap() {
        Map<String,String> map = null;
        
        map = URISupport.buildQueryMap(null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
        
        map = URISupport.buildQueryMap(Collections.<Pair<String,String>>emptyList());
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
        
        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("one", "1"));
        params.add(new Pair<>("two", "2"));
        params.add(new Pair<>("three", "3"));
        
        map = URISupport.buildQueryMap(params);
        Assert.assertNotNull(map);
        Assert.assertEquals(map.size(), 3);
        Assert.assertTrue(map.containsKey("one"));
        Assert.assertEquals(map.get("one"), "1");
        Assert.assertTrue(map.containsKey("two"));
        Assert.assertEquals(map.get("two"), "2");
        Assert.assertTrue(map.containsKey("three"));
        Assert.assertEquals(map.get("three"), "3");
    }
    
    @Test public void testGetRawQueryStringParameter() {
        // Chad's original java-support tests
        String param = URISupport.getRawQueryStringParameter(null, null);
        Assert.assertEquals(param, null);

        param = URISupport.getRawQueryStringParameter("", null);
        Assert.assertEquals(param, null);

        param = URISupport.getRawQueryStringParameter("", "");
        Assert.assertEquals(param, null);

        param = URISupport.getRawQueryStringParameter("foo", null);
        Assert.assertEquals(param, null);

        param = URISupport.getRawQueryStringParameter("foo", "");
        Assert.assertEquals(param, null);

        String queryString = "paramName1=paramValue1&paramName%3D=paramValue%26&paramName2";
        param = URISupport.getRawQueryStringParameter(queryString, "paramName1");
        Assert.assertEquals(param, "paramName1=paramValue1");

        param = URISupport.getRawQueryStringParameter(queryString, "paramName=");
        Assert.assertEquals(param, "paramName%3D=paramValue%26");

        param = URISupport.getRawQueryStringParameter(queryString, "paramName2");
        Assert.assertEquals(param, "paramName2");

        queryString = "?paramName1=paramValue1&paramName%3D=paramValue%26&paramName2#";
        param = URISupport.getRawQueryStringParameter(queryString, "paramName1");
        Assert.assertEquals(param, "paramName1=paramValue1");

        param = URISupport.getRawQueryStringParameter(queryString, "paramName=");
        Assert.assertEquals(param, "paramName%3D=paramValue%26");

        param = URISupport.getRawQueryStringParameter(queryString, "paramName2");
        Assert.assertEquals(param, "paramName2");
        
        // Tests brought in directly from v2 java-openws
        queryString = "ABC=123&Foo=Bar&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "Foo=Bar&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "ABC=123&Foo=Bar";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "Foo=Bar";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "ABC=123&Foo=Bar&XYZ456";
        param = URISupport.getRawQueryStringParameter(queryString, "NotThere");
        Assert.assertNull(param);
        
        queryString = "ABC=123&XYZ456";
        param  = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertNull(param);
        
        queryString = null;
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertNull(param);
        
        // Brent's new tests
        queryString = "ABC=123&FooFoo=BarBar&Foo=Bar&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "ABC=123&Foo=&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=");
        
        queryString = "Foo=&ABC=123&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=");
        
        queryString = "ABC=123&XYZ=456&Foo=";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=");
        
        queryString = "ABC=123&Foo&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo");
        
        queryString = "Foo&ABC=123&XYZ=456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo");
        
        queryString = "ABC=123&XYZ=456&Foo";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo");
        
        queryString = "ABC=123&&Foo=Bar&XYZ456";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=Bar");
        
        queryString = "Foo";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo");
        
        queryString = "Foo=";
        param = URISupport.getRawQueryStringParameter(queryString, "Foo");
        Assert.assertEquals(param, "Foo=");
    }

    @Test public void testParseQueryString() {
        List<Pair<String, String>> params = URISupport.parseQueryString(null);
        Assert.assertTrue(params.isEmpty());

        params = URISupport.parseQueryString("");
        Assert.assertTrue(params.isEmpty());

        String queryString = "paramName1=paramValue1&paramName%3D=paramValue%26&paramName2";
        params = URISupport.parseQueryString(queryString);
        Assert.assertTrue(params.contains(new Pair<>("paramName1", "paramValue1")));
        Assert.assertTrue(params.contains(new Pair<>("paramName=", "paramValue&")));
        Assert.assertTrue(params.contains(new Pair<>("paramName2", null)));

        queryString = "?paramName1=paramValue1&paramName%3D=paramValue%26&paramName2#";
        params = URISupport.parseQueryString(queryString);
        Assert.assertTrue(params.contains(new Pair<>("paramName1", "paramValue1")));
        Assert.assertTrue(params.contains(new Pair<>("paramName=", "paramValue&")));
        Assert.assertTrue(params.contains(new Pair<>("paramName2", null)));
    }
}