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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

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
public class DDF implements Iterable<DDF> {

    /** Name of node. */
    @Nullable private String name;
    
    /** Parent node. */
    @Nullable private DDF parent;

    /** Type enum. */
    public enum DDFType {

        /** A null node. */
        DDF_NULL(-1),

        /** An empty node with no value. */
        DDF_EMPTY(0),
        
        /** A string value. */
        DDF_STRING(1),
        
        /** An integral value of no more than 32-bits. */
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
        DDF_STRING_UNSAFE(7),
        
        /** An integral value of no more than 64-bits. */
        DDF_LONG(8);
        
        /** Type value. */
        private final int value;
        
        /**
         * Constructor.
         * 
         * @param val value of the type enum
         */
        private DDFType(final int val) {
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
        
        /**
         * Convert an integer into the corresponding enum value.
         * 
         * @param val input type
         * 
         * @return enum constant
         * 
         * @throws IllegalArgumentException if the type is out of range
         */
// Checkstyle: CyclomaticComplexity OFF
        public static DDFType valueOf(final int val) throws IllegalArgumentException {
            final DDFType type;
            switch (val) {
                case -1:
                    type = DDF_NULL;
                    break;
                        
                case 0:
                    type = DDF_EMPTY;
                    break;
                        
                case 1:
                    type = DDF_STRING;
                    break;
                        
                case 2:
                    type = DDF_INT;
                    break;

                case 3:
                    type = DDF_FLOAT;
                    break;
                    
                case 4:
                    type = DDF_STRUCT;
                    break;
                    
                case 5:
                    type = DDF_LIST;
                    break;

                case 6:
                    type = DDF_POINTER;
                    break;
                    
                case 7:
                    type = DDF_STRING_UNSAFE;
                    break;
                
                case 8:
                    type = DDF_LONG;
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unrecognized DDF type");
            }
            return type;
        }
        
    };
// Checkstyle: CyclomaticComplexity ON
    
    /** Node type. */
    @Nonnull private DDFType type;
    
    /** Reference to the value, which depends on the type. */
    @Nullable private Object value;
    
