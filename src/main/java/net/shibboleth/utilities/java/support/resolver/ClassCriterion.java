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

package net.shibboleth.utilities.java.support.resolver;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A {@link Criterion} based on class type.
 * 
 * @param <T> class type
 * 
 * @since 8.0.0
 */
public class ClassCriterion<T> implements Criterion {

    /** The type to search for. */
    @Nonnull private final Class<T> classType;
    
    /**
     * Constructor.
     *
     * @param type class type of criterion
     */
    public ClassCriterion(@Nonnull @ParameterName(name="type") final Class<T> type) {
        classType = Constraint.isNotNull(type, "Type cannot be null");
    }
    
    /**
     * Get the class type of this criterion.
     * 
     * @return class type
     */
    @Nonnull public Class<T> getType() {
        return classType;
    }
    
}