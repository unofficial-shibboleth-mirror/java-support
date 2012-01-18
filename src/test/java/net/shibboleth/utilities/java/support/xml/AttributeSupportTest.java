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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resource.ClasspathResource;
import net.shibboleth.utilities.java.support.resource.ResourceException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tests for {@link AttributeSupport};
 */
public class AttributeSupportTest {

    // Contants to test againsy
    private static final String TEST_NS = "http://example.org/NameSpace";

    private static final String TEST_PREFIX = "testns";

    private static final String TEST_ID_ATTRIBUTE = "testAttributeName";

    private static final String TEST_ID_PREFIXEDATTRIBUTE = TEST_PREFIX + ":" + TEST_ID_ATTRIBUTE;

    private static final String TEST_ID_ATTRIBUTE_VALUE = "IDAttrVALUE";

    // Set up at start of all methods
    private QName idAttrQName;

    private Element goodBaseIdSpaceLang;

    private Element noBaseIdSpaceLang;

    private Element badSpace;

    private Element preserveSpace;

    private Element attributes;

    private Element createdElement;

    // Reset before each method.
    private Document document;

    private BasicParserPool parserPool;

    @BeforeTest public void setUp() throws XMLParserException, ComponentInitializationException, SAXException,
            IOException, ResourceException {
        parserPool = new BasicParserPool();
        parserPool.initialize();

        DocumentBuilder builder = parserPool.getBuilder();
        try {
            idAttrQName = new QName(TEST_NS, TEST_ID_ATTRIBUTE, TEST_PREFIX);

            ClasspathResource resource =
                    new ClasspathResource("data/net/shibboleth/utilities/java/support/xml/attributeSupportTest.xml");
            resource.initialize();
            Document testFile = builder.parse(resource.getInputStream());

            Element root = (Element) testFile.getFirstChild();

            // Skip whitespace, grab first element
            goodBaseIdSpaceLang = (Element) root.getFirstChild().getNextSibling();
            Assert.assertEquals(goodBaseIdSpaceLang.getLocalName(), "GoodBaseIdSpaceLang");

            noBaseIdSpaceLang = (Element) goodBaseIdSpaceLang.getNextSibling().getNextSibling();
            Assert.assertEquals(noBaseIdSpaceLang.getLocalName(), "NoBaseIdSpaceLang");

            badSpace = (Element) noBaseIdSpaceLang.getNextSibling().getNextSibling();
            Assert.assertEquals(badSpace.getLocalName(), "BadSpace");

            preserveSpace = (Element) badSpace.getNextSibling().getNextSibling();
            Assert.assertEquals(preserveSpace.getLocalName(), "PreserveSpace");

            attributes = (Element) preserveSpace.getNextSibling().getNextSibling();
            Assert.assertEquals(attributes.getLocalName(), "AttributeTest");

        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    @BeforeMethod public void resetCreatedElement() throws XMLParserException {

        DocumentBuilder builder = parserPool.getBuilder();
        try {
            document = builder.newDocument();
            createdElement = document.createElement("TestElement");
            Attr attr = document.createAttributeNS(TEST_NS, TEST_ID_PREFIXEDATTRIBUTE);
            attr.setValue(TEST_ID_ATTRIBUTE_VALUE);
            Element el = document.createElement("ChildElement");
            el.setAttributeNode(attr);
            el.setIdAttributeNode(attr, true);
            createdElement.appendChild(el);
        } finally {
            parserPool.returnBuilder(builder);

        }
    }

    /**
     * Strictly speaking this test is for the parser. But we may significant assumptions on this test passing.
     * 
     * @throws XMLParserException if badness happens.
     * @throws ComponentInitializationException if badness happens.
     * @throws IOException if badness happens.
     */
    @Test public void testBadNS() throws XMLParserException, ComponentInitializationException, IOException,
            ResourceException {
        DocumentBuilder builder = parserPool.getBuilder();

        ClasspathResource resource = new ClasspathResource("data/net/shibboleth/utilities/java/support/xml/badNS1.xml");
        resource.initialize();
        boolean thrown = false;
        Document file = null;
        try {
            file = builder.parse(resource.getInputStream());
        } catch (SAXException e) {
            thrown = true;
        }
        Assert.assertTrue(
                thrown,
                "xmlns: declaration with name other than xml and namespace of http://www.w3.org/XML/1998/namespace should throw an error ");

        resource = new ClasspathResource("data/net/shibboleth/utilities/java/support/xml/badNS2.xml");
        resource.initialize();
        thrown = false;
        try {
            file = builder.parse(resource.getInputStream());
        } catch (SAXException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown,
                "xmlns:xml with namespace other than http://www.w3.org/XML/1998/namespace should throw an error ");
        Assert.assertNull(file, "shut up compiler");

        parserPool.returnBuilder(builder);
    }

    @Test public void testGetXMLId() {
        Assert.assertEquals(AttributeSupport.getXMLId(goodBaseIdSpaceLang), "identifierGoodBaseIdSpaceLang",
                "Identifier mismatch");
        Assert.assertNull(AttributeSupport.getXMLId(noBaseIdSpaceLang), "Identifier found erroneously");
        Assert.assertEquals(AttributeSupport.getXMLId(badSpace), "identifierBadSpace", "Identifier mismatch");
        Assert.assertEquals(AttributeSupport.getXMLId(preserveSpace), "identifierPreserveSpace", "Identifier mismatch");

        // test Add now that we know that get works
        boolean thrown = false;
        try {
            AttributeSupport.addXMLId(createdElement, null);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLId");

        thrown = false;
        try {
            AttributeSupport.addXMLId(null, "fr");
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLId");

        Assert.assertNull(AttributeSupport.getXMLId(createdElement), "xml:space found erroneously (test setup failure)");
        AttributeSupport.addXMLId(createdElement, TEST_ID_ATTRIBUTE_VALUE);
        Assert.assertEquals(AttributeSupport.getXMLId(createdElement), TEST_ID_ATTRIBUTE_VALUE, "addXMLId failed");

    }

    @Test public void testXMLBase() {
        Assert.assertEquals(AttributeSupport.getXMLBase(goodBaseIdSpaceLang), "http://example.org/base",
                "xml:base mismatch");
        Assert.assertNull(AttributeSupport.getXMLBase(noBaseIdSpaceLang), "xml:base found erroneously");

        // test Add
        boolean thrown = false;
        try {
            AttributeSupport.addXMLBase(createdElement, null);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLBase(null, "foo");
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLBase");

        Assert.assertNull(AttributeSupport.getXMLBase(createdElement),
                "xml:base found erroneously (test setup failure)");
        AttributeSupport.addXMLBase(createdElement, TEST_NS);
        Assert.assertEquals(AttributeSupport.getXMLBase(createdElement), TEST_NS, "addXMLBase failed");

    }

    @Test public void testXMLSpace() {
        Assert.assertEquals(AttributeSupport.getXMLSpace(goodBaseIdSpaceLang), XmlSpace.DEFAULT, "xml:space mismatch");
        Assert.assertNull(AttributeSupport.getXMLSpace(noBaseIdSpaceLang), "xml:space found erroneously");
        Assert.assertNull(AttributeSupport.getXMLSpace(badSpace), "xml:space found erroneously");

        Assert.assertEquals(AttributeSupport.getXMLSpace(preserveSpace), XmlSpace.PRESERVE, "xml:space mismatch");

        // test Add
        boolean thrown = false;
        try {
            AttributeSupport.addXMLSpace(createdElement, null);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLSpace(null, XmlSpace.DEFAULT);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLSpace");

        Assert.assertNull(AttributeSupport.getXMLSpace(createdElement),
                "xml:space found erroneously (test setup failure)");
        AttributeSupport.addXMLSpace(createdElement, XmlSpace.DEFAULT);
        Assert.assertEquals(AttributeSupport.getXMLSpace(createdElement), XmlSpace.DEFAULT, "addXMLSpace failed");
    }

    @Test public void testXMLLang() {
        Assert.assertEquals(AttributeSupport.getXMLLang(goodBaseIdSpaceLang), "fr-ca", "xml:lang mismatch");
        Assert.assertNull(AttributeSupport.getXMLLang(noBaseIdSpaceLang), "xml:lang found erroneously");

        Locale locale = AttributeSupport.getXMLLangAsLocale(goodBaseIdSpaceLang);

        // Assert.assertEquals(locale.getCountry(), "ca", "getXMLLangAsLocale test (country)");
        Assert.assertEquals(locale.getLanguage(), "fr", "getXMLLangAsLocale test (country)");

        Locale current = Locale.getDefault();
        Locale testLocale = new Locale("en", "gb");

        try {
            Locale.setDefault(testLocale);
            Assert.assertEquals(AttributeSupport.getXMLLangAsLocale(noBaseIdSpaceLang), testLocale,
                    "Defaulting behavior of getXMLLangAsLocale");
        } finally {
            Locale.setDefault(current);
        }

        // test Add
        boolean thrown = false;
        try {
            AttributeSupport.addXMLLang(createdElement, null);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLLang(null, "fr");
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLLang");

        Assert.assertNull(AttributeSupport.getXMLLang(createdElement),
                "xml:space found erroneously (test setup failure)");
        AttributeSupport.addXMLLang(createdElement, "fr");
        Assert.assertEquals(AttributeSupport.getXMLLang(createdElement), "fr", "addXMLLang failed");
    }

    @Test public void testGetID() {
        Assert.assertNull(AttributeSupport.getIdAttribute(null), "ID of null is null");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "ID of non id'd element is null");

        Attr attr = AttributeSupport.getIdAttribute((Element) createdElement.getFirstChild());
        Assert.assertEquals(attr.getValue(), TEST_ID_ATTRIBUTE_VALUE, "ID Attribute value mismatch");
        Assert.assertEquals(attr.getName(), TEST_ID_PREFIXEDATTRIBUTE, "ID Attribute name mismatch");
        Assert.assertEquals(attr.getNamespaceURI(), TEST_NS, "ID Attribute namespace mismatch");

    }

    @Test public void testHasAttribute() {
        Assert.assertFalse(AttributeSupport.hasAttribute(createdElement, idAttrQName), "Attribute lookup by QName");

        Assert.assertTrue(AttributeSupport.hasAttribute(goodBaseIdSpaceLang, XmlConstants.XML_BASE_ATTRIB_NAME),
                "attribute lookup by QName from file");
        Assert.assertFalse(AttributeSupport.hasAttribute(noBaseIdSpaceLang, XmlConstants.XML_BASE_ATTRIB_NAME),
                "attribute lookup by QName from file");

        Element child = (Element) createdElement.getFirstChild();
        Assert.assertTrue(AttributeSupport.hasAttribute(child, idAttrQName),
                "attribute lookup by QName in created element");
        Assert.assertTrue(
                AttributeSupport.hasAttribute(child, new QName(TEST_NS, TEST_ID_ATTRIBUTE, "xx" + TEST_PREFIX)),
                "attribute lookup by QName with changed prefix in created element");
        Assert.assertFalse(
                AttributeSupport.hasAttribute(child, new QName(TEST_NS + "/f", TEST_ID_ATTRIBUTE, TEST_PREFIX)),
                "attribute lookup by QName with changed NS in created element");
    }

    @Test(dependsOnMethods = {"testHasAttribute"}) public void testConstructAttribute() {
        Assert.assertFalse(AttributeSupport.hasAttribute(createdElement, idAttrQName), "precondition");
        createdElement.setAttributeNode(AttributeSupport.constructAttribute(document, idAttrQName));
        Assert.assertTrue(AttributeSupport.hasAttribute(createdElement, idAttrQName), "test constructAttribute(QName)");

        QName testQName = new QName(TEST_NS, TEST_ID_ATTRIBUTE + "XX", TEST_PREFIX);
        Assert.assertFalse(AttributeSupport.hasAttribute(createdElement, testQName), "precondition");
        createdElement.setAttributeNode(AttributeSupport.constructAttribute(document, TEST_NS,
                TEST_ID_ATTRIBUTE + "XX", TEST_PREFIX));
        Assert.assertTrue(AttributeSupport.hasAttribute(createdElement, testQName), "test constructAttribute(QName)");
    }

    @Test public void testRemoveAttribute() {
        Assert.assertFalse(AttributeSupport.removeAttribute(createdElement, idAttrQName), "Attribute remove by QName");
        Element child = (Element) createdElement.getFirstChild();
        Assert.assertTrue(AttributeSupport.removeAttribute(child, idAttrQName),
                "remove lookup by QName in created element");
        Assert.assertFalse(AttributeSupport.hasAttribute(child, idAttrQName),
                "attribute lookup by QName after it has been removed");
    }

    @Test public void testGetAttributeMethods() {
        // getAttribute(Element, QName)
        Assert.assertNull(AttributeSupport.getAttribute(noBaseIdSpaceLang, XmlConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup by QName)");
        Attr attr = AttributeSupport.getAttribute(goodBaseIdSpaceLang, XmlConstants.XML_ID_ATTRIB_NAME);
        Assert.assertNotNull(attr, "Should have found xml:id attribute");
        Assert.assertEquals(attr.getValue(), "identifierGoodBaseIdSpaceLang",
                "Should have found correct attribute by value for xml_id attribute");

        // getAttributeValue(Element, QName)
        Assert.assertNull(AttributeSupport.getAttributeValue(goodBaseIdSpaceLang, null),
                "no xml:id (lookup value with null QName)");
        Assert.assertNull(AttributeSupport.getAttributeValue(null, XmlConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup value with null element)");
        Assert.assertNull(AttributeSupport.getAttributeValue(noBaseIdSpaceLang, XmlConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup value by QName)");
        Assert.assertEquals(AttributeSupport.getAttributeValue(goodBaseIdSpaceLang, XmlConstants.XML_ID_ATTRIB_NAME),
                "identifierGoodBaseIdSpaceLang", "Should have found correct value for xml:id attribute by QName");

        // getAttributeValue(Element, String, String)
        Assert.assertNull(AttributeSupport.getAttributeValue(badSpace, XmlConstants.XML_NS, null),
                "no value lookup with null name)");
        Assert.assertNull(AttributeSupport.getAttributeValue(badSpace, XmlConstants.XML_NS, ""),
                "no value lookup with empty name)");
        Assert.assertNull(AttributeSupport.getAttributeValue(null, XmlConstants.XML_NS, "space"),
                "no value lookup with null element)");
        Assert.assertNull(AttributeSupport.getAttributeValue(noBaseIdSpaceLang, XmlConstants.XML_NS, "space"),
                "no xml:space (lookup value by name)");
        Assert.assertEquals(AttributeSupport.getAttributeValue(badSpace, XmlConstants.XML_NS, "space"), "wibble",
                "Should have found correct value for xml:space attribute by name");

        // getAttributeValueAsBoolean(Attribute)
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getAttributeValueAsBoolean(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertFalse(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrZero"))), "0 should be false");
        Assert.assertTrue(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrOne"))), "1 should be true");
        Assert.assertNull(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrThree"))), "2 should be null");
        Assert.assertFalse(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrFalse"))), "false should be false");
        Assert.assertTrue(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrTrue"))), "true should be true");
        Assert.assertNull(AttributeSupport.getAttributeValueAsBoolean(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrTrueCaps"))), "TRUE should be null");

        // getAttributeValueAsList(Attribute)
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertTrue(AttributeSupport.getAttributeValueAsList(null).isEmpty(),
                "null attribute should give empty list");
        Assert.assertTrue(
                AttributeSupport.getAttributeValueAsList(
                        AttributeSupport.getAttribute(attributes, new QName(TEST_NS, "testAttrEmpty"))).isEmpty(),
                "\"\" attribute should give empty list");
        Assert.assertEquals(AttributeSupport.getAttributeValueAsList(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrZero"))), Arrays.asList("0"), "attribute called testAttrZero");
        Assert.assertEquals(AttributeSupport.getAttributeValueAsList(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrList"))), Arrays.asList("0", "1", "2", "3", "4", "5", "6"),
                "attribute called testAttrList");

        // getAttributeValueAsQName(Attribute)
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getAttributeValueAsQName(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getAttributeValueAsQName(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertEquals(AttributeSupport.getAttributeValueAsQName(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrQName"))), idAttrQName, "attribute called testAttrQName");
        Assert.assertEquals(AttributeSupport.getAttributeValueAsQName(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrZero"))), new QName("0"), "attribute called testAttrZero");

        // getDateTimeAttributeAsLong
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getDateTimeAttributeAsLong(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getDateTimeAttributeAsLong(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertNull(AttributeSupport.getDateTimeAttributeAsLong(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"0\" should be null");
        Assert.assertEquals(
                AttributeSupport.getDateTimeAttributeAsLong(
                        AttributeSupport.getAttribute(attributes, new QName(TEST_NS, "testAttrEpochPlusOneSec")))
                        .intValue(), 1000, "attribute called testAttrEpochPlusOneSec");

        // getDurationAttributeValueAsLong
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getDurationAttributeValueAsLong(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getDurationAttributeValueAsLong(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertNull(AttributeSupport.getDurationAttributeValueAsLong(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"0\" should be null");
        Assert.assertEquals(
                AttributeSupport.getDurationAttributeValueAsLong(
                        AttributeSupport.getAttribute(attributes, new QName(TEST_NS, "testAttrMinusOneDay")))
                        .intValue(), -24 * 60 * 60 * 1000, "attribute called testAttrMinusOneDay");
    }

    @Test(dependsOnMethods = {"testGetAttributeMethods", "testGetID"}) public void testAppends() {
        String qNameBase = "name";
        String testResult = TEST_ID_ATTRIBUTE_VALUE;

        QName qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);

        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        boolean thrown = false;
        try {
            AttributeSupport.appendAttribute(null, qName, testResult);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, null, testResult);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, null);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, testResult);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(AttributeSupport.getAttributeValue(createdElement, qName), testResult,
                "appendAttribute(Element, QName, String) failed");

        // appendAttribute(Element, QName, String, boolean)
        qNameBase = qNameBase + "New";
        testResult = testResult + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Test precondition");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(null, qName, testResult, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, null, testResult, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, (String) null, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, testResult, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(AttributeSupport.getAttributeValue(createdElement, qName), testResult,
                "appendAttribute(Element, QName, String) failed");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Should not have added an id Attribute");

        qNameBase = qNameBase + "New";
        testResult = testResult + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Test precondition");
        AttributeSupport.appendAttribute(createdElement, qName, testResult, true);
        Assert.assertEquals(AttributeSupport.getIdAttribute(createdElement).getValue(), testResult,
                "id Attribute added correctly");
        AttributeSupport.removeAttribute(createdElement, qName);

        // appendAttribute(Element, QName, List<String>, boolean)
        qNameBase = qNameBase + "New";
        List<String> data = Arrays.asList("one", "2", "iii");
        testResult = "one 2 iii";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Test precondition");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(null, qName, data, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, null, data, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, (List<String>) null, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, data, false);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(AttributeSupport.getAttributeValue(createdElement, qName), testResult,
                "appendAttribute(Element, QName, String) failed");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Should not have added an id Attribute");

        qNameBase = qNameBase + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        Assert.assertNull(AttributeSupport.getIdAttribute(createdElement), "Test precondition");
        AttributeSupport.appendAttribute(createdElement, qName, data, true);
        Assert.assertEquals(AttributeSupport.getIdAttribute(createdElement).getValue(), testResult,
                "id Attribute added correctly");

        int duration = 1000;
        qNameBase = qNameBase + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(null, qName, duration);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(createdElement, null, duration);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(createdElement, qName, duration);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(
                AttributeSupport.getDurationAttributeValueAsLong(AttributeSupport.getAttribute(createdElement, qName))
                        .intValue(), duration, "getDurationAttributeValueAsLong failed");

        long time = 1000 * 60 * 60 * 24;
        qNameBase = qNameBase + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(null, qName, time);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(createdElement, null, time);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(createdElement, qName, time);
        } catch (AssertionError e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(
                AttributeSupport.getDateTimeAttributeAsLong(AttributeSupport.getAttribute(createdElement, qName))
                        .intValue(), time, "getDurationAttributeValueAsLong failed");

    }

}