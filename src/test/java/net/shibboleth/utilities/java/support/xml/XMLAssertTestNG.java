/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package net.shibboleth.utilities.java.support.xml;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTestException;
import org.custommonkey.xmlunit.NodeTester;
import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XSLTConstants;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>
 * This class is functionally identical to, and copied from, {@link org.custommonkey.xmlunit.XMLAssert}. Rather than
 * inheriting from and using JUnit's Assert class, it instead uses TestNG's {@link org.testng.Assert}.
 * </p>
 * 
 * <p>
 * Collection of static methods so that XML assertion facilities are available in any class, not just test suites.
 * Thanks to Andrew McCormick and others for suggesting this refactoring.
 * </p>
 * 
 * <p>Available assertion methods are:</p>
 * 
 * <ul>
 * <li><strong><code>assertXMLEqual</code></strong>:
 * assert that two pieces of XML markup are <i>similar</i></li>
 * <li><strong><code>assertXMLNotEqual</code></strong>:
 * assert that two pieces of XML markup are <i>different</i></li>
 * <li><strong><code>assertXMLIdentical</code></strong>:
 * assert that two pieces of XML markup are <i>identical</i>. In most cases this assertion is too strong and
 * <code>assertXMLEqual</code> is sufficient</li>
 * <li><strong><code>assertXpathExists</code></strong>:
 * assert that an XPath expression matches at least one node</li>
 * <li><strong><code>assertXpathNotExists</code></strong>:
 * assert that an XPath expression does not match any nodes</li>
 * <li><strong><code>assertXpathsEqual</code></strong>:
 * assert that the nodes obtained by executing two Xpaths are <i>similar</i></li>
 * <li><strong><code>assertXpathsNotEqual</code></strong>:
 * assert that the nodes obtained by executing two Xpaths are <i>different</i></li>
 * <li><strong><code>assertXpathValuesEqual</code></strong>:
 * assert that the flattened String obtained by executing two Xpaths are <i>similar</i></li>
 * <li><strong><code>assertXpathValuesNotEqual</code></strong>:
 * assert that the flattened String obtained by executing two Xpaths are <i>different</i></li>
 * <li><strong><code>assertXpathEvaluatesTo</code></strong>:
 * assert that the flattened String obtained by executing an Xpath is a particular value</li>
 * <li><strong><code>assertXMLValid</code></strong>:
 * assert that a piece of XML markup is valid with respect to a DTD: either by using the markup's own DTD or a different
 * DTD</li>
 * <li><strong><code>assertNodeTestPasses</code></strong>:
 * assert that a piece of XML markup passes a {@link NodeTest NodeTest}</li>
 * </ul>
 * 
 * <p>
 * All underlying similarity and difference testing is done using {@link Diff Diff} instances which can be instantiated
 * and evaluated independently of this class.
 * </p>
 * 
 * @see Diff#similar()
 * @see Diff#identical()
 * 
 * <p>
 * Examples and more at <a href="http://xmlunit.sourceforge.net">xmlunit.sourceforge.net</a>
 * </p>
 */
public class XMLAssertTestNG implements XSLTConstants {

    protected XMLAssertTestNG() {
        super();
    }

    /**
     * Assert that the result of an XML comparison is or is not similar.
     * 
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is similar
     */
    public static void assertXMLEqual(Diff diff, boolean assertion) {
        assertXMLEqual(null, diff, assertion);
    }

    /**
     * Assert that the result of an XML comparison is or is not similar.
     * 
     * @param msg additional message to display if assertion fails
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is similar
     */
    public static void assertXMLEqual(String msg, Diff diff, boolean assertion) {
        if (assertion != diff.similar()) {
            Assert.fail(getFailMessage(msg, diff));
        }
    }

    private static String getFailMessage(String msg, Diff diff) {
        StringBuffer sb = new StringBuffer();
        if (msg != null && msg.length() > 0) {
            sb.append(msg).append(", ");
        }
        return sb.append(diff.toString()).toString();
    }

