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

package net.shibboleth.utilities.java.support.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.resource.Resource;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** Tests for {@link EvaluableScript}.*/
@SuppressWarnings("javadoc")
public class EvaluableScriptTest {

    
    @Nonnull @NotEmpty private static final String SCRIPT_LANGUAGE = "JavaScript";

    /** A simple script to set a constant value. */
    @Nonnull @NotEmpty private static final String TEST_SIMPLE_SCRIPT = "importPackage(Packages.net.shibboleth.idp.attribute);\n"
            + "foo = res = new Attribute(\"bar\");\n foo.addValue(\"value\");\n";
    
    private File theFile;
    
    @AfterClass public void deleteFile() {
        if (null != theFile && theFile.exists()) {
            theFile.delete();
        }
    }
    
    @SuppressWarnings("removal")
    @Test public void testEvaluableScriptDeprecated() throws ScriptException, IOException {
       
        new EvaluableScript(SCRIPT_LANGUAGE, TEST_SIMPLE_SCRIPT);
        
        try {
            new EvaluableScript(" ", TEST_SIMPLE_SCRIPT);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }
        
        try {
            new EvaluableScript(SCRIPT_LANGUAGE, " ");
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        try {
            new EvaluableScript(nullValue(), TEST_SIMPLE_SCRIPT);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }
        
        try {
            new EvaluableScript(SCRIPT_LANGUAGE, (String) nullValue());
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        theFile = File.createTempFile("EvaluableScriptTest", ".js");

        try (final FileWriter s = new FileWriter(theFile)) {
            s.write(TEST_SIMPLE_SCRIPT, 0, TEST_SIMPLE_SCRIPT.length());
        }

        Assert.assertEquals((new EvaluableScript(SCRIPT_LANGUAGE, theFile)).getScriptLanguage(), SCRIPT_LANGUAGE);

        try (InputStream is = new FileInputStream(theFile)) {
            Assert.assertEquals((new EvaluableScript(SCRIPT_LANGUAGE, is)).getScriptLanguage(), SCRIPT_LANGUAGE);
        }
        
        try (InputStream is = new FileInputStream(theFile)) {
            Assert.assertEquals((new EvaluableScript(SCRIPT_LANGUAGE, resourceFor(is))).getScriptLanguage(), SCRIPT_LANGUAGE);
        }

        try {
            new EvaluableScript(nullValue(), theFile);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }
        
        try {
            new EvaluableScript(SCRIPT_LANGUAGE, (File) nullValue());
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }
    }

    private EvaluableScript testEvaluableScript(String language, String script) throws ComponentInitializationException {
        final EvaluableScript ev = new EvaluableScript();
        ev.setEngineName(language);
        ev.setScript(script);
        ev.initialize();
        return ev;
    }

    private EvaluableScript testEvaluableScript(String language, File script) throws ComponentInitializationException, IOException {
        final EvaluableScript ev = new EvaluableScript();
        ev.setEngineName(language);
        ev.setScript(script);
        ev.initialize();
        return ev;
    }

    private EvaluableScript testEvaluableScript(String language, InputStream script) throws ComponentInitializationException, IOException {
        final EvaluableScript ev = new EvaluableScript();
        ev.setEngineName(language);
        ev.setScript(script);
        ev.initialize();
        return ev;
    }

    private EvaluableScript testEvaluableScript(String language, Resource script) throws ComponentInitializationException, IOException {
        final EvaluableScript ev = new EvaluableScript();
        ev.setEngineName(language);
        ev.setScript(script);
        ev.initialize();
        return ev;
    }

    @Test public void testEvaluableScriptNonDeprecated() throws ScriptException, IOException, ComponentInitializationException {

        testEvaluableScript(SCRIPT_LANGUAGE, TEST_SIMPLE_SCRIPT);

        try {
            testEvaluableScript(" ", TEST_SIMPLE_SCRIPT);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        try {
            testEvaluableScript(SCRIPT_LANGUAGE, " ");
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        try {
            testEvaluableScript(nullValue(), TEST_SIMPLE_SCRIPT);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        try {
            testEvaluableScript(SCRIPT_LANGUAGE, (String) nullValue());
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        theFile = File.createTempFile("EvaluableScriptTest", ".js");

        try (final FileWriter s = new FileWriter(theFile)) {
            s.write(TEST_SIMPLE_SCRIPT, 0, TEST_SIMPLE_SCRIPT.length());
        }

        Assert.assertEquals((testEvaluableScript(SCRIPT_LANGUAGE, theFile)).getScriptLanguage(), SCRIPT_LANGUAGE);

        try (InputStream is = new FileInputStream(theFile)) {
            Assert.assertEquals((testEvaluableScript(SCRIPT_LANGUAGE, is)).getScriptLanguage(), SCRIPT_LANGUAGE);
        }

        try (InputStream is = new FileInputStream(theFile)) {
            Assert.assertEquals((testEvaluableScript(SCRIPT_LANGUAGE, resourceFor(is))).getScriptLanguage(), SCRIPT_LANGUAGE);
        }

        try {
            testEvaluableScript(nullValue(), theFile);
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }

        try {
            testEvaluableScript(SCRIPT_LANGUAGE, (File) nullValue());
            Assert.fail();
        } catch (final ConstraintViolationException e) {
            // OK
        }
    }

    private Resource resourceFor(final InputStream is) {
        return new Resource() {

            public long lastModified() throws IOException {
                return 0;
            }

            public boolean isReadable() {
                return true;
            }

            public boolean isOpen() {
                return false;
            }

            public URL getURL() throws IOException {
                return null;
            }

            public URI getURI() throws IOException {
                return null;
            }

            public InputStream getInputStream() throws IOException {
                return is;
            }

            public String getFilename() {
                return null;
            }

            public File getFile() throws IOException {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public boolean exists() {
                return true;
            }

            public Resource createRelativeResource(String relativePath) throws IOException {
                return null;
            }

            public long contentLength() throws IOException {
                return 0;
            }
        };
    }

    private <T> T nullValue() {
        return null;
    }

}
