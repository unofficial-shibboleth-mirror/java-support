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

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * An append-only multimap where each entry associates a raw type (i.e. class) to instances of that class. In addition
 * the map may also associate any supertype (i.e. all superclasses and interfaces implemented by the class) with the
 * class.
 * 
 * Null values are not supported.
 * 
 * @param <B> a bound for the types of values in the map
 */
@NotThreadSafe
public class ClassToInstanceMultiMap<B> {

    /** Whether supertypes should also be indexed. */
    private final boolean indexSupertypes;

    /** Map which backs this map. */
    @Nonnull private final HashMap<Class<?>, List<B>> backingMap;

    /** List of values that are indexed. */
    @Nonnull private final List<B> values;

    /** Constructor. Does not index supertypes. */
    public ClassToInstanceMultiMap() {
        this(false);
    }

    /**
     * Constructor.
     * 
     * @param isIndexingSupertypes indicates whether supertypes of a value should be indexed
     */
    public ClassToInstanceMultiMap(final boolean isIndexingSupertypes) {
        backingMap = new HashMap<>();
        values = new ArrayList<>();
        indexSupertypes = isIndexingSupertypes;
    }

    /** Removes all mappings from this map. */
    public void clear() {
        values.clear();
        backingMap.clear();
    }

    /**
     * Returns true if the map contains a mapping for the given key.
     * 
     * @param key key to check for in the map
     * 
     * @return true if the map contains a mapping for the specified key
     */
    public boolean containsKey(@Nullable final Class<?> key) {
        if (key == null) {
            return false;
        }

        return backingMap.containsKey(key);
    }

    /**
     * Returns true iff the map contains a mapping to the given value, false if value is null.
     * 
     * @param value value to check for in this map
     * 
     * @return true iff the map contains a mapping to the specified value
     */
    public boolean containsValue(@Nullable final B value) {
        if (value == null) {
            return false;
        }

        return values.contains(value);
    }

    /**
     * Gets the instances mapped to the given type or an empty list.
     * 
     * @param <T> type identifier
     * @param type map key
     * 
     * @return instances mapped to the given type or an empty list
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public <T> List<T> get(@Nullable final Class<T> type) {
        if (type == null) {
            return Collections.emptyList();
        }

        final List<T> indexedValues = (List<T>) backingMap.get(type);
        if (indexedValues == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(indexedValues);
    }

    /**
     * Returns true if this map contains no entries, false otherwise.
     * 
     * @return true if this map contains no entries, false otherwise
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Gets the set of keys contained in this map. The set is backed by the map so changes made to the map are reflected
     * in the set. However the set does not allow direct modification, any changes need to be done through this map.
     * 
     * @return set of keys contained in this map
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public Set<Class<?>> keys() {
        return Collections.unmodifiableSet(backingMap.keySet());
    }

    /**
     * Adds a value to this map. If {@link #indexSupertypes} is false only the values class type is used as a key to the
     * value. If {@link #indexSupertypes} is true, then the class types, all its supertypes, and all implemented
     * interfaces are used as keys to the value.
     * 
     * Duplicate values, as determined by the values {@link Object#hashCode()} and {@link Object#equals(Object)}
     * methods, are not stored. Only one instance of the value is ever stored in the map.
     * 
     * @param value value to be stored in the map
     */
    public void put(@Nonnull final B value) {
        Constraint.isNotNull(value, "Value cannot be null");

        if (!values.contains(value)) {
            values.add(value);
        }

        List<B> indexValues;
        for (final Class<?> indexKey : getIndexTypes(value)) {
            indexValues = backingMap.get(indexKey);

            if (indexValues == null) {
                indexValues = new ArrayList<>();
                backingMap.put(indexKey, indexValues);
            }

            if (!indexValues.contains(value)) {
                indexValues.add(value);
            }
        }
    }

    /**
     * Adds all the values to the map.
     * 
     * @param newValues values to be added
     * 
     * @see ClassToInstanceMultiMap#put(Object)
     */
    public void putAll(@Nullable @NonnullElements final Iterable<? extends B> newValues) {
        if (newValues == null) {
            return;
        }

        for (final B value : newValues) {
            put(value);
        }
    }

    /**
     * Adds all the values to the map. This operations operates only on the given map's value collection. Therefore,
     * regardless of the given map's policy on indexing by value supertypes, this map will index values based on its
     * policy.
     * 
     * @param map map containing values to be added
     * 
     * @see ClassToInstanceMultiMap#put(Object)
     */
    public void putAll(@Nullable @NonnullElements final ClassToInstanceMultiMap<? extends B> map) {
        if (map == null) {
            return;
        }

        putAll(map.values());
    }

