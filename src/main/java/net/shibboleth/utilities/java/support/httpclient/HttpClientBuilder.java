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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.CharsetUtils;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

//TODO retry attempts, keep alive strategy

/**
 * Builder used to construct {@link HttpClient} objects configured with particular settings.
 * 
 * <p>
 * When using the single-arg constructor variant to wrap an existing instance of
 * {@link org.apache.http.impl.client.HttpClientBuilder}, there are several caveats of which to be aware:
 * 
 * <ul>
 * 
 * <li>
 * Instances of the following which are set as the default instance on the Apache builder will be unconditionally
 * overwritten by this builder when {@link #buildClient()} is called:
 * 
 * <ul>
 * <li>{@link RequestConfig}</li>
 * <li>{@link ConnectionConfig}</li>
 * </ul>
 * 
 * <p>
 * This is due to the unfortunate fact that the Apache builder does not currently provide accessor methods to obtain the
 * default instances currently set on the builder. Therefore, if you need to set any default request or connection
 * config parameters which are not exposed by this builder, then you must use the Apache builder directly and may not
 * use this builder.
 * </p>
 * </li>
 * 
 * <li>
 * If this builder's <code>connectionDisregardTLSCertificate</code> is set to <code>true</code>, then any value
 * previously set via the Apache builder's
 * {@link org.apache.http.impl.client.HttpClientBuilder#setSSLSocketFactory} will be
 * unconditionally overwritten.</li>
 * 
 * <li>
 * If this builder is supplied with a <code>connectionProxyHost</code>, <code>connectionProxyUsername</code> and
 * <code>connectionProxyPassword</code>, then any value previously set via the Apache builder's
 * {@link org.apache.http.impl.client.HttpClientBuilder#setDefaultCredentialsProvider(CredentialsProvider)} will be
 * unconditionally overwritten.</li>
 * 
 * <li>
 * Per the Apache builder's Javadoc, if a non-null instance of {@link org.apache.http.conn.HttpClientConnectionManager}
 * is set on the Apache builder via
 * {@link org.apache.http.impl.client.HttpClientBuilder#setConnectionManager},
 * this supersedes various other properties set on the Apache builder. This includes the following
 * instances/properties on the Apache builder:
 * 
 * <ul>
 * <li><code>SSLSocketFactory</code> ({@link org.apache.http.conn.socket.LayeredConnectionSocketFactory})</li>
 * <li>{@link javax.net.ssl.SSLContext}</li>
 * <li>{@link javax.net.ssl.HostnameVerifier}</li>
 * <li>{@link org.apache.http.config.SocketConfig}</li>
 * <li>{@link ConnectionConfig}</li>
 * <li><code>maxConnTotal</code></li>
 * <li><code>maxConnPerRoute</code></li>
 * </ul>
 * 
 * <p>
 * Similarly, the following setters on this builder will become ineffective when a non-null connection manger is set on
 * the Apache builder:
 * </p>
 * 
 * <ul>
 * <li>{@link #setTLSSocketFactory(LayeredConnectionSocketFactory)}</li>
 * <li>{@link #setConnectionDisregardTLSCertificate(boolean)}</li>
 * <li>{@link #setSocketBufferSize(int)}</li>
 * <li>{@link #setHttpContentCharSet(String)}</li>
 * <li>{@link #setMaxConnectionsTotal(int)}</li>
 * <li>{@link #setMaxConnectionsPerRoute(int)}</li>
 * </ul>
 * 
 * <p>
 * Therefore, if you need to explicitly supply a connection manager instance to the Apache builder (for example in order
 * to be able to use {@link IdleConnectionSweeper}), then you must supply these properties or instances directly to the
 * connection manager rather than to this builder or the Apache builder.
 * </p>
 * </li>
 * 
 * <li>
 * Similar to the above issue, setting an explicit <code>SSLSocketFactory</code> on the Apache builder will supersede
 * the following Apache builder properties:
 * 
 * <ul>
 * <li>{@link javax.net.ssl.SSLContext}</li>
 * <li>{@link javax.net.ssl.HostnameVerifier}</li>
 * </ul>
 * </li>
 * 
 * </ul>
 */
public class HttpClientBuilder {

    /** Local IP address used when establishing connections. Default value: system default local address */
    private InetAddress socketLocalAddress;

