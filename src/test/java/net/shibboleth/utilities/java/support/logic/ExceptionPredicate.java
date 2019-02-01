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

/** A {@link Predicate} that always throws a {@link RuntimeException} of some sort. */
public class ExceptionPredicate implements Predicate<Object> {

    /** Exception that will be thrown when this function is executed. */
    private RuntimeException thrownException;

    /**
     * Constructor.
     * 
     * @param e exception that will be thrown when this function is executed
     */
    public ExceptionPredicate(@Nonnull final RuntimeException e) {
        thrownException = Constraint.isNotNull(e, "Exception can not be null");
    }

    /** {@inheritDoc} */
    public boolean test(Object arg0) {
        throw thrownException;
    }
}