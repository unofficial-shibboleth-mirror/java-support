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
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.Schema;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool.DocumentBuilderProxy;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Tests for {@link NamespaceSupport}
 */
public class BasicParserPoolTest {

    @Nonnull @NotEmpty private static final String TEST_DIR = "/net/shibboleth/utilities/java/support/xml/";

    @Nonnull @NotEmpty private static final String SCHEMA_FILE = TEST_DIR + "basicParserPoolTest.xsd";

    @Nonnull @NotEmpty private static final String XML_FILE = TEST_DIR + "basicParserPoolTest.xml";
    
    @Nonnull @NotEmpty private static final String DTD_FILE = TEST_DIR + "dtdParserPoolTest.xml";

    private BasicParserPool basicParserPool;
    /** Max size of the pool we're using. */
    int maxPoolSize = 10;
   
    @BeforeMethod public void setupEachTest() {
        basicParserPool = new BasicParserPool();
        basicParserPool.setMaxPoolSize(maxPoolSize);
    }
    
    
    @Test public void testParams() throws SAXException, ComponentInitializationException, XMLParserException, IOException {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
        attributes.put("http://apache.org/xml/properties/input-buffer-size", 2000);
        Map<String, Object> baseAttributes = new HashMap<>(attributes);
        attributes.put(null, 99);

        Map<String, Boolean> features = new HashMap<>();
        features.put("http://xml.org/sax/features/use-entity-resolver2", false);
        Map<String, Boolean> baseFeatures = new HashMap<>(features);
        attributes.put(null, true);

        basicParserPool.setBuilderAttributes(attributes);
        basicParserPool.setBuilderFeatures(features);
        basicParserPool.setCoalescing(true);
        basicParserPool.setDTDValidating(true);
        basicParserPool.setExpandEntityReferences(true);
        basicParserPool.setIgnoreComments(true);
        basicParserPool.setIgnoreElementContentWhitespace(true);
        basicParserPool.setNamespaceAware(true);
        Resource r = new ClassPathResource(SCHEMA_FILE);
        final SchemaBuilder schemaBuilder = new SchemaBuilder();
        schemaBuilder.addSchema(r.getInputStream());
        Schema schema = schemaBuilder.buildSchema();
        basicParserPool.setSchema(schema);
        basicParserPool.setXincludeAware(true);
        EntityResolver entityResolver = new MockEntityResolver();
        basicParserPool.setEntityResolver(entityResolver);
        ErrorHandler errorHandler = new MockErrorHandler();
        basicParserPool.setErrorHandler(errorHandler);
        

        basicParserPool.initialize();

        Assert.assertEquals(basicParserPool.getBuilderAttributes(), baseAttributes, "Checking attributes");
        attributes.clear();
        attributes.putAll(baseAttributes);
        attributes.put("foo", 99);
        Assert.assertNotSame(basicParserPool.getBuilderAttributes(), baseAttributes, "Checking attributes");

        Assert.assertEquals(basicParserPool.getBuilderFeatures(), baseFeatures, "Checking features");
        features.clear();
        features.putAll(baseFeatures);
        features.put("foo", false);
        Assert.assertNotSame(basicParserPool.getBuilderAttributes(), baseFeatures, "Checking features");

        Assert.assertTrue(basicParserPool.isCoalescing(), "pool Coalescing");

        DocumentBuilder builder = basicParserPool.getBuilder();

        Assert.assertTrue(basicParserPool.isDTDValidating(), "pool isDTDValidating");
        Assert.assertTrue(builder.isValidating(), "builder Validating");

        Assert.assertTrue(basicParserPool.isExpandEntityReferences(), "pool isExpandEntityReferences");
        Assert.assertTrue(basicParserPool.isIgnoreComments(), "pool isIgnoreComments");
        Assert.assertTrue(basicParserPool.isIgnoreElementContentWhitespace(), "pool isIgnoreElementContentWhitespace");

        Assert.assertTrue(builder.isNamespaceAware(), "builder NameSpaceAware");
        Assert.assertTrue(basicParserPool.isNamespaceAware(), "pool NameSpaceAware");

        Assert.assertEquals(builder.getSchema(), schema, "builder schema");
        Assert.assertEquals(basicParserPool.getSchema(), schema, "pool schema");

        Assert.assertTrue(builder.isXIncludeAware(), "builder isXIncludeAware");
        Assert.assertTrue(basicParserPool.isXincludeAware(), "pool isXIncludeAware");
        
        Assert.assertSame(basicParserPool.getEntityResolver(), entityResolver);
        
        Assert.assertSame(basicParserPool.getErrorHandler(), errorHandler);

        basicParserPool = new BasicParserPool();

        basicParserPool.setCoalescing(false);
        basicParserPool.setDTDValidating(false);
        basicParserPool.setExpandEntityReferences(false);
        basicParserPool.setIgnoreComments(false);
        basicParserPool.setIgnoreElementContentWhitespace(false);
        basicParserPool.setNamespaceAware(false);
        basicParserPool.setSchema(null);
        basicParserPool.setXincludeAware(false);
        basicParserPool.setEntityResolver(null);
        try {
            basicParserPool.setErrorHandler(nullValue());
            Assert.fail("Null ErrorHandler should have been rejected");
        } catch (ConstraintViolationException e) {
            //Expected
        }

        basicParserPool.initialize();

        Assert.assertTrue(basicParserPool.getBuilderAttributes().isEmpty(), "Checking attributes");
        // This is false because we now default in certain security-related features
        Assert.assertFalse(basicParserPool.getBuilderFeatures().isEmpty(), "Checking features");

        Assert.assertFalse(basicParserPool.isCoalescing(), "pool Coalescing");

        builder = basicParserPool.getBuilder();

        Assert.assertFalse(basicParserPool.isDTDValidating(), "pool isDTDValidating");
        Assert.assertFalse(builder.isValidating(), "builder Validating");

        Assert.assertFalse(basicParserPool.isExpandEntityReferences(), "pool isExpandEntityReferences");
        Assert.assertFalse(basicParserPool.isIgnoreComments(), "pool isIgnoreComments");
        Assert.assertFalse(basicParserPool.isIgnoreElementContentWhitespace(), "pool isIgnoreElementContentWhitespace");

        Assert.assertFalse(builder.isNamespaceAware(), "builder NameSpaceAware");
        Assert.assertFalse(basicParserPool.isNamespaceAware(), "pool NameSpaceAware");

        Assert.assertNull(builder.getSchema(), "builder schema");
        Assert.assertNull(basicParserPool.getSchema(), "pool schema");

        Assert.assertFalse(builder.isXIncludeAware(), "builder isXIncludeAware");
        Assert.assertFalse(basicParserPool.isXincludeAware(), "pool isXIncludeAware");
        
        Assert.assertNull(basicParserPool.getEntityResolver(), "EntityResolver is non-null");
        
        Assert.assertNotNull(basicParserPool.getErrorHandler(), "ErrorHandler was null");
    }

