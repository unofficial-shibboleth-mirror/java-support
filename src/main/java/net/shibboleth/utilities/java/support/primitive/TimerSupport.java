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

import java.util.Timer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

/** Timer utility methods. */
public final class TimerSupport {
    
    /** Constructor. */
    private TimerSupport() { }
    
    /**
     * Produce the name which should be used for a {@link Timer} owned by the specified object.
     * 
     * <p>The base name will constructed as follows:
     * <ul>
     *  <li>if target is instance of {@link IdentifiedComponent},
     *  use {@link IdentifiedComponent#getId()} if non-empty</li>
     *  <li>use {@link #toString()} if non-empty</li>
     *  <li>use serialized class name</li>
     * </ul>
     * </p>
     * 
     * @param obj the target object instance to evaluate
     * @return an appropriate name for a Timer owned by the specified object
     */
    @Nonnull @NotEmpty public static String getTimerName(@Nonnull final Object obj) {
        return getTimerName(obj, null);
    }
    
    /**
     * Produce the name which should be used for a {@link Timer} owned by the specified object.
     * 
     * <p>The base name will constructed as follows:
     * <ul>
     *  <li>if target is instance of {@link IdentifiedComponent},
     *  use {@link IdentifiedComponent#getId()} if non-empty</li>
     *  <li>use {@link #toString()} if non-empty</li>
     *  <li>use serialized class name</li>
     * </ul>
     * </p>
     * 
     * @param obj the target object instance to evaluate
     * @param additionalData additional qualifying data to include in the name
     * @return an appropriate name for a Timer owned by the specified object
     */
    @Nonnull @NotEmpty public static String getTimerName(@Nonnull final Object obj,
            @Nullable final String additionalData) {
        Constraint.isNotNull(obj, "Target object for Timer was null");
        
        String baseName = null;
        if (obj instanceof IdentifiedComponent
                && StringSupport.trimOrNull(((IdentifiedComponent)obj).getId()) != null) {
            baseName = StringSupport.trimOrNull(((IdentifiedComponent)obj).getId());
        } else if (StringSupport.trimOrNull(obj.toString()) != null){
            baseName = StringSupport.trimOrNull(obj.toString());
        } else {
            baseName = obj.getClass().getName();
        }
        
        return uncheckedGetTimerName(Constraint.isNotNull(baseName, "Base name for Timer was null"), additionalData);
    }
        
    /**
     * Produce the name for a {@link Timer} based on the specified base name.
     * 
     * @param baseName the base name of Timer
     * @param additionalData additional qualifying data to include in the name
     * @return an appropriate name for a Timer based on the specified base name
     */
    @Nonnull @NotEmpty public static String getTimerName(@Nonnull final String baseName,
            @Nullable final String additionalData) {
        Constraint.isNotNull(baseName, "Base name for Timer was null");
        return uncheckedGetTimerName(baseName, additionalData);
    }

    /**
     * Unchecked version of {@link #getTimerName(String, String)} that assumes base name is non-null.
     * 
     * @param baseName the base name of Timer
     * @param additionalData additional qualifying data to include in the name
     * @return an appropriate name for a Timer based on the specified base name
     */
    @Nonnull @NotEmpty private static String uncheckedGetTimerName(@Nonnull final String baseName,
            @Nullable final String additionalData) {
        
        if (additionalData != null) {
            return String.format("Timer for %s (%s)", baseName, additionalData);
        } else {
            return String.format("Timer for %s", baseName);
        }
    }
}
