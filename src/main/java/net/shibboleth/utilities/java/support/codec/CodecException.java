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

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** A general checked {@link Exception} thrown by the failure of a Codec. */
@ThreadSafe
public class CodecException extends Exception{
    
    /** Serialization ID. */
    private static final long serialVersionUID = -5363065537464226216L;

    /** Constructor. */
    public CodecException() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public CodecException(@Nullable final String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause the cause of the exception
     */
    public CodecException(@Nullable final Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param cause the cause of the exceptio
     */
    public CodecException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }


}
