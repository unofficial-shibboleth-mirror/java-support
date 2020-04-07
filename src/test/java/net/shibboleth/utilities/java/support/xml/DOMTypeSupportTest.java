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

package net.shibboleth.utilities.java.support.xml;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tests for {@link DOMTypeSupport};
 */
public class DOMTypeSupportTest {

    private ParserPool parserPool;
    private Element xsStringXSITypeElement;
    private Element noXSITypeElement;
    
    @BeforeClass public void setup() throws ComponentInitializationException, SAXException, IOException, XMLParserException {
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        parserPool = pool;
        
        DocumentBuilder builder = parserPool.getBuilder();
        Resource res = new ClassPathResource("/net/shibboleth/utilities/java/support/xml/getXSIType.xml");
        xsStringXSITypeElement = (Element) builder.parse(res.getInputStream()).getFirstChild();

        res = new ClassPathResource("/net/shibboleth/utilities/java/support/xml/noXSIType.xml");
        noXSITypeElement = (Element) builder.parse(res.getInputStream()).getFirstChild();
        
        if (null != builder) {
            parserPool.returnBuilder(builder);
        }

    }
    
    
    @Test public void testInstantToString() {
        Assert.assertEquals(DOMTypeSupport.instantToString(Instant.EPOCH.plusMillis(1000)), "1970-01-01T00:00:01.000Z", "Epoch plus one second");
        Assert.assertEquals(DOMTypeSupport.instantToString(Instant.EPOCH.plusMillis(-1000)), "1969-12-31T23:59:59.000Z", "Epoch minus one second");
    }

    @Test public void testStringToDuration() {
        Assert.assertEquals(DOMTypeSupport.stringToDuration("P0Y0M0DT00H00M01S"), Duration.ofSeconds(1), "One second duration");
        Assert.assertEquals(DOMTypeSupport.stringToDuration("-P1D"), Duration.ofDays(-1), "Back One day duration");
    }

    @Test public void testDurationToString() {
        // We have to check for two different possible return values because Oracle's and Xerces' implementations
        // are different.
        
        String onesec = DOMTypeSupport.durationToString(Duration.ofSeconds(1));
        Assert.assertTrue("P0Y0M0DT0H0M1.000S".equals(onesec) || "PT1.000S".equals(onesec), "One second duration");

        String backday = DOMTypeSupport.durationToString(Duration.ofDays(-1));
        Assert.assertTrue("-P0Y0M1DT0H0M0.000S".equals(backday) || "-P1DT0H0M0.000S".equals(backday), "Back one day duration");
    }
    
    @Test public void testGetXSIType() {
        Assert.assertEquals(DOMTypeSupport.getXSIType(xsStringXSITypeElement),
                new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"),
                "XSI type clash");
        Assert.assertNull(DOMTypeSupport.getXSIType(noXSITypeElement), "No xsiType expected");
    }

    @Test public void testHasXSIType() {
        Assert.assertTrue(DOMTypeSupport.hasXSIType(xsStringXSITypeElement)," Expected xsi:type");
        Assert.assertFalse(DOMTypeSupport.hasXSIType(noXSITypeElement), "No xsiType expected");
    }

}
