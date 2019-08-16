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

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * <p>
 * This class is used to store instances of objects that may be created independently but are, in fact, the same object.
 * For example, KeyInfo XML structures contain keys, certs, and CRLs. Multiple unique instances of
 * a KeyInfo may contain, and separately construct, the exact same cert. KeyInfo could, therefore, create a class-level
 * instance of this object store and put certs within it. In this manner the cert is only sitting in memory once and
 * each KeyInfo simply stores a reference (index) to stored object.
 * </p>
 * 
 * <p>
 * This store uses basic reference counting to keep track of how many of the respective objects are pointing to an
 * entry. Adding an object that already exists, as determined by the object's <code>equals()</code> method, simply
 * increments the reference counter. Removing an object decrements the counter. Only when the counter reaches zero is
 * the object actually freed for garbage collection.
 * </p>
 * 
 * <p>
 * <strong>Note:</strong> the instance of an object returned by {@link #get(String)} need not be the same object as 
 * stored via {@link #put(Object)}.  However, the instances will be equal according to their <code>equals()</code>.
 * The indexing and storage is based on use of {@link Map}, so the normal caveats related to use of hash-based
 * collection types apply: if the stored object's <code>hashCode()</code> and <code>equals()</code> methods are 
 * implemented based on mutable properties of the object, then those object instance's properties should not 
 * be mutated while the object is stored, otherwise unpredictable behavior will result.
 * </p>
 * 
 * @param <T> type of object being stored
 */
@ThreadSafe
public class IndexingObjectStore<T> {

    /** Read/Write lock used to control synchronization over the backing data store. */
    private ReadWriteLock rwLock;

    /** Backing object data store. */
    private Map<String, StoredObjectWrapper> objectStore;
    
    /** Map of object instances to the index value used to reference them externally. */
    private Map<T, Integer> indexStore;
    
    /** The last index sequence used. */
    private int lastIndex;

    /** Constructor. */
    public IndexingObjectStore() {
        rwLock = new ReentrantReadWriteLock();
        objectStore = new LazyMap<>();
        indexStore = new LazyMap<>();
        lastIndex = 0;
    }

    /** Clears the object store. */
    public void clear() {
        final Lock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            objectStore.clear();
            indexStore.clear();
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * Checks whether the store contains an object registered under the given index.
     * 
     * @param index the index to check
     * 
     * @return true if an object is associated with the given index, false if not
     */
    public boolean containsIndex(final String index) {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return objectStore.containsKey(index);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks whether the store contains an object instance equal to the specified one.
     * 
     * @param instance the object instance to check
     * 
     * @return true if an object instance equal to the specified one is stored, false if not
     */
    public boolean containsInstance(final T instance) {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            final Integer index = indexStore.get(instance);
            return index != null ? objectStore.containsKey(index.toString()) : false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if the store is empty.
     * 
     * @return true if the store is empty, false if not
     */
    public boolean isEmpty() {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return objectStore.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Adds the given object to the store. Technically this method only adds the object if it does not already exist in
     * the store. If it does this method simply increments the reference count of the object.
     * 
     * @param object the object to add to the store, may be null
     * 
     * @return the index that may be used to later retrieve the object or null if the object was null
     */
    public String put(final T object) {
        if (object == null) {
            return null;
        }

        final Lock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            final String index = getIndex(object);

            StoredObjectWrapper objectWrapper = objectStore.get(index);
            if (objectWrapper == null) {
                objectWrapper = new StoredObjectWrapper(object);
                objectStore.put(index, objectWrapper);
            }
            objectWrapper.incremementReferenceCount();

            return index;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets a registered object by its index.
     * 
     * @param index the index of an object previously registered, may be null
     * 
     * @return the registered object or null if no object is registered for that index
     */
    public T get(final String index) {
        if (index == null) {
            return null;
        }

        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            final StoredObjectWrapper objectWrapper = objectStore.get(index);
            if (objectWrapper != null) {
                return objectWrapper.getObject();
            }

            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Removes the object associated with the given index. Technically this method decrements the reference counter to
     * the object. If, after the decrement, the reference counter is zero then, and only then, is the object actually
     * freed for garbage collection.
     * 
     * @param index the index of the object, may be null
     */
    public void remove(final String index) {
        if (index == null) {
            return;
        }

        final Lock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            final StoredObjectWrapper objectWrapper = objectStore.get(index);
            if (objectWrapper != null) {
                objectWrapper.decremementReferenceCount();
                if (objectWrapper.getReferenceCount() == 0) {
                    objectStore.remove(index);
                    removeIndex(objectWrapper.getObject());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets the total number of unique items in the store. This number is unaffected by the reference count of the
     * individual stored objects.
     * 
     * @return number of items in the store
     */
    public int size() {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return objectStore.size();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Get the index for the specified object.
     * 
     * @param object the target object
     * @return the object index value
     */
    protected String getIndex(final T object) {
        Integer index = indexStore.get(object);
        if (index == null) {
            index = ++lastIndex;
            indexStore.put(object, index);
        }
        return index.toString();
    }
    
    /**
     * Remove the index for the specified object.
     * 
     * @param object the target index
     */
    protected void removeIndex(final T object) {
        indexStore.remove(object);
    }

    /** Wrapper class that keeps track of the reference count for a stored object. */
    private class StoredObjectWrapper {

        /** The stored object. */
        private T object;

        /** The object reference count. */
        private int referenceCount;

        /**
         * Constructor.
         * 
         * @param wrappedObject the object being wrapped
         */
        public StoredObjectWrapper(final T wrappedObject) {
            object = wrappedObject;
            referenceCount = 0;
        }

        /**
         * Gets the wrapped object.
         * 
         * @return the wrapped object
         */
        public T getObject() {
            return object;
        }

        /**
         * Gets the current reference count.
         * 
         * @return current reference count
         */
        public int getReferenceCount() {
            return referenceCount;
        }

        /** Increments the current reference count by one. */
        public void incremementReferenceCount() {
            referenceCount += 1;
        }

        /** Decrements the current reference count by one. */
        public void decremementReferenceCount() {
            referenceCount -= 1;
        }
    }
}