    @Test public void testInit() throws ComponentInitializationException, SAXException, XMLParserException, IOException {
        Boolean thrown = false;

        try {
            basicParserPool.newDocument();
        } catch (UninitializedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "newDocument before init");

        thrown = false;
        try {
            byte data[] = {1, 2};
            basicParserPool.parse(new ByteArrayInputStream(data));
        } catch (UninitializedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "parse before init");

        thrown = false;
        try {
            char data[] = {'1', '2'};
            basicParserPool.parse(new CharArrayReader(data, 0, data.length));
        } catch (UninitializedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "parse before init");

        basicParserPool.initialize();

        thrown = false;
        try {
            basicParserPool.setBuilderAttributes(new HashMap<String, Object>());
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setBuilderAttributes after init");

        thrown = false;
        try {
            basicParserPool.setBuilderFeatures(new HashMap<String, Boolean>());
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setBuilderFeatures after init");

        thrown = false;

        try {
            basicParserPool.setCoalescing(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setCoalescing after init");

        thrown = false;
        try {
            basicParserPool.setDTDValidating(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setDTDValidating after init");

        thrown = false;
        try {
            basicParserPool.setExpandEntityReferences(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setExpandEntityReferences after init");

        thrown = false;
        try {
            basicParserPool.setIgnoreComments(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setIgnoreComments after init");

        thrown = false;
        try {
            basicParserPool.setIgnoreElementContentWhitespace(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setIgnoreElementContentWhitespace after init");

        thrown = false;
        try {
            basicParserPool.setNamespaceAware(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setNamespaceAware after init");

        Resource r = new ClassPathResource(SCHEMA_FILE);
        final SchemaBuilder schemaBuilder = new SchemaBuilder();
        schemaBuilder.addSchema(r.getInputStream());
        Schema schema = schemaBuilder.buildSchema();

        thrown = false;
        try {
            basicParserPool.setSchema(schema);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setSchema after init");

        thrown = false;
        try {
            basicParserPool.setXincludeAware(true);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setXincludeAware after init");

        thrown = false;
        try {
            basicParserPool.setEntityResolver(new MockEntityResolver());
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setEntityResolver after init");

        thrown = false;
        try {
            basicParserPool.setErrorHandler(new MockErrorHandler());
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setErrorHandler after init");

        thrown = false;
        try {
            basicParserPool.initialize();
        } catch (ComponentInitializationException e) {
            thrown = true;
        }
        Assert.assertFalse(thrown, "double initialize after init");

        basicParserPool.destroy();
    }

    @Test public void testDestroy() throws ComponentInitializationException, SAXException, IOException {
        BasicParserPool pool = new BasicParserPool();
        pool.destroy();

        Boolean thrown = false;
        try {
            pool.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        } catch (ComponentInitializationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "initialize after destroy");

        try {
            pool.setBuilderAttributes(new HashMap<String, Object>());
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setBuilderAttributes after destroy");

        thrown = false;
        try {
            pool.setBuilderFeatures(new HashMap<String, Boolean>());
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setBuilderFeatures after destroy");

        thrown = false;

        try {
            pool.setCoalescing(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setCoalescing after destroy");

        thrown = false;
        try {
            pool.setDTDValidating(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setDTDValidating after destroy");

        thrown = false;
        try {
            pool.setExpandEntityReferences(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setExpandEntityReferences after destroy");

        thrown = false;
        try {
            pool.setIgnoreComments(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setIgnoreComments after destroy");

        thrown = false;
        try {
            pool.setIgnoreElementContentWhitespace(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setIgnoreElementContentWhitespace after destroy");

        thrown = false;
        try {
            pool.setNamespaceAware(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setNamespaceAware after destroy");

        Resource r = new ClassPathResource(SCHEMA_FILE);
        final SchemaBuilder schemaBuilder = new SchemaBuilder();
        schemaBuilder.addSchema(r.getInputStream());
        Schema schema = schemaBuilder.buildSchema();

        thrown = false;
        try {
            pool.setSchema(schema);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setSchema after destroy");

        thrown = false;
        try {
            pool.setXincludeAware(true);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setXincludeAware after destroy");

        thrown = false;
        try {
            pool.setEntityResolver(new MockEntityResolver());
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setEntityResolver after destroy");

        thrown = false;
        try {
            pool.setErrorHandler(new MockErrorHandler());
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setErrorHandler after destroy");

        thrown = false;
        try {
            pool.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "double initialize after destroy");

    }
    
    private void checkParsedDocument(Document document) {
        
        List<Element> list = ElementSupport.getChildElements(document);
        
        Assert.assertEquals(list.size(), 1, "Only one element at root!");
        Element root = list.get(0);
        Assert.assertEquals(root.getLocalName(), "Parent", "Check root name");
        Assert.assertEquals(root.getNamespaceURI(), "https://www.example.org/Example", "Check root namespace");
        list =  ElementSupport.getChildElements(root);
        
        Assert.assertEquals(list.size(), 2, "Two children");
        Assert.assertEquals(list.get(0).getLocalName(), "Child1", "Check first child name");
        Assert.assertEquals(list.get(0).getNamespaceURI(), "https://www.example.org/Example", "Check first child namespace");
        Assert.assertEquals(list.get(1).getLocalName(), "Child2", "Check second child name");
        Assert.assertEquals(list.get(1).getNamespaceURI(), "https://www.example.org/Example", "Check second child namespace");
    }

    @Test public void testParse() throws IOException, ComponentInitializationException, XMLParserException, FileNotFoundException {

        basicParserPool.initialize();
        
        Resource r = new ClassPathResource(XML_FILE);
        
        checkParsedDocument(basicParserPool.parse(r.getInputStream()));
        
        // Get file name the same way that SchemaBuilderTest does
        checkParsedDocument(basicParserPool.parse(new FileReader("src/test/resources/" + XML_FILE)));
    }

    @Test(expectedExceptions=XMLParserException.class)
    public void testDTD() throws IOException, ComponentInitializationException, XMLParserException, FileNotFoundException {

        basicParserPool.initialize();
        
        Resource r = new ClassPathResource(DTD_FILE);
        
        basicParserPool.parse(r.getInputStream());
    }
    
    @Test public void testNewDocument() throws ComponentInitializationException, XMLParserException {
        basicParserPool.initialize();
        Assert.assertNotNull(basicParserPool.newDocument(), "Create new document");
    }
    
    private void testDeadBuilder(DocumentBuilder builder) {
        boolean thrown = false;
        try {
            builder.newDocument();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Dead builder should not create a new document");
    }

    @Test public void testGetReturn() throws ComponentInitializationException, XMLParserException { 
        basicParserPool.initialize();
        
        DocumentBuilder builder = basicParserPool.getBuilder();
        
        basicParserPool.returnBuilder(builder);
        
        testDeadBuilder(builder);

        basicParserPool.returnBuilder(builder);
        
        DocumentBuilder builders[] = new DocumentBuilder[10];
        builder = basicParserPool.getBuilder();

        int i;
        for (i = 0; i < builders.length; i++) {
            builders[i] = basicParserPool.getBuilder();
        }
        DocumentBuilder builder2 = basicParserPool.getBuilder();
               
        System.gc();
        System.gc();
        
        builder.newDocument();
        for (i = 0; i < builders.length; i++) {
            builders[i].newDocument();
        }
        builder2.newDocument();
        
        basicParserPool.returnBuilder(builder);
        for (i = 0; i < builders.length; i++) {
            basicParserPool.returnBuilder(builders[i]);
        }
        basicParserPool.returnBuilder(builder2);
        System.gc();
        System.gc();
        testDeadBuilder(builder);
        for (i = 0; i < builders.length; i++) {
            testDeadBuilder(builders[i]);
        }
        testDeadBuilder(builder2);
        
        builder = null;
        for (i = 0; i < builders.length; i++) {
            builders[i] = null;
        }
        builder2 = null;
        System.gc();
        System.gc();
        
        for (i = 0; i < builders.length; i++) {
            builders[i] = basicParserPool.getBuilder();
            builders[i].newDocument();
        }
        for (i = 0; i < builders.length; i++) {
            basicParserPool.returnBuilder(builders[i]);
        }
    }
    /**
     * Test issue reported in JXT-46 - a parser should not be checked into the pool multiple times
     * via the auto-checkin mechanism by the proxy finalize().
     * 
     * @throws XMLParserException 
     * @throws ComponentInitializationException 
     * @throws InterruptedException 
     */
    @Test
    public void testFinalize() throws XMLParserException, ComponentInitializationException {
BasicParserPool pool = new BasicParserPool();
pool.initialize();
        Assert.assertEquals(0, pool.getPoolSize());
        
        // Check out and return a builder
        DocumentBuilder builder = pool.getBuilder();
        pool.returnBuilder(builder);
        
        Assert.assertEquals(1, pool.getPoolSize());
        
        // Get rid of any references to the first builder we got, so that it will be GCed
        //builder = null;
        // Do explicit GC and sleep a little make sure proxy finalize() gets called
        //System.out.println("Garbage collection and sleep");
        //System.gc();
        //Thread.sleep(3000);
        //System.out.println("Done sleeping");
        
        // Rather than relying on forcing GC behavior in the test as above, which was in initial debugging this problem,
        // explicitly invoke finalize() to simulate.
        // (which we can do b/c it's protected access *and* we're in the same package)
        try {
            ((DocumentBuilderProxy)builder).finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        Assert.assertEquals(1, pool.getPoolSize());
        
        
        // Both of these would have been the same instance pre-bug fix.
        DocumentBuilder builder1 = ((DocumentBuilderProxy) pool.getBuilder()).getProxiedBuilder();
        Assert.assertNotNull(builder1);
        DocumentBuilder builder2 = ((DocumentBuilderProxy) pool.getBuilder()).getProxiedBuilder();
        Assert.assertNotNull(builder2);
        Assert.assertFalse(builder1.equals(builder2));
        
    }
    
    
    /**
     * Test for caller (illegally) returning a builder multiple times to pool.
     * 
     * @throws XMLParserException
     * @throws ComponentInitializationException 
     */
    @Test
    public void testExplicitMultipleReturn() throws XMLParserException, ComponentInitializationException {
        basicParserPool.initialize();
        Assert.assertEquals(0, basicParserPool.getPoolSize());
        
        // Check out and return a builder
        DocumentBuilder builder = basicParserPool.getBuilder();
        
        basicParserPool.returnBuilder(builder);
        Assert.assertEquals(1, basicParserPool.getPoolSize());
        
        // This isn't legal to do, but should be silently detected and ignored
        basicParserPool.returnBuilder(builder);
        Assert.assertEquals(1, basicParserPool.getPoolSize());
        
        DocumentBuilder builder1 = ((DocumentBuilderProxy) basicParserPool.getBuilder()).getProxiedBuilder();
        Assert.assertNotNull(builder1);
        DocumentBuilder builder2 = ((DocumentBuilderProxy) basicParserPool.getBuilder()).getProxiedBuilder();
        Assert.assertNotNull(builder2);
        Assert.assertFalse(builder1.equals(builder2));
        
    }
    
    /**
     * Test for a caller illegally using a parser proxy after it has been returned. 
     * 
     * @throws XMLParserException 
     * @throws URISyntaxException 
     * @throws ComponentInitializationException 
     * @throws IOException 
     * 
     */
    @Test
    public void testParserUseAfterReturn() throws XMLParserException, URISyntaxException, ComponentInitializationException {
        basicParserPool.initialize();
        String testPath = "/net/shibboleth/utilities/java/support/xml/foo.xml";
        InputStream is = BasicParserPoolTest.class.getResourceAsStream(testPath);
        File file = new File(this.getClass().getResource(testPath).toURI());
        
        // Check out and return a builder
        DocumentBuilder builder = basicParserPool.getBuilder();
        
        try {
            builder.parse(file);
        } catch (IllegalStateException e) {
            Assert.fail("Parser proxy was in a valid state");
        } catch (SAXException e) {
            Assert.fail("Parser proxy was in a valid state");
        } catch (IOException e) {
            Assert.fail("Parser proxy was in a valid state");
        }
        
        basicParserPool.returnBuilder(builder);
        
        
        try {
            builder.parse(file);
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IllegalStateException e) {
            // do nothing, expected
        } catch (SAXException e) {
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IOException e) {
            Assert.fail("Parser proxy was in an illegal state");
        }
        
        try {
            builder.parse(is);
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IllegalStateException e) {
            // do nothing, expected
        } catch (SAXException e) {
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IOException e) {
            Assert.fail("Parser proxy was in an illegal state");
        }
        
        try {
            builder.parse(new InputSource(is));
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IllegalStateException e) {
            // do nothing, expected
        } catch (SAXException e) {
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IOException e) {
            Assert.fail("Parser proxy was in an illegal state");
        }
        
        try {
            builder.parse(file.toURI().toString());
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IllegalStateException e) {
            // do nothing, expected
        } catch (SAXException e) {
            Assert.fail("Parser proxy was in an illegal state");
        } catch (IOException e) {
            Assert.fail("Parser proxy was in an illegal state");
        }
        
    }
    
    /**
     * Test that only maxPoolSize parsers are ever cached.
     * 
     * @throws XMLParserException
     * @throws ComponentInitializationException 
     */
    @Test
    public void testMaxPoolSize() throws XMLParserException, ComponentInitializationException {
        basicParserPool.initialize();
        Assert.assertEquals(0, basicParserPool.getPoolSize());
        
        ArrayList<DocumentBuilder> list = new ArrayList<>();
        
        // Get 3x the maxPoolSize number of builders
        for (int i=0; i < 3*maxPoolSize; i++) {
            list.add(basicParserPool.getBuilder());
        }
        
        Assert.assertEquals(0, basicParserPool.getPoolSize());
        
        for (DocumentBuilder b : list) {
           basicParserPool.returnBuilder(b); 
        }
        
        // Even though we return 3*maxPoolSize builders, only maxPoolSize should be cached
        Assert.assertEquals(maxPoolSize, basicParserPool.getPoolSize());
    }
    
    private <T> T nullValue() {
        return null;
    }
        
    // Helpers
    
    public static class MockEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return null;
        }
    }
    
    public static class MockErrorHandler implements ErrorHandler {
        public void warning(SAXParseException exception) throws SAXException { }
        public void error(SAXParseException exception) throws SAXException { }
        public void fatalError(SAXParseException exception) throws SAXException { }
    }

}
