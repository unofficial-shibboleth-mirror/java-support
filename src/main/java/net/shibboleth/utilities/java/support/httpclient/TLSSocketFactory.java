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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An implementation of HttpClient {@link LayeredConnectionSocketFactory} that is a factory
 * for TLS sockets.
 * 
 * <p>
 * This class is functionally modeled on {@link org.apache.http.conn.ssl.SSLConnectionSocketFactory},
 * but provides better support for subclassing, as well as specific additional features:
 * </p>
 *
 * <ul>
 *   <li>Factory hostname verifier defaults to {@link StrictHostnameVerifier} rather than 
 *       {@link BrowserCompatHostnameVerifier}</li>
 *   <li>Per-request specification of enabled TLS protocols and cipher suites via {@link HttpContext} attributes.</li>
 *   <li>Per-request specification of hostname verifier via {@link HttpContext} attribute.</li>
 * </ul>
 */
@ThreadSafe
public class TLSSocketFactory implements LayeredConnectionSocketFactory {
    
    /** HttpContext key for a a list of TLS protocols to enable on the socket.  
     * Must be an instance of {@link List}&lt;{@link String}&gt;. */
    @Nonnull @NotEmpty public static final String CONTEXT_KEY_TLS_PROTOCOLS = "javasupport.TLSProtocols";
    
    /** HttpContext key for a a list of TLS cipher suites to enable on the socket.  
     * Must be an instance of {@link List}&lt;{@link String}&gt;. */
    @Nonnull @NotEmpty public static final String CONTEXT_KEY_TLS_CIPHER_SUITES = "javasupport.TLSCipherSuites";
    
    /** HttpContext key for an instance of {@link X509HostnameVerifier}. */
    @Nonnull @NotEmpty public static final String CONTEXT_KEY_HOSTNAME_VERIFIER = "javasupport.HostnameVerifier";

    /** Protocol: TLS. */
    @Nonnull @NotEmpty public static final String TLS = "TLS";
    
    /** Protocol: SSL. */
    @Nonnull @NotEmpty public static final String SSL = "SSL";
    
    /** Protocol: SSLv2. */
    @Nonnull @NotEmpty public static final String SSLV2 = "SSLv2";

    /** Hostname verifier which passes all hostnames. */
    @Nonnull public static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();

    /** Hostname verifier which implements a strict policy. */
    @Nonnull public static final HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifier();
    
    /** Logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TLSSocketFactory.class);

    /** Socket factory. */
    @Nonnull private final SSLSocketFactory socketfactory;
    
    /** Hostname verifier. */
    @Nonnull private final HostnameVerifier hostnameVerifier;
    
    /** Factory-wide supported protocols. */
    private final String[] supportedProtocols;
    
    /** Factory-wide supported cipher suites. */
    private final String[] supportedCipherSuites;

    /**
     * Constructor.
     *
     * @param sslContext the effective SSLContext instance
     */
    public TLSSocketFactory(
            @Nonnull final SSLContext sslContext) {
        this(sslContext, STRICT_HOSTNAME_VERIFIER);
    }

    /**
     * Constructor.
     *
     * @param sslContext the effective SSLContext instance
     * @param verifier the effective hostname verifier
     */
    public TLSSocketFactory(
            @Nonnull final SSLContext sslContext, 
            @Nullable final HostnameVerifier verifier) {
        this(Constraint.isNotNull(sslContext, "SSL context cannot be null").getSocketFactory(), null, null, verifier);
    }

    /**
     * Constructor.
     *
     * @param sslContext the effective SSLContext instance
     * @param protocols the factory-wide enabled TLS protocols
     * @param cipherSuites the factory-wide enabled TLS cipher suites
     * @param verifier the effective hostname verifier
     */
    public TLSSocketFactory(
            @Nonnull final SSLContext sslContext,
            @Nullable final String[] protocols,
            @Nullable final String[] cipherSuites,
            @Nullable final HostnameVerifier verifier) {
        this(Constraint.isNotNull(sslContext, "SSL context cannot be null").getSocketFactory(),
                protocols, cipherSuites, verifier);
    }

