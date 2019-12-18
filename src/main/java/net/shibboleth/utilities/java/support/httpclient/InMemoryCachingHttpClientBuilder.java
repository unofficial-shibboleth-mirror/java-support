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

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;

import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.HeapResourceFactory;

/**
 * An {@link org.apache.http.client.HttpClient} builder that supports RFC 2616 caching.
 * <p>
 * This client will cache information retrieved from the remote server in memory. The backing store does
 * <strong>not</strong> perform any resource management (e.g., removing content that has nominally expired) so, special
 * care must be taken to tune the {@link #maxCacheEntries} and {@link #maxCacheEntrySize} appropriately so that the
 * system's memory is not fully consumed.
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
public class InMemoryCachingHttpClientBuilder extends HttpClientBuilder {

    /** The maximum number of cached responses. Default: 50 */
    private int maxCacheEntries;

    /** The maximum response body size, in bytes, that will be eligible for caching. Default: 1048576 (1 megabyte) */
    private long maxCacheEntrySize;

    /**
     * Constructor.
     */
    public InMemoryCachingHttpClientBuilder() {
        this(CachingHttpClientBuilder.create());
    }
    
    /**
     * Constructor.
     * 
     * @param builder builder of clients used to fetch data from remote servers
     */
    public InMemoryCachingHttpClientBuilder(@Nonnull final CachingHttpClientBuilder builder) {
        super(builder);
        maxCacheEntries = 50;
        maxCacheEntrySize = 1048576;
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
        maxCacheEntrySize = Constraint.isGreaterThan(0, size, "Maximum cache entry size must be greater than 0");
    }

    /** {@inheritDoc} */
    protected void decorateApacheBuilder() throws Exception {
        super.decorateApacheBuilder();
        
        // Note: This cast is safe because of constructor enforcement.
        final CachingHttpClientBuilder cachingBuilder = (CachingHttpClientBuilder) getApacheBuilder();

        final CacheConfig.Builder cacheConfigBuilder = CacheConfig.custom();
        cacheConfigBuilder.setMaxCacheEntries(maxCacheEntries);
        cacheConfigBuilder.setMaxObjectSize(maxCacheEntrySize);
        cacheConfigBuilder.setHeuristicCachingEnabled(false);
        cacheConfigBuilder.setSharedCache(false);
        final CacheConfig cacheConfig = cacheConfigBuilder.build();
        
        cachingBuilder.setCacheConfig(cacheConfig);
        cachingBuilder.setResourceFactory(new HeapResourceFactory());
        cachingBuilder.setHttpCacheStorage(new BasicHttpCacheStorage(cacheConfig));
    }
}