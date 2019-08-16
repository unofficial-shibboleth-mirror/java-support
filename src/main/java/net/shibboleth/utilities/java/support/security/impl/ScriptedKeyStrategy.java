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

package net.shibboleth.utilities.java.support.security.impl;

import java.security.KeyException;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.TimerSupport;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;
import net.shibboleth.utilities.java.support.security.DataSealerKeyStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a strategy for access to versioned symmetric keys using scripts.
 * 
 * <p>Suitable for integrating with external key services.</p>
 */
public class ScriptedKeyStrategy extends AbstractInitializableComponent implements DataSealerKeyStrategy {
    
    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(ScriptedKeyStrategy.class);

    /** Script to obtain keys. */
    @NonnullAfterInit private EvaluableScript keyScript;
    
    /** Custom object for script. */
    @Nullable private Object customObject;
    
    /** Current key alias loaded. */
    @NonnullAfterInit private String currentAlias;

    /** Current default key loaded. */
    @NonnullAfterInit private SecretKey defaultKey;
    
    /** Cache of keys. */
    @Nonnull private final LinkedHashMap<String,SecretKey> keyCache;
    
    /** Time between key update checks. Default value: (PT15M). */
    @Nonnull private Duration updateInterval;

    /** Timer used to schedule update tasks. */
    @Nullable private Timer updateTaskTimer;

    /** Timer used to schedule update tasks if no external one set. */
    @Nullable private Timer internalTaskTimer;

    /** Task that checks for updated key version. */
    @Nullable private TimerTask updateTask;
    
    /** Size of key cache to maintain. */
    @NonNegative private long cacheSize;
    
    /** Constructor. */
    public ScriptedKeyStrategy() {
        cacheSize = 30;
        keyCache = new LinkedHashMap<>((int) cacheSize);
        updateInterval = Duration.ofMinutes(15);
    }

