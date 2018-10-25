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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Test {@link ElementSupport}. These tests use {@link Test#dependsOnMethods()} to ensure that we suppress a test if
 * functionality it relies on has not been tested correctly. This avoids false failures.
 */
public class ElementSupportTest {

    @Nonnull @NotEmpty private static final String TEST_NS = "http://example.org/NameSpace";

    @Nonnull @NotEmpty private static final String OTHER_NS = "http://example.org/OtherSpace";

    @Nonnull @NotEmpty private static final String TEST_PREFIX = "testns";

    @Nonnull @NotEmpty private static final String TEST_ELEMENT_NAME = "Element1";

    @Nonnull @NotEmpty private static final String ROOT_ELEMENT = "Container";

    @Nonnull private static final QName TEST_ELEMENT_QNAME = new QName(TEST_NS, TEST_ELEMENT_NAME, TEST_PREFIX);

    private BasicParserPool parserPool;

    private Document testFileDocument;

    private Document testerDocument;

    private Element rootElement;

    @BeforeTest public void setUp() throws XMLParserException, ComponentInitializationException, SAXException,
            IOException {
        parserPool = new BasicParserPool();
        parserPool.setCoalescing(false);
        
        parserPool.initialize();

        DocumentBuilder builder = parserPool.getBuilder();
        try {

            Resource resource =
                    new ClassPathResource("/net/shibboleth/utilities/java/support/xml/elementSupportTest.xml");
            testFileDocument = builder.parse(resource.getInputStream());
            rootElement = (Element) testFileDocument.getFirstChild();
            testerDocument = builder.newDocument();

        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    @Test public void testIsElementNamed() {
        Assert.assertFalse(ElementSupport.isElementNamed(null, TEST_NS, ROOT_ELEMENT),
                "not find if provided element is null");
        Assert.assertFalse(ElementSupport.isElementNamed(rootElement, null, ROOT_ELEMENT),
                "not find if provided namespace is null");
        Assert.assertFalse(ElementSupport.isElementNamed(rootElement, TEST_NS, null),
                "not find if provided attribute name is null");
        Assert.assertFalse(ElementSupport.isElementNamed(rootElement, null),
                "not find if provided attribute QName is null");

        Assert.assertTrue(ElementSupport.isElementNamed(rootElement, new QName(TEST_NS, ROOT_ELEMENT, TEST_PREFIX)),
                "lookup against QNAME");
        Assert.assertTrue(ElementSupport.isElementNamed(rootElement, TEST_NS, ROOT_ELEMENT), "lookup against name");

        Assert.assertFalse(ElementSupport.isElementNamed(rootElement, TEST_NS, ROOT_ELEMENT.toUpperCase()),
                "lookup against upper of name");
        Assert.assertFalse(ElementSupport.isElementNamed(rootElement, TEST_NS.toUpperCase(), ROOT_ELEMENT),
                "lookup against upper of namespace");

    }

    @Test(dependsOnMethods = {"testIsElementNamed"}) public void testGetChildElements() {
        Assert.assertTrue(ElementSupport.getChildElements(null).isEmpty(), "Null should provide empty list");
        Assert.assertTrue(ElementSupport.getChildElements(rootElement, null).isEmpty(),
                "Null QName should provide empty list");

        Assert.assertEquals(ElementSupport.getChildElements(rootElement).size(), 8, "unnanmed element lookup");
        List<Element> list = ElementSupport.getChildElements(rootElement, TEST_ELEMENT_QNAME);
        Assert.assertEquals(list.size(), 3, "Named element lookup");
        for (Element e : list) {
            Assert.assertTrue(ElementSupport.isElementNamed(e, TEST_ELEMENT_QNAME));
        }
    }

    @Test(dependsOnMethods = {"testIsElementNamed"}) public void testGetChildElementsByTagName() {
        Assert.assertTrue(ElementSupport.getChildElementsByTagName(rootElement, null).isEmpty(),
                "getChildElementsByTagName: Null name should provide empty list");
        Assert.assertTrue(ElementSupport.getChildElementsByTagName(null, TEST_ELEMENT_NAME).isEmpty(),
                "getChildElementsByTagName: Null root  should provide empty list");
        Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(rootElement, TEST_NS, null).isEmpty(),
                "getChildElementsByTagName: Null name should provide empty list");
        Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(rootElement, null, TEST_ELEMENT_NAME).isEmpty(),
                "getChildElementsByTagNameNS: Null name space should provide empty list");
        Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(null, TEST_NS, TEST_ELEMENT_NAME).isEmpty(),
                "getChildElementsByTagNameNS: Null root  should provide empty list");

        List<Element> list = ElementSupport.getChildElementsByTagName(rootElement, TEST_ELEMENT_NAME);
        Assert.assertEquals(list.size(), 5, "getChildElementsByTagName size");
        int i = 0;
        for (Element e : list) {
            if (ElementSupport.isElementNamed(e, TEST_ELEMENT_QNAME)) {
                i++;
            }
        }
        Assert.assertEquals(i, 3, "getChildElementsByTagName size");

        list = ElementSupport.getChildElementsByTagNameNS(rootElement, TEST_NS, TEST_ELEMENT_NAME);
        Assert.assertEquals(list.size(), 3, "getChildElementsByTagName size");
        for (Element e : list) {
            Assert.assertTrue(ElementSupport.isElementNamed(e, TEST_ELEMENT_QNAME));
        }

    }

    @Test(dependsOnMethods = {"testGetChildElementsByTagName"}) public void testGetElementAncestor() {
        Assert.assertNull(ElementSupport.getElementAncestor(null),
                "getElementAncestor: Null element should provide null result");
        Assert.assertNull(ElementSupport.getElementAncestor(rootElement),
                "getElementAncestor: root node should provide null result");

        Element child = ElementSupport.getChildElementsByTagName(rootElement, "Element4").get(0);
        Element grandChild = ElementSupport.getChildElementsByTagName(child, "Element1").get(0);

        Assert.assertEquals(ElementSupport.getElementAncestor(child), rootElement, "getElementAncestor for child");
        Assert.assertEquals(ElementSupport.getElementAncestor(grandChild), child, "getElementAncestor for grand child");

    }

    @Test(dependsOnMethods = {"testGetChildElementsByTagName"}) public void testGetElementContentAsString() {
        Assert.assertTrue(ElementSupport.getElementContentAsString(null).isEmpty(),
                "getElementContentAsList: Null element should provide empty result");

        Assert.assertTrue(StringSupport.trim(ElementSupport.getElementContentAsString(rootElement)).isEmpty(),
                "getElementContentAsList: Empty element should provide empty result");

        Element interesting =
                ElementSupport.getChildElementsByTagName(
                        ElementSupport.getChildElementsByTagName(rootElement, "Element4").get(0), "Element1").get(0);

        Assert.assertEquals(ElementSupport.getElementContentAsString(interesting), "Some Random foo" + "\n" + "test");
    }

    @Test(dependsOnMethods = {"testGetChildElementsByTagName"}) public void testGetElementContentAsList() {
        Assert.assertTrue(ElementSupport.getElementContentAsList(null).isEmpty(),
                "getElementContentAsList: Null element should provide empty result");

        Assert.assertTrue(ElementSupport.getElementContentAsList(rootElement).isEmpty(),
                "getElementContentAsList: Empty element should provide empty result");

        Element interesting =
                ElementSupport.getChildElementsByTagName(
                        ElementSupport.getChildElementsByTagName(rootElement, "Element4").get(0), "Element1").get(0);

        Assert.assertEquals(ElementSupport.getElementContentAsList(interesting),
                Arrays.asList("Some", "Random", "foo", "test"));

    }

    @Test(dependsOnMethods = {"testGetChildElementsByTagName"}) public void testGetElementContentAsQName() {
        Assert.assertNull(ElementSupport.getElementContentAsQName(null),
                "getElementContentAsQName: Null element should provide empty result");
        Assert.assertNull(ElementSupport.getElementContentAsQName(rootElement),
                "getElementContentAsQName: Empty element should provide empty result");

        Element parent = ElementSupport.getChildElementsByTagName(rootElement, "Element4").get(0);
        List<Element> children = ElementSupport.getChildElementsByTagName(parent, "QName");

        Assert.assertEquals(ElementSupport.getElementContentAsQName(children.get(0)), new QName(OTHER_NS, "localname"));
        Assert.assertNull(ElementSupport.getElementContentAsQName(children.get(1)),
                "getElementContentAsQName: invalid qname should return null");
    }

    @Test(dependsOnMethods = {"testIsElementNamed"}) public void testGetChildAndNext() {
        Assert.assertNull(ElementSupport.getFirstChildElement(null),
                "getFirstChildElement: Null element should provide null result");

        Assert.assertNull(ElementSupport.getNextSiblingElement(null),
                "getNextSiblingElement: Null element should provide null result");

        Element element = ElementSupport.getFirstChildElement(rootElement);
        Assert.assertTrue(ElementSupport.isElementNamed(element, TEST_ELEMENT_QNAME), "getFirstChildElement");
        Assert.assertNull(ElementSupport.getFirstChildElement(element),
                "getFirstChildElement: Empty element should provide null result");

        element = ElementSupport.getNextSiblingElement(element);
        Assert.assertTrue(ElementSupport.isElementNamed(element, TEST_ELEMENT_QNAME), "getNextSiblingElement 1");

        element = ElementSupport.getNextSiblingElement(element);
        QName qName = new QName(TEST_NS, "Element2", TEST_PREFIX);
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 2");

        element = ElementSupport.getNextSiblingElement(element);
        qName = new QName(TEST_NS, TEST_ELEMENT_NAME, "mynsagain");
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 3");

        element = ElementSupport.getNextSiblingElement(element);
        qName = new QName(OTHER_NS, TEST_ELEMENT_NAME, TEST_PREFIX);
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 4 ");

        element = ElementSupport.getNextSiblingElement(element);
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 5");

        element = ElementSupport.getNextSiblingElement(element);
        qName = new QName(OTHER_NS, "Element2", TEST_PREFIX);
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 6");

        element = ElementSupport.getNextSiblingElement(element);
        qName = new QName(TEST_NS, "Element4", TEST_PREFIX);
        Assert.assertTrue(ElementSupport.isElementNamed(element, qName), "getNextSiblingElement 7");

        Assert.assertNull(ElementSupport.getNextSiblingElement(element),
                "getNextSiblingElement: final element should provide null result");
    }

    @Test public void testGetIndexedChildElements() {
        Assert.assertTrue(ElementSupport.getIndexedChildElements(null).isEmpty(),
                "getIndexedChildElements with null input");
        Map<QName, List<Element>> map = ElementSupport.getIndexedChildElements(rootElement);

        Assert.assertEquals(map.get(TEST_ELEMENT_QNAME).size(), 3, "getIndexedChildElements for " + TEST_ELEMENT_QNAME);
        map.remove(TEST_ELEMENT_QNAME);

        QName qname = new QName(TEST_NS, "Element2", TEST_PREFIX);
        Assert.assertEquals(map.get(qname).size(), 1, "getIndexedChildElements for " + qname);
        map.remove(qname);

        qname = new QName(OTHER_NS, "Element1", "otherns");
        Assert.assertEquals(map.get(qname).size(), 2, "getIndexedChildElements for " + qname);
        map.remove(qname);

        qname = new QName(OTHER_NS, "Element2", "otherns");
        Assert.assertEquals(map.get(qname).size(), 1, "getIndexedChildElements for " + qname);
        Element elementNoKids = map.get(qname).get(0);
        map.remove(qname);

        qname = new QName(TEST_NS, "Element4", TEST_PREFIX);
        Assert.assertEquals(map.get(qname).size(), 1, "getIndexedChildElements for " + qname);
        map.remove(qname);

        Assert.assertTrue(map.isEmpty(), "getIndexedChildElements found extra elements");

        Assert.assertTrue(ElementSupport.getIndexedChildElements(elementNoKids).isEmpty(),
                "getIndexedChildElements with a no child element");

    }

    @Test(dependsOnMethods = {"testConstructElement"}) public void testAdoptElement() throws XMLParserException {
        DocumentBuilder builder = parserPool.getBuilder();
        try {
            Document otherDocument = builder.newDocument();
            Element element = ElementSupport.constructElement(testerDocument, TEST_ELEMENT_QNAME);
            testerDocument.appendChild(element);

            //
            // Failure conditions
            //
            boolean thrown = false;
            try {
                ElementSupport.adoptElement(nullValue(), element);
            } catch (ConstraintViolationException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown, "adoptElement: Null Document should assert");

            thrown = false;
            try {
                ElementSupport.adoptElement(testerDocument, nullValue());
            } catch (ConstraintViolationException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown, "adoptElement: Null Element should assert");

            //
            // And success
            //
            Assert.assertNotSame(element.getOwnerDocument(), otherDocument, "Initially: element not owned");
            Assert.assertEquals(element.getOwnerDocument(), testerDocument, "Initially: element owned");
            ElementSupport.adoptElement(testerDocument, element);
            Assert.assertNotSame(element.getOwnerDocument(), otherDocument, "ReAdopt: element not owned");
            Assert.assertEquals(element.getOwnerDocument(), testerDocument, "ReAdopt: element owned");
            ElementSupport.adoptElement(otherDocument, element);
            Assert.assertEquals(element.getOwnerDocument(), otherDocument, "After: element owned");
            Assert.assertNotSame(element.getOwnerDocument(), testerDocument, "After:  new element not owned");

        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    @Test(dependsOnMethods = {"testConstructElement", "testGetChildElements", "testGetChildElementsByTagName"}) public
            void testAppendChildElement() throws XMLParserException {
        DocumentBuilder builder = parserPool.getBuilder();
        try {
            Document myDocument = builder.newDocument();
            Element myRoot =
                    ElementSupport.constructElement(testerDocument, new QName(TEST_NS, ROOT_ELEMENT, TEST_PREFIX));
            Element testElement = ElementSupport.constructElement(testerDocument, TEST_ELEMENT_QNAME);
            boolean thrown = false;
            try {
                ElementSupport.appendChildElement(nullValue(), testElement);
            } catch (ConstraintViolationException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown, "appendChildElement: assert on null parent");

            Assert.assertTrue(ElementSupport.getChildElements(myRoot).isEmpty(),
                    "appendChildElement: no elements to start with");
            ElementSupport.appendChildElement(myRoot, testElement);
            Assert.assertEquals(ElementSupport.getChildElements(myRoot).size(), 1,
                    "appendChildElement: One elements after one insertion");
            ElementSupport.appendChildElement(myRoot, testElement);
            Assert.assertEquals(ElementSupport.getChildElements(myRoot).size(), 1,
                    "appendChildElement: One elements after re insertion");

            QName qName = new QName(OTHER_NS, TEST_ELEMENT_NAME);
            ElementSupport.appendChildElement(myRoot, ElementSupport.constructElement(myDocument, TEST_ELEMENT_QNAME));
            ElementSupport.appendChildElement(myRoot, ElementSupport.constructElement(myDocument, qName));
            ElementSupport.appendChildElement(myRoot, ElementSupport.constructElement(myDocument, TEST_ELEMENT_QNAME));
            ElementSupport.appendChildElement(myRoot, ElementSupport.constructElement(myDocument, qName));
            Assert.assertEquals(ElementSupport.getChildElements(myRoot).size(), 5,
                    "appendChildElement:  elements after insertion");
            Assert.assertEquals(ElementSupport.getChildElementsByTagNameNS(myRoot, TEST_NS, TEST_ELEMENT_NAME).size(),
                    3, "appendChildElement:  elements after insertion");
            Assert.assertEquals(ElementSupport.getChildElementsByTagNameNS(myRoot, OTHER_NS, TEST_ELEMENT_NAME).size(),
                    2, "appendChildElement:  elements after insertion");
        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    @Test(dependsOnMethods = {"testConstructElement"}) public void testAppendChildText() {
        Element element = ElementSupport.constructElement(testerDocument, TEST_ELEMENT_QNAME);
        Assert.assertNull(StringSupport.trimOrNull(element.getTextContent()),
                "appendTextContent: initially element has no text");
        boolean thrown = false;
        try {
            ElementSupport.appendTextContent(nullValue(), "test");
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "appendTextContent: null element should assert");

        ElementSupport.appendTextContent(element, null);
        Assert.assertNull(StringSupport.trimOrNull(element.getTextContent()),
                "appendTextContent: After null assert element has no text");
        ElementSupport.appendTextContent(element, "");
        Assert.assertEquals(element.getTextContent().length(), 0, "appendTextContent: contents match even when empty");

        ElementSupport.appendTextContent(element, " test Text");
        Assert.assertEquals(element.getTextContent(), " test Text", "appendTextContent: contents match");

        ElementSupport.appendTextContent(element, " more Text");
        Assert.assertEquals(element.getTextContent(), " test Text more Text",
                "appendTextContent: contents after second append");
    }

    @Test(dependsOnMethods = {"testGetChildElementsByTagName"}) public void testConstructElement()
            throws XMLParserException {
        Element e = ElementSupport.constructElement(testerDocument, TEST_ELEMENT_QNAME);
        Assert.assertTrue(ElementSupport.isElementNamed(e, TEST_ELEMENT_QNAME));

        e = ElementSupport.constructElement(testerDocument, TEST_NS, TEST_ELEMENT_NAME, TEST_PREFIX);
        Assert.assertTrue(ElementSupport.isElementNamed(e, TEST_ELEMENT_QNAME));
    }

    @Test public void testConstructElementBadParms() throws XMLParserException {
        boolean thrown = false;
        try {
            ElementSupport.constructElement(nullValue(), TEST_ELEMENT_QNAME);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "constructElement(Document, QName): null Document should throw");

        thrown = false;
        try {
            ElementSupport.constructElement(testerDocument, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "constructElement(Document, QName): null QName should throw");

        thrown = false;
        try {
            ElementSupport.constructElement(testerDocument, TEST_NS, nullValue(), TEST_PREFIX);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "constructElement(Document, String, String, String): null ElementName should throw");

        thrown = false;
        try {
            ElementSupport.constructElement(nullValue(), TEST_NS, TEST_ELEMENT_NAME, TEST_PREFIX);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "constructElement(Document, String, String, String): null ElementName should throw");

    }

    @Test(dependsOnMethods = {"testConstructElement", "testGetChildElements"}) public void testSetDocumentElement()
            throws XMLParserException {
        DocumentBuilder builder = parserPool.getBuilder();
        try {
            Document myDocument = builder.newDocument();
            Element myRoot =
                    ElementSupport.constructElement(testerDocument, new QName(TEST_NS, ROOT_ELEMENT, TEST_PREFIX));
            boolean thrown = false;
            try {
                ElementSupport.setDocumentElement(myDocument, nullValue());
            } catch (ConstraintViolationException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown, "setDocumentElement: null Element should assert");

            thrown = false;
            try {
                ElementSupport.setDocumentElement(nullValue(), myRoot);
            } catch (ConstraintViolationException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown, "setDocumentElement: null Document should assert");
            Assert.assertTrue(ElementSupport.getChildElements(myDocument).isEmpty(), "New document should be empty");

            ElementSupport.setDocumentElement(myDocument, myRoot);
            List<Element> list = ElementSupport.getChildElements(myDocument);
            Assert.assertEquals(list.size(), 1, "setDocumentElement: One child after element set");
            Assert.assertEquals(list.get(0), myRoot, "setDocumentElement: after element set");

            ElementSupport.setDocumentElement(myDocument, myRoot);
            list = ElementSupport.getChildElements(myDocument);
            Assert.assertEquals(list.size(), 1, "setDocumentElement: One child after element re-set");
            Assert.assertEquals(list.get(0), myRoot, "setDocumentElement: after element re-set");

            Element otherRoot =
                    ElementSupport.constructElement(testerDocument, new QName(TEST_NS, ROOT_ELEMENT, TEST_PREFIX));

            ElementSupport.setDocumentElement(myDocument, otherRoot);
            list = ElementSupport.getChildElements(myDocument);
            Assert.assertEquals(list.size(), 1, "setDocumentElement: One child after element re-set");
            Assert.assertEquals(list.get(0), otherRoot, "setDocumentElement: after element re-set");

        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    private <T> T nullValue() {
        return null;
    }

}