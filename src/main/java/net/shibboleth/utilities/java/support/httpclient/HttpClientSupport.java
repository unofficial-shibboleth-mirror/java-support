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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;

/**
 * Support class for using {@link org.apache.http.client.HttpClient} and related components.
 */
public final class HttpClientSupport {
    
    /** Constructor to prevent instantiation. */
    private HttpClientSupport() { }
    
    /**
     * Build an instance of TLS-capable {@link LayeredConnectionSocketFactory} which uses
     * the standard JSSE default {@link SSLContext} and which performs
     * strict hostname verification.
     * 
     * @return a new instance of HttpClient SSL connection socket factory
     */
    @Nonnull public static LayeredConnectionSocketFactory buildStrictTLSSocketFactory() {
        return new TLSSocketFactoryBuilder()
            .setHostnameVerifier(new StrictHostnameVerifier())
            .build();
    }
    
    /**
     * Build a TLS-capable instance of {@link LayeredConnectionSocketFactory} which accepts all peer certificates
     * and performs no hostname verification.
     * 
     * @return a new instance of HttpClient SSL connection socket factory
     */
    @Nonnull public static LayeredConnectionSocketFactory buildNoTrustTLSSocketFactory() {
        return new TLSSocketFactoryBuilder()
            .setTrustManagers(Collections.<TrustManager>singletonList(buildNoTrustX509TrustManager()))
            .setHostnameVerifier(new AllowAllHostnameVerifier())
            .build();
    }
    
    /**
     * Build an instance of {@link SSLConnectionSocketFactory} which uses
     * the standard HttpClient default {@link SSLContext} and which uses
     * a strict hostname verifier {@link SSLConnectionSocketFactory#STRICT_HOSTNAME_VERIFIER}.
     * 
     * @return a new instance of HttpClient SSL connection socket factory
     * 
     * @deprecated use instead {@link #buildStrictTLSSocketFactory()}
     */
    @Deprecated
    @Nonnull public static SSLConnectionSocketFactory buildStrictSSLConnectionSocketFactory() {
        DeprecationSupport.warnOnce(ObjectType.METHOD,
                "net.shibboleth.utilities.java.support.httpclient.HttpClientSupport" +
                        ".buildStrictSSLConnectionSocketFactory", null, "buildStrictTLSSocketFactory");

        return new SSLConnectionSocketFactory(
                SSLContexts.createDefault(), 
                SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }
    
     /**
     * Build an instance of {@link SSLConnectionSocketFactory} which accepts all peer certificates
     * and performs no hostname verification.
     * 
     * @return a new instance of HttpClient SSL connection socket factory
     * 
     * @deprecated use instead {@link #buildNoTrustTLSSocketFactory()}
     */
    @Deprecated
    @Nonnull public static SSLConnectionSocketFactory buildNoTrustSSLConnectionSocketFactory() {
        DeprecationSupport.warnOnce(ObjectType.METHOD,
                "net.shibboleth.utilities.java.support.httpclient.HttpClientSupport" +
                        ".buildNoTrustSSLConnectionSocketFactory", null, "buildNoTrustTLSSocketFactory");
        
        final X509TrustManager noTrustManager = buildNoTrustX509TrustManager();

        try {
            final SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] {noTrustManager}, null);
            return new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("TLS SSLContext type is required to be supported by the JVM but is not", e);
        } catch (final KeyManagementException e) {
            throw new RuntimeException("Somehow the trust everything trust manager didn't trust everything", e);
        }
        
    }
    
    /**
     * Build an instance of {@link X509TrustManager} which trusts all certificates.
     * 
     * @return a new trust manager instance
     */
    @Nonnull public static X509TrustManager buildNoTrustX509TrustManager() {
        // Checkstyle: AnonInnerLength OFF
        return new X509TrustManager() {
            private Logger log = LoggerFactory.getLogger(HttpClientSupport.class.getName()
                    + ".NoTrustX509TrustManager");

            public X509Certificate[] getAcceptedIssuers() {
                log.trace("In getAcceptedIssuers");
                return null;
            }

            public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                log.trace("In checkServerTrusted");
                if (chain != null) {
                    log.trace("Cert chain length: {}", chain.length);
                    for (final X509Certificate cert : chain) {
                        log.trace("Cert key type: {}, subject: {}",
                                cert.getPublicKey().getAlgorithm(), cert.getSubjectX500Principal().getName());
                    }
                } else {
                    log.trace("Cert chain was null");
                }
                // accept everything
            }

            public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                log.trace("In checkClientTrusted");
                // accept everything
            }
        };
        // Checkstyle: AnonInnerLength ON
        
    }

