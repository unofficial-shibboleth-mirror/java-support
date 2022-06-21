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

package net.shibboleth.utilities.java.support.scripting;

import javax.annotation.Nonnull;
import javax.script.ScriptContext;

/**
 * Interface implemented by components that extend the {@link ScriptContext} of various
 * components that rely on JSR-223 scripting.
 * 
 * @since 9.0.0
 */
public interface ScriptContextExtender {

    /**
     * Instructs the implementing component to decorate the input context with any additional
     * content.
     * 
     * <p>The context will already contain any default objects and typically the implementation
     * of this interface will access those objects for context while adding additional content.</p>
     * 
     * <p>Extenders should generally ensure that they do not overwrite existing context variables with
     * the same name, but may do so if the semantics are understood.</p>
     * 
     * @param scriptedClass the class of the calling component
     * @param scriptContext the context, as decorated by the calling component
     */
    void extendContext(@Nonnull final Class<?> scriptedClass, @Nonnull final ScriptContext scriptContext);

}