    /** Maximum period inactivity between two consecutive data packets. Default value: (60 seconds) */
    @Nonnull private Duration socketTimeout;

    /** Socket buffer size in bytes. Default size is 8192 bytes. */
    private int socketBufferSize;

    /** Maximum length of time to wait for the connection to be established. Default value: (60 seconds) */
    @Nonnull private Duration connectionTimeout;
    
    /**
     * Maximum length of time to wait for a connection to be returned from the connection manager.
     * Default value: (60 seconds);
     */
    @Nonnull private Duration connectionRequestTimeout;
    
    /**
     * Max total simultaneous connections allowed by the pooling connection manager.
     */
    private int maxConnectionsTotal;
    
    /**
     * Max simultaneous connections per route allowed by the pooling connection manager.
     */
    private int maxConnectionsPerRoute;

    /** Whether the SSL/TLS certificates used by the responder should be ignored. Default value: false */
    private boolean connectionDisregardTLSCertificate;
    
    /** The TLS socket factory to use.  Optional, defaults to null. */
    @Nullable private LayeredConnectionSocketFactory tlsSocketFactory;

    /** Whether to instruct the server to close the connection after it has sent its response. Default value: true */
    private boolean connectionCloseAfterResponse;

    /**
     * Whether to check a connection for staleness before using. This can be an expensive operation. Default value:
     * false
     */
    private boolean connectionStaleCheck;

    /** Host name of the HTTP proxy server through which connections will be made. Default value: null. */
    @Nullable private String connectionProxyHost;
    
    /** Apache UserAgent. */
    @Nullable private String userAgent;

    /** Port number of the HTTP proxy server through which connections will be made. Default value: 8080. */
    private int connectionProxyPort;

    /** Username used to connect to the HTTP proxy server. Default value: null. */
    @Nullable private String connectionProxyUsername;

    /** Password used to connect to the HTTP proxy server. Default value: null. */
    @Nullable private String connectionProxyPassword;

    /** Whether to follow HTTP redirects. Default value: true */
    private boolean httpFollowRedirects;

    /** Character set used for HTTP entity content. Default value: UTF-8 */
    @Nullable private String httpContentCharSet;

    /** Handler which determines if a request should be retried after a recoverable exception during execution. */
    @Nullable private HttpRequestRetryHandler retryHandler;

    /** Strategy which determines if a request should be retried given the response from the target server. */
    @Nullable private ServiceUnavailableRetryStrategy serviceUnavailStrategy;
    
    /** Flag for disabling auth caching.*/
    private boolean disableAuthCaching;

    /** Flag for disabling automatic retries.*/
    private boolean disableAutomaticRetries;

    /** Flag for disabling connection state.*/
    private boolean disableConnectionState;

    /** Flag for disabling content compression.*/
    private boolean disableContentCompression;

    /** Flag for disabling cookie management.*/
    private boolean disableCookieManagement;

    /** Flag for disabling redirect handling.*/
    private boolean disableRedirectHandling;

    /** Flag for enabling use of system properties.*/
    private boolean useSystemProperties;

    /** List of request interceptors to add first. */
    @Nonnull @NonnullElements @Unmodifiable @NotLive private List<HttpRequestInterceptor> requestInterceptorsFirst;

    /** List of request interceptors to add last. */
    @Nonnull @NonnullElements @Unmodifiable @NotLive private List<HttpRequestInterceptor> requestInterceptorsLast;

    /** List of response interceptors to add first. */
    @Nonnull @NonnullElements @Unmodifiable @NotLive private List<HttpResponseInterceptor> responseInterceptorsFirst;

    /** List of response interceptors to add last. */
    @Nonnull @NonnullElements @Unmodifiable @NotLive private List<HttpResponseInterceptor> responseInterceptorsLast;
    
    /** List of static context handlers. */
    @Nonnull @NonnullElements @Unmodifiable @NotLive private List<HttpClientContextHandler> staticContextHandlers;

    /** The Apache HttpClientBuilder 4.3+ instance over which to layer this builder. */
    private org.apache.http.impl.client.HttpClientBuilder apacheBuilder;

    /** Constructor. */
    public HttpClientBuilder() {
        this(org.apache.http.impl.client.HttpClientBuilder.create());
    }

