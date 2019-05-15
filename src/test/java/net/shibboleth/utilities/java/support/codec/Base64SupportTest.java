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
    

    @BeforeClass
    public void setUp() throws DecoderException {
        PLAIN_BYTES = Hex.decodeHex("14fb9c03d97e".toCharArray());
    }
    
    /** Test Base64 encoding content. */
    @Test public void testEncode() {
        Assert.assertEquals(Base64Support.encode(PLAIN_TEXT.getBytes(), false), UNCHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encode(PLAIN_TEXT.getBytes(), true), CHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encode(PLAIN_BYTES, false), UNCHUNCKED_ENCODED_BYTES);
    }

    /** Test Base64 decoding content. */
    @Test public void testDecode() {
        Assert.assertEquals(new String(Base64Support.decode(UNCHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(new String(Base64Support.decode(CHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(Base64Support.decode(UNCHUNCKED_ENCODED_BYTES), PLAIN_BYTES);
    }
    
    /** Test Base64 encoding content. */
    @Test public void testEncodeURLSafe() {
        Assert.assertEquals(Base64Support.encodeURLSafe(PLAIN_TEXT.getBytes()), URLSAFE_UNCHUNCKED_ENCODED_TEXT);
        Assert.assertEquals(Base64Support.encodeURLSafe(PLAIN_BYTES), URLSAFE_UNCHUNCKED_ENCODED_BYTES);
    }

    /** Test Base64 decoding content. */
    @Test public void testDecodeURLSafe() {
        Assert.assertEquals(new String(Base64Support.decodeURLSafe(URLSAFE_UNCHUNCKED_ENCODED_TEXT)), PLAIN_TEXT);
        Assert.assertEquals(Base64Support.decodeURLSafe(URLSAFE_UNCHUNCKED_ENCODED_BYTES), PLAIN_BYTES);
    }
}