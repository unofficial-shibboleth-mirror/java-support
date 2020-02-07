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

package net.shibboleth.utilities.java.support.codec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

/** {@link Base64Support} unit test. */
public class Base64SupportTest {

    /** A plain text string to be encoded. */
    private final static String PLAIN_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean "
            + "malesuada, eros tempor aliquam ullamcorper, mauris velit iaculis metus, quis vulputate diam quam";

    /** Encoded version of the plain text. */
    private final static String UNCHUNCKED_ENCODED_TEXT =
            "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4g"
                    + "QWVuZWFuIG1hbGVzdWFkYSwgZXJvcyB0ZW1wb3IgYWxpcXVhbSB1bGxhbWNvcnBlciwgbWF1cmlz"
                    + "IHZlbGl0IGlhY3VsaXMgbWV0dXMsIHF1aXMgdnVscHV0YXRlIGRpYW0gcXVhbQ==";

    private final static String CHUNCKED_ENCODED_TEXT =
            "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4g\n"
                    + "QWVuZWFuIG1hbGVzdWFkYSwgZXJvcyB0ZW1wb3IgYWxpcXVhbSB1bGxhbWNvcnBlciwgbWF1cmlz\n"
                    + "IHZlbGl0IGlhY3VsaXMgbWV0dXMsIHF1aXMgdnVscHV0YXRlIGRpYW0gcXVhbQ==";
    
    private final static String URLSAFE_UNCHUNCKED_ENCODED_TEXT =
            "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4g"
                    + "QWVuZWFuIG1hbGVzdWFkYSwgZXJvcyB0ZW1wb3IgYWxpcXVhbSB1bGxhbWNvcnBlciwgbWF1cmlz"
                    + "IHZlbGl0IGlhY3VsaXMgbWV0dXMsIHF1aXMgdnVscHV0YXRlIGRpYW0gcXVhbQ";

    //Inited below
    private static byte[] PLAIN_BYTES;
    
    private final static String UNCHUNCKED_ENCODED_BYTES = "FPucA9l+";
    
    private final static String URLSAFE_UNCHUNCKED_ENCODED_BYTES = "FPucA9l-";
   
    
    /** 
     * Invalid base64 string as it has invalid trailing digits.
     * Correctly fails with commons-codec 1.14 and greater.
     * @see <a href="https://issues.apache.org/jira/browse/CODEC-270">CODEC-270</a>
     */
    private final static String INVALID_BASE64_TRAILING = "AB==";
    
    /** Invalid base64, should not produce a result.*/
    private final static String INVALID_BASE64 = "ZE=";
    
    /** Empty string.*/
    private final static String EMPTY_STRING = "";
    

    @BeforeClass
    public void setUp() throws DecoderException {
        PLAIN_BYTES = Hex.decodeHex("14fb9c03d97e".toCharArray());
    }
    
    /** Test Base64 encoding content. 
     * 
     * @throws EncodingException on encoding failure
     */
    @Test public void testEncode() throws EncodingException {
        Assert.assertEquals(Base64Support.encode(PLAIN_TEXT.getBytes(), false), UNCHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encode(PLAIN_TEXT.getBytes(), true), CHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encode(PLAIN_BYTES, false), UNCHUNCKED_ENCODED_BYTES);
    }
    
    /** 
     * Test encoding an empty byte array produces an empty string and does not throw any exceptions.
     * 
     * @throws EncodingException on encoding failure. Should not happen.
     */
    @Test public void testEncodeEmptyByteArray() throws EncodingException {
        final String encoded = Base64Support.encode(new byte[0], false);
        Assert.assertNotNull(encoded);
        Assert.assertEquals(encoded, EMPTY_STRING);
    }
    
    /**
     * Test a null byte array argument violates the method contract and throws a {@link ConstraintViolationException}.
     * 
     * @throws EncodingException on encoding failure.
     */
    @Test(expectedExceptions = ConstraintViolationException.class) public void testEncodeNullInput() 
            throws EncodingException {
        Base64Support.encode(null,false);
    }
    
    /**
     * Test a null string argument violates the method contract and throws a {@link ConstraintViolationException}.
     * 
     * @throws DecodingException on decoding failure.
     */
    @Test(expectedExceptions = ConstraintViolationException.class) public void testDecodeNullInput() 
            throws DecodingException {
        Base64Support.decode(null);
    }

    /** Test Base64 decoding content. 
     *  
     * @throws DecodingException on decoding failure.
     */
    @Test public void testDecode() throws DecodingException {
        Assert.assertEquals(new String(Base64Support.decode(UNCHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(new String(Base64Support.decode(CHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(Base64Support.decode(UNCHUNCKED_ENCODED_BYTES), PLAIN_BYTES);
    }
    
    /**
     * Test that an invalid base64 input string does not return a response, 
     * instead throwing a {@link DecodingException}.
     *  
     * @throws DecodingException on decoding failure. 
     */
    @Test(expectedExceptions = DecodingException.class) public void testDecodeInvalidInput() 
            throws DecodingException {
        Base64Support.decode(INVALID_BASE64);
        
    }
    
    /**
     * Test that an empty input produces a byte array of 0 length. 
     * 
     * @throws DecodingException on decoding failure. 
     */
    @Test public void testEmptyDecodedOutput() throws DecodingException {
        byte[] decoded = Base64Support.decode(EMPTY_STRING);
        Assert.assertNotNull(decoded);
        Assert.assertTrue(decoded.length==0);
    }
    
    /**
     * Test that when the last encoded character (before the paddings if any) is a valid base 64 alphabet 
     * but not a possible value, a {@link DecodingException} is thrown.
     * 
     * @throws DecodingException on decoding failure.
     */
    @Test(expectedExceptions = DecodingException.class) public void testDecodeInvalidTrailingBitsInput() 
            throws DecodingException {
        Base64Support.decode(INVALID_BASE64_TRAILING);
        
    }
    
    /** 
     * Test Base64 encoding content. 
     * 
     * @throws EncodingException thrown if failure to base64 encode
     */
    @Test public void testEncodeURLSafe() throws EncodingException {
        Assert.assertEquals(Base64Support.encodeURLSafe(PLAIN_TEXT.getBytes()), URLSAFE_UNCHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encodeURLSafe(PLAIN_BYTES), URLSAFE_UNCHUNCKED_ENCODED_BYTES);
    }

    /** Test Base64 decoding content. 
     *  
     * @throws DecodingException on decoding failure.
     */
    @Test public void testDecodeURLSafe() throws DecodingException {
        Assert.assertEquals(new String(Base64Support.decodeURLSafe(URLSAFE_UNCHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(Base64Support.decodeURLSafe(URLSAFE_UNCHUNCKED_ENCODED_BYTES), PLAIN_BYTES);
    }
}