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

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tests for {@link DOMTypeSupport};
 */
public class QNameSupportTest {

    @Nonnull @NotEmpty private static final String NAME_1 = "name1";

    @Nonnull @NotEmpty private static final String NAME_2 = "name2";

    @Nonnull @NotEmpty private static final String NAME_3 = "name3";

    @Nonnull @NotEmpty private static final String NAMESPACE_1 = "http://example.org/NameSpace1";

    @Nonnull @NotEmpty private static final String NAMESPACE_2 = "http://example.org/NameSpace2";

    @Nonnull @NotEmpty private static final String DEFAULT_NAMESPACE = "http://example.org/DefaultSpace";

    @Nonnull @NotEmpty private static final String PREFIX_1 = "myns1";

    @Nonnull @NotEmpty private static final String PREFIX_2 = "myns2";

    private ParserPool parserPool;

    private Element parent;

    private Element child;

    @BeforeClass public void setup() throws ComponentInitializationException, SAXException, IOException,
            XMLParserException {
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        parserPool = pool;

        DocumentBuilder builder = parserPool.getBuilder();
        Resource resource =
                new ClassPathResource("/net/shibboleth/utilities/java/support/xml/qNameSupportTest.xml");

        Document testFile = builder.parse(resource.getInputStream());
        parent = (Element) testFile.getFirstChild();

        child = ElementSupport.getFirstChildElement(parent);

        if (null != builder) {
            parserPool.returnBuilder(builder);
        }

    }

    @Test public void testConstructQName() {
        QName qn = QNameSupport.constructQName(NAMESPACE_1, NAME_1, PREFIX_1);
        Assert.assertEquals(qn.getLocalPart(), NAME_1, "Simple qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), NAMESPACE_1, "Simple qname construction");
        Assert.assertEquals(qn.getPrefix(), PREFIX_1, "Simple qname construction");

        qn = QNameSupport.constructQName("", NAME_2, PREFIX_2);
        Assert.assertEquals(qn.getLocalPart(), NAME_2, "Simple qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), "", "Simple qname construction");
        Assert.assertEquals(qn.getPrefix(), PREFIX_2, "Simple qname construction");

        qn = QNameSupport.constructQName(NAMESPACE_2, NAME_2, "");
        Assert.assertEquals(qn.getLocalPart(), NAME_2, "Simple qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), NAMESPACE_2, "Simple qname construction");
        Assert.assertEquals(qn.getPrefix(), "", "Simple qname construction");

        boolean thrown = false;
        try {
            QNameSupport.constructQName(NAMESPACE_2, "", PREFIX_1);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Fail to construct a qname with a null namespace");

        qn = QNameSupport.constructQName(child, NAME_1);
        Assert.assertEquals(qn.getLocalPart(), NAME_1, "Element qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), DEFAULT_NAMESPACE, "Element qname construction");
        Assert.assertEquals(qn.getPrefix(), "", "Element qname construction");

        qn = QNameSupport.constructQName(child, PREFIX_1 + ":" + NAME_2);
        Assert.assertEquals(qn.getLocalPart(), NAME_2, "Element qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), NAMESPACE_1, "Element qname construction");
        Assert.assertEquals(qn.getPrefix(), PREFIX_1, "Element qname construction");

        qn = QNameSupport.constructQName(child, PREFIX_2 + ":" + NAME_3);
        Assert.assertEquals(qn.getLocalPart(), NAME_3, "Element qname construction");
        Assert.assertEquals(qn.getNamespaceURI(), "", "Element qname construction");
        Assert.assertEquals(qn.getPrefix(), PREFIX_2, "Element qname construction");

        thrown = false;
        try {
            QNameSupport.constructQName(child, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Element qname construction");

        thrown = false;
        try {
            QNameSupport.constructQName(nullValue(), PREFIX_2 + ":" + NAME_3);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Element qname construction");

    }

    @Test public void testGetNodeQName() {
        QName qn = QNameSupport.getNodeQName(parent);
        Assert.assertEquals(qn.getLocalPart(), "Parent", "Get Node QName");
        Assert.assertEquals(qn.getNamespaceURI(), DEFAULT_NAMESPACE, "Get Node QName");
        Assert.assertEquals(qn.getPrefix(), "", "Get Node QName");

        qn = QNameSupport.getNodeQName(child);
        Assert.assertEquals(qn.getLocalPart(), "Child", "Get Node QName");
        Assert.assertEquals(qn.getNamespaceURI(), NAMESPACE_1, "Get Node QName");
        Assert.assertEquals(qn.getPrefix(), PREFIX_1, "Get Node QName");

    }

    @Test public void testQNameToContentString() {
        Assert.assertEquals(QNameSupport.qnameToContentString(new QName(NAMESPACE_1, NAME_1, PREFIX_1)), PREFIX_1 + ":"
                + NAME_1);
        Assert.assertEquals(QNameSupport.qnameToContentString(new QName(NAME_2)), NAME_2);
    }

    private <T> T nullValue() {
        return null;
    }

}