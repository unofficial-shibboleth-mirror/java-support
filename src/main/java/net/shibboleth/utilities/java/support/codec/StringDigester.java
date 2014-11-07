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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.Function;

/**
 * A function impl which accepts a String input, digests it according to a specified {@link MessageDigest} algorithm,
 * and then returns the output in a specified format: Base64-encoded or hexadecimal with with lower or upper 
 * case characters.
 */
public class StringDigester implements Function<String, String> {
    
    /** The output format determining how the the digested byte[] is converted to the output String. */
    public enum OutputFormat {
        /** Base64-encoding. */
        BASE64,
        /** Hexadecimal encoding, with lower case characters.*/
        HEX_LOWER,
        /** Hexadecimal encoding, with upper case characters.*/
        HEX_UPPER
    };
    
    /** The default input character set.*/
    @Nonnull public static final Charset DEFAULT_INPUT_CHARSET = Charsets.UTF_8;
    
    /** Logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StringDigester.class);
    
    /** The message digest algorithm to use. */
    @Nonnull @NotEmpty private String digestAlgorithm;
    
    /** The output format instance used to determine how the digested byte[] is converted to the output String. */
    @Nonnull private OutputFormat outputFormat;
    
    /** The Charset instance used in converting the input String to a byte[]. */
    @Nonnull private Charset inputCharset;
    
    /** Optional salt to add into the digest. */
    @Nullable private String salt;
    
    /**
     * Constructor.
     * 
     * <p>The input character set will be UTF-8.</p>
     *
     * @param algorithm the JCA digest algorithm identifier
     * @param format the output format used to convert the digested[] to the output string
     * @throws NoSuchAlgorithmException thrown if the digestAlgorithm is not invalid or unsupported
     */
    public StringDigester(@Nonnull @NotEmpty final String algorithm, @Nonnull final OutputFormat format) 
            throws NoSuchAlgorithmException {
       this(algorithm, format, DEFAULT_INPUT_CHARSET); 
    }
    
    /**
     * Constructor.
     *
     * @param algorithm the JCA digest algorithm identifier
     * @param format the output format used to convert the digested[] to the output string
     * @param charset the character set to use in converting the input string to a byte[] prior to digesting
     * @throws NoSuchAlgorithmException thrown if the digestAlgorithm is not invalid or unsupported
     */
    public StringDigester(@Nonnull @NotEmpty final String algorithm, @Nonnull final OutputFormat format, 
            @Nullable final Charset charset) throws NoSuchAlgorithmException {
        
        digestAlgorithm = Constraint.isNotNull(StringSupport.trimOrNull(algorithm), 
                "Digest algorithm was null or empty");
        
        // Test the digest algorithm upfront to make sure it's valid. If not, it will throw.
        MessageDigest.getInstance(digestAlgorithm);
        
        outputFormat = format;
        if (charset != null) {
            inputCharset = charset;
        } else {
            inputCharset = DEFAULT_INPUT_CHARSET;
        }
        
    }
    
    /**
     * Set a salt to add to the digest input for obfuscation.
     * 
     * @param s salt value
     */
    public void setSalt(@Nullable final String s) {
        salt = s;
    }

    /** {@inheritDoc} */
    @Nullable public String apply(@Nullable final String input) {
        String trimmed = StringSupport.trimOrNull(input);
        if (trimmed == null) {
            log.debug("Trimmed input was null, returning null");
            return null;
        }
        
        if (salt != null) {
            trimmed = salt + trimmed;
        }
        
        log.debug("Digesting input '{}' as charset '{}' with digest algorithm '{}' and output format '{}'", 
                trimmed, inputCharset.displayName(), digestAlgorithm, outputFormat);
        
        final byte[] inputBytes = trimmed.getBytes(inputCharset);
        
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen, b/c we tested it earlier in the ctor, so just log and return null.
            log.error("Digest algorithm '{}' was invalid", digestAlgorithm, e);
            return null;
        }
        
        final byte[] digestedBytes = digest.digest(inputBytes);
        
        if (digestedBytes == null) {
            log.debug("Digested output was null, returning null");
            return null;
        }
        
        String output = null;
        
        switch(outputFormat) {
            case BASE64:
                output = Base64Support.encode(digestedBytes, false);
                break;
            case HEX_LOWER:
                output = new String(Hex.encodeHex(digestedBytes, true));
                break;
            case HEX_UPPER:
                output = new String(Hex.encodeHex(digestedBytes, false));
                break;
            default:
                break;
        }
        
        log.debug("Produced digested and formatted output '{}'", output);
        
        return output;
    }

}