    /**
     * Remove the specified value from the map and from the value list of all indexes.
     * 
     * <p>
     * If the value list for a type index becomes empty due to the value removal, the entire type index will be removed
     * and {@link #containsKey(Class)} for that type will then return <code>false</code>.
     * </p>
     * 
     * @param value the value to remove
     */
    public void remove(@Nullable final B value) {
        final B checked = Constraint.isNotNull(value, "Value cannot be null");

        values.remove(checked);

        List<B> indexValues;
        for (final Class<?> indexKey : getIndexTypes(checked)) {
            indexValues = backingMap.get(indexKey);
            if (indexValues != null) {
                indexValues.remove(checked);
                if (indexValues.isEmpty()) {
                    backingMap.remove(indexKey);
                }
            }
        }
    }

    /**
     * Remove the specified values from the map and from the value list of all indexes.
     * 
     * <p>
     * If the value list for a type index becomes empty due to a value removal, the entire type index will be removed
     * and {@link #containsKey(Class)} for that type will then return <code>false</code>.
     * </p>
     * 
     * @param removeValues the values to remove
     */
    public void removeAll(@Nullable @NonnullElements final Iterable<? extends B> removeValues) {
        if (removeValues == null) {
            return;
        }

        for (final B value : removeValues) {
            remove(value);
        }
    }

    /**
     * Remove the values contained in the specified map from this map and from the value list of all indexes.
     * 
     * <p>
     * If the value list for a type index becomes empty due to a value removal, the entire type index will be removed
     * and {@link #containsKey(Class)} for that type will then return <code>false</code>.
     * </p>
     * 
     * @param map the map containing the values to remove
     */
    public void removeAll(@Nullable @NonnullElements final ClassToInstanceMultiMap<? extends B> map) {
        if (map == null) {
            return;
        }

        removeAll(map.values());
    }

    /**
     * Remove from the map all values which have the specified type.
     * 
     * <p>
     * Note that when a value was indexed by multiple superclass and/or interface types, it will be removed from all
     * those type indexes, not just the specified one.
     * </p>
     * 
     * <p>
     * If the value list for a type index becomes empty due to a value removal, the entire type index will be removed
     * and {@link #containsKey(Class)} for that type will then return <code>false</code>.
     * </p>
     * 
     * @param type the type of values to remove
     */
    public void remove(@Nullable final Class<?> type) {
        if (type == null) {
            return;
        }

        final List<B> indexValues = backingMap.remove(type);

        if (indexValues != null) {
            for (final B value : indexValues) {
                remove(value);
            }
        }
    }

    /**
     * The collection of values currently present in the map. This collection is backed by the map so changes to the
     * map will be reflected in the collection. However the collection does not allow direct modification so any changes
     * must be done through this map.
     * 
     * @return collection of values currently present in the map
     */
    @Nonnull @NonnullElements @Unmodifiable @Live public Collection<? extends B> values() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Get the effective set of all class types via which the specified value should be indexed.
     * 
     * @param value the value to index
     * @return the set of classes by which to index the value
     */
    @Nonnull @NonnullElements private Set<Class<?>> getIndexTypes(@Nonnull final B value) {
        final HashSet<Class<?>> indexTypes = new HashSet<>();
        indexTypes.add(value.getClass());

        if (indexSupertypes) {
            getSuperTypes(value.getClass(), indexTypes);
        }

        return indexTypes;
    }

    /**
     * Gets all of the superclasses and interfaces implemented by the given class.
     * 
     * @param clazz class for which supertypes will be determined
     * @param accumulator collection to which supertypes are added as they are determined
     */
    private void getSuperTypes(@Nonnull final Class<?> clazz,
            @Nonnull @NonnullElements final Set<Class<?>> accumulator) {
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            accumulator.add(superclass);
            getSuperTypes(superclass, accumulator);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (final Class<?> iface : interfaces) {
                accumulator.add(iface);
                getSuperTypes(iface, accumulator);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return backingMap.hashCode() + values.hashCode();
    }

    /** {@inheritDoc} */
    @Override public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj instanceof ClassToInstanceMultiMap<?>) {
            final ClassToInstanceMultiMap<?> cast = (ClassToInstanceMultiMap<?>) obj;

            return backingMap.equals(cast.backingMap) && values.equals(cast.values);
        }
        return false;
    }
    
}