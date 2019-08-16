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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/** Set of helper methods for working with DOM Elements. */
public final class ElementSupport {

    /** Constructor. */
    private ElementSupport() {
    }

    /**
     * Adopts an element into a document if the child is not already in the document.
     * 
     * @param adoptee the element to be adopted
     * @param adopter the document into which the element is adopted
     */
    public static void adoptElement(@Nonnull final Document adopter, @Nonnull final Element adoptee) {
        Constraint.isNotNull(adoptee, "Adoptee Element may not be null");
        Constraint.isNotNull(adopter, "Adopter Element may not be null");

        if (!(adoptee.getOwnerDocument().isSameNode(adopter))) {
            if (adopter.adoptNode(adoptee) == null) {
                // This can happen if the adopter and adoptee were produced by different DOM implementations
                throw new RuntimeException("DOM Element node adoption failed. This is most likely caused by the "
                        + "Element and Document being produced by different DOM implementations.");
            }
        }
    }

    /**
     * Appends the child Element to the parent Element, adopting the child Element into the parent's Document if needed.
     * 
     * @param parentElement the parent Element
     * @param childElement the child Element
     */
    public static void appendChildElement(@Nonnull final Element parentElement, @Nullable final Element childElement) {
        if (childElement == null) {
            return;
        }

        Constraint.isNotNull(parentElement, "Parent Element may not be null");
        final Document parentDocument = parentElement.getOwnerDocument();
        if (!parentDocument.equals(childElement.getOwnerDocument())) {
            adoptElement(parentDocument, childElement);
        }

        parentElement.appendChild(childElement);
    }

    /**
     * Creates a text node with the given content and appends it as child to the given element.
     * 
     * @param element the element to receive the text node
     * @param textContent the content for the text node
     */
    public static void appendTextContent(@Nonnull final Element element, @Nullable final String textContent) {
        if (textContent == null) {
            return;
        }
        Constraint.isNotNull(element, "Element may not be null");
        final Text textNode = element.getOwnerDocument().createTextNode(textContent);
        element.appendChild(textNode);
    }

    /**
     * Constructs an element, rooted in the given document, with the given name.
     * 
     * @param document the document containing the element
     * @param elementName the name of the element, must contain a local name, may contain a namespace URI and prefix
     * 
     * @return the element
     */
    public static Element constructElement(@Nonnull final Document document, @Nonnull final QName elementName) {
        Constraint.isNotNull(elementName, "Element name can not be null");
        return constructElement(document, elementName.getNamespaceURI(), elementName.getLocalPart(),
                elementName.getPrefix());
    }

    /**
     * Constructs an element, rooted in the given document, with the given information.
     * 
     * @param document the document containing the element
     * @param namespaceURI the URI of the namespace the element is in
     * @param localName the element's local name
     * @param prefix the prefix of the namespace the element is in
     * 
     * @return the element
     */
    public static Element constructElement(@Nonnull final Document document, @Nullable final String namespaceURI,
            @Nonnull final String localName, @Nullable final String prefix) {
        Constraint.isNotNull(document, "Document may not be null");

        final String trimmedLocalName =
                Constraint
                        .isNotNull(StringSupport.trimOrNull(localName), "Element local name may not be null or empty");

        final String qualifiedName;
        final String trimmedPrefix = StringSupport.trimOrNull(prefix);
        if (trimmedPrefix != null) {
            qualifiedName = trimmedPrefix + ":" + StringSupport.trimOrNull(trimmedLocalName);
        } else {
            qualifiedName = StringSupport.trimOrNull(trimmedLocalName);
        }

        return document.createElementNS(StringSupport.trimOrNull(namespaceURI), qualifiedName);
    }

    /**
     * Gets the child elements of the given element.
     * 
     * @param root element to get the child elements of
     * 
     * @return list of child elements or an empty list if the root is null.
     */
    @Nonnull public static List<Element> getChildElements(@Nullable final Node root) {

        if (root == null) {
            return Collections.emptyList();
        }

        final ArrayList<Element> children = new ArrayList<>();

        Element childNode = getFirstChildElement(root);
        while (childNode != null) {
            children.add(childNode);
            childNode = getNextSiblingElement(childNode);
        }

        return children;
    }

