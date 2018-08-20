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

package net.shibboleth.utilities.java.support.service;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.primitive.TimerSupport;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link ReloadableService}. This base class will use a background thread that will perform a periodic
 * check, via {@link #shouldReload()}, and, if required, invoke the services {@link #reload()} method. <br/>
 * 
 * This class does <em>not</em> deal with any synchronization. That is left to implementing classes.
 * 
 * @param <T> The sort of service this implements.
 */
public abstract class AbstractReloadableService<T> extends AbstractIdentifiableInitializableComponent implements
        ReloadableService<T>, UnmodifiableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractReloadableService.class);

    /** Number of milliseconds between one reload check and another. */
    @Duration private long reloadCheckDelay;

    /** Timer used to schedule configuration reload tasks. */
    @Nullable private Timer reloadTaskTimer;

    /** Timer used to schedule reload tasks if no external one set. */
    @Nullable private Timer internalTaskTimer;

    /** Watcher that monitors the set of configuration resources for this service for changes. */
    @Nullable private ServiceReloadTask reloadTask;

    /** The last time time the service was reloaded, whether successful or not. */
    @Nullable private DateTime lastReloadInstant;

    /** The last time the service was reloaded successfully. */
    @Nullable private DateTime lastSuccessfulReleaseInstant;

    /** The cause of the last reload failure, if the last reload failed. */
    @Nullable private Throwable reloadFailureCause;

    /** Do we fail immediately if the config is bogus? */
    private boolean failFast;

    /** The log prefix. */
    @Nullable private String logPrefix;

    /** Constructor. */
    public AbstractReloadableService() {
        reloadCheckDelay = 0;
    }

    /**
     * Gets the number of milliseconds between one reload check and another. A value of 0 or less indicates that no
     * reloading will be performed.
     * 
     * <p>
     * Default value: 0
     * </p>
     * 
     * @return number of milliseconds between one reload check and another
     */
    @Duration public long getReloadCheckDelay() {
        return reloadCheckDelay;
    }

    /**
     * Sets the number of milliseconds between one reload check and another. A value of 0 or less indicates that no
     * reloading will be performed.
     * 
     * This setting can not be changed after the service has been initialized.
     * 
     * @param delay number of milliseconds between one reload check and another
     */
    @Duration public void setReloadCheckDelay(@Duration final long delay) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        reloadCheckDelay = delay;
    }

    /**
     * Gets the timer used to schedule configuration reload tasks.
     * 
     * @return timer used to schedule configuration reload tasks
     */
    @Nullable public Timer getReloadTaskTimer() {
        return reloadTaskTimer;
    }

    /**
     * Sets the timer used to schedule configuration reload tasks.
     * 
     * This setting can not be changed after the service has been initialized.
     * 
     * @param timer timer used to schedule configuration reload tasks
     */
    public void setReloadTaskTimer(@Nullable final Timer timer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        reloadTaskTimer = timer;
    }

    /** {@inheritDoc} */
    @Override @Nullable public DateTime getLastReloadAttemptInstant() {
        return lastReloadInstant;
    }

    /** {@inheritDoc} */
    @Override @Nullable public DateTime getLastSuccessfulReloadInstant() {
        return lastSuccessfulReleaseInstant;
    }

    /** {@inheritDoc} */
    @Override @Nullable public Throwable getReloadFailureCause() {
        return reloadFailureCause;
    }

    /**
     * Do we fail fast?
     * 
     * @return whether we fail fast.
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * Sets whether we fail fast.
     * 
     * @param value what to set.
     */
    public void setFailFast(final boolean value) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        failFast = value;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        log.info("{} Performing initial load", getLogPrefix());
        try {
            lastReloadInstant = new DateTime(ISOChronology.getInstanceUTC());
            doReload();
            lastSuccessfulReleaseInstant = lastReloadInstant;
        } catch (final ServiceException e) {
            if (isFailFast()) {
                throw new ComponentInitializationException(getLogPrefix() + " could not perform initial load", e);
            }
            log.error("{} Initial load failed", getLogPrefix(), e);
            if (reloadCheckDelay > 0) {
                log.info("{} Continuing to poll configuration", getLogPrefix());
            } else {
                log.error("{} No further attempts will be made to reload", getLogPrefix());
            }
        } catch (final Exception e) {
            throw new ComponentInitializationException(getLogPrefix() + " Unexpected error during initial load", e);
        }

        if (reloadCheckDelay > 0) {
            if (null == reloadTaskTimer) {
                log.debug("{} No reload task timer specified, creating default", getLogPrefix());
                internalTaskTimer = new Timer(TimerSupport.getTimerName(this), true);
            } else {
                internalTaskTimer = reloadTaskTimer;
            }
            log.info("{} Reload time set to: {}, starting refresh thread", getLogPrefix(), reloadCheckDelay);
            reloadTask = new ServiceReloadTask();
            internalTaskTimer.schedule(reloadTask, reloadCheckDelay, reloadCheckDelay);
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        log.info("{} Starting shutdown", getLogPrefix());
        if (reloadTask != null) {
            reloadTask.cancel();
            reloadTask = null;
        }
        if (reloadTaskTimer == null && internalTaskTimer != null) {
            internalTaskTimer.cancel();
        }
        internalTaskTimer = null;
        log.info("{} Completing shutdown", getLogPrefix());
        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override public final void reload() {

        final DateTime now = new DateTime(ISOChronology.getInstanceUTC());
        lastReloadInstant = now;

        try {
            doReload();
            lastSuccessfulReleaseInstant = now;
            reloadFailureCause = null;
        } catch (final ServiceException e) {
            log.error("{} Reload for {} failed", getLogPrefix(), getId(), e);
            reloadFailureCause = e;
            throw e;
        }
    }

    /**
     * Called by the {@link ServiceReloadTask} to determine if the service should be reloaded.
     * 
     * <p>
     * No lock is held when this method is called, so any locking needed should be handled internally.
     * </p>
     * 
     * @return true iff the service should be reloaded
     */
    protected abstract boolean shouldReload();

    /**
     * Performs the actual reload.
     * 
     * <p>
     * No lock is held when this method is called, so any locking needed should be handled internally.
     * </p>
     * 
     * @throws ServiceException thrown if there is a problem reloading the service
     */
    protected void doReload() {
        log.info("{} Reloading service configuration", getLogPrefix());
    }

    /**
     * Return a string which is to be prepended to all log messages.
     * 
     * @return "Service '<definitionID>' :"
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronized clearing of per class cache.
        String prefix = logPrefix;
        if (null == prefix) {
            if (getId() != null) {
                final StringBuilder builder = new StringBuilder("Service '").append(getId()).append("':");
                prefix = builder.toString();
                if (null == logPrefix) {
                    logPrefix = prefix;
                }
            } else {
                prefix = "Service:";
            }
        }
        return prefix;
    }

    /**
     * A watcher that determines if a service should be reloaded and does so as appropriate.
     */
    protected class ServiceReloadTask extends TimerTask {

        /** {@inheritDoc} */
        @Override public void run() {

            if (shouldReload()) {
                try {
                    reload();
                } catch (final ServiceException se) {
                    log.debug("{} Previously logged error during reload", getLogPrefix(), se);
                } catch (final Throwable t) {
                    log.error("{} Unexpected error during reload", getLogPrefix(), t);
                }
            }
        }
    }

}