    /**
     * Constructor.
     * 
     * @param builder the Apache HttpClientBuilder 4.3+ instance over which to layer this builder
     */
    public HttpClientBuilder(@Nonnull final org.apache.http.impl.client.HttpClientBuilder builder) {
        apacheBuilder = Constraint.isNotNull(builder, "Apache HttpClientBuilder may not be null");
        resetDefaults();
    }

    /** Resets all builder parameters to their defaults. */
    public void resetDefaults() {
        maxConnectionsTotal = -1;
        maxConnectionsPerRoute = -1;
        socketLocalAddress = null;
        socketBufferSize = 8192;
        socketTimeout = Duration.ofSeconds(60);
        connectionTimeout = Duration.ofSeconds(60);
        connectionRequestTimeout = Duration.ofSeconds(60);
        connectionDisregardTLSCertificate = false;
        connectionCloseAfterResponse = true;
        connectionStaleCheck = false;
        connectionProxyHost = null;
        connectionProxyPort = 8080;
        connectionProxyUsername = null;
        connectionProxyPassword = null;
        httpFollowRedirects = true;
        httpContentCharSet = "UTF-8";
        userAgent = null;
        
        requestInterceptorsFirst = Collections.emptyList();
        requestInterceptorsLast = Collections.emptyList();
        responseInterceptorsFirst = Collections.emptyList();
        responseInterceptorsLast = Collections.emptyList();
        staticContextHandlers = Collections.emptyList();
    }

    /**
     * Gets the max total simultaneous connections allowed by the pooling connection manager.
     * 
     * @return the max total connections
     */
    public int getMaxConnectionsTotal() {
        return maxConnectionsTotal;
    }

    /**
     * Sets the max total simultaneous connections allowed by the pooling connection manager.
     * 
     * @param max the max total connection
     */
    public void setMaxConnectionsTotal(final int max) {
        maxConnectionsTotal = max;
    }

    /**
     * Gets the max simultaneous connections per route allowed by the pooling connection manager.
     * 
     * @return the max connections per route
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Sets the max simultaneous connections per route allowed by the pooling connection manager.
     * 
     * @param max the max connections per route
     */
    public void setMaxConnectionsPerRoute(final int max) {
        maxConnectionsPerRoute = max;
    }

    /**
     * Gets the local IP address used when making requests.
     * 
     * @return local IP address used when making requests
     */
    public InetAddress getSocketLocalAddress() {
        return socketLocalAddress;
    }

    /**
     * Sets the local IP address used when making requests.
     * 
     * @param address local IP address used when making requests
     */
    public void setSocketLocalAddress(final InetAddress address) {
        socketLocalAddress = address;
    }

    /**
     * Sets the local IP address used when making requests.
     * 
     * @param ipOrHost IP address or hostname, never null
     * 
     * @throws UnknownHostException thrown if the given IP or hostname can not be resolved
     */
    public void setSocketLocalAddress(final String ipOrHost) throws UnknownHostException {
        socketLocalAddress = InetAddress.getByName(Constraint.isNotNull(ipOrHost, "IP or hostname may not be null"));
    }

    /**
     * Gets the maximum period inactivity between two consecutive data packets. A value of less than 1 ms
     * indicates no timeout.
     * 
     * @return maximum period inactivity between two consecutive data packets
     */
    @Nonnull public Duration getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the maximum period inactivity between two consecutive data packets. A value of less than 1 ms
     * indicates no timeout.
     * 
     * @param timeout maximum period inactivity between two consecutive data packets
     */
    public void setSocketTimeout(@Nonnull final Duration timeout) {
        Constraint.isNotNull(timeout, "Timeout cannot be null");
        Constraint.isLessThanOrEqual(Integer.MAX_VALUE, timeout.toMillis(), "Timeout too large");

        socketTimeout = timeout;
    }

    /**
     * Gets the size of the socket buffer, in bytes, used for request/response buffering.
     * 
     * @return size of the socket buffer, in bytes, used for request/response buffering
     */
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    /**
     * Sets size of the socket buffer, in bytes, used for request/response buffering.
     * 
     * @param size size of the socket buffer, in bytes, used for request/response buffering; must be greater than 0
     */
    public void setSocketBufferSize(final int size) {
        socketBufferSize = Constraint.isGreaterThan(0, size, "Socket buffer size must be greater than 0");
    }

