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

package net.shibboleth.utilities.java.support.xml;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Set of helper methods for working with DOM namespaces. */
public final class NamespaceSupport {

    /** Constructor. */
    private NamespaceSupport() {
    }

    /**
     * Adds a namespace declaration (xmlns:) attribute to the given element.
     * 
     * @param element the element to add the attribute to
     * @param namespaceURI the URI of the namespace.  Cannot be null or empty (semantics would be undefined)
     * @param prefix the prefix for the namespace. If this is null this is the default namespace being added.
     */
    public static void appendNamespaceDeclaration(@Nonnull final Element element, @Nonnull final String namespaceURI,
            @Nullable final String prefix) {
        Constraint.isNotNull(element, "Element may not be null");

        final String nsURI = StringSupport.trimOrNull(namespaceURI);
        final String nsPrefix = StringSupport.trimOrNull(prefix);
        
        Constraint.isNotNull(nsURI, "namespace may not be null or empty");

        final String attributeName;
        if (nsPrefix == null) {
            if (null == element.getPrefix()) {
                
                if (!namespaceURI.equals(element.getNamespaceURI())) {
                    //
                    // We cannot change this so complain.
                    //
                    throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                            "Cannot replace an element's default namespace");
                }
            }
            attributeName = XMLConstants.XMLNS_PREFIX;
        } else {
            if (nsPrefix.equals(element.getPrefix()) && !namespaceURI.equals(element.getNamespaceURI())) {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                        "Cannot replace an element's default namespace");
            }
            attributeName = XMLConstants.XMLNS_PREFIX + ":" + nsPrefix;
        }

        element.setAttributeNS(XMLConstants.XMLNS_NS, attributeName, nsURI);
    }

    /**
     * Looks up the namespace URI associated with the given prefix starting at the given element. This method differs
     * from {@link Node#lookupNamespaceURI(java.lang.String)} in that it only returns those namespaces actually declared
     * by an xmlns attribute. The Node method also checks the namespace a particular node was created in by
     * way of a call like {@link org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)} even if the
     * resulting element doesn't have a namespace declaration attribute.
     * 
     * @param startingElement the starting element
     * @param stoppingElement the ancestor of the starting element that serves as the upper-bound, inclusive, for the
     *            search
     * @param prefix the prefix to look up. If null then the default namespace is sought and returned.
     * 
     * @return the namespace URI for the given prefix or null
     */
// Checkstyle: CyclomaticComplexity OFF    
    @Nullable public static String lookupNamespaceURI(@Nullable final Element startingElement,
            @Nullable final Element stoppingElement, @Nullable final String prefix) {
        
        if (startingElement == null) {
            return null;
        }

        // This code is a modified version of the lookup code within Xerces
        if (startingElement.hasAttributes()) {
            final NamedNodeMap map = startingElement.getAttributes();
            final int length = map.getLength();
            Node attr;
            String value;
            for (int i = 0; i < length; i++) {
                attr = map.item(i);
                value = attr.getNodeValue();
                if (Objects.equals(attr.getNamespaceURI(), XMLConstants.XMLNS_NS)) {
                    // at this point we are dealing with DOM Level 2 nodes only
                    if (Objects.equals(attr.getLocalName(), XMLConstants.XMLNS_PREFIX) && prefix == null) {
                        // default namespace
                        return value;
                    } else if (Objects.equals(attr.getPrefix(), XMLConstants.XMLNS_PREFIX)
                            && Objects.equals(attr.getLocalName(), prefix)) {
                        // non default namespace
                        return value;
                    }
                }
            }
        }

        if (startingElement != stoppingElement) {
            final Element ancestor = ElementSupport.getElementAncestor(startingElement);
            if (ancestor != null) {
                return lookupNamespaceURI(ancestor, stoppingElement, prefix);
            }
        }

        return null;
    }
