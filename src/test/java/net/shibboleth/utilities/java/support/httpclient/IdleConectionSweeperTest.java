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

import java.time.Duration;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.component.DestroyedComponentException;

/** {@link IdleConnectionSweeper} unit test. */
public class IdleConectionSweeperTest {

    private final Duration SWEEP_INTERVAL = Duration.ofMillis(50);

    @Test public void test() throws Exception {
        MyCm  connectionManager = new MyCm();

        IdleConnectionSweeper sweeper = new IdleConnectionSweeper(connectionManager, Duration.ofMillis(30), SWEEP_INTERVAL);
        Thread.yield(); // for luck.
        if (!connectionManager.isCloseCalled()) {
            Thread.sleep(25+SWEEP_INTERVAL.toMillis());
            Thread.yield();
            if (!connectionManager.isCloseCalled()) {
                // Windows sometimes takes its time...
                Thread.sleep(25+SWEEP_INTERVAL.toMillis());
                Thread.yield();
                Assert.assertTrue(connectionManager.isCloseCalled());
            }
        }

        sweeper.destroy();
        Assert.assertTrue(sweeper.isDestroyed());

        try {
            sweeper.scheduledExecutionTime();
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }

        connectionManager = new MyCm();

        Timer timer = new Timer(true);
        sweeper = new IdleConnectionSweeper(connectionManager, Duration.ofMillis(30), SWEEP_INTERVAL, timer);
        Thread.yield();
        if (!connectionManager.isCloseCalled()) {
            Thread.sleep(SWEEP_INTERVAL.toMillis());
            Thread.yield();
            if (!connectionManager.isCloseCalled()) {
                // Windows sometimes takes its time...
                Thread.sleep(SWEEP_INTERVAL.toMillis());
                Thread.yield();
                Assert.assertTrue(connectionManager.isCloseCalled());
            }
        }

        sweeper.destroy();
        Assert.assertTrue(sweeper.isDestroyed());

        try {
            sweeper.scheduledExecutionTime();
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
        timer.cancel();
    }
    
    private class MyCm extends PoolingHttpClientConnectionManager {
        private boolean closeCalled;
        public void closeIdleConnections(long idletime, TimeUnit timeUnit) {
            closeCalled = true;
            super.closeIdleConnections(idletime, timeUnit);
        }
        public boolean isCloseCalled() {
            return closeCalled;
        }
    }
}