    /**
     * Gets the maximum length of time to wait for the connection to be established. A value of less
     * than 1 ms indicates no timeout.
     * 
     * @return maximum length of time to wait for the connection to be established
     */
    @Nonnull public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the maximum length of time to wait for the connection to be established. A value of less
     * than 1 ms indicates no timeout.
     * 
     * @param timeout maximum length of time to wait for the connection to be established
     */
    public void setConnectionTimeout(@Nonnull final Duration timeout) {
        Constraint.isNotNull(timeout, "Connection timeout cannot be null");
        Constraint.isLessThanOrEqual(Integer.MAX_VALUE, timeout.toMillis(), "Connection timeout too large");

        connectionTimeout = timeout;
    }

    /**
     * Gets the maximum length of time to wait for a connection to be returned from the connection
     * manager. A value of less than 1 ms indicates no timeout.
     * 
     * @return maximum length of time to wait for the connection to be established
     */
    @Nonnull public Duration getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Sets the maximum length of time to wait for a connection to be returned from the connection
     * manager. A value of less than 1 ms indicates no timeout.
     * 
     * @param timeout maximum length of time to wait for the connection to be established
     */
    public void setConnectionRequestTimeout(@Nonnull final Duration timeout) {
        Constraint.isNotNull(timeout, "Connection request timeout cannot be null");
        Constraint.isLessThanOrEqual(Integer.MAX_VALUE, timeout.toMillis(), "Connection request timeout too large");
        
        connectionRequestTimeout = timeout;
    }

    /**
     * Gets whether the responder's SSL/TLS certificate should be ignored.
     * 
     * <p>
     * This flag is overridden and ignored if a custom TLS socket factory is specified via
     * {@link #setTLSSocketFactory}.
     * </p>
     * 
     * @return whether the responder's SSL/TLS certificate should be ignored
     */
    public boolean isConnectionDisregardTLSCertificate() {
        return connectionDisregardTLSCertificate;
    }

    /**
     * Sets whether the responder's SSL/TLS certificate should be ignored.
     * 
     * <p>
     * This flag is overridden and ignored if a custom TLS socket factory is specified via
     * {@link #setTLSSocketFactory}.
     * </p>
     * 
     * @param disregard whether the responder's SSL/TLS certificate should be ignored
     */
    public void setConnectionDisregardTLSCertificate(final boolean disregard) {
        connectionDisregardTLSCertificate = disregard;
    }

    /**
     * Get the TLS socket factory to use.
     * 
     * @return the socket factory, or null.
     */
    @Nullable public LayeredConnectionSocketFactory getTLSSocketFactory() {
        return tlsSocketFactory;
    }

    /**
     * Set the TLS socket factory to use.
     * 
     * @param factory the new socket factory, may be null
     */
    public void setTLSSocketFactory(@Nullable final LayeredConnectionSocketFactory factory) {
        tlsSocketFactory = factory;
    }

    /**
     * Gets whether to instruct the server to close the connection after it has sent its response.
     * 
     * @return whether to instruct the server to close the connection after it has sent its response
     */
    public boolean isConnectionCloseAfterResponse() {
        return connectionCloseAfterResponse;
    }

    /**
     * Sets whether to instruct the server to close the connection after it has sent its response.
     * 
     * @param close whether to instruct the server to close the connection after it has sent its response
     */
    public void setConnectionCloseAfterResponse(final boolean close) {
        connectionCloseAfterResponse = close;
    }

    /**
     * Gets whether reused connections are checked if they are closed before being used by the client.
     * 
     * @return whether reused connections are checked if they are closed before being used by the client
     * 
     * @deprecated use {@link #isConnectionStaleCheck()}
     */
    @Deprecated(forRemoval=true)
    public boolean isConnectionStalecheck() {
        return isConnectionStaleCheck();
    }

    /**
     * Sets whether reused connections are checked if they are closed before being used by the client. Checking can take
     * up to 30ms (per request). If checking is turned off an I/O error occurs if the connection is used request. This
     * should be enabled uncles the code using the client explicitly handles the error case and retries connection as
     * appropriate.
     * 
     * @param check whether reused connections are checked if they are closed before being used by the client
     * 
     * @deprecated use {@link #setConnectionStaleCheck(boolean)}
     */
    @Deprecated(forRemoval=true)
    public void setConnectionStalecheck(final boolean check) {
        setConnectionStaleCheck(check);
    }
    
