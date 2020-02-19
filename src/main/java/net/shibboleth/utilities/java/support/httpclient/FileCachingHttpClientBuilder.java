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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.primitive.TimerSupport;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.FileResourceFactory;
import org.apache.http.impl.client.cache.ManagedHttpCacheStorage;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link org.apache.http.client.HttpClient} builder that supports RFC 2616 caching.
 * <p>
 * Cached content is written to disk. Special care should be taken so that multiple clients do not share a single cache
 * directory unintentionally. This could result in sensitive data being available in ways it should not be.
 * </p>
 * 
 * <p>
 * When using the single-arg constructor variant to wrap an existing instance of
 * {@link CachingHttpClientBuilder}, there are several caveats of which to be aware:
 * </p>
 * 
 * <ul>
 * 
 * <li>
 * Several important non-caching-specific caveats are enumerated in this class's superclass {@link HttpClientBuilder}.
 * </li>
 * 
 * <li>
 * Instances of the following which are set as the default instance on the Apache builder will be
 * unconditionally overwritten by this builder when {@link #buildClient()} is called:
 * 
 *   <ul>
 *   <li>{@link CacheConfig}</li>
 *   </ul>
 *   
 *   <p>
 *   This is due to the unfortunate fact that the Apache builder does not currently provide accessor methods to
 *   obtain the default instances currently set on the builder.  Therefore, if you need to set any default cache
 *   config parameters which are not exposed by this builder, then you must use the Apache
 *   builder directly and may not use this builder.
 *   </p>
 * </li>
 * 
 * </ul>
 */
public class FileCachingHttpClientBuilder extends HttpClientBuilder {

    /**
     * Directory in which cached content will be stored. Default:
     * <code>System.getProperty("java.io.tmpdir") + File.separator + "wwwcache"</code>
     */
    private File cacheDir;

    /** The maximum number of cached responses. Default: 100 */
    private int maxCacheEntries;

    /** The maximum response body size, in bytes, that will be eligible for caching. Default: 10485760 (10 megabytes) */
    private long maxCacheEntrySize;
    
    /** Interval at which the storage maintenance task should run. */
    @Nonnull private Duration maintentanceTaskInterval;
    
    /** The current managed storage instance. */
    private ManagedHttpCacheStorage managedStorage;
    
    /**
     * Constructor.
     */
    public FileCachingHttpClientBuilder() {
        this(CachingHttpClientBuilder.create());
    }
    
    /**
     * Constructor.
     * 
     * @param builder builder of clients used to fetch data from remote servers
     */
    public FileCachingHttpClientBuilder(@Nonnull final CachingHttpClientBuilder builder) {
        super(builder);
        cacheDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "wwwcache");
        maxCacheEntries = 100;
        maxCacheEntrySize = 10485760;
        maintentanceTaskInterval = Duration.ofMinutes(30);
    }

    /**
     * Gets the directory in which cached content will be stored.
     * 
     * @return directory in which cached content will be stored
     */
    public File getCacheDirectory() {
        return cacheDir;
    }

    /**
     * Sets the directory in which cached content will be stored.
     * 
     * @param directoryPath filesystem path to the directory
     */
    public void setCacheDirectory(@Nonnull @NotEmpty final String directoryPath) {
        final String trimmedPath =
                Constraint.isNotNull(StringSupport.trimOrNull(directoryPath),
                        "Cache directory path can not be null or empty");
        cacheDir = new File(trimmedPath);
    }

    /**
     * Sets the directory in which cached content will be stored.
     * 
     * @param directory the directory
     */
    public void setCacheDirectory(@Nonnull final File directory) {
        cacheDir = Constraint.isNotNull(directory, "Cache directory can not be null");
    }

    /**
     * Gets the maximum number of cached responses.
     * 
     * @return maximum number of cached responses
     */
    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    /**
     * Sets the maximum number of cached responses.
     * 
     * @param maxEntries maximum number of cached responses, must be greater than zero
     */
    public void setMaxCacheEntries(final int maxEntries) {
        maxCacheEntries =
                (int) Constraint.isGreaterThan(0, maxEntries, "Maximum number of cache entries must be greater than 0");
    }

    /**
     * Gets the maximum response body size, in bytes, that will be eligible for caching.
     * 
     * @return maximum response body size that will be eligible for caching
     */
    public long getMaxCacheEntrySize() {
        return maxCacheEntrySize;
    }

    /**
     * Sets the maximum response body size, in bytes, that will be eligible for caching.
     * 
     * @param size maximum response body size that will be eligible for caching, must be greater than zero
     */
    public void setMaxCacheEntrySize(final long size) {
        maxCacheEntrySize = (int) Constraint.isGreaterThan(0, size, "Maximum cache entry size must be greater than 0");
    }

    /**
     * Get the interval at which the storage maintenance task should run.
     * 
     * @return the maintenance task interval
     */
    @Nonnull public Duration getMaintentanceTaskInterval() {
        return maintentanceTaskInterval;
    }

    /**
     * Set the interval at which the storage maintenance task should run.
     * 
     * @param value the new maintenance task interval
     */
    public void setMaintentanceTaskInterval(@Nonnull final Duration value) {
        Constraint.isNotNull(value, "Interval cannot be null");
        Constraint.isFalse(value.isNegative() || value.isZero(), "Interval must be positive");
        
        maintentanceTaskInterval = value;
    }

    /** {@inheritDoc} */
    protected void decorateApacheBuilder() throws Exception {
        super.decorateApacheBuilder();
        
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IOException("Unable to create cache directory " + cacheDir.getAbsolutePath());
            }
        }

        if (!cacheDir.canRead()) {
            throw new IOException("Cache directory '" + cacheDir.getAbsolutePath() + "' is not readable");
        }

        if (!cacheDir.canWrite()) {
            throw new IOException("Cache directory '" + cacheDir.getAbsolutePath() + "' is not writable");
        }
        
        final CachingHttpClientBuilder cachingBuilder = (CachingHttpClientBuilder) getApacheBuilder();

        final CacheConfig.Builder cacheConfigBuilder = CacheConfig.custom();
        cacheConfigBuilder.setMaxCacheEntries(maxCacheEntries);
        cacheConfigBuilder.setMaxObjectSize(maxCacheEntrySize);
        cacheConfigBuilder.setHeuristicCachingEnabled(false);
        cacheConfigBuilder.setSharedCache(false);
        final CacheConfig cacheConfig = cacheConfigBuilder.build();
        
        cachingBuilder.setCacheConfig(cacheConfig);
        cachingBuilder.setResourceFactory(new FileResourceFactory(cacheDir));
        // Because of the way the builder is structured, we need to temporarily store this for access in buildClient().
        // This impl's buildClient() is synchronized, so don't have concurrency issues.
        managedStorage = new ManagedHttpCacheStorage(cacheConfig);
        cachingBuilder.setHttpCacheStorage(managedStorage);
    }

    /** {@inheritDoc} */
    public synchronized HttpClient buildClient() throws Exception {
        final CloseableHttpClient client = (CloseableHttpClient) super.buildClient();
        final ManagedHttpCacheStorage tempStorage = managedStorage;
        // Null this out so we don't keep a reference, inhibiting garbage collection.
        managedStorage = null;
        return new StorageManagingHttpClient(client, tempStorage, getMaintentanceTaskInterval().toMillis());
    }
    
    /**
     * Class which wraps a caching instance of {@link CloseableHttpClient} and its associated 
     * {@link ManagedHttpCacheStorage}, and manages the scheduled maintenance and lifecycle of the latter.
     */
    private static class StorageManagingHttpClient extends CloseableHttpClient 
            implements InitializableComponent, DestructableComponent {
        
        /** Logger. */
        private Logger log = LoggerFactory.getLogger(StorageManagingHttpClient.class);
        
        /** The wrapped HttpClient instance. */
        private CloseableHttpClient httpClient;
        
        /** The cache storage instance to manage. */
        private ManagedHttpCacheStorage storage;
        
        /** Interval of the scheduled maintenance task. */
        private long maintenanceTaskInterval;
        
        /** Initialized flag. */
        private boolean initialized;
        
        /** Destroyed flag. */
        private boolean destroyed;
        
        /** Scheduled task timer. */
        private Timer timer;
        
        /** The scheduled storage maintenance task. */
        private TimerTask maintenanceTask;
        
        /**
         * Constructor.
         *
         * @param wrappedClient the wrapped HttpClient instance
         * @param managedStorage the managed cache storage instance
         * @param taskInterval the interval at which storage maintenance should run
         */
        public StorageManagingHttpClient(@Nonnull final CloseableHttpClient wrappedClient, 
                @Nonnull final ManagedHttpCacheStorage managedStorage, final long taskInterval)  {
           httpClient = Constraint.isNotNull(wrappedClient, "HttpClient was null");
           storage = Constraint.isNotNull(managedStorage, "ManagedHttpCacheStorage was null");
           maintenanceTaskInterval = taskInterval;
        }

        /** {@inheritDoc} */
        protected CloseableHttpResponse doExecute(final HttpHost target, final HttpRequest request,
                final HttpContext context)
                throws IOException, ClientProtocolException {
            ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
            ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
            return httpClient.execute(target, request, context);
        }

        /** {@inheritDoc} */
        @Override
        @Deprecated
        public org.apache.http.params.HttpParams getParams() {
            ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
            ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
            return httpClient.getParams();
        }
        
        /** {@inheritDoc} */
        @Override
        @Deprecated
        public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
            ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
            ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
            return httpClient.getConnectionManager();
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws IOException {
            ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
            ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
            httpClient.close();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isInitialized() {
            return initialized;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isDestroyed() {
            return destroyed;
        }

        /** {@inheritDoc} */
        @Override
        public void initialize() throws ComponentInitializationException {
            timer = new Timer(TimerSupport.getTimerName(this), true);
            maintenanceTask = new StorageMaintenanceTask(storage);
            timer.schedule(maintenanceTask, maintenanceTaskInterval, maintenanceTaskInterval);
            
            initialized = true;
        }

        /** {@inheritDoc} */
        public void destroy() {
            maintenanceTask.cancel();
            timer.cancel();
            maintenanceTask = null;
            timer = null;
            
            try {
                log.debug("Executing ManagedHttpCacheStorage shutdown()");
                storage.shutdown();
            } catch (final Throwable t) {
                log.warn("Error invoking ManagedHttpCacheStorage shutdown()", t);
            }
            storage = null;
            
            httpClient = null;
            
            destroyed = true;
        }
        
    }
    
    /**
     * Scheduled task to manage an instance of {@link ManagedHttpCacheStorage}.
     */
    private static class StorageMaintenanceTask extends TimerTask {
        
        /** Logger. */
        private Logger log = LoggerFactory.getLogger(StorageMaintenanceTask.class);
        
        /** The managed cache storage instance. */
        private ManagedHttpCacheStorage storage;
        
        /**
         * Constructor.
         *
         * @param managedStorage the managed cache storage instance
         */
        public StorageMaintenanceTask(@Nonnull final ManagedHttpCacheStorage managedStorage) {
           storage = Constraint.isNotNull(managedStorage, "ManagedHttpCacheStorage was null");
        }

        /** {@inheritDoc} */
        public void run() {
            try {
                log.debug("Executing ManagedHttpCacheStorage cleanResources()");
                storage.cleanResources();
            } catch (final Throwable t) {
                log.warn("Error invoking ManagedHttpCacheStorage cleanResources()", t);
            }
        }
        
    }
    
}