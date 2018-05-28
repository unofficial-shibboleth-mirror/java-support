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

package net.shibboleth.utilities.java.support.security;

import java.security.SecureRandom;

import javax.annotation.Nonnull;

import org.apache.commons.codec.BinaryEncoder;

import net.shibboleth.utilities.java.support.annotation.ParameterName;

/**
 * A specialized subclass of {@link RandomIdentifierGenerationStrategy} which constrains the supplied
 * random number generator to be an instance of {@link SecureRandom}.
 */
public class SecureRandomIdentifierGenerationStrategy extends RandomIdentifierGenerationStrategy {

    /**
     * Constructor. Initializes the random number source to a new {@link SecureRandom}, size of identifier is set to 16
     * bytes, and the encoder is set to a {@link org.apache.commons.codec.binary.Hex}.
     */
    public SecureRandomIdentifierGenerationStrategy() {
        
    }

    /**
     * Constructor. Initializes the random number source to a new {@link SecureRandom} and the encoder is set to a
     * {@link org.apache.commons.codec.binary.Hex}.
     * 
     * @param identifierSize number of random bytes in identifier
     */
    public SecureRandomIdentifierGenerationStrategy(@ParameterName(name="identifierSize") final int identifierSize) {
        super(identifierSize);
    }

    /**
     * Constructor.
     * 
     * @param source source of random bytes
     * @param identifierSize number of random bytes in the identifier
     * @param identifierEncoder encoder used to convert random bytes to string identifier
     */
    public SecureRandomIdentifierGenerationStrategy(@ParameterName(name="source") @Nonnull final SecureRandom source, 
            @ParameterName(name="identifierSize") final int identifierSize,
            @ParameterName(name="identifierEncoder") @Nonnull final BinaryEncoder identifierEncoder) {
        super(source, identifierSize, identifierEncoder);
    }

}