    /**
     * Gets whether reused connections are checked if they are closed before being used by the client.
     * 
     * @return whether reused connections are checked if they are closed before being used by the client
     * 
     * @deprecated use a custom-configured connection pool manger.
     *     See {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager#setValidateAfterInactivity(int)}
     * 
     */
    @Deprecated(forRemoval=true)
    public boolean isConnectionStaleCheck() {
        return connectionStaleCheck;
    }

    /**
     * Sets whether reused connections are checked if they are closed before being used by the client. Checking can take
     * up to 30ms (per request). If checking is turned off an I/O error occurs if the connection is used request. This
     * should be enabled uncles the code using the client explicitly handles the error case and retries connection as
     * appropriate.
     * 
     * @param check whether reused connections are checked if they are closed before being used by the client
     * 
     * @deprecated use a custom-configured connection pool manger.
     *     See {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager#setValidateAfterInactivity(int)}
     */
    @Deprecated(forRemoval=true)
    public void setConnectionStaleCheck(final boolean check) {
        connectionStaleCheck = check;
    }

    /**
     * Gets the hostname of the default proxy used when making connection. A null indicates no default proxy.
     * 
     * @return hostname of the default proxy used when making connection
     */
    @Nullable public String getConnectionProxyHost() {
        return connectionProxyHost;
    }

    /**
     * Sets the hostname of the default proxy used when making connection. A null indicates no default proxy.
     * 
     * @param host hostname of the default proxy used when making connection
     */
    public void setConnectionProxyHost(@Nullable final String host) {
        connectionProxyHost = StringSupport.trimOrNull(host);
    }

    /**
     * Gets the port of the default proxy used when making connection.
     * 
     * @return port of the default proxy used when making connection
     */
    public int getConnectionProxyPort() {
        return connectionProxyPort;
    }

    /**
     * Sets the port of the default proxy used when making connection.
     * 
     * @param port port of the default proxy used when making connection; must be greater than 0 and less than 65536
     */
    public void setConnectionProxyPort(final int port) {
        connectionProxyPort =
                (int) Constraint.numberInRangeExclusive(0, 65536, port,
                        "Proxy port must be between 0 and 65536, exclusive");
    }

    /**
     * Gets the username to use when authenticating to the proxy.
     * 
     * @return username to use when authenticating to the proxy
     */
    @Nullable public String getConnectionProxyUsername() {
        return connectionProxyUsername;
    }

    /**
     * Sets the username to use when authenticating to the proxy.
     * 
     * @param usename username to use when authenticating to the proxy; may be null
     */
    public void setConnectionProxyUsername(@Nullable final String usename) {
        connectionProxyUsername = usename;
    }

    /**
     * Gets the password used when authenticating to the proxy.
     * 
     * @return password used when authenticating to the proxy
     */
    @Nullable public String getConnectionProxyPassword() {
        return connectionProxyPassword;
    }

    /**
     * Sets the password used when authenticating to the proxy.
     * 
     * @param password password used when authenticating to the proxy; may be null
     */
    public void setConnectionProxyPassword(@Nullable final String password) {
        connectionProxyPassword = password;
    }

    /**
     * Gets whether HTTP redirects will be followed.
     * 
     * @return whether HTTP redirects will be followed
     */
    public boolean isHttpFollowRedirects() {
        return httpFollowRedirects;
    }

    /**
     * Gets whether HTTP redirects will be followed.
     * 
     * @param followRedirects true if redirects are followed, false otherwise
     */
    public void setHttpFollowRedirects(final boolean followRedirects) {
        httpFollowRedirects = followRedirects;
    }

    /**
     * Gets the character set used with the HTTP entity (body).
     * 
     * @return character set used with the HTTP entity (body)
     */
    @Nullable public String getHttpContentCharSet() {
        return httpContentCharSet;
    }

    /**
     * Sets the character set used with the HTTP entity (body).
     * 
     * @param charSet character set used with the HTTP entity (body)
     */
    public void setHttpContentCharSet(@Nullable final String charSet) {
        httpContentCharSet = charSet;
    }

