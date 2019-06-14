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

package net.shibboleth.utilities.java.support.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Utility class for building URLs. May also be used to parse a URL into its individual components.
 * All components will be converted UTF-8 encoding and then application/x-www-form-urlencoded when built.
 * 
 * This class is not thread-safe.
 */
public class URLBuilder {

    /** URL schema (http, https, etc). */
    @Nullable private String scheme;

    /** User name in the URL. */
    @Nullable private String username;

    /** Password in the URL. */
    @Nullable private String password;

    /** Host for the URL. */
    @Nullable private String host;

    /** URL port number. */
    @Nullable private Integer port;

    /** URL path. */
    @Nullable private String path;

    /** Parameters in the query string. */
    @Nonnull private List<Pair<String, String>> queryParams;

    /** URL fragment. */
    private String fragement;

    /**
     * Constructor.
     */
    public URLBuilder() {
        queryParams = new ArrayList<>();
    }

    /**
     * Constructor.
     * 
     * @param baseURL URL to parse and use as basis for creating other URLs
     * 
     * @throws MalformedURLException thrown if the given base URL is not well formed
     * 
     */
    public URLBuilder(@Nonnull @NotEmpty final String baseURL) throws MalformedURLException {
        final URL url = new URL(baseURL);
        
        setScheme(url.getProtocol());
        
        final String userInfo = url.getUserInfo();
        if (!Strings.isNullOrEmpty(userInfo)) {
            if (userInfo.contains(":")) {
                final String[] userInfoComps = userInfo.split(":");
                setUsername(URISupport.doURLDecode(userInfoComps[0]));
                setPassword(URISupport.doURLDecode(userInfoComps[1]));
            } else {
                setUsername(userInfo);
            }
        }
        
        setHost(url.getHost());
        if (url.getPort() > 0) {
            setPort(url.getPort());
        }
        setPath(url.getPath());
        
        queryParams = new ArrayList<>();
        final String queryString = url.getQuery();
        if (!Strings.isNullOrEmpty(queryString)) {
            final String[] queryComps = queryString.split("&");
            for (final String queryComp :queryComps) {
                final int where = queryComp.indexOf('=');
                if (where < 0) {
                    final String paramName = URISupport.doURLDecode(queryComp);
                    queryParams.add(new Pair<String, String>(paramName, null));
                } else {
                    String paramName = URISupport.doURLDecode(queryComp.substring(0, where));
                    if ("".equals(paramName)) {
                        paramName = null;
                    }
                    String paramValue = URISupport.doURLDecode(queryComp.substring(where+1));
                    if ("".equals(paramValue)) {
                        paramValue = null;
                    }
                    queryParams.add(new Pair<>(paramName, paramValue));
                }
            }
        }
        
        setFragment(url.getRef());
    }

    /**
     * Gets the URL fragment in its decoded form.
     * 
     * @return URL fragment in its decoded form
     */
    @Nullable public String getFragment() {
        return fragement;
    }

    /**
     * Sets the URL fragment in its decoded form.
     * 
     * @param newFragment URL fragment in its decoded form
     */
    public void setFragment(@Nullable final String newFragment) {
        fragement = StringSupport.trimOrNull(newFragment);
    }

    /**
     * Gets the host component of the URL.
     * 
     * @return host component of the URL
     */
    @Nullable public String getHost() {
        return host;
    }

    /**
     * Sets the host component of the URL.
     * 
     * @param newHost host component of the URL
     */
    public void setHost(@Nullable final String newHost) {
        host = StringSupport.trimOrNull(newHost);
    }

    /**
     * Gets the user's password in the URL.
     * 
     * @return user's password in the URL
     */
    @Nullable public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password in the URL.
     * 
     * @param newPassword user's password in the URL
     */
    public void setPassword(@Nullable final String newPassword) {
        password = StringSupport.trimOrNull(newPassword);
    }

    /**
     * Gets the path component of the URL.
     * 
     * @return path component of the URL
     */
    @Nullable public String getPath() {
        return path;
    }

