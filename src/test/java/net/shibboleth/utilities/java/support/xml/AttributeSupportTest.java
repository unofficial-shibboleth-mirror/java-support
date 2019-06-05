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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

    // Contants to test against
    @Nonnull @NotEmpty private static final String TEST_NS = "http://example.org/NameSpace";

    @Nonnull @NotEmpty private static final String TEST_PREFIX = "testns";

    @Nonnull @NotEmpty private static final String TEST_ID_ATTRIBUTE = "testAttributeName";

    @Nonnull @NotEmpty private static final String TEST_ID_PREFIXEDATTRIBUTE = TEST_PREFIX + ":" + TEST_ID_ATTRIBUTE;

    @Nonnull @NotEmpty private static final String TEST_ID_ATTRIBUTE_VALUE = "IDAttrVALUE";

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
            IOException {
        parserPool = new BasicParserPool();
        parserPool.initialize();

        DocumentBuilder builder = parserPool.getBuilder();
        try {
            idAttrQName = new QName(TEST_NS, TEST_ID_ATTRIBUTE, TEST_PREFIX);

            Resource resource =
                    new ClassPathResource("/net/shibboleth/utilities/java/support/xml/attributeSupportTest.xml");
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
    @Test public void testBadNS() throws XMLParserException, ComponentInitializationException, IOException {
        DocumentBuilder builder = parserPool.getBuilder();

        Resource resource = new ClassPathResource("/net/shibboleth/utilities/java/support/xml/badNS1.xml");
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

        resource = new ClassPathResource("/net/shibboleth/utilities/java/support/xml/badNS2.xml");
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
            AttributeSupport.addXMLId(createdElement, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLId");

        thrown = false;
        try {
            AttributeSupport.addXMLId(nullValue(), "fr");
        } catch (ConstraintViolationException e) {
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
            AttributeSupport.addXMLBase(createdElement, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLBase(nullValue(), "foo");
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLBase");

        Assert.assertNull(AttributeSupport.getXMLBase(createdElement),
                "xml:base found erroneously (test setup failure)");
        AttributeSupport.addXMLBase(createdElement, TEST_NS);
        Assert.assertEquals(AttributeSupport.getXMLBase(createdElement), TEST_NS, "addXMLBase failed");

    }

    @Test public void testXMLSpace() {
        Assert.assertEquals(AttributeSupport.getXMLSpace(goodBaseIdSpaceLang), XMLSpace.DEFAULT, "xml:space mismatch");
        Assert.assertNull(AttributeSupport.getXMLSpace(noBaseIdSpaceLang), "xml:space found erroneously");
        Assert.assertNull(AttributeSupport.getXMLSpace(badSpace), "xml:space found erroneously");

        Assert.assertEquals(AttributeSupport.getXMLSpace(preserveSpace), XMLSpace.PRESERVE, "xml:space mismatch");

        // test Add
        boolean thrown = false;
        try {
            AttributeSupport.addXMLSpace(createdElement, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLSpace(nullValue(), XMLSpace.DEFAULT);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element to addXMLSpace");

        Assert.assertNull(AttributeSupport.getXMLSpace(createdElement),
                "xml:space found erroneously (test setup failure)");
        AttributeSupport.addXMLSpace(createdElement, XMLSpace.DEFAULT);
        Assert.assertEquals(AttributeSupport.getXMLSpace(createdElement), XMLSpace.DEFAULT, "addXMLSpace failed");
    }

    @Test public void testXMLLang() {
        Assert.assertEquals(AttributeSupport.getXMLLang(goodBaseIdSpaceLang), "fr-ca", "xml:lang mismatch");
        Assert.assertNull(AttributeSupport.getXMLLang(noBaseIdSpaceLang), "xml:lang found erroneously");

        // test Add
        boolean thrown = false;
        try {
            AttributeSupport.addXMLLang(createdElement, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string to addXMLBase");

        thrown = false;
        try {
            AttributeSupport.addXMLLang(nullValue(), "fr");
        } catch (ConstraintViolationException e) {
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

        Assert.assertTrue(AttributeSupport.hasAttribute(goodBaseIdSpaceLang, XMLConstants.XML_BASE_ATTRIB_NAME),
                "attribute lookup by QName from file");
        Assert.assertFalse(AttributeSupport.hasAttribute(noBaseIdSpaceLang, XMLConstants.XML_BASE_ATTRIB_NAME),
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
        Assert.assertNull(AttributeSupport.getAttribute(noBaseIdSpaceLang, XMLConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup by QName)");
        Attr attr = AttributeSupport.getAttribute(goodBaseIdSpaceLang, XMLConstants.XML_ID_ATTRIB_NAME);
        Assert.assertNotNull(attr, "Should have found xml:id attribute");
        Assert.assertEquals(attr.getValue(), "identifierGoodBaseIdSpaceLang",
                "Should have found correct attribute by value for xml_id attribute");

        // getAttributeValue(Element, QName)
        Assert.assertNull(AttributeSupport.getAttributeValue(goodBaseIdSpaceLang, null),
                "no xml:id (lookup value with null QName)");
        Assert.assertNull(AttributeSupport.getAttributeValue(null, XMLConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup value with null element)");
        Assert.assertNull(AttributeSupport.getAttributeValue(noBaseIdSpaceLang, XMLConstants.XML_ID_ATTRIB_NAME),
                "no xml:id (lookup value by QName)");
        Assert.assertEquals(AttributeSupport.getAttributeValue(goodBaseIdSpaceLang, XMLConstants.XML_ID_ATTRIB_NAME),
                "identifierGoodBaseIdSpaceLang", "Should have found correct value for xml:id attribute by QName");

        // getAttributeValue(Element, String, String)
        Assert.assertNull(AttributeSupport.getAttributeValue(badSpace, XMLConstants.XML_NS, null),
                "no value lookup with null name)");
        Assert.assertNull(AttributeSupport.getAttributeValue(badSpace, XMLConstants.XML_NS, ""),
                "no value lookup with empty name)");
        Assert.assertNull(AttributeSupport.getAttributeValue(null, XMLConstants.XML_NS, "space"),
                "no value lookup with null element)");
        Assert.assertNull(AttributeSupport.getAttributeValue(noBaseIdSpaceLang, XMLConstants.XML_NS, "space"),
                "no xml:space (lookup value by name)");
        Assert.assertEquals(AttributeSupport.getAttributeValue(badSpace, XMLConstants.XML_NS, "space"), "wibble",
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

        // getDateTimeAttribute
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getDateTimeAttribute(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getDateTimeAttribute(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertNull(AttributeSupport.getDateTimeAttribute(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"0\" should be null");
        Assert.assertEquals(
                AttributeSupport.getDateTimeAttribute(
                        AttributeSupport.getAttribute(attributes, new QName(TEST_NS, "testAttrEpochPlusOneSec"))),
                Instant.ofEpochSecond(1), "attribute called testAttrEpochPlusOneSec");

        // getDurationAttributeValueAsLong
        // Use the previously tested AttributeSupport.getAttribute
        Assert.assertNull(AttributeSupport.getDurationAttributeValue(null), "null attribute should be null");
        Assert.assertNull(AttributeSupport.getDurationAttributeValue(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"\" should be null");
        Assert.assertNull(AttributeSupport.getDurationAttributeValue(AttributeSupport.getAttribute(attributes,
                new QName(TEST_NS, "testAttrEmpty"))), "\"0\" should be null");
        Assert.assertEquals(
                AttributeSupport.getDurationAttributeValue(
                        AttributeSupport.getAttribute(attributes, new QName(TEST_NS, "testAttrMinusOneDay"))),
                        Duration.ofDays(-1), "attribute called testAttrMinusOneDay");
    }

    @Test(dependsOnMethods = {"testGetAttributeMethods", "testGetID"}) public void testAppends() {
        String qNameBase = "name";
        String testResult = TEST_ID_ATTRIBUTE_VALUE;

        QName qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);

        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        boolean thrown = false;
        try {
            AttributeSupport.appendAttribute(nullValue(), qName, testResult);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, nullValue(), testResult);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, testResult);
        } catch (ConstraintViolationException e) {
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
            AttributeSupport.appendAttribute(nullValue(), qName, testResult, false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, nullValue(), testResult, false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, (String) nullValue(), false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, testResult, false);
        } catch (ConstraintViolationException e) {
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
            AttributeSupport.appendAttribute(nullValue(), qName, data, false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, nullValue(), data, false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, (List<String>) null, false);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null string should throw");

        thrown = false;
        try {
            AttributeSupport.appendAttribute(createdElement, qName, data, false);
        } catch (ConstraintViolationException e) {
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

        final var duration = Duration.ofSeconds(1);
        qNameBase = qNameBase + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(nullValue(), qName, duration);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(createdElement, nullValue(), duration);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendDurationAttribute(createdElement, qName, duration);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(
                AttributeSupport.getDurationAttributeValue(AttributeSupport.getAttribute(createdElement, qName)),
                        duration, "getDurationAttributeValueAsLong failed");

        // Construct a time that contains nothing below the level of milliseconds,
        // for compatibility with representations that don't have higher precision.
        final var time = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        qNameBase = qNameBase + "New";
        qName = new QName(TEST_NS, qNameBase, TEST_PREFIX);
        Assert.assertNull(AttributeSupport.getAttributeValue(createdElement, qName), "Test precondition");
        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(nullValue(), qName, time);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null element should throw");

        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(createdElement, nullValue(), time);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null qname should throw");

        thrown = false;
        try {
            AttributeSupport.appendDateTimeAttribute(createdElement, qName, time);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "All non nulls should not throw");
        Assert.assertEquals(
                AttributeSupport.getDateTimeAttribute(AttributeSupport.getAttribute(createdElement, qName)),
                        time, "getDurationAttributeValueAsLong failed");

    }

    private <T> T nullValue() {
        return null;
    }

}