    /**
     * Assert that the result of an XML comparison is or is not identical
     * 
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is identical
     */
    public static void assertXMLIdentical(Diff diff, boolean assertion) {
        assertXMLIdentical(null, diff, assertion);
    }

    /**
     * Assert that the result of an XML comparison is or is not identical
     * 
     * @param msg Message to display if assertion fails
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is identical
     */
    public static void assertXMLIdentical(String msg, Diff diff, boolean assertion) {
        if (assertion != diff.identical()) {
            Assert.fail(getFailMessage(msg, diff));
        }
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(InputSource control, InputSource test) throws SAXException, IOException {
        assertXMLEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(String control, String test) throws SAXException, IOException {
        assertXMLEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     */
    public static void assertXMLEqual(Document control, Document test) {
        assertXMLEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(Reader control, Reader test) throws SAXException, IOException {
        assertXMLEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(String err, InputSource control, InputSource test) throws SAXException,
            IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, true);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(String err, String control, String test) throws SAXException, IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, true);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     */
    public static void assertXMLEqual(String err, Document control, Document test) {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, true);
    }

    /**
     * Assert that two XML documents are similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLEqual(String err, Reader control, Reader test) throws SAXException, IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, true);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(InputSource control, InputSource test) throws SAXException, IOException {
        assertXMLNotEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(String control, String test) throws SAXException, IOException {
        assertXMLNotEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     */
    public static void assertXMLNotEqual(Document control, Document test) {
        assertXMLNotEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(Reader control, Reader test) throws SAXException, IOException {
        assertXMLNotEqual(null, control, test);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(String err, InputSource control, InputSource test) throws SAXException,
            IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, false);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(String err, String control, String test) throws SAXException, IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, false);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     */
    public static void assertXMLNotEqual(String err, Document control, Document test) {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, false);
    }

    /**
     * Assert that two XML documents are NOT similar
     * 
     * @param err Message to be displayed on assertion failure
     * @param control XML to be compared against
     * @param test XML to be tested
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertXMLNotEqual(String err, Reader control, Reader test) throws SAXException, IOException {
        Diff diff = new Diff(control, test);
        assertXMLEqual(err, diff, false);
    }

    /**
     * Assert that the node lists of two Xpaths in the same document are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsEqual(String controlXpath, String testXpath, Document document)
            throws XpathException {
        assertXpathsEqual(controlXpath, document, testXpath, document);
    }

    /**
     * Assert that the node lists of two Xpaths in the same document are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsEqual(String controlXpath, String testXpath, InputSource document)
            throws SAXException, IOException, XpathException {
        assertXpathsEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(document));
    }

    /**
     * Assert that the node lists of two Xpaths in the same XML string are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathsEqual(String controlXpath, String testXpath, String inXMLString)
            throws SAXException, IOException, XpathException {
        assertXpathsEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(inXMLString));
    }

    /**
     * Assert that the node lists of two Xpaths in two documents are equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsEqual(String controlXpath, InputSource controlDocument, String testXpath,
            InputSource testDocument) throws SAXException, IOException, XpathException {
        assertXpathsEqual(controlXpath, XMLUnit.buildControlDocument(controlDocument), testXpath,
                XMLUnit.buildTestDocument(testDocument));
    }

    /**
     * Assert that the node lists of two Xpaths in two XML strings are equal
     * 
     * @param controlXpath ...
     * @param inControlXMLString ...
     * @param testXpath ...
     * @param inTestXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathsEqual(String controlXpath, String inControlXMLString, String testXpath,
            String inTestXMLString) throws SAXException, IOException, XpathException {
        assertXpathsEqual(controlXpath, XMLUnit.buildControlDocument(inControlXMLString), testXpath,
                XMLUnit.buildTestDocument(inTestXMLString));
    }

    /**
     * Assert that the node lists of two Xpaths in two documents are equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsEqual(String controlXpath, Document controlDocument, String testXpath,
            Document testDocument) throws XpathException {
        assertXpathEquality(controlXpath, controlDocument, testXpath, testDocument, true);
    }

    /**
     * Assert that the node lists of two Xpaths in the same document are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsNotEqual(String controlXpath, String testXpath, Document document)
            throws XpathException {
        assertXpathsNotEqual(controlXpath, document, testXpath, document);
    }

    /**
     * Assert that the node lists of two Xpaths in the same document are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsNotEqual(String controlXpath, String testXpath, InputSource document)
            throws SAXException, IOException, XpathException {
        assertXpathsNotEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(document));
    }

    /**
     * Assert that the node lists of two Xpaths in the same XML string are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathsNotEqual(String controlXpath, String testXpath, String inXMLString)
            throws SAXException, IOException, XpathException {
        assertXpathsNotEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(inXMLString));
    }

    /**
     * Assert that the node lists of two Xpaths in two XML strings are NOT equal
     * 
     * @param controlXpath ...
     * @param inControlXMLString ...
     * @param testXpath ...
     * @param inTestXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathsNotEqual(String controlXpath, String inControlXMLString, String testXpath,
            String inTestXMLString) throws SAXException, IOException, XpathException {
        assertXpathsNotEqual(controlXpath, XMLUnit.buildControlDocument(inControlXMLString), testXpath,
                XMLUnit.buildTestDocument(inTestXMLString));
    }

    /**
     * Assert that the node lists of two Xpaths in two XML strings are NOT equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathsNotEqual(String controlXpath, InputSource controlDocument, String testXpath,
            InputSource testDocument) throws SAXException, IOException, XpathException {
        assertXpathsNotEqual(controlXpath, XMLUnit.buildControlDocument(controlDocument), testXpath,
                XMLUnit.buildTestDocument(testDocument));
    }

    /**
     * Assert that the node lists of two Xpaths in two documents are NOT equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathsNotEqual(String controlXpath, Document controlDocument, String testXpath,
            Document testDocument) throws XpathException {
        assertXpathEquality(controlXpath, controlDocument, testXpath, testDocument, false);
    }

    /**
     * Assert that the node lists of two Xpaths in two documents are equal or not.
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @param equal whether the values should be equal.
     * @throws XpathException ...
     * @see XpathEngine
     */
    private static void assertXpathEquality(String controlXpath, Document controlDocument, String testXpath,
            Document testDocument, boolean equal) throws XpathException {
        XpathEngine xpath = XMLUnit.newXpathEngine();
        Diff diff =
                new Diff(asXpathResultDocument(XMLUnit.newControlParser(),
                        xpath.getMatchingNodes(controlXpath, controlDocument)), asXpathResultDocument(
                        XMLUnit.newTestParser(), xpath.getMatchingNodes(testXpath, testDocument)));
        assertXMLEqual(diff, equal);
    }

    /**
     * Assert that the evaluation of two Xpaths in the same document are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathValuesEqual(String controlXpath, String testXpath, Document document)
            throws XpathException {
        assertXpathValuesEqual(controlXpath, document, testXpath, document);
    }

    /**
     * Assert that the evaluation of two Xpaths in the same XML string are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesEqual(String controlXpath, String testXpath, InputSource document)
            throws SAXException, IOException, XpathException {
        assertXpathValuesEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(document));
    }

    /**
     * Assert that the evaluation of two Xpaths in the same XML string are equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesEqual(String controlXpath, String testXpath, String inXMLString)
            throws SAXException, IOException, XpathException {
        assertXpathValuesEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(inXMLString));
    }

    /**
     * Assert that the evaluation of two Xpaths in two XML strings are equal
     * 
     * @param controlXpath ...
     * @param control ...
     * @param testXpath ...
     * @param test ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesEqual(String controlXpath, InputSource control, String testXpath,
            InputSource test) throws SAXException, IOException, XpathException {
        assertXpathValuesEqual(controlXpath, XMLUnit.buildControlDocument(control), testXpath,
                XMLUnit.buildTestDocument(test));
    }

    /**
     * Assert that the evaluation of two Xpaths in two XML strings are equal
     * 
     * @param controlXpath ...
     * @param inControlXMLString ...
     * @param testXpath ...
     * @param inTestXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesEqual(String controlXpath, String inControlXMLString, String testXpath,
            String inTestXMLString) throws SAXException, IOException, XpathException {
        assertXpathValuesEqual(controlXpath, XMLUnit.buildControlDocument(inControlXMLString), testXpath,
                XMLUnit.buildTestDocument(inTestXMLString));
    }

    /**
     * Assert that the evaluation of two Xpaths in two documents are equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws XpathException ...
     * @see XpathEngine
     */
    public static void assertXpathValuesEqual(String controlXpath, Document controlDocument, String testXpath,
            Document testDocument) throws XpathException {
        XpathEngine xpath = XMLUnit.newXpathEngine();
        Assert.assertEquals(xpath.evaluate(controlXpath, controlDocument),
                xpath.evaluate(controlXpath, controlDocument));
    }

    /**
     * Assert that the evaluation of two Xpaths in the same XML string are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param control ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, String testXpath, InputSource control)
            throws SAXException, IOException, XpathException {
        assertXpathValuesNotEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(control));
    }

    /**
     * Assert that the evaluation of two Xpaths in the same XML string are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, String testXpath, String inXMLString)
            throws SAXException, IOException, XpathException {
        assertXpathValuesNotEqual(controlXpath, testXpath, XMLUnit.buildControlDocument(inXMLString));
    }

    /**
     * Assert that the evaluation of two Xpaths in the same document are NOT equal
     * 
     * @param controlXpath ...
     * @param testXpath ...
     * @param document ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, String testXpath, Document document)
            throws XpathException {
        assertXpathValuesNotEqual(controlXpath, document, testXpath, document);
    }

    /**
     * Assert that the evaluation of two Xpaths in two XML strings are NOT equal
     * 
     * @param controlXpath ...
     * @param control ...
     * @param testXpath ...
     * @param test ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, InputSource control, String testXpath,
            InputSource test) throws SAXException, IOException, XpathException {
        assertXpathValuesNotEqual(controlXpath, XMLUnit.buildControlDocument(control), testXpath,
                XMLUnit.buildTestDocument(test));
    }

    /**
     * Assert that the evaluation of two Xpaths in two XML strings are NOT equal
     * 
     * @param controlXpath ...
     * @param inControlXMLString ...
     * @param testXpath ...
     * @param inTestXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, String inControlXMLString, String testXpath,
            String inTestXMLString) throws SAXException, IOException, XpathException {
        assertXpathValuesNotEqual(controlXpath, XMLUnit.buildControlDocument(inControlXMLString), testXpath,
                XMLUnit.buildTestDocument(inTestXMLString));
    }

    /**
     * Assert that the evaluation of two Xpaths in two documents are NOT equal
     * 
     * @param controlXpath ...
     * @param controlDocument ...
     * @param testXpath ...
     * @param testDocument ...
     * @throws XpathException ...
     */
    public static void assertXpathValuesNotEqual(String controlXpath, Document controlDocument, String testXpath,
            Document testDocument) throws XpathException {
        XpathEngine xpath = XMLUnit.newXpathEngine();
        String control = xpath.evaluate(controlXpath, controlDocument);
        String test = xpath.evaluate(testXpath, testDocument);
        if (control != null) {
            if (control.equals(test)) {
                Assert.fail("Expected test value NOT to be equal to control but both were " + test);
            }
        } else if (test != null) {
            Assert.fail("control xPath evaluated to empty node set, " + "but test xPath evaluated to " + test);
        }
    }

    /**
     * Assert the value of an Xpath expression in an XML document.
     * 
     * @param expectedValue ...
     * @param xpathExpression ...
     * @param control ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathEvaluatesTo(String expectedValue, String xpathExpression, InputSource control)
            throws SAXException, IOException, XpathException {
        Document document = XMLUnit.buildControlDocument(control);
        assertXpathEvaluatesTo(expectedValue, xpathExpression, document);
    }

    /**
     * Assert the value of an Xpath expression in an XML String
     * 
     * @param expectedValue ...
     * @param xpathExpression ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathEvaluatesTo(String expectedValue, String xpathExpression, String inXMLString)
            throws SAXException, IOException, XpathException {
        Document document = XMLUnit.buildControlDocument(inXMLString);
        assertXpathEvaluatesTo(expectedValue, xpathExpression, document);
    }

    /**
     * Assert the value of an Xpath expression in an DOM Document
     * 
     * @param expectedValue ...
     * @param xPathExpression ...
     * @param inDocument ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathEvaluatesTo(String expectedValue, String xPathExpression, Document inDocument)
            throws XpathException {
        XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
        Assert.assertEquals(simpleXpathEngine.evaluate(xPathExpression, inDocument), expectedValue);
    }

    /**
     * Assert that a specific XPath exists in some given XML
     * 
     * @param xPathExpression ...
     * @param control ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathExists(String xPathExpression, InputSource control) throws IOException, SAXException,
            XpathException {
        Document inDocument = XMLUnit.buildControlDocument(control);
        assertXpathExists(xPathExpression, inDocument);
    }

    /**
     * Assert that a specific XPath exists in some given XML
     * 
     * @param xPathExpression ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathExists(String xPathExpression, String inXMLString) throws IOException, SAXException,
            XpathException {
        Document inDocument = XMLUnit.buildControlDocument(inXMLString);
        assertXpathExists(xPathExpression, inDocument);
    }

    /**
     * Assert that a specific XPath exists in some given XML
     * 
     * @param xPathExpression ...
     * @param inDocument ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathExists(String xPathExpression, Document inDocument) throws XpathException {
        XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
        NodeList nodeList = simpleXpathEngine.getMatchingNodes(xPathExpression, inDocument);
        int matches = nodeList.getLength();
        Assert.assertTrue(matches > 0, "Expecting to find matches for Xpath " + xPathExpression);
    }

    /**
     * Assert that a specific XPath does NOT exist in some given XML
     * 
     * @param xPathExpression ...
     * @param control ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathNotExists(String xPathExpression, InputSource control) throws IOException,
            SAXException, XpathException {
        Document inDocument = XMLUnit.buildControlDocument(control);
        assertXpathNotExists(xPathExpression, inDocument);
    }

    /**
     * Assert that a specific XPath does NOT exist in some given XML
     * 
     * @param xPathExpression ...
     * @param inXMLString ...
     * @throws SAXException ...
     * @throws IOException ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathNotExists(String xPathExpression, String inXMLString) throws IOException,
            SAXException, XpathException {
        Document inDocument = XMLUnit.buildControlDocument(inXMLString);
        assertXpathNotExists(xPathExpression, inDocument);
    }

    /**
     * Assert that a specific XPath does NOT exist in some given XML
     * 
     * @param xPathExpression ...
     * @param inDocument ...
     * @throws XpathException ...
     * @see XpathEngine which provides the underlying evaluation mechanism
     */
    public static void assertXpathNotExists(String xPathExpression, Document inDocument) throws XpathException {
        XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
        NodeList nodeList = simpleXpathEngine.getMatchingNodes(xPathExpression, inDocument);
        int matches = nodeList.getLength();
        Assert.assertEquals(matches, 0, "Should be zero matches for Xpath " + xPathExpression);
    }

    /**
     * Assert that an InputSource containing XML contains valid XML: the document must contain a DOCTYPE declaration to
     * be validated
     * 
     * @param xml ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(InputSource xml) throws SAXException, ConfigurationException {
        assertXMLValid(new Validator(xml));
    }

    /**
     * Assert that a String containing XML contains valid XML: the String must contain a DOCTYPE declaration to be
     * validated
     * 
     * @param xmlString ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(String xmlString) throws SAXException, ConfigurationException {
        assertXMLValid(new Validator(xmlString));
    }

    /**
     * Assert that an InputSource containing XML contains valid XML: the document must contain a DOCTYPE to be
     * validated, but the validation will use the systemId to obtain the DTD
     * 
     * @param xml ...
     * @param systemId ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(InputSource xml, String systemId) throws SAXException, ConfigurationException {
        assertXMLValid(new Validator(xml, systemId));
    }

    /**
     * Assert that a String containing XML contains valid XML: the String must contain a DOCTYPE to be validated, but
     * the validation will use the systemId to obtain the DTD
     * 
     * @param xmlString ...
     * @param systemId ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(String xmlString, String systemId) throws SAXException, ConfigurationException {
        assertXMLValid(new Validator(xmlString, systemId));
    }

    /**
     * Assert that a piece of XML contains valid XML: the document will be given a DOCTYPE to be validated with the name
     * and systemId specified regardless of whether it already contains a doctype declaration.
     * 
     * @param xml ...
     * @param systemId ...
     * @param doctype ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(InputSource xml, String systemId, String doctype) throws SAXException,
            ConfigurationException {
        assertXMLValid(new Validator(xml, systemId, doctype));
    }

    /**
     * Assert that a String containing XML contains valid XML: the String will be given a DOCTYPE to be validated with
     * the name and systemId specified regardless of whether it already contains a doctype declaration.
     * 
     * @param xmlString ...
     * @param systemId ...
     * @param doctype ...
     * @throws SAXException ...
     * @throws ConfigurationException if validation could not be turned on
     * @see Validator
     */
    public static void assertXMLValid(String xmlString, String systemId, String doctype) throws SAXException,
            ConfigurationException {
        assertXMLValid(new Validator(new StringReader(xmlString), systemId, doctype));
    }

    /**
     * Assert that a Validator instance returns <code>isValid() == true</code>
     * 
     * @param validator ...
     */
    public static void assertXMLValid(Validator validator) {
        Assert.assertEquals(validator.isValid(), true, validator.toString());
    }

    /**
     * Execute a <code>NodeTest</code> for a single node type
     * and assert that it passes
     * 
     * @param xml XML to be tested
     * @param tester The test strategy
     * @param nodeType The node type to be tested: constants defined in {@link org.w3c.dom.Node} e.g.
     *            <code>Node.ELEMENT_NODE</code>
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertNodeTestPasses(InputSource xml, NodeTester tester, short nodeType) throws SAXException,
            IOException {
        NodeTest test = new NodeTest(xml);
        assertNodeTestPasses(test, tester, new short[] {nodeType}, true);
    }

    /**
     * Execute a <code>NodeTest</code> for a single node type
     * and assert that it passes
     * 
     * @param xmlString XML to be tested
     * @param tester The test strategy
     * @param nodeType The node type to be tested: constants defined in {@link org.w3c.dom.Node} e.g.
     *            <code>Node.ELEMENT_NODE</code>
     * @throws SAXException ...
     * @throws IOException ...
     */
    public static void assertNodeTestPasses(String xmlString, NodeTester tester, short nodeType) throws SAXException,
            IOException {
        NodeTest test = new NodeTest(xmlString);
        assertNodeTestPasses(test, tester, new short[] {nodeType}, true);
    }

    /**
     * Execute a <code>NodeTest</code> for multiple node types and make an
     * assertion about it whether it is expected to pass
     * 
     * @param test a NodeTest instance containing the XML source to be tested
     * @param tester The test strategy
     * @param nodeTypes The node types to be tested: constants defined in {@link org.w3c.dom.Node} e.g.
     *            <code>Node.ELEMENT_NODE</code>
     * @param assertion true if the test is expected to pass, false otherwise
     */
    public static void assertNodeTestPasses(NodeTest test, NodeTester tester, short[] nodeTypes, boolean assertion) {
        try {
            test.performTest(tester, nodeTypes);
            if (!assertion) {
                Assert.fail("Expected node test to fail, but it passed!");
            }
        } catch (NodeTestException e) {
            if (assertion) {
                Assert.fail("Expected node test to pass, but it failed! " + e.getMessage());
            }
        }
    }

    private static Document asXpathResultDocument(final DocumentBuilder builder, final NodeList nodes) {
        final Document d = builder.newDocument();
        final Element root = d.createElement("xpathResult");
        d.appendChild(root);
        final int length = nodes.getLength();
        for (int i = 0; i < length; i++) {
            root.appendChild(d.importNode(nodes.item(i), true));
        }
        return d;
    }
}