    /**
     * Constructor.
     *
     * @param factory the effective SSL socket factory
     * @param verifier the effective hostname verifier
     */
    public TLSSocketFactory(
            @Nonnull final SSLSocketFactory factory, 
            @Nullable final HostnameVerifier verifier) {
        this(factory, null, null, verifier);
    }

    /**
     * Constructor.
     *
     * @param factory the effective SSL socket factory
     * @param protocols the factory-wide enabled TLS protocols
     * @param cipherSuites the factory-wide enabled TLS cipher suites
     * @param verifier the effective hostname verifier
     */
    public TLSSocketFactory(
            @Nonnull final SSLSocketFactory factory,
            @Nullable final String[] protocols,
            @Nullable final String[] cipherSuites,
            @Nullable final HostnameVerifier verifier) {
        socketfactory = Constraint.isNotNull(factory, "SSL socket factory cannot be null");
        supportedProtocols = protocols;
        supportedCipherSuites = cipherSuites;
        hostnameVerifier = verifier != null ? verifier : STRICT_HOSTNAME_VERIFIER;
    }

    /**
     * Get the JSSE socket factory instance.
     * 
     * @return the socket factory
     */
    @Nonnull protected SSLSocketFactory getSocketfactory() {
        return socketfactory;
    }
    
    /**
     * Get the configured hostname verifier.
     * 
     * @return the hostname verifier
     */
    @Nonnull protected HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Get the configured factory-wide supported protocols.
     * 
     * @return the configured protocols
     */
    @Nullable protected String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    /**
     * Get the configured factory-wide supported cipher suites.
     * 
     * @return the configured cipher suites
     */
    @Nullable protected String[] getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    /**
     * Performs any custom initialization for a newly created SSLSocket
     * (before the SSL handshake happens).
     *
     * The default implementation is a no-op, but could be overridden to, e.g.,
     * call {@link javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[])}.
     * 
     * @param socket the SSL socket instance being prepared
     * @param context the current HttpContext instance 
     * 
     * @throws IOException if there is an error customizing the socket
     */
    protected void prepareSocket(@Nonnull final SSLSocket socket, @Nullable final HttpContext context) 
            throws IOException {
        
    }

    /** {@inheritDoc} */
    @Nonnull public Socket createSocket(@Nullable final HttpContext context) throws IOException {
        log.trace("In createSocket");
        return SocketFactory.getDefault().createSocket();
    }

    // Checkstyle: ParameterNumber OFF
    /** {@inheritDoc} */
    public Socket connectSocket(
            final int connectTimeout,
            final Socket socket,
            final HttpHost host,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpContext context) throws IOException {
        
        log.trace("In connectSocket");
        
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        
        final Socket sock = socket != null ? socket : createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            if (connectTimeout > 0 && sock.getSoTimeout() == 0) {
                sock.setSoTimeout(connectTimeout);
            }
            sock.connect(remoteAddress, connectTimeout);
        } catch (final IOException ex) {
            try {
                sock.close();
            } catch (final IOException ignore) {
            }
            throw ex;
        }
        // Setup SSL layering if necessary
        if (sock instanceof SSLSocket) {
            final SSLSocket sslsock = (SSLSocket) sock;
            sslsock.startHandshake();
            verifyHostname(sslsock, host.getHostName(), context);
            return sock;
        }
        