    /**
     * Gets user agent.
     * 
     * @return The user agent.
     */
    @Nullable public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets user agent.
     * 
     * @param what what to set.  If this is null Apache will use the default.
     */
    public void setUserAgent(@Nullable final String what) {
        userAgent = what;
    }

    /**
     * Get the handler which determines if a request should be retried after a recoverable exception during execution.
     * 
     * @return handler which determines if a request should be retried
     */
    @Nullable public HttpRequestRetryHandler getHttpRequestRetryHandler() {
        return retryHandler;
    }

    /**
     * Set the handler which determines if a request should be retried after a recoverable exception during execution.
     * 
     * @param handler handler which determines if a request should be retried
     */
    public void setHttpRequestRetryHandler(@Nullable final HttpRequestRetryHandler handler) {
        retryHandler = handler;
    }

    /**
     * Get the handler which determines if a request should be retried given the response from the target server.
     * 
     * @return handler which determines if a request should be retried
     */
    @Nullable public ServiceUnavailableRetryStrategy getServiceUnavailableRetryHandler() {
        return serviceUnavailStrategy;
    }

    /**
     * Set the strategy which determines if a request should be retried given the response from the target server.
     * 
     * @param strategy handler which determines if a request should be retried
     */
    public void setServiceUnavailableRetryHandler(@Nullable final ServiceUnavailableRetryStrategy strategy) {
        serviceUnavailStrategy = strategy;
    }
    
    /** 
     * Get the flag for disabling auth caching.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableAuthCaching() {
        return disableAuthCaching;
    }

    /** 
     * Set the flag for disabling auth caching.
     * 
     * @param flag true if disabled, false if not
     */
    public void setDisableAuthCaching(final boolean flag) {
        disableAuthCaching = flag;
    }

    /** 
     * Get the flag for disabling automatic retries.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableAutomaticRetries() {
        return disableAutomaticRetries;
    }

    /** 
     * Set the flag for disabling automatic retries.
     * 
     * @param flag true if disabled, false if not
     */
    public void setDisableAutomaticRetries(final boolean flag) {
        disableAutomaticRetries = flag;
    }

    /** 
     * Get the flag for disabling connection state.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableConnectionState() {
        return disableConnectionState;
    }
    
    /** 
     * Set the flag for disabling connection state.
     * 
     * @param flag true if disabled, false if not
     */
 
    public void setDisableConnectionState(final boolean flag) {
        disableConnectionState = flag;
    }

    /** 
     * Get the flag for disabling content compression.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableContentCompression() {
        return disableContentCompression;
    }

    /** 
     * Set the flag for disabling content compression.
     * 
     * @param flag true if disabled, false if not
     */
    public void setDisableContentCompression(final boolean flag) {
        disableContentCompression = flag;
    }

    /** 
     * Get the flag for disabling cookie management.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableCookieManagement() {
        return disableCookieManagement;
    }

    /** 
     * Set the flag for disabling cookie management.
     * 
     * @param flag true if disabled, false if not
     */
    public void setDisableCookieManagement(final boolean flag) {
        disableCookieManagement = flag;
    }

    /** 
     * Get the flag for disabling redirect handling.
     * 
     * @return true if disabled, false if not
     */
    public boolean isDisableRedirectHandling() {
        return disableRedirectHandling;
    }

    /** 
     * Set the flag for disabling redirect handling.
     * 
     * @param flag true if disabled, false if not
     */
    public void setDisableRedirectHandling(final boolean flag) {
        disableRedirectHandling = flag;
    }

    /**
     * Get the flag enabling use of system properties.
     * 
     * @return true if enabled, false if not
     */
    public boolean isUseSystemProperties() {
        return useSystemProperties;
    }

    /**
     * Set the flag enabling use of system properties.
     * 
     * @param flag true if enabled, false if not
     */
    public void setUseSystemProperties(final boolean flag) {
        useSystemProperties = flag;
    }

