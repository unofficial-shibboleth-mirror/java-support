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

package net.shibboleth.utilities.java.support.httpclient;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.TimerSupport;

import org.apache.http.conn.HttpClientConnectionManager;

/** A utility that periodically closes idle connections held by an {@link HttpClientConnectionManager}. */
public class IdleConnectionSweeper implements DestructableComponent {

    /** Whether this sweeper has been destroyed. */
    private boolean destroyed;

    /**
     * Whether this sweeper created a {@link TimerTask} to use and thus should destroy when {@link #destroy()} is
     * invoked.
     */
    private boolean createdTimer;

    /** HttpClientConnectionManager whose connections will be swept. */
    private final HttpClientConnectionManager connectionManager;

    /** Timer used to schedule and execute the sweeping task. */
    private final Timer taskTimer;

    /** Sweeping task executed by the timer. */
    private final TimerTask sweeper;
    
    /** Time at which the sweeper last executed. */
    @Nullable private long executionTime;

    /**
     * Constructor. This method will create a daemon {@link Timer} and use it to periodically sweep connections.
     * 
     * @param manager HTTP client connection manager whose connections will be swept
     * @param idleTimeout length of time, in milliseconds, connection may be idle before being closed down
     * @param sweepInterval length of time, in milliseconds, between sweeps
     */
    public IdleConnectionSweeper(@Nonnull final HttpClientConnectionManager manager, final long idleTimeout,
            final long sweepInterval) {
        this(manager, idleTimeout, sweepInterval,
                new Timer(TimerSupport.getTimerName(IdleConnectionSweeper.class.getName(), null), true));
        createdTimer = true;
    }

    /**
     * Constructor.
     * 
     * @param manager HTTP client connection manager whose connections will be swept
     * @param idleTimeout length of time, in milliseconds, connection may be idle before being closed down
     * @param sweepInterval length of time, in milliseconds, between sweeps
     * @param backgroundTimer timer used to schedule the background sweeping task
     */
    public IdleConnectionSweeper(@Nonnull final HttpClientConnectionManager manager, final long idleTimeout,
            final long sweepInterval, @Nonnull final Timer backgroundTimer) {
        connectionManager = Constraint.isNotNull(manager, "HttpClientConnectionManager can not be null");
        taskTimer = Constraint.isNotNull(backgroundTimer, "Sweeper task timer can not be null");

        sweeper = new TimerTask() {
            public void run() {
                executionTime = System.currentTimeMillis();
                connectionManager.closeIdleConnections(idleTimeout, TimeUnit.MILLISECONDS);
            }
        };

        taskTimer.schedule(sweeper, sweepInterval, sweepInterval);
    }

    /**
     * Gets the time, in milliseconds since the epoch, when the sweeper last executed or, if it has not yet executed,
     * when it was first scheduled to run.
     * 
     * @return the time when the sweeper last executed or when it was first scheduled to run
     */
    public long scheduledExecutionTime() {
        if (isDestroyed()) {
            throw new DestroyedComponentException();
        }
        if (executionTime != 0) {
            return executionTime;
        }

        return sweeper.scheduledExecutionTime();
    }

    /** {@inheritDoc} */
    public boolean isDestroyed() {
        return destroyed;
    }

    /** {@inheritDoc} */
    public synchronized void destroy() {
        sweeper.cancel();

        if (createdTimer) {
            taskTimer.cancel();
        }

        destroyed = true;
    }
}