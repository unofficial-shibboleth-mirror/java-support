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

package net.shibboleth.utilities.java.support.collection;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/**
 * A specialization of {@link ClassToInstanceMultiMap} which exposes a map-specific
 * instance of {@link ReadWriteLock}.
 * 
 * <p>
 * Callers of the map are generally responsible for explicitly locking
 * and unlocking for reading and writing, based on application use cases and concurrency 
 * requirements.  For simple single-statement atomic operations, convenience
 * methods are supplied which execute the corresponding superclass operation under the 
 * read or write lock, as appropriate.
 * </p>
 * 
 * @param <B> a bound for the types of values in the map
 */
public class LockableClassToInstanceMultiMap<B> extends ClassToInstanceMultiMap<B> {
    
    /** The map's read write lock. */
    @Nonnull private final ReadWriteLock readWriteLock;

    /**
     * Constructor.
     */
    public LockableClassToInstanceMultiMap() {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param isIndexingSupertypes indicates whether supertypes of a value should be indexed
     */
    public LockableClassToInstanceMultiMap(final boolean isIndexingSupertypes) {
        super(isIndexingSupertypes);
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    /**
     * Get the map-specific instance of the {@link ReadWriteLock}.
     * 
     * <p>
     * Callers of the map are responsible for explicitly locking (and unlocking)
     * for reading and/or writing, based on application use cases.
     * </p>
     * 
     * @return Returns the rwlock.
     */
    @Nonnull public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     */
    public void clearWithLock() {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            clear();
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @param key key to check for in the map
     * 
     * @return true if the map contains a mapping for the specified key
     */
    public boolean containsKeyWithLock(@Nullable final Class<?> key) {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return containsKey(key);
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @param value value to check for in this map
     * 
     * @return true if the map contains a mapping to the specified value
     */
    public boolean containsValueWithLock(@Nonnull final B value) {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return containsValue(value);
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @param <T> type identifier
     * @param type map key
     * 
     * @return instances mapped to the given type or an empty list
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public <T> List<T> getWithLock(@Nullable final Class<T> type) {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return get(type);
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @return true if this map contains no entries, false otherwise
     */
    public boolean isEmptyWithLock() {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return isEmpty();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @return set of keys contained in this map
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public Set<Class<?>> keysWithLock() {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return keys();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param value value to be stored in the map
     */
    public void putWithLock(@Nonnull final B value) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            put(value);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param map map containing values to be added
     */
    public void putAllWithLock(@Nullable @NonnullElements final ClassToInstanceMultiMap<? extends B> map) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            putAll(map);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param newValues values to be added
     */
    public void putAllWithLock(@Nullable @NonnullElements final Iterable<? extends B> newValues) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            putAll(newValues);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param value the value to remove
     */
    public void removeWithLock(@Nonnull final B value) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            remove(value);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param type the type of values to remove
     */
    public void removeWithLock(@Nullable final Class<?> type) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            remove(type);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param map the map containing the values to remove
     */
    public void removeAllWithLock(@Nullable @NonnullElements final ClassToInstanceMultiMap<? extends B> map) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            removeAll(map);
        } finally {
            writeLock.unlock();
        }
    }
    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a write lock.
     * 
     * @param removeValues the values to remove
     */ 
    public void removeAllWithLock(@Nullable @NonnullElements final Iterable<? extends B> removeValues) {
        final Lock writeLock = getReadWriteLock().writeLock();
        try {
            writeLock.lock();
            removeAll(removeValues);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Convenience method which executes the like-named method from superclass {@link ClassToInstanceMultiMap}
     * under a read lock.
     * 
     * @return collection of values currently present in the map
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public Collection<? extends B> valuesWithLock() {
        final Lock readLock = getReadWriteLock().readLock();
        try {
            readLock.lock();
            return values();
        } finally {
            readLock.unlock();
        }
    }
    
}