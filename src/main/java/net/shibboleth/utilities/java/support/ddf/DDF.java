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

package net.shibboleth.utilities.java.support.ddf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * The core object in the DDF mode, this is a node in a tree of objects that
 * make up the entire data structure.
 * 
 * <p>Each node contains references to its parant and children, if any,
 * as well as data type and possibly a value if a leaf node.</p>
 * 
 * <p>Most of the types are self-explanatory, but strings may be "safe" or "unsafe".
 * Safe strings are understood to be Unicode that can be safely converted between UTF-8
 * and UTF-16. Unsafe strings are represented as Java String objects but have an unknown
 * character encoding so the individual code points above 127 are essentially undefined
 * and cannot be assumed to represent the "correct" value. They may only be compared
 * with other values that are understood to represent the same range of values.</p> 
 * 
 * <p>The method names do not align to normal Java conventions for compatibility with
 * the other version(s) of the same API.</p>
 */
@NotThreadSafe
public class DDF {

    /** Name of node. */
    @Nullable private String name;
    
    /** Parent node. */
    @Nullable private DDF parent;

    /** Type enum. */
    public enum DDFType {

        /** An empty node with no value. */
        DDF_EMPTY(0),
        
        /** A string value. */
        DDF_STRING(1),
        
        /** An integral value. */
        DDF_INT(2),
        
        /** A floating point value. */
        DDF_FLOAT(3),
        
        /** A structure with named children. */
        DDF_STRUCT(4),
        
        /** An ordered list. */
        DDF_LIST(5),
        
        /** A reference to any object. */
        DDF_POINTER(6),
        
        /** A string that cannot be assumed to be UTF-8 (see above docs). */
        DDF_STRING_UNSAFE(7);
        
        /** Type value. */
        private final int value;
        
        /**
         * Constructor.
         * 
         * @param val value of the type enum
         */
        private DDFType(@NonNegative final int val) {
            value = val;
        }
        
        /**
         * Get the type value.
         * 
         * @return type value
         */
        public int getValue() {
            return value;
        }
        
    };
    
    /** Node type. */
    @Nonnull private DDFType type;
    
    /** Reference to the value, which depends on the type. */
    @Nullable private Object value;
    
