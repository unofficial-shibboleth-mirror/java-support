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

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.TimerSupport;

/**
 * Base class for {@link ReloadableService}. This base class will use a background thread that will perform a periodic
 * check, via {@link #shouldReload()}, and, if required, invoke the service's {@link #reload()} method.
 * 
 * <p>This class does <em>not</em> deal with any synchronization; that is left to implementing classes.</p>
 * 
 * @param <T> The sort of service this implements.
 */
public abstract class AbstractReloadableService<T> extends AbstractIdentifiableInitializableComponent implements
        ReloadableService<T>, UnmodifiableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractReloadableService.class);

    /** Time between one reload check and another. */
    @Nonnull private Duration reloadCheckDelay;

    /** Timer used to schedule configuration reload tasks. */
    @Nullable private Timer reloadTaskTimer;

    /** Timer used to schedule reload tasks if no external one set. */
    @Nullable private Timer internalTaskTimer;

    /** Watcher that monitors the set of configuration resources for this service for changes. */
    @Nullable private ServiceReloadTask reloadTask;

    /** The last time the service was reloaded, whether successful or not. */
    @Nullable private Instant lastReloadInstant;

    /** The last time the service was reloaded successfully. */
    @Nullable private Instant lastSuccessfulReleaseInstant;

    /** The cause of the last reload failure, if the last reload failed. */
    @Nullable private Throwable reloadFailureCause;

    /** Do we fail immediately if the config is bogus? */
    private boolean failFast;

    /** The log prefix. */
    @Nullable private String logPrefix;

    /** Constructor. */
    public AbstractReloadableService() {
        reloadCheckDelay = Duration.ZERO;
    }

    /**
     * Gets the time between one reload check and another. A value of 0 or less indicates that no
     * reloading will be performed.
     * 
     * <p>
     * Default value: 0
     * </p>
     * 
     * @return time between one reload check and another
     */
    @Nonnull public Duration getReloadCheckDelay() {
        return reloadCheckDelay;
    }

    /**
     * Sets the time between one reload check and another. A value of 0 or less indicates that no
     * reloading will be performed.
     * 
     * <p>This setting cannot be changed after the service has been initialized.</p>
     * 
     * @param delay between one reload check and another
     */
    public void setReloadCheckDelay(@Nonnull final Duration delay) {
        checkSetterPreconditions();

        reloadCheckDelay = Constraint.isNotNull(delay, "Delay cannot be null");
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
        checkSetterPreconditions();

        reloadTaskTimer = timer;
    }

    /** {@inheritDoc} */
    @Nullable public Instant getLastReloadAttemptInstant() {
        return lastReloadInstant;
    }

    /** {@inheritDoc} */
    @Nullable public Instant getLastSuccessfulReloadInstant() {
        return lastSuccessfulReleaseInstant;
    }

    /** {@inheritDoc} */
    @Nullable public Throwable getReloadFailureCause() {
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
        checkSetterPreconditions();
        failFast = value;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        log.info("{} Performing initial load", getLogPrefix());
        try {
            lastReloadInstant = Instant.now();
            doReload();
            lastSuccessfulReleaseInstant = lastReloadInstant;
        } catch (final ServiceException e) {
            if (isFailFast()) {
                throw new ComponentInitializationException(getLogPrefix() + " could not perform initial load", e);
            }
            log.error("{} Initial load failed", getLogPrefix(), e);
            
            if (reloadCheckDelay.isNegative() || reloadCheckDelay.isZero()) {
                log.error("{} No further attempts will be made to reload", getLogPrefix());
            } else {
                log.info("{} Continuing to poll configuration", getLogPrefix());
            }
        } catch (final Exception e) {
            throw new ComponentInitializationException(getLogPrefix() + " Unexpected error during initial load", e);
        }

        if (!(reloadCheckDelay.isNegative() || reloadCheckDelay.isZero())) {
            if (null == reloadTaskTimer) {
                log.debug("{} No reload task timer specified, creating default", getLogPrefix());
                internalTaskTimer = new Timer(TimerSupport.getTimerName(this), true);
            } else {
                internalTaskTimer = reloadTaskTimer;
            }
            log.info("{} Reload interval set to: {}, starting refresh thread", getLogPrefix(), reloadCheckDelay);
            reloadTask = new ServiceReloadTask();
            internalTaskTimer.schedule(reloadTask, reloadCheckDelay.toMillis(), reloadCheckDelay.toMillis());
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

        final Instant now = Instant.now();
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
     * @return "Service '&lt;definitionID&gt;' :"
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