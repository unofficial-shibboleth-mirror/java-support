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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * A builder for instances of {@link SSLConnectionSocketFactory} which allows easy specification
 * of the full range of supported factory inputs.
 */
public class SSLConnectionSocketFactoryBuilder {
    
    /** The default protocol used when obtaining the SSLContxt instance. */
    private static final String DEFAULT_CONTEXT_PROTOCOL = "TLS";
    
    /** The default hostname verifier used by the socket factory. */
    private static final X509HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = 
            SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER;
    
    /** The protocol used when obtaining the SSLContext instance. */
    private String sslContextProtocol;
    
    /** The JCA provider used when obtaining the SSLContext instance. */
    private String sslContextProvider;
    
    /** The list of KeyManagers used when initializing the SSLContext instance. */
    private List<KeyManager> keyManagers;
    
    /** The list of TrustManagers used when initializing the SSLContext instance. */
    private List<TrustManager> trustManagers;
    
    /** The SecureRandom instance used when initializing the SSLContext instance. */
    private SecureRandom secureRandom;
    
    /** The hostname verifier used by the socket factory. */
    private X509HostnameVerifier hostnameVerifier;
    
    /** The SSL/TLS protocols enabled on sockets produced by the socket factory. */
    private List<String> enabledProtocols;
    
    /** The SSL/TLS cipher suites enabled on sockets produced the socket factory. */
    private List<String> enabledCipherSuites;
    
    /**
     * Get the protocol specifier used when obtaining an instance of {@link SSLContext}
     * via {@link SSLContext#getInstance(String)}.
     * 
     * <p>If not specified, the value "TLS" will be used.</p>
     * 
     * @return the protocol, or null
     */
    @Nullable public String getSSLContextProtocol() {
        return sslContextProtocol;
    }

    /**
     * Set the protocol specifier used when obtaining an instance of {@link SSLContext}
     * via {@link SSLContext#getInstance(String)}.
     * 
     * <p>If not specified, the value "TLS" will be used.</p>
     * 
     * @param protocol the protocol, may be null
     */
    public void setSSLContextProtocol(@Nullable final String protocol) {
        sslContextProtocol = StringSupport.trimOrNull(protocol);
    }

    /**
     * Get the JCA provider name used when obtaining an instance of {@link SSLContext}
     * via {@link SSLContext#getInstance(String, String))}.
     * 
     * @return the provider namer, or null
     */
    @Nullable public String getSSLContextProvider() {
        return sslContextProvider;
    }

    /**
     * Set the JCA provider name used when obtaining an instance of {@link SSLContext}
     * via {@link SSLContext#getInstance(String, String)}.
     * 
     * @param provider the provider name, may be null
     */
    public void setSSLContextProvider(@Nullable final String provider) {
        sslContextProvider = StringSupport.trimOrNull(provider);
    }

    /**
     * Get the list of {@link KeyManager}s used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @return the list of key managers, or null
     */
    @Nullable public List<KeyManager> getKeyManagers() {
        return keyManagers;
    }

    /**
     * Set the list of {@link KeyManager}s used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @param managers the list of key managers, or null
     */
    public void setKeyManagers(@Nullable final List<KeyManager> managers) {
        if (managers == null) {
            keyManagers = null;
        } else {
            keyManagers = new ArrayList<>(Collections2.filter(managers, Predicates.notNull()));
            if (keyManagers.isEmpty()) {
                keyManagers = null;
            }
        }
    }

    /**
     * Get the list of {@link TrustManager}s used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @return the list of trust managers, or null
     */
    @Nullable public List<TrustManager> getTrustManagers() {
        return trustManagers;
    }

    /**
     * Set the list of {@link TrustManager}s used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @param managers the list of trust managers, or null
     */
    public void setTrustManagers(@Nullable final List<TrustManager> managers) {
        if (managers == null) {
            trustManagers = null;
        } else {
            trustManagers = new ArrayList<>(Collections2.filter(managers, Predicates.notNull()));
            if (trustManagers.isEmpty()) {
                trustManagers = null;
            }
        }
    }

