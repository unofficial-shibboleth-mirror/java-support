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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

/** A {@link Function} that applies {@link StringSupport#trimOrNull(String)} to a given input string. */
@ThreadSafe
public class TrimOrNullStringFunction implements Function<String, String> {

    /** A singleton instance of this function. */
    @Nonnull @NotEmpty public static final TrimOrNullStringFunction INSTANCE = new TrimOrNullStringFunction();

    /** {@inheritDoc} */
    @Nullable @NotEmpty public String apply(@Nullable final String input) {
        return StringSupport.trimOrNull(input);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        return obj instanceof TrimOrNullStringFunction;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 31;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}