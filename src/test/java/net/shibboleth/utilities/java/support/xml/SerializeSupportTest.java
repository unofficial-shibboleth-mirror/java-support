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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Tests for {@link NamespaceSupport}
 */
public class SerializeSupportTest {

    private Element parent;

    private BasicParserPool parserPool;

    // Helper methods, from http://www.java2s.com/Code/Java/XML/ComparetwoDOMNodes.htm
    private int countNonNamespaceAttributes(final NamedNodeMap attrs) {
        int n = 0;
        for (int i = 0; i < attrs.getLength(); i++) {
            final Attr attr = (Attr) attrs.item(i);
            if (!attr.getName().startsWith("xmlns")) {
                n++;
            }
        }
        return n;
    }

    private void assertEquals(final Node expected, final Node actual) {

        Assert.assertEquals(expected.getNodeType(), actual.getNodeType(),
                "Different types of nodes: " + expected + " " + actual);

        if (expected instanceof Document) {
            final Document expectedDoc = (Document) expected;
            final Document actualDoc = (Document) actual;
            assertEquals(expectedDoc.getDocumentElement(), actualDoc.getDocumentElement());
        } else if (expected instanceof Element) {
            final Element expectedElement = (Element) expected;
            final Element actualElement = (Element) actual;

            // compare element names
            Assert.assertEquals(expectedElement.getLocalName(), actualElement.getLocalName(),
                    "Element names do not match: " + expectedElement.getLocalName() + " "
                            + actualElement.getLocalName());
            // compare element ns
            final String expectedNS = expectedElement.getNamespaceURI();
            final String actualNS = actualElement.getNamespaceURI();
            Assert.assertFalse(
                    (expectedNS == null && actualNS != null) || (expectedNS != null && !expectedNS.equals(actualNS)),
                    "Element namespaces names do not match: " + expectedNS + " " + actualNS);

            final String elementName = "{" + expectedElement.getNamespaceURI() + "}" + actualElement.getLocalName();

            // compare attributes
            final NamedNodeMap expectedAttrs = expectedElement.getAttributes();
            final NamedNodeMap actualAttrs = actualElement.getAttributes();
            Assert.assertEquals(countNonNamespaceAttributes(expectedAttrs), countNonNamespaceAttributes(actualAttrs),
                    ": Number of attributes do not match up: " + countNonNamespaceAttributes(expectedAttrs) + " "
                            + countNonNamespaceAttributes(actualAttrs));

            for (int i = 0; i < expectedAttrs.getLength(); i++) {
                final Attr expectedAttr = (Attr) expectedAttrs.item(i);
                if (expectedAttr.getName().startsWith("xmlns")) {
                    continue;
                }
                Attr actualAttr = null;
                if (expectedAttr.getNamespaceURI() == null) {
                    actualAttr = (Attr) actualAttrs.getNamedItem(expectedAttr.getName());
                } else {
                    actualAttr = (Attr) actualAttrs.getNamedItemNS(expectedAttr.getNamespaceURI(),
                            expectedAttr.getLocalName());
                }
                Assert.assertNotNull(actualAttr, elementName + ": No attribute found:" + expectedAttr);

                Assert.assertEquals(expectedAttr.getValue(), actualAttr.getValue(), elementName
                        + ": Attribute values do not match: " + expectedAttr.getValue() + " " + actualAttr.getValue());
            }

            // compare children
            final NodeList expectedChildren = expectedElement.getChildNodes();
            final NodeList actualChildren = actualElement.getChildNodes();

            Assert.assertEquals(expectedChildren.getLength(), actualChildren.getLength(),
                    elementName + ": Number of children do not match up: " + expectedChildren.getLength() + " "
                            + actualChildren.getLength());

            for (int i = 0; i < expectedChildren.getLength(); i++) {
                final Node expectedChild = expectedChildren.item(i);
                final Node actualChild = actualChildren.item(i);
                assertEquals(expectedChild, actualChild);
            }
        } 
    }

    @BeforeTest public void setup()
            throws XMLParserException, ComponentInitializationException, SAXException, IOException {
        parserPool = new BasicParserPool();
        parserPool.initialize();
        final DocumentBuilder builder = parserPool.getBuilder();
        try {
            final Resource resource =
                    new ClassPathResource("/net/shibboleth/utilities/java/support/xml/serializeSupportTest.xml");
            final Document testFile = builder.parse(resource.getInputStream());

            parent = (Element) testFile.getFirstChild();

        } finally {
            parserPool.returnBuilder(builder);
        }
    }

    @Test public void testNodeToString() throws Exception {
        //
        // Serialize then parse and serialize again
        //
        final String s = SerializeSupport.nodeToString(parent);

        final Document dom = parserPool.parse(new StringReader(s));

        Assert.assertEquals(SerializeSupport.nodeToString(dom.getFirstChild()), s, "Should serialize to same output");
        assertEquals(parent, dom.getFirstChild());
    }

    @Test(dependsOnMethods = {"testNodeToString"}) public void testWriteNode() throws XMLParserException {
        //
        // Serialize then parse and serialize again
        //
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        SerializeSupport.writeNode(parent, output);

        final byte data[] = output.toByteArray();

        final Document dom = parserPool.parse(new ByteArrayInputStream(data));

        Assert.assertEquals(SerializeSupport.nodeToString(dom.getFirstChild()), SerializeSupport.nodeToString(parent),
                "Should serialize to same output");

        assertEquals(dom.getFirstChild(), parent);
    }

    @Test(dependsOnMethods = {"testNodeToString"}) public void testPrettyPrintXML() throws XMLParserException {
        //
        // Pretty print then parse and compare.  Pretty print again and compare.
        //
        final String s = SerializeSupport.prettyPrintXML(parent);

        final Document dom = parserPool.parse(new StringReader(s));

        // JSPT-75:  Versions < 9 (i.e. until 1.8) would pass this bogus test.
        Assert.assertTrue(!System.getProperty("java.version").startsWith("1.") ||
                SerializeSupport.nodeToString(dom.getFirstChild()).equals(SerializeSupport.nodeToString(parent)),
                "Should serialize to same output");

        assertEquals(dom.getFirstChild(), parent);

        Assert.assertEquals(SerializeSupport.prettyPrintXML(dom.getFirstChild()) , s, "Pretty print should match");
    }

    @Test(dependsOnMethods = {"testNodeToString"}) public void testLSOps() throws XMLParserException {

        final DOMImplementationLS domLS = SerializeSupport.getDOMImplementationLS(parent);
        final LSSerializer serializer = SerializeSupport.getLSSerializer(domLS, null);

        final LSOutput serializerOut = domLS.createLSOutput();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        serializerOut.setByteStream(output);

        serializer.write(parent, serializerOut);

        final Document dom = parserPool.parse(new ByteArrayInputStream(output.toByteArray()));
        Assert.assertEquals(SerializeSupport.nodeToString(dom.getFirstChild()), SerializeSupport.nodeToString(parent),
                "Should serialize to same output");
        assertEquals(dom.getFirstChild(), parent);

    }
}