    /** Constructor. */
    public DDF() {
        type = DDFType.DDF_NULL;
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
     *
     * @param n node name
     */
    public DDF(@Nullable @NotEmpty final String n) {
        type = DDFType.DDF_EMPTY;
        name(n);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
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
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
     *
     * @param n node name
     * @param val byte array value, handled without knowledge of the encoding
     */
    public DDF(@Nullable @NotEmpty final String n, @Nullable final byte[] val) {
        this(n);
        unsafe_string(val);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
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
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
     *
     * @param n node name
     * @param val long integer value
     */
    public DDF(@Nullable @NotEmpty final String n, final long val) {
        this(n);
        longinteger(val);
    }

    /**
     * Constructor.
     *
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
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
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
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
        type = DDFType.DDF_NULL;
        return this;
    }
    
    /**
     * Performs a deep copy of the node and all children, if any.
     * 
     * @return the copy
     */
// Checkstyle: CyclomaticComplexity OFF
    @SuppressWarnings("unchecked")
    @Nonnull DDF copy() {
        final DDF dup = new DDF(name);
        
        switch (type) {
            case DDF_NULL:
                dup.destroy();
                break;
                
            case DDF_EMPTY:
                break;
                
            case DDF_STRING:
                dup.string((String) value);
                break;
                
            case DDF_STRING_UNSAFE:
                dup.unsafe_string((byte[]) value);
                break;
                
            case DDF_INT:
                dup.integer((Integer) value);
                break;

            case DDF_LONG:
                dup.longinteger((Long) value);
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
// Checkstyle: CyclomaticComplexity ON
    
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
     * <p>For compatibility, the name is constrained to no more than 255 characters.</p>
     * 
     * <p>The name will not be set if the node is already a child of a structure.</p>
     * 
     * @param n the new name
     * 
     * @return this object
     */
    @Nonnull public DDF name(@Nullable @NotEmpty final String n) {
        if (!isnull() && (parent == null || !parent.isstruct())) {
            if (n != null) {
                name = Constraint.isNotEmpty(n.substring(0,Integer.min(n.length(), 255)), "Name cannot be empty");
            } else {
                name = null;
            }
        }
        return this;
    }
    
    /**
     * Returns true iff the node is null.
     * 
     * @return true iff the node is null
     */
    public boolean isnull() {
        return type == DDFType.DDF_NULL;
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
     * Returns true iff the node is a string.
     * 
     * @return true iff the node is a string
     */
    public boolean isstring() {
        return type == DDFType.DDF_STRING;
    }

    /**
     * Returns true iff the node is an unsafe string.
     * 
     * @return true iff the node is an unsafe string
     */
    public boolean isunsafestring() {
        return type == DDFType.DDF_STRING_UNSAFE;
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
     * Returns true iff the node is a long integer.
     * 
     * @return true iff the node is a long integer
     */
    public boolean islong() {
        return type == DDFType.DDF_LONG;
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
     * Get the byte array value of this node if an unsafe string.
     * 
     * @return the byte array value or null
     */
// Checkstyle: MethodName OFF
    @Nullable public byte[] unsafe_string() {
        return isunsafestring() ? (byte[]) value : null;
    }
// Checkstyle: MethodName ON

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
            case DDF_LONG:
                return ((Long) value).intValue();
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
     * Get the long integer value of this node.
     * 
     * <p>Longs are coerced from other types based on numeric conversions
     * or the count of a structure or list.</p> 
     * 
     * @return the long integer value or null
     */
    @Nullable public Long longinteger() {
        
        switch(type) {
            case DDF_INT:
                return ((Integer) value).longValue();
            case DDF_LONG:
                return (Long) value;
            case DDF_FLOAT:
                return ((Double) value).longValue();
            case DDF_STRING:
                try {
                    return Long.valueOf((String) value);
                } catch (final NumberFormatException e) {
                    // Swallow.
                    return null;
                }
            case DDF_STRUCT:
                return (long) ((Map<?,?>) value).size();
            case DDF_LIST:
                return (long) ((List<?>) value).size();
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
            case DDF_LONG:
                return ((Long) value).doubleValue();
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
    @Nonnull public DDF unsafe_string(@Nullable final byte[] val) {
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
    @Nonnull public DDF string(final long val) {
        return string(Long.toString(val));
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
     * Converts this node to a long integer type/value.
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF longinteger(final long val) {
        empty();
        value = Long.valueOf(val);
        type = DDFType.DDF_LONG;
        return this;
    }

    /**
     * Converts this node to a long integer type/value based on the converted form of the input.
     * 
     * <p>A conversion error will assign zero as the value.</p>
     * 
     * @param val value to inject
     * 
     * @return this object
     */
    @Nonnull public DDF longinteger(@Nonnull @NotEmpty final String val) {
        empty();
        try {
            return longinteger(Long.valueOf(val));
        } catch (final NumberFormatException e) {
            return longinteger(0);
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
        if ((!isstruct() && !islist()) || child.isnull() || this == child.parent) {
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
        if (!islist() || child.isnull() || before.parent != this) {
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
        if (!islist() || child.isnull() || after.parent != this) {
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
     * Expose an immutable map representing a structure node.
     * 
     * @return immutable map, or null if the node is not a structure
     */
    @SuppressWarnings("unchecked")
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String,DDF> asMap() {
        if (isstruct()) {
            return Map.copyOf((Map<String,DDF>) value);
        }
        
        return Collections.emptyMap();
    }

    /**
     * Expose an immutable list representing a structure or list node.
     * 
     * @return immutable list, or null if the node is not a structure or list
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
     * @return the last node added to the nested tree, or a null node if unable to do so
     */
    @Nonnull public DDF addmember(@Nonnull @NotEmpty final String path) {
        final String[] tokens = Constraint.isNotEmpty(path, "Path cannot be null").split("\\.");
        Constraint.isNotEmpty(tokens, "Path did not produce an array of path segments");
        
        if (!isnull()) {
            DDF base = this;
            for (final String segment : tokens) {
                if (!base.isstruct()) {
                    base.structure();
                }
                
                DDF node = base.getmember(segment);
                if (node.isnull()) {
                    node = base.add(new DDF(segment));
                }
                
                base = node;
            }
            
            return base;
        }
        
        return new DDF();
    }

    /**
     * Access a (possibly nested) structure member via dotted path notation, also allowing access to
     * list elements via "[n]" array notation.
     * 
     * <p>Failure to navigate the tree at any point will cause a null node to be returned.</p>
     * 
     * @param path dotted path to use
     * 
     * @return the matching node, or a null node
     */
// Checkstyle: CyclomaticComplexity OFF
    @SuppressWarnings("unchecked")
    @Nonnull public DDF getmember(@Nonnull @NotEmpty final String path) {
        final String[] tokens = path.split("\\.");
        if (tokens == null || tokens.length == 0 || isnull()) {
            return new DDF();
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
                if (current.islist() && index < ((List<DDF>) current.value).size()) {
                    current = ((List<DDF>) current.value).get(index);
                } else {
                    return new DDF();
                }
                i++;
            } else if (current.isstruct()) {
                // Access the named element and advance the path.
                current = ((Map<String,DDF>) current.value).get(tokens[i]);
                if (current == null) {
                    return new DDF();
                }
                i++;
            } else if (current.islist()) {
                // Access first element of list, don't advance the path.
                current = ((List<DDF>) current.value).get(0);
                if (current == null) {
                    return new DDF();
                }
            } else {
                return new DDF();
            }
        }
        
        return current;
    }

    /** {@inheritDoc} */
    @Nonnull public Iterator<DDF> iterator() {
        final List<DDF> list = asList();
        if (list != null) {
            return list.iterator();
        }
        return Collections.emptyListIterator();
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

    /** 
     * {@inheritDoc}
     * 
     * <p>The string output is for debugging purposes and should not be used when serializing.</p>
     */
    @Override
    @Nonnull public String toString() {
        return dump(new StringBuilder(), 0).toString();
    }

// Checkstyle: MethodLength|CyclomaticComplexity OFF
    /**
     * Helper method to dump to a string for debugging.
     * 
     * @param builder string builder to use
     * @param indent size of indent
     * 
     * @return the first parameter
     */
    @Nonnull private StringBuilder dump(@Nonnull final StringBuilder builder, final long indent) {
        
        for (long i = 0; i < indent; ++i) {
            builder.append(' ');
        }
        
        switch (type) {

            case DDF_NULL:
                builder.append("null");
                break;
                
            case DDF_EMPTY:
                builder.append("empty");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                break;

            case DDF_STRING:
                builder.append("String");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ");
                if (value != null) {
                    builder.append('"').append(((String) value).replace("\"", "\\\"")).append('"');
                } else {
                    builder.append("null");
                }
                break;
                
            case DDF_STRING_UNSAFE:
                builder.append("byte[]");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ");
                if (value != null) {
                    builder.append('{');
                    for (final byte b : (byte[]) value) {
                        builder.append(Integer.toHexString(b)).append(", ");
                    }
                    builder.append('}');
                } else {
                    builder.append("null");
                }
                break;

            case DDF_INT:
                builder.append("Integer");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ").append(value);
                break;

            case DDF_LONG:
                builder.append("Long");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ").append(value);
                break;

            case DDF_FLOAT:
                builder.append("Double");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ").append(value);
                break;

            case DDF_STRUCT:
                builder.append("struct");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = {");
                if (!((Map<?,?>) value).isEmpty()) {
                    builder.append('\n');
                    for (final DDF child : this) {
                        child.dump(builder, indent + 2);
                    }
                }
                for (long i = 0; i < indent; ++i) {
                    builder.append(' ');
                }
                builder.append('}');
                break;

            case DDF_LIST:
                builder.append("DDF[").append(((List<?>) value).size()).append(']');
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = {");

                if (!((List<?>) value).isEmpty()) {
                    builder.append('\n');
                    for (final DDF child : this) {
                        child.dump(builder, indent + 2);
                    }
                }
                for (long i = 0; i < indent; ++i) {
                    builder.append(' ');
                }
                builder.append('}');
                break;

            case DDF_POINTER:
                builder.append("Object");
                if (name != null) {
                    builder.append(' ').append(name);
                }
                builder.append(" = ");
                if (value != null) {
                    builder.append(value);
                } else {
                    builder.append("null");
                }
                break;

            default:
                builder.append("UNKNOWN -- WARNING: ILLEGAL VALUE");
        }
        builder.append(";\n");
        
        return builder;
    }
    
    /**
     * Serialize this object to a provided stream.
     * 
     * @param os output stream
     *
     * @return the output stream
     * 
     * @throws IOException if an error occurs
     */
    @Nonnull public OutputStream serialize(@Nonnull final OutputStream os) throws IOException {
        if (!isnull()) {
            if (name != null) {
                encode(os, name.getBytes("UTF8"));
            } else {
                os.write('.');
            }
            os.write(' ');

            switch (type) {
                case DDF_EMPTY:
                case DDF_POINTER:
                    os.write(Integer.toString(DDFType.DDF_EMPTY.getValue()).getBytes("UTF8"));
                    os.write('\n');
                    break;

                case DDF_STRING:
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    if (value != null) {
                        os.write(' ');
                        encode(os, ((String) value).getBytes("UTF-8"));
                    }
                    os.write('\n');
                    break;

                case DDF_STRING_UNSAFE:
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    if (value != null) {
                        os.write(' ');
                        encode(os, (byte[]) value);
                    }
                    os.write('\n');
                    break;

                case DDF_INT:
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    os.write(' ');
                    os.write(Integer.toString((Integer) value).getBytes("UTF8"));
                    os.write('\n');
                    break;

                case DDF_LONG:
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    os.write(' ');
                    os.write(Long.toString((Long) value).getBytes("UTF8"));
                    os.write('\n');
                    break;

                case DDF_FLOAT:
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    os.write(' ');
                    os.write(Double.toString((Double) value).getBytes("UTF8"));
                    os.write('\n');
                    break;

                case DDF_STRUCT:
                    @SuppressWarnings("unchecked")
                    final Collection<DDF> members = ((Map<String,DDF>) value).values();
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    os.write(' ');
                    os.write(Integer.toString(members.size()).getBytes("UTF8"));
                    os.write('\n');
                    for (final DDF child : members) {
                        child.serialize(os);
                    }
                    break;

                case DDF_LIST:
                    @SuppressWarnings("unchecked")
                    final Collection<DDF> children = (List<DDF>) value;
                    os.write(Integer.toString(type.getValue()).getBytes("UTF8"));
                    os.write(' ');
                    os.write(Integer.toString(children.size()).getBytes("UTF8"));
                    os.write('\n');
                    for (final DDF child : children) {
                        child.serialize(os);
                    }
                    break;

                default:
                    break;
            }
        }
        
        return os;
    }
    
    /**
     * Parses a seralized DDF from an input stream.
     * 
     * @param is input stream
     * 
     * @return the parsed object
     * 
     * @throws IOException if an error occurs
     */
// Checkstyle: ReturnCount OFF
    @Nonnull public static DDF deserialize(@Nonnull final InputStream is) throws IOException {
        
        int ch;
        final StringBuilder nameBuilder = new StringBuilder();

        // First field is the name.
        while ((ch = is.read()) != -1 && !Character.isWhitespace(ch)) {
            if (ch >= 0 && ch <= 127) {
                // The int is a code point from 0..255, but our grammar constrains this to 0..127 so
                // this is a safe append, to promote the ASCII into Unicode.
                nameBuilder.appendCodePoint(ch);
            } else {
                throw new IOException("Invalid code point outside US-ASCII range");
            }
        }
        
        if (ch != 0x20) {
            // Name has to be followed by a space.
            // This will also cover an early line or stream termination.
            throw new IOException("Name not followed by space character");
        }
        
        final String name = nameBuilder.toString();
        if (name.isEmpty()) {
            // No name field.
            throw new IOException("Name field missing");
        }
        
        final DDF obj = new DDF(null);
        if (!".".equals(name)) {
            // The name is stipulated to be UTF-8 safe so any high order ASCII characters are
            // assumed to be part of a multi-byte sequence.
            try {
                obj.name(URLDecoder.decode(name, "UTF-8"));
            } catch (final IllegalArgumentException e) {
                throw new IOException(e);
            }
        }
        
        // Next field is the numeric type designation.
        final StringBuilder typeBuilder = new StringBuilder();
        while ((ch = is.read()) != -1 && Character.isDigit(ch)) {
            // This is safe because the byte contract of the stream disallows
            // any non-ASCII digit from satisfying the isDigit check.
            typeBuilder.appendCodePoint(ch);
        }
        
        // Before continuing, we convert the string into a DDF type.
        final DDFType type;
        try {
            type = DDFType.valueOf(Integer.valueOf(typeBuilder.toString()));
        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid DDF type");
        }

        // Process typical value types.
        final StringBuilder valueBuilder = new StringBuilder();
        switch (type) {
            case DDF_EMPTY:
            case DDF_POINTER:
                if (ch != 0x0A) {
                    throw new IOException("Empty/pointer record not terminated by linefeed");
                }
                // Nothing else to do, it's already empty.
                return obj;
            
            case DDF_STRING:
            case DDF_STRING_UNSAFE:
                if (ch == 0x0A) {
                    if (type == DDFType.DDF_STRING) {
                        return obj.string(null);
                    }
                    return obj.unsafe_string(null);
                } else if (ch != 0x20) {
                    throw new IOException("Type field not followed by space character");
                }
                
                while ((ch = is.read()) != -1 && !Character.isWhitespace(ch)) {
                    if (ch >= 0 && ch <= 127) {
                        // The int is a code point from 0..255, but our grammar constrains this to 0..127 so
                        // this is a safe append, to promote the ASCII into Unicode.
                        valueBuilder.appendCodePoint(ch);
                    } else {
                        throw new IOException("Invalid code point outside US-ASCII range");
                    }
                }
                
                if (ch != 0x0A) {
                    throw new IOException("String value not followed by linefeed");
                }
                
                try {
                    if (type == DDFType.DDF_STRING) {
                        // String values are handled as UTF-8.
                        return obj.string(URLDecoder.decode(valueBuilder.toString(), "UTF-8"));
                    }
                    
                    // Unsafe string values are processed as ISO-8859-1.
                    // They may be anything, but it will guarantee a single byte encoding.
                    return obj.unsafe_string(
                            URLDecoder.decode(valueBuilder.toString(), "ISO-8859-1").getBytes("ISO-8859-1"));
                    
                } catch (final IllegalArgumentException e) {
                    throw new IOException(e);
                }

            case DDF_INT:
            case DDF_LONG:
            case DDF_FLOAT:
                if (ch != 0x20) {
                    throw new IOException("Type field not followed by space character");
                }
                
                while ((ch = is.read()) != -1 && !Character.isWhitespace(ch)) {
                    if (ch >= 0 && ch <= 127) {
                        // The int is a code point from 0..255, but our grammar constrains this to 0..127 so
                        // this is a safe append, to promote the ASCII into Unicode.
                        valueBuilder.appendCodePoint(ch);
                    } else {
                        throw new IOException("Invalid code point outside US-ASCII range");
                    }
                }
                
                if (ch != 0x0A) {
                    throw new IOException("Numeric value not followed by linefeed");
                } else if (valueBuilder.length() == 0) {
                    throw new IOException("Numeric value missing");
                }
                
                if (type == DDFType.DDF_INT) {
                    return obj.integer(valueBuilder.toString());
                } else if (type == DDFType.DDF_LONG) {
                    return obj.longinteger(valueBuilder.toString());
                }
                return obj.floating(valueBuilder.toString());
                
            case DDF_STRUCT:
            case DDF_LIST:
                if (ch != 0x20) {
                    throw new IOException("Type field not followed by space character");
                }

                while ((ch = is.read()) != -1 && Character.isDigit(ch)) {
                    // This is safe because the byte contract of the stream disallows
                    // any non-ASCII digit from satisfying the isDigit check.
                    valueBuilder.appendCodePoint(ch);
                }
                
                if (ch != 0x0A) {
                    throw new IOException("Record count not followed by linefeed");
                } else if (valueBuilder.length() == 0) {
                    throw new IOException("Record count missing");
                }
                
                int count;
                try {
                    count = Integer.valueOf(valueBuilder.toString());
                } catch (final NumberFormatException e) {
                    throw new IOException("Invalid record count");
                }
                
                if (type == DDFType.DDF_STRUCT) {
                    obj.structure();
                } else {
                    obj.list();
                }
                
                for (; count > 0; --count) {
                    obj.add(deserialize(is));
                }
                return obj;

            default:
                throw new IOException("Unexpected record type");
        }
    }
// Checkstyle: MethodLength|CyclomaticComplexity|ReturnCount ON

    /**
     * A simple encoder for non-ASCII characters.
     * 
     * <p>Made this package-accessible for unit testing.</p>
     * 
     * @param os output stream
     * @param bytes bytes to encode
     * 
     * @throws IOException if an error occurs
     */
    static void encode(@Nonnull final OutputStream os, @Nonnull final byte[] bytes) throws IOException {
        for (final byte b : bytes) {
            final int i = Byte.toUnsignedInt(b);
            if (i < 0x30 || i > 0x7A) {
                os.write('%');
                os.write(hexchar(i >>> 4));
                os.write(hexchar(i & 0x0F));
            } else {
                os.write(b);
            }
        }
    }
    
    /**
     * Converts a byte into a hex character.
     * 
     * @param b input byte
     * 
     * @return the hex character equivalent (capitalized)
     */
    private static int hexchar(final int b) {
        // 48 is '0' and 65 is 'A'
        return (b <= 9) ? (48 + b) : (65 + b - 10);
    }
    
}