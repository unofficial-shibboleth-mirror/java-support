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

package net.shibboleth.utilities.java.support.net.impl;

import java.net.MalformedURLException;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.net.SimpleURLCanonicalizer;
import net.shibboleth.utilities.java.support.net.URIComparator;
import net.shibboleth.utilities.java.support.net.URIException;


/**
 * A basic implementation of {@link URIComparator} that compares
 * URL's by canonicalizing them as per {@link SimpleURLCanonicalizer},
 * and then compares the resulting string representations for equality 
 * using {@link Object#equals}. If {@link #isCaseInsensitive()} is true,
 * then the equality test is instead performed using {@link String#equalsIgnoreCase(String)}.
 */
public class BasicURLComparator implements URIComparator {
    
    /** The case-insensitivity flag. */
    private boolean caseInsensitive;

    /**
     * Get the case-insensitivity flag value.
     * @return Returns the caseInsensitive.
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * Set the case-insensitivity flag value.
     * @param flag The caseInsensitive to set.
     */
    public void setCaseInsensitive(final boolean flag) {
        caseInsensitive = flag;
    }

    /** {@inheritDoc}.*/
    public boolean compare(@Nullable final String uri1, @Nullable final String uri2) throws URIException {
        if (uri1 == null) {
            return uri2 == null;
        } else if (uri2 == null) {
            return uri1 == null;
        } else {
            String uri1Canon = null;
            
            try {
                uri1Canon = SimpleURLCanonicalizer.canonicalize(uri1);
            } catch (final MalformedURLException e) {
                throw new URIException("URI was invalid: " + uri1Canon);
            }
            
            String uri2Canon = null;
            try {
                uri2Canon = SimpleURLCanonicalizer.canonicalize(uri2);
            } catch (final MalformedURLException e) {
                throw new URIException("URI was invalid: " + uri2Canon);
            }
            
            if (isCaseInsensitive()) {
                return uri1Canon.equalsIgnoreCase(uri2Canon);
            }
            return uri1Canon.equals(uri2Canon);
        }
    }

}