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

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

/**
 * Unit test for {@link DDFSupport}.
 */
public class DDFSupportTest {
    
    @Nullable BasicParserPool parserPool;
    
    /**
     * Init parser.
     * 
     * @throws ComponentInitializationException 
     */
    @BeforeClass
    public void setUp() throws ComponentInitializationException {
        parserPool = new BasicParserPool();
        parserPool.initialize();
    }
    
   /**
    * Teardown. 
    */
    @AfterClass
   public void tearDown() {
       parserPool.destroy();
   }
    
    /**
     * Test conversion.
     * 
     * @throws IOException 
     * @throws XMLParserException 
     */
    @Test
    public void test() throws XMLParserException, IOException {
        final Document doc = parserPool.parse(getClass().getResourceAsStream("zork.xml"));
        final DDF ddf = DDFSupport.fromElement(doc.getDocumentElement());
        
        Assert.assertNotNull(ddf);
        Assert.assertEquals(ddf.name(), "root");
        Assert.assertEquals(ddf.getmember("foo").string(), "bar");

        final List<DDF> children = ddf.getmember(DDFSupport.CHILD_ELEMENTS_MEMBER).asList();
        Assert.assertEquals(children.size(), 2);
        
        DDF child = children.get(0);
        Assert.assertEquals(child.name(), "zork");
        Assert.assertEquals(child.getmember("zorkmids").integer(), 10);
        Assert.assertEquals(child.getmember("underground").string(), "true");
        Assert.assertTrue(child.getmember(DDFSupport.CONTENT_MEMBER).isnull());
        Assert.assertTrue(child.getmember(DDFSupport.CHILD_ELEMENTS_MEMBER).isnull());

        child = children.get(1);
        Assert.assertEquals(child.name(), "frobnitz");
        Assert.assertEquals(child.getmember(DDFSupport.CONTENT_MEMBER).string(), "grue");
        Assert.assertTrue(child.getmember(DDFSupport.CHILD_ELEMENTS_MEMBER).isnull());

        Assert.assertTrue(ddf.getmember(DDFSupport.CONTENT_MEMBER).isnull());
    }

}