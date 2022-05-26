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

import javax.annotation.Nonnull;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * Helper methods for {@link DDF} usage.
 * 
 * @since 9.0.0
 */
public final class DDFSupport {

    /** Name of child element member created by {@link #fromElement(Element)} method. */
    @Nonnull @NotEmpty static final String CHILD_ELEMENTS_MEMBER = "_children";

    /** Name of content member created by {@link #fromElement(Element)} method. */
    @Nonnull @NotEmpty static final String CONTENT_MEMBER = "_content";

    /** Private constructor. */
    private DDFSupport() {
        
    }

    /**
     * Converts a DOM tree rooted at the input element into a {@link DDF} mirroring the tree.
     *
     * <p>The name of the object is the local name of the element. 
     * 
     * <p>Attributes are converted into named structure members based on the local names.</p>
     * 
     * <p>Child elements are recursively processed into a list named {@link #CHILD_ELEMENTS_MEMBER}.</p>
     * 
     * <p>Text content is stored in a structure member named {@link #CONTENT_MEMBER}.</p>
     * 
     * <p>Namespaces are ignored.</p>
     * 
     * @param element input element
     * 
     * @return the converted object
     */
    static @Nonnull DDF fromElement(@Nonnull final Element element) {
        
        Constraint.isNotNull(element, "Element cannot be null");
        
        // Named after element.
        final DDF obj = new DDF(element.getLocalName());
        
        // Each attribute is added as a string member.
        final NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); ++i) {
                final Attr attr = (Attr) attrs.item(i);
                obj.addmember(attr.getLocalName()).string(attr.getValue());
            }
        }

        DDF children = null;
        
        // Recursively convert each child to a child DDF and add it to a list. 
        Element child = ElementSupport.getFirstChildElement(element);
        while (child != null) {
            if (children == null) {
                children = obj.addmember(CHILD_ELEMENTS_MEMBER).list();
            }
            children.add(fromElement(child));
            child = ElementSupport.getNextSiblingElement(child);
        }
        
        final String content = ElementSupport.getElementContentAsString(element);
        if (content != null && !content.isBlank()) {
            obj.addmember(CONTENT_MEMBER).string(content);
        }
        
        return obj;
    }
    
}