    /** Constructor. */
    public DDF() {
        type = DDFType.DDF_EMPTY;
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     *
     * @param n node name
     */
    public DDF(@Nullable @NotEmpty final String n) {
        this();
        name(n);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     *
     * @param n node name
     * @param val string value, assumed to be "safe" Unicode
     */
    public DDF(@Nullable @NotEmpty final String n, @Nullable final String val) {
        this(n);
        string(val);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     *
     * @param n node name
     * @param val integer value
     */
    public DDF(@Nullable @NotEmpty final String n, final int val) {
        this(n);
        integer(val);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     *
     * @param n node name
     * @param val floating value
     */
    public DDF(@Nullable @NotEmpty final String n, final double val) {
        this(n);
        floating(val);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     *
     * @param n node name
     * @param val object value
     */
    public DDF(@Nullable @NotEmpty final String n, @Nullable final Object val) {
        this(n);
        pointer(val);
    }

    /**
     * Destroys a node's content, resets it to a null object and clears its name.
     * 
     * <p>This is primarily for tree maintenance, given the lack of need for explicit
     * memory management.</p>
     * 
     * @return this object
     */
    @Nonnull public DDF destroy() {
        remove().empty().name(null);
        type = null;
        return this;
    }
    
    /**
     * Performs a deep copy of the node and all children, if any.
     * 
     * @return the copy
     */
    @SuppressWarnings("unchecked")
    @Nonnull DDF copy() {
        final DDF dup = new DDF(name);
        
        switch (type) {
            case DDF_EMPTY:
                break;
                
            case DDF_STRING:
                dup.string((String) value);
                break;
                
            case DDF_STRING_UNSAFE:
                dup.unsafe_string((String) value);
                break;
                
            case DDF_INT:
                dup.integer((Integer) value);
                break;
                
            case DDF_FLOAT:
                dup.floating((Double) value);
                break;
                
            case DDF_STRUCT:
                dup.structure();
                for (final DDF ddf : ((Map<String,DDF>) value).values()) {
                    dup.add(ddf.copy());
                }
                break;
                
            case DDF_LIST:
                dup.list();
                for (final DDF ddf : (List<DDF>) value) {
                    dup.add(ddf.copy());
                }
                break;
                
            default:
        }
        
        return dup;
    }
    
    /**
     * Get the node name.
     * 
     * @return the name
     */
    @Nullable public String name() {
        return name;
    }
    
    /**
     * Set the node name.
     * 
     * <p>For compatibility, the name is constrained to <= 255 characters.</p>
     * 
     * <p>The name will not be set if the node is already a child of a structure.</p>
     * 
     * @param n the new name
     * 
     * @return this object
     */
    @Nonnull public DDF name(@Nullable @NotEmpty final String n) {
        if (parent == null || !parent.isstruct()) {
            if (n != null) {
                name = Constraint.isNotEmpty(n.substring(0,Integer.min(n.length(), 255)), "Name cannot be empty");
            } else {
                name = null;
            }
        }
        return this;
    }
    
    /**
     * Returns true iff the node is empty.
     * 
     * @return true iff the node is empty
     */
    public boolean isempty() {
        return type == DDFType.DDF_EMPTY;
    }

    /**
     * Returns true iff the node is a string (safe or not).
     * 
     * @return true iff the node is a string (safe or not)
     */
    public boolean isstring() {
        return type == DDFType.DDF_STRING || type == DDFType.DDF_STRING_UNSAFE;
    }

    /**
     * Returns true iff the node is an integer.
     * 
     * @return true iff the node is an integer
     */
    public boolean isint() {
        return type == DDFType.DDF_INT;
    }

    /**
     * Returns true iff the node is a floating point.
     * 
     * @return true iff the node is a floating point
     */
    public boolean isfloat() {
        return type == DDFType.DDF_FLOAT;
    }

    /**
     * Returns true iff the node is a structure.
     * 
     * @return true iff the node is a structure
     */
    public boolean isstruct() {
        return type == DDFType.DDF_STRUCT;
    }

    /**
     * Returns true iff the node is a list/array.
     * 
     * @return true iff the node is a list/array.
     */
    public boolean islist() {
        return type == DDFType.DDF_LIST;
    }

    /**
     * Returns true iff the node is a pointer (i.e., object reference).
     * 
     * @return true iff the node is a pointer (i.e., object reference)
     */
    public boolean ispointer() {
        return type == DDFType.DDF_POINTER;
    }

    /**
     * Get the string value of this node.
     * 
     * <p>The string value of a non-string value is null.</p>
     * 
     * @return the string value or null
     */
    @Nullable public String string() {
        return isstring() ? (String) value : null;
    }

    /**
     * Get the integer value of this node.
     * 
     * <p>Integers are coerced from other types based on numeric conversions
     * or the count of a structure or list.</p> 
     * 
     * @return the integer value or null
     */
    @Nullable public Integer integer() {
        
        switch(type) {
            case DDF_INT:
                return (Integer) value;
            case DDF_FLOAT:
                return ((Double) value).intValue();
            case DDF_STRING:
                try {
                    return Integer.valueOf((String) value);
                } catch (final NumberFormatException e) {
                    // Swallow.
                    return null;
                }
            case DDF_STRUCT:
                return ((Map<?,?>) value).size();
            case DDF_LIST:
                return ((List<?>) value).size();
            default:
                break;
        }
        
        return null;
    }
    
    /**
     * Get the floating point value of this node.
     * 
     * <p>Doubles are coerced from other types based on numeric conversions
     * or the count of a structure or list.</p> 
     *
     * @return the floating point value or null
     */
    @Nullable public Double floating() {
        
        switch(type) {
            case DDF_INT:
                return ((Integer) value).doubleValue();
            case DDF_FLOAT:
                return (Double) value;
            case DDF_STRING:
                try {
                    return Double.valueOf((String) value);
                } catch (final NumberFormatException e) {
                    // Swallow.
                    return null;
                }
            case DDF_STRUCT:
                return (double) ((Map<?,?>) value).size();
            case DDF_LIST:
                return (double) ((List<?>) value).size();
            default:
                break;
        }
        
        return null;
    }

    /**
     * Get the pointer/reference value of this node, which is just an {@link Object}.
     * 
     * @return pointer/reference value or null
     */
    @Nullable public Object pointer() {
        return ispointer() ? value : null;
    }


    /**
     * Converts this node to an empty type/value.
     * 
     * <p>All children should be considered disposed of, though in Java this is
     * circumventable by means of maintaining references to them.</p>
     * 
     * @return this object
     */
    @Nonnull public DDF empty() {
        type = DDFType.DDF_EMPTY;
        value = null;
        return this;
    }

    /**
     * Converts this node to a string type/value.
     * 
     * @param val the value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF string(@Nullable final String val) {
        empty();
        value = val;
        type = DDFType.DDF_STRING;
        return this;
    }

    /**
     * Converts this node to an unsafe string type/value.
     * 
     * @param val the value to inject
     * 
     * @return this object
     */
// Checkstyle: MethodName OFF
    @Nonnull public DDF unsafe_string(@Nullable final String val) {
        empty();
        value = val;
        type = DDFType.DDF_STRING_UNSAFE;
        return this;
    }
// Checkstyle: MethodName ON

    /**
     * Converts this node to a string type/value based on the converted form of the input.
     * 
     * @param val input value
     * 
     * @return this object
     */
    @Nonnull public DDF string(final int val) {
        return string(Integer.toString(val));
    }

    /**
     * Converts this node to a string type/value based on the converted form of the input.
     * 
     * @param val input value
     * 
     * @return this object
     */
    @Nonnull public DDF string(final double val) {
        return string(Double.toString(val));
    }

    /**
     * Converts this node to an integer type/value.
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF integer(final int val) {
        empty();
        value = Integer.valueOf(val);
        type = DDFType.DDF_INT;
        return this;
    }

    /**
     * Converts this node to an integer type/value based on the converted form of the input.
     * 
     * <p>A conversion error will assign zero as the value.</p>
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF integer(@Nonnull @NotEmpty final String val) {
        empty();
        try {
            return integer(Integer.valueOf(val));
        } catch (final NumberFormatException e) {
            return integer(0);
        }
    }

    /**
     * Converts this node to an floating point type/value.
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF floating(final double val) {
        empty();
        value = Double.valueOf(val);
        type = DDFType.DDF_FLOAT;
        return this;
    }

    /**
     * Converts this node to a floating point type/value based on the converted form of the input.
     * 
     * <p>A conversion error will assign zero as the value.</p>
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF floating(@Nonnull @NotEmpty final String val) {
        empty();
        try {
            return floating(Double.valueOf(val));
        } catch (final NumberFormatException e) {
            return floating(0.0);
        }
    }

    /**
     * Converts this node to a structure.
     * 
     * @return this object
     */
    @Nonnull public DDF structure() {
        empty();
        value = new LinkedHashMap<String,DDF>();
        type = DDFType.DDF_STRUCT;
        return this;
    }

    /**
     * Converts this node to a list/array.
     * 
     * @return this object
     */
    @Nonnull public DDF list() {
        empty();
        value = new ArrayList<DDF>();
        type = DDFType.DDF_LIST;
        return this;
    }

    /**
     * Converts this node to a pointer/reference type.
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF pointer(@Nonnull final Object val) {
        empty();
        value = val;
        type = DDFType.DDF_POINTER;
        return this;
    }

    /**
     * Adds a node to the end of a struct or list and returns it.
     * 
     * <p>If this node is not a struct or list or the child is a null node, then it is returned
     * with no further action.</p>
     * 
     * <p>If this node is a struct with an existing member by the same name, the input
     * replaces that member.</p>
     * 
     * @param child the child to add
     * 
     * @return the child
     */
    @SuppressWarnings("unchecked")
    @Nonnull public DDF add(@Nonnull final DDF child) {
        if ((!isstruct() && !islist()) || this == child.parent) {
            return child;
        }

        if (isstruct()) {
            if (child.name == null) {
                return child;
            }
            getmember(child.name).destroy();
            child.remove();
            ((Map<String,DDF>) value).put(child.name, child);
        } else {
            child.remove();
            ((List<DDF>) value).add(child);
        }

        child.parent = this;
        return child;
    }

    /**
     * Adds a node to a list prior to a specified node.
     * 
     * <p>If this node is not a list, does not contain the second parameter, or
     * either parameter is a null node, then the first parameter is returned with
     * no further action.</p> 
     * 
     * @param child the child to add
     * @param before the node to insert the child before
     * 
     * @return the child
     */
    @Nonnull public DDF addbefore(@Nonnull final DDF child, @Nonnull final DDF before) {
        if (!islist() || before.parent != this) {
            return child;
        }

        child.remove();
        @SuppressWarnings("unchecked")
        final List<DDF> list = (List<DDF>) value;
        list.add(list.indexOf(before), child);
        child.parent = this;
        return child;
    }

    /**
     * Adds a node to a list after a specified node.
     * 
     * <p>If this node is not a list, does not contain the second parameter, or
     * either parameter is a null node, then the first parameter is returned with
     * no further action.</p> 
     * 
     * @param child the child to add
     * @param after the node to insert the child after
     * 
     * @return the child
     */
    @Nonnull public DDF addafter(@Nonnull final DDF child, @Nonnull final DDF after) {
        if (!islist() || after.parent != this) {
            return child;
        }

        child.remove();
        
        @SuppressWarnings("unchecked")
        final List<DDF> list = (List<DDF>) value;
        
        final int i = list.indexOf(after);
        if (i == list.size() - 1) {
            list.add(child);
        } else {
            list.add(i + 1, child);
        }
        child.parent = this;
        
        return child;
    }
    
    /**
     * Isolate this object from its surrounding nodes and return it.
     * 
     * @return this object
     */
    @SuppressWarnings("unchecked")
    @Nonnull public DDF remove() {
        if (parent != null) {
            if (parent.isstruct()) {
                ((Map<String,DDF>) parent.value).remove(name);
            } else {
                ((List<DDF>) parent.value).remove(this);
            }
            
            parent = null;
        }
        return this;
    }
    
    /**
     * Get the parent node.
     * 
     * @return parent node
     */
    @Nullable public DDF parent() {
        return parent;
    }
    
    /**
     * Expose an immutable map representing a structure or list node.
     * 
     * @return immutable map, or null
     */
    @SuppressWarnings("unchecked")
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String,DDF> asMap() {
        if (isstruct()) {
            return Map.copyOf((Map<String,DDF>) value);
        } else if (islist()) {
            return ((List<DDF>) value).stream().collect(
                    Collectors.toUnmodifiableMap(DDF::name, Function.identity()));
        }
        
        return Collections.emptyMap();
    }

    /**
     * Expose an immutable list representing a structure or list node.
     * 
     * @return immutable list, or null
     */
    @SuppressWarnings("unchecked")
    @Nonnull @NonnullElements @Unmodifiable @NotLive public List<DDF> asList() {
        if (isstruct()) {
            return List.copyOf(((Map<String,DDF>) value).values());
        } else if (islist()) {
            return List.copyOf((List<DDF>) value);
        }
        
        return Collections.emptyList();
    }

    /**
     * Adds a new empty node to a structure, possibly creating nested structures based
     * on dotted path notation (existing nodes matching the path segments are not altered
     * other than to convert them to structures).
     * 
     * <p>The input path MUST contain at least one non-empty path segment.</p>
     * 
     * <p>This node will be converted to a structure if not already one.</p>
     * 
     * @param path dotted path to use
     * 
     * @return the last node added to the nested tree
     */
    @Nonnull public DDF addmember(@Nonnull @NotEmpty final String path) {
        final String[] tokens = Constraint.isNotEmpty(path, "Path cannot be null").split("\\.");
        Constraint.isNotEmpty(tokens, "Path did not produce an array of path segments");
        
        DDF base = this;
        for (final String segment : tokens) {
            if (!base.isstruct()) {
                base.structure();
            }
            
            DDF node = base.getmember(segment);
            if (node == null) {
                node = base.add(new DDF(segment));
            }
            
            base = node;
        }
        
        return base;
    }

    /**
     * Access a (possibly nested) structure member via dotted path notation, also allowing access to
     * list elements via "[n]" array notation.
     * 
     * <p>Failure to navigate the tree at any point will cause a null to be returned.
     * 
     * @param path dotted path to use
     * 
     * @return the matching node, or null
     */
// Checkstyle: CyclomaticComplexity OFF
    @SuppressWarnings("unchecked")
    @Nullable public DDF getmember(@Nonnull @NotEmpty final String path) {
        final String[] tokens = path.split("\\.");
        if (tokens == null || tokens.length == 0) {
            return null;
        }

        DDF current = this;

        for (int i = 0; i < tokens.length;) {
            if (tokens[i].startsWith("[") && tokens[i].endsWith("]")) {
                // Attempt to access a list entry via [n] notation and advance the path.
                int index;
                try {
                    index = Integer.valueOf(tokens[i].substring(1, tokens[i].length() - 1));
                } catch(final NumberFormatException e) {
                    index = 0;
                }
                if (islist() && index < ((List<DDF>) current.value).size()) {
                    current = ((List<DDF>) current.value).get(index);
                } else {
                    return null;
                }
                i++;
            } else if (current.isstruct()) {
                // Access the named element and advance the path.
                current = ((Map<String,DDF>) current.value).get(tokens[i]);
                if (current == null) {
                    return null;
                }
                i++;
            } else if (current.islist()) {
                // Access first element of list, don't advance the path.
                current = ((List<DDF>) current.value).get(0);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        
        return current;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final DDF other = (DDF) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        if (type != other.type) {
            return false;
        }
        
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        
        return true;
    }
// Checkstyle: CyclomaticComplexity ON
        
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

}