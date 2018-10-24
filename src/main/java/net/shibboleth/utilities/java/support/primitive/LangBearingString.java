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

package net.shibboleth.utilities.java.support.primitive;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;

/**
 * An object that represents a string associated with a language tag/locale.
 */
public class LangBearingString extends Pair<String,String> {

    /**
     * Constructor.
     * 
     * @param value the value
     */
    public LangBearingString(@Nullable final String value) {
        super(value, null);
    }

    /**
     * Constructor.
     * 
     * @param value the value
     * @param lang the language
     */
    public LangBearingString(@Nullable final String value, @Nullable @NotEmpty final String lang) {
        super(value, StringSupport.trimOrNull(lang));
    }
    
    /**
     * Get the string value.
     * 
     * @return value
     */
    @Nullable public String getValue() {
        return getFirst();
    }
    
    /**
     * Get the language.
     * 
     * @return language
     */
    @Nullable @NotEmpty public String getLang() {
        return getSecond();
    }    

    /** {@inheritDoc} */
    @Override
    public void setSecond(@Nullable final String newSecond) {
        super.setSecond(StringSupport.trimOrNull(newSecond));
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString();
    }
    
}