// Checkstyle: CyclomaticComplexity ON
    
    /**
     * Looks up the namespace prefix associated with the given URI starting at the given element. This method differs
     * from {@link Node#lookupPrefix(java.lang.String)} in that only those namespaces declared by an xmlns attribute
     * are inspected. The Node method also checks the namespace a particular node was created in by way of a
     * call like {@link org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)} even if the resulting
     * element doesn't have a namespace delcaration attribute.
     * <p>
     * <em>Note:</em>The prefix returned may not necessarily refer to the corresponding namespace within this element
     * (if, for instance the prefix is associated with different namespaces at different points of the hierarchy.
     * 
     * @param startingElement the starting element
     * @param stoppingElement the ancestor of the starting element that serves as the upper-bound, inclusive, for the
     *            search
     * @param namespaceURI the uri to look up
     * 
     * @return the prefix for the given namespace URI or null if non exists or the the URI is for the default namespace.
     */
    @Nullable public static String lookupPrefix(@Nonnull final Element startingElement,
            @Nullable final Element stoppingElement, @Nullable final String namespaceURI) {
        Constraint.isNotNull(startingElement, "Starting element may not be null");

        if (null == namespaceURI) {
            return null;
        }

        // This code is a modified version of the lookup code within Xerces
        if (startingElement.hasAttributes()) {
            final NamedNodeMap map = startingElement.getAttributes();
            final int length = map.getLength();
            Node attr;
            String localName;
            String foundNamespace;
            for (int i = 0; i < length; i++) {
                attr = map.item(i);
                if (Objects.equals(attr.getNamespaceURI(), XMLConstants.XMLNS_NS)) {
                    // DOM Level 2 nodes
                    if ((Objects.equals(attr.getPrefix(), XMLConstants.XMLNS_PREFIX))
                            && Objects.equals(attr.getNodeValue(), namespaceURI)) {

                        localName = attr.getLocalName();
                        foundNamespace = startingElement.lookupNamespaceURI(localName);
                        if (Objects.equals(foundNamespace, namespaceURI)) {
                            return localName;
                        }
                    }

                }
            }
        }

        if (startingElement != stoppingElement) {
            final Element ancestor = ElementSupport.getElementAncestor(startingElement);
            if (ancestor != null) {
                return lookupPrefix(ancestor, stoppingElement, namespaceURI);
            }
        }

        return null;
    }

    /**
     * Ensures that all the visibly used namespaces referenced by the given Element or its descendants are declared by
     * the given Element or one of its descendants.
     * 
     * <strong>NOTE:</strong> This is a very costly operation.
     * 
     * @param domElement the element to act as the root of the namespace declarations
     */
    public static void rootNamespaces(@Nullable final Element domElement) {
        rootNamespaces(domElement, domElement);
    }

    /**
     * Recursively called function that ensures all the visibly used namespaces referenced by the given Element or its
     * descendants are declared if they don't appear in the list of already resolved namespaces.
     * 
     * @param domElement the Element
     * @param upperNamespaceSearchBound the "root" element of the fragment where namespaces may be rooted
     */
    //CheckStyle: MethodLength OFF
    //CheckStyle: CyclomaticComplexity OFF
    private static void rootNamespaces(@Nullable final Element domElement,
            @Nullable final Element upperNamespaceSearchBound) {
        if (domElement == null) {
            return;
        }

        String namespaceURI = null;
        String namespacePrefix = domElement.getPrefix();

        // Check if the namespace for this element is already declared on this element
        boolean nsDeclaredOnElement = false;
        if (namespacePrefix == null) {
            nsDeclaredOnElement = domElement.hasAttributeNS(null, XMLConstants.XMLNS_PREFIX);
        } else {
            nsDeclaredOnElement = domElement.hasAttributeNS(XMLConstants.XMLNS_NS, namespacePrefix);
        }

        if (!nsDeclaredOnElement) {
            // Namespace for element was not declared on the element itself, see if the namespace is declared on
            // an ancestral element within the subtree where namespaces much be rooted
            namespaceURI = lookupNamespaceURI(domElement, upperNamespaceSearchBound, namespacePrefix);

            if (namespaceURI == null) {
                // Namespace for the element is not declared on any ancestral nodes within the subtree where namespaces
                // must be rooted. Resolve the namespace from ancestors outside that subtree.
                namespaceURI = lookupNamespaceURI(upperNamespaceSearchBound, null, namespacePrefix);
                if (namespaceURI != null) {
                    // Namespace resolved outside the subtree where namespaces must be declared so declare the namespace
                    // on this element (within the subtree).
                    appendNamespaceDeclaration(domElement, namespaceURI, namespacePrefix);
                } else {
                    // Namespace couldn't be resolved from any ancestor. If the namespace prefix is null then the
                    // element is simply in the undeclared default document namespace, which is fine. If it isn't null
                    // then a namespace prefix, that hasn't properly been declared, is being used.
                    if (namespacePrefix != null) {
                        throw new DOMException(DOMException.NAMESPACE_ERR, "Unable to resolve namespace prefix "
                                + namespacePrefix + " found on element " + QNameSupport.getNodeQName(domElement));
                    }
                }
            }
        }

        // Make sure all the attribute URIs are rooted here or have been rooted in an ancestor
        final NamedNodeMap attributes = domElement.getAttributes();
        Node attributeNode;
        for (int i = 0; i < attributes.getLength(); i++) {
            namespacePrefix = null;
            namespaceURI = null;
            attributeNode = attributes.item(i);

            // Shouldn't need this check, but just to be safe, we have it
            if (attributeNode.getNodeType() != Node.ATTRIBUTE_NODE) {
                continue;
            }

            namespacePrefix = StringSupport.trimOrNull(attributeNode.getPrefix());
            if (namespacePrefix != null) {
                // If it's the "xmlns" prefix then it is the namespace declaration,
                // don't try to look it up and redeclare it
                if (namespacePrefix.equals(XMLConstants.XMLNS_PREFIX)
                        || namespacePrefix.equals(XMLConstants.XML_PREFIX)) {
                    continue;
                }

                // check to see if the namespace for the prefix has already been defined within the XML fragment
                namespaceURI = lookupNamespaceURI(domElement, upperNamespaceSearchBound, namespacePrefix);
                if (namespaceURI == null) {
                    namespaceURI = lookupNamespaceURI(upperNamespaceSearchBound, null, namespacePrefix);
                    if (namespaceURI == null) {
                        throw new DOMException(DOMException.NAMESPACE_ERR, "Unable to resolve namespace prefix "
                                + namespacePrefix + " found on attribute " + QNameSupport.getNodeQName(attributeNode)
                                + " found on element " + QNameSupport.getNodeQName(domElement));
                    }

                    appendNamespaceDeclaration(domElement, namespaceURI, namespacePrefix);
                }
            }
        }

        // Now for the child elements, we pass a copy of the resolved namespace list in order to
        // maintain proper scoping of namespaces.
        Element childNode = ElementSupport.getFirstChildElement(domElement);
        while (childNode != null) {
            rootNamespaces(childNode, upperNamespaceSearchBound);
            childNode = ElementSupport.getNextSiblingElement(childNode);
        }
    }
    //CheckStyle: MethodLength ON
    //CheckStyle: CyclomaticComplexity ON
}