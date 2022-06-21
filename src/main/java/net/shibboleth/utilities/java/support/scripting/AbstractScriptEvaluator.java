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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that evaluates an {@link EvaluableScript} against a set of inputs
 * and returns the result.
 * 
 * @since 7.4.0
 */
public abstract class AbstractScriptEvaluator extends AbstractInitializableComponent {

    /** The default language is Javascript. */
    @Nonnull @NotEmpty public static final String DEFAULT_ENGINE = "JavaScript";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractScriptEvaluator.class);

    /** The script we care about. */
    @Nonnull private final EvaluableScript script;
    
    /** Extension objects to apply. */
    @Nonnull private Collection<ScriptContextExtender> contextExtenders;

    /** Debugging info. */
    @Nullable private String logPrefix;

    /** The output type to validate. */
    @Nullable private Class<?> outputType;
    
    /** A custom object to inject into the script. */
    @Nullable private Object customObject;
    
    /** Whether to raise runtime exceptions if a script fails. */
    private boolean hideExceptions;
    
    /** Value to return from script if an error occurs. */
    @Nullable private Object returnOnError;

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     */
    public AbstractScriptEvaluator(@Nonnull @ParameterName(name="theScript") final EvaluableScript theScript) {
        script = Constraint.isNotNull(theScript, "Supplied script cannot be null");
        contextExtenders = Collections.emptyList();
    }

    /**
     * Get log prefix for debugging.
     * 
     * @return log prefix
     */
    @Nullable protected String getLogPrefix() {
        return logPrefix;
    }
    
    /**
     * Set log prefix for debugging.
     * 
     * @param prefix log prefix
     */
    public void setLogPrefix(@Nullable final String prefix) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        logPrefix = prefix;
    }
    
    /**
     * Get the output type to be enforced.
     * 
     * @return output type
     */
    @Nullable protected Class<?> getOutputType() {
        return outputType;
    }
    
    /**
     * Set the output type to be enforced.
     * 
     * @param type output type
     */
    protected void setOutputType(@Nullable final Class<?> type) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        outputType = type;
    }
    
    /**
     * Return the custom (externally provided) object.
     * 
     * @return the custom object
     */
    @Nullable protected Object getCustomObject() {
        return customObject;
    }

    /**
     * Set the custom (externally provided) object.
     * 
     * @param object the custom object
     */
    public void setCustomObject(@Nullable final Object object) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        customObject = object;
    }

    /**
     * Get whether to hide exceptions in script execution.
     * 
     * @return whether to hide exceptions in script execution
     */
    protected boolean getHideExceptions() {
        return hideExceptions;
    }
    
    /**
     * Set whether to hide exceptions in script execution (default is false).
     * 
     * @param flag flag to set
     */
    public void setHideExceptions(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        hideExceptions = flag;
    }

    /**
     * Get value to return if an error occurs.
     * 
     * @return value to return
     */
    @Nullable protected Object getReturnOnError() {
        return returnOnError;
    }
    
    /**
     * Set value to return if an error occurs.
     * 
     * @param value value to return
     */
    protected void setReturnOnError(@Nullable final Object value) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        returnOnError = value;
    }
    
    /**
     * Set {@link ScriptContextExtender} instances to apply when populating script context.
     * 
     * @param extenders extenders to apply
     * 
     * @since 9.0.0
     */
    public void setContextExtenders(@Nullable @NonnullElements final Collection<ScriptContextExtender> extenders) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if (extenders != null) {
            contextExtenders = List.copyOf(extenders);
        } else {
            contextExtenders = Collections.emptyList();
        }
    }

    /**
     * Evaluate the script.
     * 
     * @param input input parameters
     * 
     * @return script result
     */
    @Nullable protected Object evaluate(@Nullable final Object... input) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);
        
        prepareContext(scriptContext, input);
        
        contextExtenders.forEach(x -> x.extendContext(getClass(), scriptContext));

        try {
            final Object result = script.eval(scriptContext);

            if (null != getOutputType() && null != result && !getOutputType().isInstance(result)) {
                log.error("{} Output of type {} was not of type {}", getLogPrefix(), result.getClass(),
                        getOutputType());
                return getReturnOnError();
            }
            
            return finalizeContext(scriptContext, result);
            
        } catch (final ScriptException e) {
            if (getHideExceptions()) {
                log.warn("{} Suppressing exception thrown by script", getLogPrefix(), e);
                return getReturnOnError();
            }
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Pre-process the script context before execution.
     * 
     * @param scriptContext the script context
     * @param input the input
     */
    protected abstract void prepareContext(@Nonnull final ScriptContext scriptContext, @Nullable final Object... input);

    /**
     * Complete processing by evaluating the result of the script and returning the final result to the caller.
     * 
     *  <p>The default implementation just returns the result.</p> 
     * 
     * @param scriptContext the context after execution
     * @param scriptResult the result of script execution
     * 
     * @return the final result to return, or null
     * @throws ScriptException to signal a failure after script execution
     */
    @Nullable protected Object finalizeContext(@Nonnull final ScriptContext scriptContext,
            @Nullable final Object scriptResult) throws ScriptException {
        return scriptResult;
    }
}