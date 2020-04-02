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

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

/** {@link Base32Support} unit test. */
public class Base32SupportTest {

    
    /** A plain text string to be encoded. */
    private final static String PLAIN_TEXT = "test data";
    
    /** Base32 encoded version of <code>PLAIN_TEXT</code>. */
    private final static String ENCODED_TEXT = "ORSXG5BAMRQXIYI=";
                       
    /** 
     * Invalid base32 string as it has invalid trailing digits.
     * Correctly fails with commons-codec 1.14 and greater.
     * @see <a href="https://issues.apache.org/jira/browse/CODEC-270">CODEC-270</a>
     */
    private final static String INVALID_BASE32_TRAILING="AB======";
    
    /** Invalid base32, should not produce a result.*/
    private final static String INVALID_BASE32="MC======";
    
    
    /**
     * Test that an invalid base32 input string does not return a response, instead throwing a {@link DecodingException}. 
     * 
     * @throws DecodingException when base32 decoding fails, expected.
     */
    @Test(expectedExceptions = DecodingException.class) public void testDecodeInvalidInput() throws DecodingException {
        Base32Support.decode(INVALID_BASE32);
        
    }
    
    /**
     * Test that when the last encoded character (before the paddings if any) is a valid base 32 alphabet 
     * but not a possible value an {@link DecodingException} is thrown.
     * 
     * @throws DecodingException when base32 decoding fails
     */
    @Test(expectedExceptions = DecodingException.class) public void testDecodeInvalidTrailingBitsInput() throws DecodingException {
        Base32Support.decode(INVALID_BASE32_TRAILING);
        
    }
    
    /**
     * Test a null byte array argument violates the method contract and throws a {@link ConstraintViolationException}.
     * 
     * @throws EncodingException on encoding failure. 
     */
    @Test(expectedExceptions = ConstraintViolationException.class) public void testEncodeNullInput() 
            throws EncodingException {
        Base32Support.encode(null, false);
    }
    
    /**
     * Test encoding a byte array works.
     * 
     * @throws EncodingException if there is an issue encoding the byte array, should not happen.
     */
    @Test public void testEncode() throws EncodingException {        
        Assert.assertEquals(ENCODED_TEXT,  Base32Support.encode(PLAIN_TEXT.getBytes(), false));
    }
  
}