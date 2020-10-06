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

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base32;

/**
 * Helper class for working with {@link Base32}.
 * 
 * <p>
 * This helper class specifically addresses that waste of the Apache Codec encode/decode static methods creating new
 * instances of the {@link Base32} for every operation. It also provides the helper method to produce both chunked and
 * unchunked encoded content as strings.
 * </p>
 */
public final class Base32Support {

    /** Chunk the encoded data into 76-character lines broken by CRLF characters. */
    public static final boolean CHUNKED = true;

    /** Do not chunk encoded data. */
    public static final boolean UNCHUNKED = false;

    /** Encoder used to produce chunked output. */
    @Nonnull private static final Base32 CHUNKED_ENCODER = new Base32(76, new byte[] { '\n' },
            false, (byte)'=', CodecPolicy.STRICT);

    /** Encoder used to produce unchunked output. */
    @Nonnull private static final Base32 UNCHUNKED_ENCODER = new Base32(0, new byte[] { '\n' },
            false, (byte)'=', CodecPolicy.STRICT);

    /** Constructor. */
    private Base32Support() {

    }

    /**
     * Base32 encodes the given binary data.
     * 
     * @param data data to encode
     * @param chunked whether the encoded data should be chunked or not
     * 
     * @return the base32 encoded data
     * @throws EncodingException when any {@link Exception} is thrown from the underlying encoder, 
     *                                  or the output is null.
     */
    @Nonnull public static String encode(@Nonnull final byte[] data, final boolean chunked) throws EncodingException {
        Constraint.isNotNull(data, "Binary data to be encoded can not be null");
        
        try {
            String encoded = null;
            if (chunked) {
                encoded = StringSupport.trim(CHUNKED_ENCODER.encodeToString(data));
            } else {
                encoded = StringSupport.trim(UNCHUNKED_ENCODER.encodeToString(data));
            }
            //TODO: can this ever be null, do we need to check for null?
            if (null == encoded) {
                throw new EncodingException("Base32 encoded string was null");
            }        
            return encoded;
        } catch (final Exception e) {
            //wrap any exception on invalid input with our own.
            throw new EncodingException("Unable to base32 encode data: "+e.getMessage(),e);
        }
    }

    /**
     * Decodes (un)chunked Base32 encoded data.
     * 
     * @param data Base32 encoded data
     * 
     * @return the decoded data
     * 
     * @throws DecodingException when an {@link Exception} is thrown from
     *                              the underlying decoder, or the output is null.
     */
    @Nonnull public static byte[] decode(@Nonnull final String data) throws DecodingException {
        Constraint.isNotNull(data, "Base32 encoded data can not be null");
        try {
            final byte[] decoded = CHUNKED_ENCODER.decode(data);
            //TODO: can this ever be null, do we need to check for null?
            if (null == decoded) {
                throw new DecodingException("Base32 decoded data was null");
            }          
            return decoded;
        } catch (final Exception e) {
          //wrap any exception on invalid input with our own.
            throw new DecodingException("Unable to Base32 decode input data, input invalid",e);
        }
    }
}