    /**
     * Set the script to run to access keys.
     * 
     * @param script script to run
     */
    public void setKeyScript(@Nonnull final EvaluableScript script) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        keyScript = Constraint.isNotNull(script, "Script cannot be null");
    }
    

    /**
     * Set the custom (externally provided) object.
     * 
     * @param object the custom object
     */
    public void setCustomObject(@Nullable final Object object) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        customObject = object;
    }

    /**
     * Set the time between key update checks. A value of 0 indicates that no updates will be
     * performed.
     * 
     * This setting cannot be changed after the service has been initialized.
     * 
     * @param interval time between key update checks
     */
    public void setUpdateInterval(@Nonnull final Duration interval) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        Constraint.isNotNull(interval, "Interval cannot be null");
        Constraint.isFalse(interval.isNegative(), "Interval cannot be negative");

        updateInterval = interval;
    }

    /**
     * Set the timer used to schedule update tasks.
     * 
     * This setting cannot be changed after the service has been initialized.
     * 
     * @param timer timer used to schedule update tasks
     */
    public void setUpdateTaskTimer(@Nullable final Timer timer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        updateTaskTimer = timer;
    }
    
    /**
     * Set the number of keys to cache.
     * 
     * <p>Defaults to 30.</p>
     * 
     * @param size size of cache
     */
    public void setCacheSize(@NonNegative final long size) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        cacheSize = Constraint.isGreaterThanOrEqual(0, size, "Key cache size cannot be negative");
    }
    
    /** {@inheritDoc} */
    @Override
    public void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (keyScript == null) {
            throw new ComponentInitializationException("Script cannot be null");
        }
        
        try {
            updateDefaultKey();
    
        } catch (final KeyException e) {
            log.error("Error loading default key", e);
            throw new ComponentInitializationException("Exception loading the default key", e);
        }

        if (!updateInterval.isZero()) {
            updateTask = new TimerTask() {
                public void run() {
                    try {
                        updateDefaultKey();
                    } catch (final KeyException e) {
                        
                    }
                }
            };
            if (updateTaskTimer == null) {
                internalTaskTimer = new Timer(TimerSupport.getTimerName(this), true);
            } else {
                internalTaskTimer = updateTaskTimer;
            }
            internalTaskTimer.schedule(updateTask, updateInterval.toMillis(), updateInterval.toMillis());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doDestroy() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            if (updateTaskTimer == null) {
                internalTaskTimer.cancel();
            }
            internalTaskTimer = null;
        }
        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Nonnull public Pair<String,SecretKey> getDefaultKey() throws KeyException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        synchronized(this) {
            if (defaultKey != null) {
                return new Pair<>(currentAlias, defaultKey);
            }
            throw new KeyException("Default key unavailable");
        }
    }
    
    /** {@inheritDoc} */
    @Nonnull public SecretKey getKey(@Nonnull @NotEmpty final String name) throws KeyException {
        synchronized(this) {
            if (defaultKey != null && name.equals(currentAlias)) {
                return defaultKey;
            } else if (keyCache.containsKey(name)) {
                return keyCache.get(name);
            }
        }

        try {
            final SimpleScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setAttribute("custom", customObject, ScriptContext.ENGINE_SCOPE);
            scriptContext.setAttribute("name", name, ScriptContext.ENGINE_SCOPE);
            
            final Object result = keyScript.eval(scriptContext);
            
            if (result instanceof SecretKey) {
                synchronized(this) {
                    keyCache.put(name, (SecretKey) result);
                }
                log.debug("Loaded key '{}' from external script", name);
                return (SecretKey) result;
            } else if (result instanceof Pair && ((Pair<?,?>) result).getSecond() instanceof SecretKey) {
                synchronized(this) {
                    keyCache.put(name, (SecretKey) ((Pair<?,?>) result).getSecond());
                }
                log.debug("Loaded key '{}' from external script", name);
                return (SecretKey) ((Pair<?,?>) result).getSecond();
            } else {
                throw new KeyException("Script did not return SecretKey or Pair<String,SecretKey> result.");
            }
        } catch (final ScriptException e) {
            throw new KeyException(e);
        }
    }

    /**
     * Update the loaded copy of the default key based on the current key version if it's out of date
     * (loading key version from scratch if need be).
     * 
     * <p>Also purge cache to limit size.</p>
     * 
     * @throws KeyException if the key cannot be updated
     */
    private void updateDefaultKey() throws KeyException {
        
        synchronized(this) {
            int size = keyCache.size();
            if (size > cacheSize) {
                final Iterator<String> iter = keyCache.keySet().iterator();
                while (size > cacheSize) {
                    iter.next();
                    iter.remove();
                    size--;
                }
            }
        }
            
        try {
            final SimpleScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setAttribute("custom", customObject, ScriptContext.ENGINE_SCOPE);
            
            final Object result = keyScript.eval(scriptContext);
            
            if (result instanceof Pair) {
                final Pair<?,?> p = (Pair<?,?>) result;
                if (p.getFirst() instanceof String && p.getSecond() instanceof SecretKey) {
                    synchronized(this) {
                        if (currentAlias == null) {
                            log.info("Loaded initial default key: {}", p.getFirst());
                        } else if (!currentAlias.equals(p.getFirst())) {
                            log.info("Updated default key from {} to {}", currentAlias, p.getFirst());
                        } else {
                            log.debug("Default key version has not changed, still {}", currentAlias);
                            return;
                        }
                        
                        currentAlias = (String) p.getFirst();
                        defaultKey = (SecretKey) p.getSecond();
                        keyCache.put((String) p.getFirst(), (SecretKey) p.getSecond());
                    }
                } else {
                    throw new KeyException("Script did not return Pair<String,SecretKey> result.");
                }
            } else {
                throw new KeyException("Script did not return Pair<String,SecretKey> result.");
            }
        } catch (final ScriptException e) {
            throw new KeyException(e);
        }
    }
    
}