    /**
     * Get the {@link SecureRandom} instance used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @return the secure random instance, or null
     */
    @Nullable public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    /**
     * Set the {@link SecureRandom} instance used to initialize the {@link SSLContext}
     * via {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
     * 
     * @param random the secure random instance, or null
     */
    public void setSecureRandom(@Nullable final SecureRandom random) {
        secureRandom = random;
    }
    
    /**
     * Get the {@link X509HostnameVerifier} instance used by the socket factory.
     * 
     * <p>If not specified, defaults to {@link SSLConnectionSocketFactory#STRICT_HOSTNAME_VERIFIER}.
     * 
     * @return the hostname verifier, or null
     */
    @Nullable public X509HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Set the {@link X509HostnameVerifier} instance used by the socket factory.
     * 
     * <p>If not specified, defaults to {@link SSLConnectionSocketFactory#STRICT_HOSTNAME_VERIFIER}.
     * 
     * @param verifier the hostname verifier, or null
     */
    public void setHostnameVerifier(@Nullable final X509HostnameVerifier verifier) {
        hostnameVerifier = verifier;
    }

    /**
     * Get the list of enabled SSL/TLS protocols on sockets produced by the factory.
     * 
     * @return the list of protocols, or null
     */
    @Nullable public List<String> getEnabledProtocols() {
        return enabledProtocols;
    }

    /**
     * Set the list of enabled SSL/TLS protocols on sockets produced by the factory.
     * 
     * @param protocols the list of protocols, or null
     */
    public void setEnabledProtocols(@Nullable final List<String> protocols) {
        enabledProtocols = new ArrayList<>(StringSupport.normalizeStringCollection(protocols));
        if (enabledProtocols.isEmpty()) {
            enabledProtocols = null;
        }
    }

    /**
     * Get the list of enabled SSL/TLS cipher suites on sockets produced by the factory.
     * 
     * @return the list of cipher suites, or null
     */
    @Nullable public List<String> getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    /**
     * Set the list of enabled SSL/TLS cipher suites on sockets produced by the factory.
     * 
     * @param cipherSuites the list of cipher suites, or null
     */
    public void setEnabledCipherSuites(@Nullable final List<String> cipherSuites) {
        enabledCipherSuites = new ArrayList<>(StringSupport.normalizeStringCollection(cipherSuites));
        if (enabledCipherSuites.isEmpty()) {
            enabledCipherSuites = null;
        }
    }

    /**
     * Build a new instance of {@link SSLConnectionSocketFactory}, based on the properties specified
     * to this builder instance.
     * 
     * @return a new socket factory instance
     */
    @Nonnull public SSLConnectionSocketFactory build() {
        X509HostnameVerifier verifier = hostnameVerifier;
        if (verifier == null) {
            verifier = DEFAULT_HOSTNAME_VERIFIER;
        }

        SSLContext sslcontext = buildSSLContext();
        return new SSLConnectionSocketFactory(sslcontext, 
                enabledProtocols != null ? enabledProtocols.toArray(new String[0]) : null, 
                enabledCipherSuites != null ? enabledCipherSuites.toArray(new String[0]) : null, 
                verifier);
    }
    
    /**
     * Build a new instance of {@SSLContext} based on the properties specified on this builder instance.
     * 
     * @return a new SSLContext instance
     */
    @Nonnull private SSLContext buildSSLContext() {
        String protocol = sslContextProtocol;
        if (protocol == null) {
            protocol = DEFAULT_CONTEXT_PROTOCOL;
        }
        
        try {
            SSLContext sslcontext;
            if (sslContextProvider != null) {
                sslcontext = SSLContext.getInstance(protocol, sslContextProvider);
            } else {
                sslcontext = SSLContext.getInstance(protocol);
            }
            
            sslcontext.init(
                    keyManagers != null ? keyManagers.toArray(new KeyManager[0]) : null,
                    trustManagers != null ? trustManagers.toArray(new TrustManager[0]) : null, 
                    secureRandom);
            
            return sslcontext;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Problem obtaining SSLContext, unsupported protocol: " + sslContextProtocol, e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException("Problem obtaining SSLContext, invalid provider: " + sslContextProvider, e);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Key Problem initializing SSLContext", e);
        }
        
    }

}
