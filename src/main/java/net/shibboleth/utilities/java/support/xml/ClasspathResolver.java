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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A entity resolver that resolves an entity's location within the classpath.
 * 
 * Entity URIs <strong>must</strong> begin with the prefix <code>classpath:</code> and be followed by either an
 * absolute or relative classpath. Relative classpaths are relative to <strong>this</strong> class.
 * 
 * This resolver will <strong>not</strong> attempt to resolve any other URIs.
 */
public class ClasspathResolver implements EntityResolver, LSResourceResolver {

    /** URI scheme for classpath locations. */
    public static final String CLASSPATH_URI_SCHEME = "classpath:";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ClasspathResolver.class);

    /** {@inheritDoc} */
    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        final InputStream resourceStream = resolver(publicId, systemId);
        if (resourceStream != null) {
            final InputSource is = new InputSource(resourceStream);
            is.setSystemId(systemId);
            is.setPublicId(publicId);
            return is;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
 public LSInput resolveResource(final String type, final String namespaceURI, final String publicId,
            final String systemId, final String baseURI) {
        return new LSInputImpl(publicId, systemId, resolver(publicId, systemId));
    }

    /**
     * Resolves an id against the classpath. System ID is tried first, then public ID.
     * 
     * @param publicId resources public ID
     * @param systemId resources system ID
     * 
     * @return resolved resource or null
     */
    protected InputStream resolver(final String publicId, final String systemId) {
        String resource = null;
        InputStream resourceIns = null;

        if (systemId.startsWith(CLASSPATH_URI_SCHEME)) {
            log.trace("Attempting to resolve, within the classpath, the entity with the following system id: {}",
                    systemId);
            resource = systemId.replaceFirst("classpath:", "");
            resourceIns = getClass().getResourceAsStream(resource);
        }

        if (resourceIns == null && publicId != null && publicId.startsWith(CLASSPATH_URI_SCHEME)) {
            log.trace("Attempting to resolve, within the classpath, the entity with the following public id: {}",
                    resource);
            resource = publicId.replaceFirst("classpath:", "");
            resourceIns = getClass().getResourceAsStream(resource);
        }

        if (resourceIns == null) {
            log.trace("Entity was not resolved from classpath, public id ({}), system id({})", publicId, systemId);
            return null;
        }
        log.trace("Entity resolved from classpath");
        return resourceIns;
    }

    /**
     * Implementation of DOM 3 {@link LSInput}.
     */
    protected class LSInputImpl implements LSInput {

        /** Public ID of the resolved resource. */
        private String publicId;

        /** System ID of the resolved resource. */
        private String systemId;

        /** Resolved resource. */
        private BufferedInputStream buffInput;

        /**
         * Constructor.
         * 
         * @param pubId public id of the resolved resource
         * @param sysId system id of the resolved resource
         * @param input resolved resource
         */
        public LSInputImpl(final String pubId, final String sysId, final InputStream input) {
            publicId = pubId;
            systemId = sysId;
            buffInput = new BufferedInputStream(input);
        }

        /** {@inheritDoc} */
        @Override
        public String getBaseURI() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public InputStream getByteStream() {
            return buffInput;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getCertifiedText() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Reader getCharacterStream() {
            return new InputStreamReader(buffInput);
        }

        /** {@inheritDoc} */
        @Override
        public String getEncoding() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public String getPublicId() {
            return publicId;
        }

        /** {@inheritDoc} */
        @Override
        public String getStringData() {
            synchronized (buffInput) {
                try {
                    buffInput.reset();
                    final byte[] input = new byte[buffInput.available()];
                    buffInput.read(input);
                    return new String(input);
                } catch (final IOException e) {
                    return null;
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public String getSystemId() {
            return systemId;
        }

        /** {@inheritDoc} */
        @Override
        public void setBaseURI(final String uri) {
        }

        /** {@inheritDoc} */
        @Override
        public void setByteStream(final InputStream byteStream) {
        }

        /** {@inheritDoc} */
        @Override
        public void setCertifiedText(final boolean isCertifiedText) {
        }

        /** {@inheritDoc} */
        @Override
        public void setCharacterStream(final Reader characterStream) {
        }

        /** {@inheritDoc} */
        @Override
        public void setEncoding(final String encoding) {
        }

        /** {@inheritDoc} */
        @Override
        public void setPublicId(final String id) {
        }

        /** {@inheritDoc} */
        @Override
        public void setStringData(final String stringData) {
        }

        /** {@inheritDoc} */
        @Override
        public void setSystemId(final String id) {
        }
    }
}