        return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
    }
    // Checkstyle: ParameterNumber ON

    /** {@inheritDoc} */
    public Socket createLayeredSocket(
            final Socket socket,
            final String target,
            final int port,
            final HttpContext context) throws IOException {
        
        log.trace("In createLayeredSocket");
        
        final SSLSocket sslsock = (SSLSocket) getSocketfactory().createSocket(
                socket,
                target,
                port,
                true);
        
        final String[] contextProtocols = getListAttribute(context, CONTEXT_KEY_TLS_PROTOCOLS);
        if (contextProtocols != null) {
            sslsock.setEnabledProtocols(contextProtocols);
        } else if (getSupportedProtocols() != null) {
            sslsock.setEnabledProtocols(getSupportedProtocols());
        } else {
            // If supported protocols are not explicitly set, remove all SSL protocol versions
            final String[] allProtocols = sslsock.getSupportedProtocols();
            final List<String> enabledProtocols = new ArrayList<>(allProtocols.length);
            for (final String protocol: allProtocols) {
                if (!protocol.startsWith("SSL")) {
                    enabledProtocols.add(protocol);
                }
            }
            sslsock.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
        }
        
        final String[] contextCipherSuites = getListAttribute(context, CONTEXT_KEY_TLS_CIPHER_SUITES);
        if (contextCipherSuites != null) {
            sslsock.setEnabledCipherSuites(contextCipherSuites);
        } else if (getSupportedCipherSuites() != null) {
            sslsock.setEnabledCipherSuites(getSupportedCipherSuites());
        }
        
        prepareSocket(sslsock, context);
        sslsock.startHandshake();
        logSocketInfo(sslsock);
        verifyHostname(sslsock, target, context);
        return sslsock;
    }
    
    /**
     * Log various diagnostic information from the {@link SSLSocket} and {@link SSLSession}.
     * 
     * @param socket the SSLSocket instance
     */
    private void logSocketInfo(final SSLSocket socket) {
        final SSLSession session = socket.getSession();
        if (log.isDebugEnabled()) {
            log.debug("Connected to: {}", socket.getRemoteSocketAddress());
            
            log.debug("Supported protocols: {}", (Object)socket.getSupportedProtocols());
            log.debug("Enabled protocols:   {}", (Object)socket.getEnabledProtocols());
            log.debug("Selected protocol:   {}", session.getProtocol());
            
            log.debug("Supported cipher suites: {}", (Object)socket.getSupportedCipherSuites());
            log.debug("Enabled cipher suites:   {}", (Object)socket.getEnabledCipherSuites());
            log.debug("Selected cipher suite:   {}", session.getCipherSuite());
        }
        
        if (log.isTraceEnabled()) {
            try {
                log.trace("Peer principal: {}", session.getPeerPrincipal());
                log.trace("Peer certificates: {}", (Object)session.getPeerCertificates());
                log.trace("Local principal: {}", session.getLocalPrincipal());
                log.trace("Local certificates: {}", (Object)session.getLocalCertificates());
            } catch (final SSLPeerUnverifiedException e) {
                log.warn("SSL exception enumerating peer certificates", e);
            }
        }
    }

    /**
     * Get a normalized String array from a context attribute holding a {@link List}&lt;{@link String}&gt;.
     * 
     * @param context the current HttpContext
     * @param contextKey the attribute context key
     * 
     * @return a String array, or null
     */
    @Nullable protected String[] getListAttribute(@Nullable final HttpContext context,
            @Nonnull final String contextKey) {
        if (context == null) {
            return null;
        }
        final List<String> values = new ArrayList<>(StringSupport.normalizeStringCollection(
                (List<String>) context.getAttribute(contextKey)));
        if (values != null && !values.isEmpty()) {
            return values.toArray(new String[values.size()]);
        } else {
            return null;
        }
    }

    /**
     * Verify the peer's socket hostname against the supplied expected name.
     * 
     * @param sslsock the SSL socket being prepared
     * @param hostname the expected hostname
     * @param context the current HttpContext instance
     * 
     * @throws IOException if peer failed hostname verification, or if there was an error during verification
     */
    protected void verifyHostname(@Nonnull final SSLSocket sslsock, @Nonnull final String hostname, 
            @Nullable final HttpContext context) throws IOException {
        
            HostnameVerifier verifier = null;
            if (context != null) {
                verifier = (HostnameVerifier) context.getAttribute(CONTEXT_KEY_HOSTNAME_VERIFIER);
            }
            if (verifier == null) {
                verifier = getHostnameVerifier(); 
            }
            if (! verifier.verify(hostname, sslsock.getSession())) {
                throw new SSLPeerUnverifiedException("TLS hostname verification failed for hostname: " + hostname);
            }
    }

}