    /**
     * Sets the path component of the URL.
     * 
     * @param newPath path component of the URL
     */
    public void setPath(@Nullable final String newPath) {
        path = StringSupport.trimOrNull(newPath);
    }

    /**
     * Gets the port component of the URL.
     * 
     * @return port component of the URL
     */
    @Nullable public Integer getPort() {
        return port;
    }

    /**
     * Sets the port component of the URL.
     * 
     * @param newPort port component of the URL
     */
    public void setPort(@Nullable final Integer newPort) {
        port = newPort;
    }

    /**
     * Gets the query string parameters for the URL. Params may be added and removed through the map interface.
     * 
     * @return query string parameters for the URL
     */
    @Nonnull public List<Pair<String, String>> getQueryParams() {
        return queryParams;
    }

    /**
     * Gets the URL scheme (http, https, etc).
     * 
     * @return URL scheme (http, https, etc)
     */
    @Nullable public String getScheme() {
        return scheme;
    }

    /**
     * Sets the URL scheme (http, https, etc).
     * 
     * @param newScheme URL scheme (http, https, etc)
     */
    public void setScheme(@Nullable final String newScheme) {
        scheme = StringSupport.trimOrNull(newScheme);
    }

    /**
     * Gets the user name component of the URL.
     * 
     * @return user name component of the URL
     */
    @Nullable public String getUsername() {
        return username;
    }

    /**
     * Sets the user name component of the URL.
     * 
     * @param newUsername user name component of the URL
     */
    public void setUsername(@Nullable final String newUsername) {
        username = StringSupport.trimOrNull(newUsername);
    }

    /**
     * Builds a URL from the given data. The constructed URL may not be valid if sufficient information is not
     * provided. The returned URL will be appropriately encoded using application/x-www-form-urlencoded with appropriate
     * encoding of UTF-8 characters.
     * 
     * @return URL built from the given data
     */
    // Checkstyle: CyclomaticComplexity OFF
    @Nullable public String buildURL() {
        final StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(scheme)) {
            builder.append(scheme);
            builder.append("://");
        }

        if (!Strings.isNullOrEmpty(username)) {
            builder.append(username);
            if (!Strings.isNullOrEmpty(password)) {
                builder.append(":");
                builder.append(password);
            }

            builder.append("@");
        }

        if (!Strings.isNullOrEmpty(host)) {
            builder.append(host);
            if (port != null && port > 0) {
                builder.append(":");
                builder.append(port);
            }
        }

        if (!Strings.isNullOrEmpty(path)) {
            if (!path.startsWith("/")) {
                builder.append("/");
            }
            builder.append(path);
        }

        final String queryString = buildQueryString();
        if (!Strings.isNullOrEmpty(queryString)) {
            builder.append("?");
            builder.append(queryString);
        }

        if (!Strings.isNullOrEmpty(fragement)) {
            builder.append("#");
            builder.append(fragement);
        }

        return builder.toString();
    }
    // Checkstyle: CyclomaticComplexity ON

    /**
     * Builds the query string for the URL.
     * 
     * @return query string for the URL or null if there are now query parameters
     */
    @SuppressWarnings("deprecation")
    @Nullable public String buildQueryString() {
        if (queryParams.size() > 0) {

            String name;
            String value;
            final StringBuilder builder = new StringBuilder();

            Pair<String, String> param;
            for (int i = 0; i < queryParams.size(); i++) {
                param = queryParams.get(i);
                name = StringSupport.trimOrNull(param.getFirst());

                if (name != null) {
                    builder.append(URISupport.doURLEncode(name));
                    value = StringSupport.trimOrNull(param.getSecond());
                    if (value != null) {
                        builder.append("=");
                        builder.append(URISupport.doURLEncode(value));
                    }
                    if (i < queryParams.size() - 1) {
                        builder.append("&");
                    }
                }
            }
            return builder.toString();
        }

        return null;
    }
    
}