// Checkstyle: CyclomaticComplexity OFF
    /**
     * Get the entity content as a String, using the provided default character set
     * if none is found in the entity.
     * 
     * <p>If defaultCharset is null, the default "ISO-8859-1" is used.</p>
     *
     * @param entity must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @param maxLength limit on size of content
     * 
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     *   
     * @throws ParseException if header elements cannot be parsed
     * @throws IOException if an error occurs reading the input stream, or the size exceeds limits
     * @throws UnsupportedCharsetException when the content's charset is not available
     */
    @Nullable public static String toString(@Nonnull final HttpEntity entity, @Nullable final Charset defaultCharset,
            final int maxLength) throws IOException, ParseException {
        try (final InputStream instream = entity.getContent()) {
            if (instream == null) {
                return null;
            }
            if (entity.getContentLength() > maxLength || entity.getContentLength() > Integer.MAX_VALUE) {
                throw new IOException("HTTP entity size exceeded limit");
            }
            int i = (int) entity.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            Charset charset = null;
            try {
                final ContentType contentType = ContentType.get(entity);
                if (contentType != null) {
                    charset = contentType.getCharset();
                }
            } catch (final UnsupportedCharsetException ex) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
            if (charset == null) {
                charset = defaultCharset;
            }
            if (charset == null) {
                charset = HTTP.DEF_CONTENT_CHARSET;
            }
            try (final Reader reader = new InputStreamReader(instream, charset)) {
                final CharArrayBuffer buffer = new CharArrayBuffer(i);
                final char[] tmp = new char[1024];
                int size = 0;
                int l;
                while((l = reader.read(tmp)) != -1) {
                    size += l;
                    if (size > maxLength) {
                        throw new IOException("HTTP entity size exceeded limit");
                    }
                    buffer.append(tmp, 0, l);
                }
                return buffer.toString();
            }
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
    /**
     * Get the entity content as a String, using the provided default character set
     * if none is found in the entity.
     * If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @param maxLength limit on size of content
     * 
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     *   
     * @throws ParseException if header elements cannot be parsed
     * @throws IOException if an error occurs reading the input stream, or the size exceeds limits
     * @throws UnsupportedCharsetException when the content's charset is not available
     */
    @Nullable public static String toString(@Nonnull final HttpEntity entity, @Nullable final String defaultCharset,
            final int maxLength) throws IOException, ParseException {
        return toString(entity, defaultCharset != null ? Charset.forName(defaultCharset) : null, maxLength);
    }

    /**
     * Read the contents of an entity and return it as a String.
     * The content is converted using the character set from the entity (if any),
     * failing that, "ISO-8859-1" is used.
     *
     * @param entity the entity to convert to a string; must not be null
     * @param maxLength limit on size of content
     * 
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     * 
     * @throws ParseException if header elements cannot be parsed
     * @throws IOException if an error occurs reading the input stream, or the size exceeds limits
     * @throws UnsupportedCharsetException when the content's charset is not available
     */
    @Nullable public static String toString(@Nonnull final HttpEntity entity, final int maxLength)
        throws IOException, ParseException {
        return toString(entity, (Charset) null, maxLength);
    }

}