    /**
     * Gets the child nodes with the given local tag name. If you need to retrieve multiple, named, children consider
     * using {@link #getChildElements(Node)}.
     * 
     * @param root element to retrieve the children from
     * @param name name of the child elements to be retrieved
     * 
     * @return list of child elements, never null
     */
    @Nonnull public static List<Element> getChildElements(@Nullable final Node root, @Nullable final QName name) {
        if (root == null || name == null) {
            return Collections.emptyList();
        }

        return getChildElementsByTagNameNS(root, name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Returns the first child element of the supplied element with the supplied name if it exists. Otherwise null. See
     * {@link #getChildElements(Node, QName)}.
     * 
     * @param root element to parse child elements
     * @param name name of the child elements to parse
     * 
     * @return first child element or null
     */
    @Nullable public static Element getFirstChildElement(@Nullable final Node root, @Nullable final QName name) {
        final List<Element> elements = ElementSupport.getChildElements(root, name);
        if (elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    /**
     * Gets the child nodes with the given local tag name. If you need to retrieve multiple, named, children consider
     * using {@link #getChildElements(Node)}.
     * 
     * @param root element to retrieve the children from
     * @param localName local, tag, name of the child element
     * 
     * @return list of child elements, never null
     */
    @Nonnull public static List<Element> getChildElementsByTagName(@Nullable final Node root,
            @Nullable final String localName) {
        if (root == null) {
            return Collections.emptyList();
        }

        final ArrayList<Element> children = new ArrayList<>();

        Element childNode = getFirstChildElement(root);
        while (childNode != null) {
            if (Objects.equals(childNode.getLocalName(), localName)) {
                children.add(childNode);
            }
            childNode = getNextSiblingElement(childNode);
        }

        return children;
    }

    /**
     * Gets the child nodes with the given namespace qualified tag name. If you need to retrieve multiple, named,
     * children consider using {@link #getChildElements(Node)}.
     * 
     * @param root element to retrieve the children from
     * @param namespaceURI namespace URI of the child element
     * @param localName local, tag, name of the child element
     * 
     * @return list of child elements, never null
     */
    @Nonnull public static List<Element> getChildElementsByTagNameNS(@Nullable final Node root,
            @Nullable final String namespaceURI, @Nullable final String localName) {
        if (root == null || localName == null) {
            return Collections.emptyList();
        }

        final ArrayList<Element> children = new ArrayList<>();

        Element childNode = getFirstChildElement(root);
        while (childNode != null) {
            if (isElementNamed(childNode, namespaceURI, localName)) {
                children.add(childNode);
            }
            childNode = getNextSiblingElement(childNode);
        }

        return children;
    }

    /**
     * Gets the ancestor element node to the given node.
     * 
     * @param currentNode the node to retrieve the ancestor for
     * 
     * @return the ancestral element node of the current node, or null
     */
    @Nullable public static Element getElementAncestor(@Nullable final Node currentNode) {
        if (currentNode == null) {
            return null;
        }

        final Node parent = currentNode.getParentNode();
        if (parent != null) {
            if (parent.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) parent;
            }
            return getElementAncestor(parent);
        }
        return null;
    }

    /**
     * Gets the text content for this Element only. Whereas {@link Node#getTextContent()} will return all text for this
     * element and all children, this just grabs the text for this element (which may be spread over multiple lines).
     * 
     * @param element The element to look at.
     * @return The text content, or "" if there is none, never null.
     * 
     */
    @Nonnull public static String getElementContentAsString(@Nullable final Element element) {
        if (element == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();

        Node node = element.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                builder.append(((Text) node).getNodeValue());
            }
            node = node.getNextSibling();
        }

        return builder.toString();
    }

    /**
     * Gets the value of a list-type element as a list.
     * 
     * @param element element whose value will be turned into a list
     * 
     * @return list of values, never null
     */
    @Nonnull public static List<String> getElementContentAsList(@Nullable final Element element) {
        if (element == null) {
            return Collections.emptyList();
        }
        return StringSupport.stringToList(getElementContentAsString(element), XMLConstants.LIST_DELIMITERS);
    }

    /**
     * Constructs a QName from an element's adjacent Text child nodes.
     * 
     * @param element the element with a QName value
     * 
     * @return a QName from an element's value, or null if the given element is empty
     */
    @Nullable public static QName getElementContentAsQName(@Nullable final Element element) {
        if (element == null) {
            return null;
        }
        final String elementContent = StringSupport.trimOrNull(getElementContentAsString(element));

        if (elementContent == null) {
            return null;
        }

        QName result = null;
        final String[] valueComponents = elementContent.split(":");
        if (valueComponents.length == 1) {
            result = QNameSupport.constructQName(element.lookupNamespaceURI(null), valueComponents[0], null);
        } else if (valueComponents.length == 2) {
            result =
                    QNameSupport.constructQName(element.lookupNamespaceURI(valueComponents[0]), valueComponents[1],
                            valueComponents[0]);
        }
        return result;
    }

    /**
     * Gets the first child Element of the node, skipping any Text nodes such as whitespace.
     * 
     * @param n The parent in which to search for children
     * @return The first child Element of n, or null if none
     */
    @Nullable public static Element getFirstChildElement(@Nullable final Node n) {
        if (n == null) {
            return null;
        }

        Node child = n.getFirstChild();
        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }

        return child != null ? (Element) child : null;
    }

    /**
     * Gets the child elements of the given element in a single iteration.
     * 
     * @param root element to get the child elements of
     * 
     * @return child elements indexed by namespace qualified tag name, never null
     */
    @Nonnull public static Map<QName, List<Element>> getIndexedChildElements(@Nullable final Element root) {
        if (root == null) {
            return Collections.emptyMap();
        }

        final Map<QName, List<Element>> children = new HashMap<>();

        Element e = getFirstChildElement(root);
        while (e != null) {
            final QName qname = QNameSupport.getNodeQName(e);
            List<Element> elements = children.get(qname);
            if (elements == null) {
                elements = new ArrayList<>();
                children.put(qname, elements);
            }

            elements.add(e);

            e = getNextSiblingElement(e);
        }

        return children;
    }

    /**
     * Gets the next sibling Element of the node, skipping any Text nodes such as whitespace.
     * 
     * @param n The sibling to start with
     * @return The next sibling Element of n, or null if none
     */
    @Nullable public static Element getNextSiblingElement(@Nullable final Node n) {
        if (n == null) {
            return null;
        }

        Node sib = n.getNextSibling();
        while (sib != null && sib.getNodeType() != Node.ELEMENT_NODE) {
            sib = sib.getNextSibling();
        }

        return sib != null ? (Element) sib : null;
    }

    /**
     * Check if the given Element has the given name.
     * 
     * @param e element to check
     * @param name name to check for
     * 
     * @return true if the element has the given name, false otherwise
     */
    public static boolean isElementNamed(@Nullable final Element e, @Nullable final QName name) {
        if (name == null) {
            return false;
        }

        return isElementNamed(e, name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Shortcut for checking a DOM element node's namespace and local name.
     * 
     * @param e An element to compare against
     * @param ns An XML namespace to compare
     * @param localName A local name to compare
     * @return true iff the element's local name and namespace match the parameters
     */
    public static boolean isElementNamed(@Nullable final Element e, @Nullable final String ns,
            @Nullable final String localName) {
        return e != null && Objects.equals(ns, e.getNamespaceURI()) && Objects.equals(localName, e.getLocalName());
    }

    /**
     * Sets a given Element as the root element of a given document. If the given element is not owned by the given
     * document than it is adopted first.
     * 
     * @param document document whose root element will be set
     * @param element element that will be the new root element
     */
    public static void setDocumentElement(@Nonnull final Document document, @Nonnull final Element element) {
        Constraint.isNotNull(document, "Document may not be null");
        Constraint.isNotNull(element, "Element may not be null");

        final Element rootElement = document.getDocumentElement();
        if (rootElement == null) {
            adoptElement(document, element);
            document.appendChild(element);
        } else if (!rootElement.isSameNode(element)) {
            adoptElement(document, element);
            document.replaceChild(element, rootElement);
        }

    }
}