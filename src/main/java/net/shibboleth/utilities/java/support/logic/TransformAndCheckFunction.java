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

import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

/**
 * A {@link Function} that receives an input, runs it through a pre-processor and checks the result against a
 * constraint. If the constraint matches that value is returned, wrapped in an {@link Optional}. If the constraint is
 * not met and {@link #failOnConstraintViolation} is false then {@link Optional#absent()} is returned. If the constraint
 * is not met and {@link #failOnConstraintViolation} is true then an {@link IllegalArgumentException} is thrown.
 * 
 * @param <T> type of input accepted by this function
 */
@ThreadSafe
public class TransformAndCheckFunction<T> implements Function<T, Optional<? extends T>> {

    /** A function applied to input prior to being constraint checked and accepted. */
    private final Function<T, ? extends T> preprocessor;

    /** A constraint which must be met in order for an input to be valid. */
    private final java.util.function.Predicate<T> constraint;

    /** Whether input that does not meet the constraint should cause an error or just be silently dropped. */
    private final boolean failOnConstraintViolation;

    /**
     * Constructor.
     * 
     * @param inputPreprocessor function applied to input prior to being constraint checked and accepted
     * @param inputConstraint constraint which must be met in order for an input to be valid
     * @param failOnInputConstraintViolation whether input that does not meet the constraint should cause an error or
     *            just be ignored
     */
    public TransformAndCheckFunction(@Nonnull final Function<T, ? extends T> inputPreprocessor,
            @Nonnull final java.util.function.Predicate<T> inputConstraint,
            final boolean failOnInputConstraintViolation) {
        preprocessor = Constraint.isNotNull(inputPreprocessor, "Input preprocessor can not be null");
        constraint = Constraint.isNotNull(inputConstraint, "Input constraint can not be null");
        failOnConstraintViolation = failOnInputConstraintViolation;
    }

    /** {@inheritDoc} */
    public Optional<? extends T> apply(final T input) {
        final T processedValue = preprocessor.apply(input);

        final boolean meetsCriteria = constraint.test(processedValue);

        if (meetsCriteria) {
            return Optional.of(processedValue);
        }

        if (failOnConstraintViolation) {
            throw new IllegalArgumentException(input + " does not meet constraint");
        }
        
        return Optional.absent();
    }

    /** {@inheritDoc} */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof TransformAndCheckFunction) {
            final TransformAndCheckFunction<?> other = (TransformAndCheckFunction<?>) obj;
            return java.util.Objects.equals(preprocessor, other.preprocessor)
                    && java.util.Objects.equals(constraint, other.constraint)
                    && java.util.Objects.equals(failOnConstraintViolation, other.failOnConstraintViolation);
        }

        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return Objects.hash(preprocessor, constraint, failOnConstraintViolation);
    }

    /** {@inheritDoc} */
    public String toString() {
        return MoreObjects.toStringHelper(this).add("preprocessor", preprocessor).add("constraint", constraint)
                .add("failOnConstraintViolation", failOnConstraintViolation).toString();
    }
}