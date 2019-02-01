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

package net.shibboleth.utilities.java.support.logic;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * A {@link Predicate} that checks that a given input string matches a regular expression. If a given
 * input is <code>null</code> this predicate returns <code>false</code>.
 * 
 * @since 7.4.0
 */
public class RegexPredicate implements Predicate<CharSequence> {

    /** Regular expression. */
    @Nonnull  private Pattern pattern;

    /**
     * Pattern constructor.
     * 
     * @param p the pattern to match the input against
     */
    public RegexPredicate(@Nonnull final Pattern p) {
        pattern = p;
    }

    /**
     * String constructor.
     * 
     * @param s the pattern to match the input against
     */
    public RegexPredicate(@Nonnull @NotEmpty final String s) {
        pattern = Pattern.compile(s);
    }

    /** {@inheritDoc} */
    public boolean test(final CharSequence input) {
        if (input == null) {
            return false;
        }

        return pattern.matcher(input).matches();
    }
    
}