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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.Beta;

import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** Helper methods for building {@link URI}s and parsing some HTTP URL information. */
public final class URISupport {

    /** Constructor. */
    private URISupport() {
    }

    /**
     * Sets the fragment of a URI.
     * 
     * @param prototype prototype URI that provides information other than the fragment
     * @param fragment fragment for the new URI
     * 
     * @return new URI built from the prototype URI and the given fragment
     */
    public static URI setFragment(final URI prototype, final String fragment) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), prototype.getHost(), prototype.getPort(),
                    prototype.getPath(), prototype.getQuery(), trimOrNullFragment(fragment));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal fragment text", e);
        }
    }

    /**
     * Sets the host of a URI.
     * 
     * @param prototype prototype URI that provides information other than the host
     * @param host host for the new URI
     * 
     * @return new URI built from the prototype URI and the given host
     */
    public static URI setHost(final URI prototype, final String host) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), StringSupport.trimOrNull(host),
                    prototype.getPort(), prototype.getPath(), prototype.getQuery(), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal host", e);
        }
    }

    /**
     * Sets the path of a URI.
     * 
     * @param prototype prototype URI that provides information other than the path
     * @param path path for the new URI
     * 
     * @return new URI built from the prototype URI and the given path
     */
    public static URI setPath(final URI prototype, final String path) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), prototype.getHost(), prototype.getPort(),
                    trimOrNullPath(path), prototype.getQuery(), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal path", e);
        }
    }

    /**
     * Sets the port of a URI.
     * 
     * @param prototype prototype URI that provides information other than the port
     * @param port port for the new URI
     * 
     * @return new URI built from the prototype URI and the given port
     */
    public static URI setPort(final URI prototype, final int port) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), prototype.getHost(), port,
                    prototype.getPath(), prototype.getQuery(), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal port", e);
        }
    }

    /**
     * Sets the query of a URI.
     * 
     * <p>
     * <b>WARNING:</b> If the supplied query parameter names and/or values contain '%' characters 
     * (for example because they are already Base64-encoded), then the approach of using {@link URI} 
     * instances to work with the URI/URL may not be appropriate.  Per its documentation, the 
     * {@link URI} constructors always encode '%' characters, which can lead to cases of double-encoding.
     * For an alternative way of manipulating URL's see {@link URLBuilder}.
     * </p>
     * 
     * @param prototype prototype URI that provides information other than the query
     * @param query query for the new URI
     * 
     * @return new URI built from the prototype URI and the given query
     */
    public static URI setQuery(final URI prototype, final String query) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), prototype.getHost(), prototype.getPort(),
                    prototype.getPath(), trimOrNullQuery(query), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal query", e);
        }
    }

    /**
     * Sets the query of a URI.
     * 
     * <p>
     * <b>WARNING:</b> If the supplied query parameter names and/or values contain '%' characters 
     * (for example because they are already Base64-encoded), then the approach of using {@link URI} 
     * instances to work with the URI/URL may not be appropriate.  Per its documentation, the 
     * {@link URI} constructors always encode '%' characters, which can lead to cases of double-encoding.
     * For an alternative way of manipulating URL's see {@link URLBuilder}.
     * </p>
     * 
     * @param prototype prototype URI that provides information other than the query
     * @param parameters query parameters for the new URI
     * 
     * @return new URI built from the prototype URI and the given query
     */
    public static URI setQuery(final URI prototype, final List<Pair<String, String>> parameters) {
        try {
            return new URI(prototype.getScheme(), prototype.getUserInfo(), prototype.getHost(), prototype.getPort(),
                    prototype.getPath(), buildQuery(parameters), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal query", e);
        }
    }

    /**
     * Sets the scheme of a URI.
     * 
     * @param prototype prototype URI that provides information other than the scheme
     * @param scheme scheme for the new URI
     * 
     * @return new URI built from the prototype URI and the given scheme
     */
    public static URI setScheme(final URI prototype, final String scheme) {
        try {
            return new URI(StringSupport.trimOrNull(scheme), prototype.getUserInfo(), prototype.getHost(),
                    prototype.getPort(), prototype.getPath(), prototype.getQuery(), prototype.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Illegal scheme", e);
        }
    }
    
    /**
     * Create a file: URI from an absolute path, dealing with the Windows, non leading "/" issue.
     *
     * <p>Windows absolute paths have a habit of starting with a "DosDeviceName" (such as <code>C:\absolute\path</code>
     * if we blindly convert that to a file URI by prepending "file://", then we end up with a URI which has "C:" as 
     * the network segment.  So if we need to have an absolute file path based URI (JAAS is the example) we call this
     * method which hides the hideous implementation.</p>
     *
     * @param path the absolute file path to convert
     * @return a suitable URI
     * @throws URISyntaxException if the URI contructor fails
     */
    public static URI fileURIFromAbsolutePath(final String path) throws URISyntaxException {
        final StringBuilder uriPath = new StringBuilder(path.length()+8);
        
        uriPath.append("file://");
        if (!path.startsWith("/")) {
            // it's windows
            uriPath.append('/');
        }
        uriPath.append(path);
        return new URI(uriPath.toString());
    }

    /**
     * Builds an RFC-3968 encoded URL query component from a collection of parameters.
     * 
     * @param parameters collection of parameters from which to build the URL query component, may be null or empty
     * 
     * @return RFC-3968 encoded URL query or null if the parameter collection was null or empty
     */
    public static String buildQuery(final List<Pair<String, String>> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        boolean firstParam = true;
        for (final Pair<String, String> parameter : parameters) {
            if (firstParam) {
                firstParam = false;
            } else {
                builder.append("&");
            }

            builder.append(doURLEncode(parameter.getFirst()));
            builder.append("=");
            if (parameter.getSecond() != null) {
                builder.append(doURLEncode(parameter.getSecond()));
            }
        }

        return builder.toString();
    }
    
    /**
     * Builds a map from a collection of parameters.
     * 
     * @param parameters collection of parameters from which to build the corresponding, may be null or empty
     * 
     * @return a non-null map of query parameter name-&gt; value. Keys will be non-null. Values may be null.
     */
    @Nonnull public static Map<String,String> buildQueryMap(@Nullable final List<Pair<String, String>> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return Collections.emptyMap();
        }
        
        final HashMap<String,String> map = new HashMap<>();
        for (final Pair<String,String> param : parameters) {
            if (param.getFirst() != null) {
                map.put(param.getFirst(), param.getSecond());
            }
        }
        
        return map;
    }

    /**
     * Get the first raw (i.e.RFC-3968 encoded) query string component with the specified parameter name. This method
     * assumes the common query string format of one or more 'paramName=paramValue' pairs separated by '&amp;'.
     * 
     * The component will be returned as a string in the form 'paramName=paramValue' (minus the quotes).
     * 
     * @param queryString the URL encoded HTTP URL query string
     * @param paramName the URL decoded name of the parameter to find
     * @return the found component, or null if query string or param name is null/empty or the parameter is not found
     */
    @Nullable public static String getRawQueryStringParameter(@Nullable final String queryString,
            @Nullable final String paramName) {
        final String trimmedQuery = trimOrNullQuery(queryString);
        final String trimmedName = StringSupport.trimOrNull(paramName);
        if (trimmedQuery == null || trimmedName == null) {
            return null;
        }

        final String encodedName = doURLEncode(trimmedName);
        
        final String[] candidates = trimmedQuery.split("&");
        for (final String candidate : candidates) {
            if (candidate.startsWith(encodedName+"=") || candidate.equals(encodedName)) {
                return candidate;
            }
        }
        
        return null;
    }

    /**
     * Parses a RFC-3968 encoded query string in to a set of name/value pairs. This method assumes the common query
     * string format of one or more 'paramName=paramValue' pairs separate by '&amp;'. Both parameter names and values
     * will be URL decoded. Parameters without values will be represented in the returned map as a key associated with
     * the value <code>null</code>.
     * 
     * @param queryString URL encoded query string
     * 
     * @return the parameters from the query string, never null
     */
    public static List<Pair<String, String>> parseQueryString(final String queryString) {
        final String trimmedQuery = trimOrNullQuery(queryString);
        if (trimmedQuery == null) {
            return new LazyList<>();
        }

        final ArrayList<Pair<String, String>> queryParams = new ArrayList<>();
        final String[] paramPairs = trimmedQuery.split("&");
        String[] param;
        for (final String paramPair : paramPairs) {
            param = paramPair.split("=");
            if (param.length == 1) {
                queryParams.add(new Pair<>(doURLDecode(param[0]), (String) null));
            } else {
                queryParams.add(new Pair<>(doURLDecode(param[0]), doURLDecode(param[1])));
            }
        }

        return queryParams;
    }

    /**
     * Trims an RFC-3968 encoded URL path component. If the given path is null or empty then null is returned. If the
     * given path ends with '?' then it is removed. If the given path ends with '#' then it is removed.
     * 
     * @param path path to trim
     * 
     * @return the trimmed path or null
     */
    public static String trimOrNullPath(final String path) {
        String trimmedPath = StringSupport.trimOrNull(path);
        if (trimmedPath == null) {
            return null;
        }

        if (trimmedPath.startsWith("?")) {
            trimmedPath = trimmedPath.substring(1);
        }

        if (trimmedPath.endsWith("?") || trimmedPath.endsWith("#")) {
            trimmedPath = trimmedPath.substring(0, trimmedPath.length() - 1);
        }

        return trimmedPath;
    }

    /**
     * Trims an RFC-3968 encoded URL query component. If the given query is null or empty then null is returned. If the
     * given query starts with '?' then it is removed. If the given query ends with '#' then it is removed.
     * 
     * @param query query to trim
     * 
     * @return the trimmed query or null
     */
    public static String trimOrNullQuery(final String query) {
        String trimmedQuery = StringSupport.trimOrNull(query);
        if (trimmedQuery == null) {
            return null;
        }

        if (trimmedQuery.startsWith("?")) {
            trimmedQuery = trimmedQuery.substring(1);
        }

        if (trimmedQuery.endsWith("#")) {
            trimmedQuery = trimmedQuery.substring(0, trimmedQuery.length() - 1);
        }

        return trimmedQuery;
    }

    /**
     * Trims an RFC-3968 encoded URL fragment component. If the given fragment is null or empty then null is returned.
     * If the given fragment starts with '#' then it is removed.
     * 
     * @param fragment fragment to trim
     * 
     * @return the trimmed fragment or null
     */
    public static String trimOrNullFragment(final String fragment) {
        String trimmedFragment = StringSupport.trimOrNull(fragment);
        if (trimmedFragment == null) {
            return null;
        }

        if (trimmedFragment.startsWith("#")) {
            trimmedFragment = trimmedFragment.substring(1);
        }

        return trimmedFragment;
    }

    /**
     * Perform URL decoding on the given string.
     * 
     * @param value the string to decode
     * @return the decoded string
     */
    public static String doURLDecode(final String value) {
        if (value == null) {
            return null;
        }

        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // UTF-8 encoding is required to be supported by all JVMs
            return null;
        }
    }

    /**
     * Perform URL encoding on the given string appropriate for form or query string parameters.
     * 
     * <p>This method is <strong>not</strong> appropriate for the encoding of data for other
     * parts of a URL such as a path or fragment.</p>
     * 
     * <p>Consider using Guava's UrlEscapers class for any future uses for this functionality.</p>
     * 
     * @param value the string to encode
     * @return the encoded string
     * 
     * @deprecated
     */
    @Deprecated
    public static String doURLEncode(final String value) {
        if (value == null) {
            return null;
        }

        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // UTF-8 encoding is required to be supported by all JVMs
            return null;
        }
    }
}