    /**
     * Get the list of request interceptors to add first.
     * 
     * @return the list of interceptors
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public List<HttpRequestInterceptor> getFirstRequestInterceptors() {
        return requestInterceptorsFirst;
    }
    
    /**
     * Set the list of request interceptors to add first.
     * 
     * @param interceptors the list of interceptors, may be null
     */
    public void setFirstRequestInterceptors(
            @Nullable @NonnullElements final List<HttpRequestInterceptor> interceptors) {
        if (interceptors != null) {
            requestInterceptorsFirst = List.copyOf(interceptors);
        } else {
            requestInterceptorsFirst = Collections.emptyList();
        }
    }

    /**
     * Get the list of request interceptors to add last.
     * 
     * @return the list of interceptors
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public List<HttpRequestInterceptor> getLastRequestInterceptors() {
        return requestInterceptorsLast;
    }

    /**
     * Set the list of request interceptors to add last.
     * 
     * @param interceptors the list of interceptors, may be null
     */
    public void setLastRequestInterceptors(@Nullable @NonnullElements final List<HttpRequestInterceptor> interceptors) {
        if (interceptors != null) {
            requestInterceptorsLast = List.copyOf(interceptors);
        } else {
            requestInterceptorsLast = Collections.emptyList();
        }
    }

    /**
     * Get the list of response interceptors to add first.
     * 
     * @return the list of interceptors
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public List<HttpResponseInterceptor> getFirstResponseInterceptors() {
        return responseInterceptorsFirst;
    }

    /**
     * Set the list of response interceptors to add first.
     * 
     * @param interceptors the list of interceptors, may be null
     */
    public void setFirstResponseInterceptors(
            @Nullable @NonnullElements final List<HttpResponseInterceptor> interceptors) {
        if (interceptors != null) {
            responseInterceptorsFirst = List.copyOf(interceptors);
        } else {
            responseInterceptorsFirst = Collections.emptyList();
        }
    }

    /**
     * Get the list of response interceptors to add last.
     * 
     * @return the list of interceptors
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public List<HttpResponseInterceptor> getLastResponseInterceptors() {
        return responseInterceptorsLast;
    }

    /**
     * Set the list of response interceptors to add last.
     * 
     * @param interceptors the list of interceptors, may be null
     */
    public void setLastResponseInterceptors(
            @Nullable @NonnullElements final List<HttpResponseInterceptor> interceptors) {
        if (interceptors != null) {
            responseInterceptorsLast = List.copyOf(interceptors);
        } else {
            responseInterceptorsLast = Collections.emptyList();
        }
    }

    /**
     * Get the list of static {@link HttpClientContextHandler}.
     * 
     * @return the list of handlers
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public List<HttpClientContextHandler> getStaticContextHandlers() {
        return staticContextHandlers;
    }

    /**
     * Set the list of static {@link HttpClientContextHandler}.
     * 
     * @param handlers the list of handlers, may be null
     */
    public void setStaticContextHandlers(@Nullable @NonnullElements final List<HttpClientContextHandler> handlers) {
        if (handlers != null) {
            staticContextHandlers = List.copyOf(handlers);
        } else {
            staticContextHandlers = Collections.emptyList();
        }
    }

    /**
     * Constructs an {@link HttpClient} using the settings of this builder.
     * 
     * @return the constructed client
     * 
     * @throws Exception if there is any problem building the new client instance
     */
    public HttpClient buildClient() throws Exception {
        decorateApacheBuilder();
        return new ContextHandlingHttpClient(getApacheBuilder().build(), getStaticContextHandlers());
    }

