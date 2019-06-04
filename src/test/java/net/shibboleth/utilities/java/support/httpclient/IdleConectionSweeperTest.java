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
import java.time.Instant;
import java.util.Timer;

import net.shibboleth.utilities.java.support.component.DestroyedComponentException;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link IdleConnectionSweeper} unit test. */
public class IdleConectionSweeperTest {

    private final Duration SWEEP_INTERVAL = Duration.ofMillis(50);

    @Test public void test() throws Exception {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        IdleConnectionSweeper sweeper = new IdleConnectionSweeper(connectionManager, Duration.ofMillis(30), SWEEP_INTERVAL);
        Thread.sleep(75);
        Assert.assertTrue(sweeper.scheduledExecutionTime().plus(SWEEP_INTERVAL).isAfter(Instant.now()));

        sweeper.destroy();
        Assert.assertTrue(sweeper.isDestroyed());

        try {
            sweeper.scheduledExecutionTime();
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }

        Timer timer = new Timer(true);
        sweeper = new IdleConnectionSweeper(connectionManager, Duration.ofMillis(30), SWEEP_INTERVAL, timer);
        Thread.sleep(75);
        Assert.assertTrue(sweeper.scheduledExecutionTime().plus(SWEEP_INTERVAL).isAfter(Instant.now()));

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
    
}