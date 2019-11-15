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

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.net.MediaType;

/**
 *
 */
public class MediaTypeSupportTest {
    
    @Test public void testValidateContentType() {
        String contentType = null;
        
        // No Content-type
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8), 
                true, 
                false));
        
        Assert.assertFalse(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8), 
                false, 
                false));
        
        // With charset parameter
        contentType = "text/xml; charset=utf-8";
        
        Assert.assertFalse(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8.withoutParameters(), MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.ANY_TEXT_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.ANY_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        // No parameters
        contentType = "text/xml";
        
        Assert.assertFalse(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.create("application", "foobar")), 
                true, 
                false));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                false));
        
        // Not valid, because the text/xml valid type includes parameters
        Assert.assertFalse(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.XML_UTF_8.withoutParameters(), MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.ANY_TEXT_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
        Assert.assertTrue(MediaTypeSupport.validateContentType(contentType, 
                Set.of(MediaType.ANY_TYPE, MediaType.create("application", "foobar")), 
                true, 
                true));
        
    }

}
