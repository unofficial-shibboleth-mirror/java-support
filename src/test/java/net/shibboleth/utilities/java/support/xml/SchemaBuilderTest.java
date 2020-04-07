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

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resource.TestResourceConverter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Tests for {@link NamespaceSupport}
 */
public class SchemaBuilderTest {

    private static final String TEST_DIR = "/net/shibboleth/utilities/java/support/xml/schemaBuilderTestDir/";

    private static final String FILE_ROOT = "src/test/resources/";

    private static final String FIRST_SCHEMA_FILE = "schemaBuilderTest-schemaFirstLoaded.xsd";

    private static final String SECOND_SCHEMA_FILE = "schemaBuilderTest-schemaSecondLoaded.xsd";

    private Resource works;

    private Resource fails;

    private StreamSource workingSource() throws IOException {
        return new StreamSource(works.getInputStream());
    }

    private StreamSource failingSource() throws IOException {
        return new StreamSource(fails.getInputStream());
    }

    @BeforeClass public void setup()  {
        works = new ClassPathResource(TEST_DIR + "schemaBuilderTest-works.xml");
        fails = new ClassPathResource(TEST_DIR + "schemaBuilderTest-fails.xml");
    }

    @Test public void testFiles() throws SAXException, IOException {
        FileInputStream first = new FileInputStream(FILE_ROOT + TEST_DIR + FIRST_SCHEMA_FILE);
        FileInputStream second = new FileInputStream(FILE_ROOT + TEST_DIR + SECOND_SCHEMA_FILE);

        final SchemaBuilder builder = new SchemaBuilder();
        builder.addSchema(first);
        builder.addSchema(second);
        Schema schema = builder.buildSchema();

        Validator validator = schema.newValidator();

        validator.validate(workingSource());

        boolean thrown = false;
        try {
            validator.validate(failingSource());
        } catch (final Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should fail to validate");
    }

    @Test public void testInputStream() throws SAXException, IOException, ComponentInitializationException {
        Resource first = new ClassPathResource(TEST_DIR + FIRST_SCHEMA_FILE);
        Resource second = new ClassPathResource(TEST_DIR + SECOND_SCHEMA_FILE);

        final SchemaBuilder builder = new SchemaBuilder();
        builder.addSchema(first.getInputStream());
        builder.addSchema(second.getInputStream());
        Schema schema = builder.buildSchema();

        Validator validator = schema.newValidator();

        validator.validate(workingSource());

        boolean thrown = false;
        try {
            validator.validate(failingSource());
        } catch (final Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should fail to validate");
    }
    
    @Test public void testResource() throws SAXException, IOException, ComponentInitializationException {
        net.shibboleth.utilities.java.support.resource.Resource first = TestResourceConverter.of(new ClassPathResource(TEST_DIR + FIRST_SCHEMA_FILE));
        net.shibboleth.utilities.java.support.resource.Resource second = TestResourceConverter.of(new ClassPathResource(TEST_DIR + SECOND_SCHEMA_FILE));

        final SchemaBuilder builder = new SchemaBuilder();
        builder.addSchema(first);
        builder.addSchema(second);
        Schema schema = builder.buildSchema();

        Validator validator = schema.newValidator();

        validator.validate(workingSource());

        boolean thrown = false;
        try {
            validator.validate(failingSource());
        } catch (final Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should fail to validate");
    }

}