    /**
     * Decorate the Apache builder as determined by this builder's parameters. Subclasses will likely add additional
     * decoration.
     * 
     * @throws Exception if there is a problem decorating the Apache builder
     */
    // Checkstyle: CyclomaticComplexity|MethodLength OFF
    @SuppressWarnings("deprecation")
    protected void decorateApacheBuilder() throws Exception {
        final org.apache.http.impl.client.HttpClientBuilder builder = getApacheBuilder();
        
        if (getTLSSocketFactory() != null) {
            builder.setSSLSocketFactory(getTLSSocketFactory());
        } else if (connectionDisregardTLSCertificate) {
            builder.setSSLSocketFactory(HttpClientSupport.buildNoTrustTLSSocketFactory());
        } else {
            builder.setSSLSocketFactory(HttpClientSupport.buildStrictTLSSocketFactory());
        }

        if (connectionCloseAfterResponse) {
            if (!getFirstRequestInterceptors().stream().anyMatch(RequestConnectionClose.class::isInstance)
                    && !getLastRequestInterceptors().stream().anyMatch(RequestConnectionClose.class::isInstance)) {
                builder.addInterceptorLast(new RequestConnectionClose());
            }
        }
        
        if (maxConnectionsTotal > 0) {
            builder.setMaxConnTotal(maxConnectionsTotal);
        }
        
        if (maxConnectionsPerRoute > 0) {
            builder.setMaxConnPerRoute(maxConnectionsPerRoute);
        }
        
        if (retryHandler != null) {
            builder.setRetryHandler(retryHandler);
        }
        
        if (serviceUnavailStrategy != null) {
            builder.setServiceUnavailableRetryStrategy(serviceUnavailStrategy);
        }
        
        // These boolean and interceptor properties can otherwise only be supplied
        // to the Apache builder via a fluent-style API.
        
        if (isDisableAuthCaching()) {
            builder.disableAuthCaching();
        }

        if (isDisableAutomaticRetries()) {
            builder.disableAutomaticRetries();
        }

        if (isDisableConnectionState()) {
           builder.disableConnectionState();
        }

        if (isDisableContentCompression()) {
            builder.disableContentCompression();
        }

        if (isDisableCookieManagement()) {
            builder.disableCookieManagement();
        }

        if (isDisableRedirectHandling()) {
            builder.disableRedirectHandling();
        }

        if (isUseSystemProperties()) {
            builder.useSystemProperties();
        }

        getFirstRequestInterceptors().forEach(builder::addInterceptorFirst);
        getLastRequestInterceptors().forEach(builder::addInterceptorLast);
        getFirstResponseInterceptors().forEach(builder::addInterceptorFirst);
        getLastResponseInterceptors().forEach(builder::addInterceptorLast);

        // RequestConfig params
        final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        if (socketLocalAddress != null) {
            requestConfigBuilder.setLocalAddress(socketLocalAddress);
        }

        if (!socketTimeout.isNegative()) {
            requestConfigBuilder.setSocketTimeout((int) socketTimeout.toMillis());
        }

        if (!connectionTimeout.isNegative()) {
            requestConfigBuilder.setConnectTimeout((int) connectionTimeout.toMillis());
        }
        
        if (!connectionRequestTimeout.isNegative()) {
            requestConfigBuilder.setConnectionRequestTimeout((int) connectionRequestTimeout.toMillis());
        }
        
        // TODO: When we remove the deprecated 'connectionStaleCheck' when we move to HC 5,
        // be sure to also remove the @SuppressWarnings("deprecation") on this method.
        requestConfigBuilder.setStaleConnectionCheckEnabled(connectionStaleCheck);

        requestConfigBuilder.setRedirectsEnabled(httpFollowRedirects);

        if (connectionProxyHost != null) {
            final HttpHost proxyHost = new HttpHost(connectionProxyHost, connectionProxyPort);
            requestConfigBuilder.setProxy(proxyHost);

            if (connectionProxyUsername != null && connectionProxyPassword != null) {
                final CredentialsProvider credProvider = new BasicCredentialsProvider();
                credProvider.setCredentials(new AuthScope(connectionProxyHost, connectionProxyPort),
                        new UsernamePasswordCredentials(connectionProxyUsername, connectionProxyPassword));
                builder.setDefaultCredentialsProvider(credProvider);
            }
        }

        // ConnectionConfig params
        final ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();

        connectionConfigBuilder.setBufferSize(socketBufferSize);

        if (httpContentCharSet != null) {
            connectionConfigBuilder.setCharset(CharsetUtils.get(httpContentCharSet));
        }

        builder.setDefaultRequestConfig(requestConfigBuilder.build());
        builder.setDefaultConnectionConfig(connectionConfigBuilder.build());
        
        if (null != userAgent) {
            builder.setUserAgent(userAgent);
        }
    }
    // Checkstyle: CyclomaticComplexity|MethodLength ON

    /**
     * Get the Apache {@link org.apache.http.impl.client.HttpClientBuilder} instance over which this builder will be
     * layered. Subclasses may override to return a specialized subclass.
     * 
     * @return the Apache HttpClientBuilder instance to use
     */
    protected org.apache.http.impl.client.HttpClientBuilder getApacheBuilder() {
        return apacheBuilder;
    }

}