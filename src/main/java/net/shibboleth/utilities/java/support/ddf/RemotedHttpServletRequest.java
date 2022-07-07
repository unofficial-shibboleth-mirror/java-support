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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ReadListener;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.net.URISupport;

/**
 * Wraps a {@link DDF} object reflecting a remoted message encapsulating an HTTP request.
 * 
 * <p>Potential TODOs are noted in various places. One outstanding issue is header case.
 * The SP code never did case-folding of the header names, so we may need to adjust that
 * on the C++ side to force-lower them in transit and then lower them on access.</p>
 * 
 */
@NotThreadSafe
public class RemotedHttpServletRequest implements HttpServletRequest {

    /** Empty byte array for empty bodies. */
    @Nonnull private static final byte[] EMPTY_BODY = new byte[0];
    
    /** UTF-8 decoder. */
    @Nonnull private static final CharsetDecoder UTF_8 =
            Charset.forName("UTF-8").newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

    /** ISO single byte decoder. */
    @Nonnull private static final CharsetDecoder ISO_8859_1 =
            Charset.forName("ISO-8859-1").newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

    /** Underlying object containing remoted data. */
    @Nonnull private final DDF obj;
    
    /** Cookie array. */
    @Nullable @NonnullElements private List<Cookie> cookies;
    
    /** Parameter map. */
    @Nullable private Map<String, String[]> parameters;
    
    /**
     * Constructor.
     *
     * @param ddf remoted request information
     */
    public RemotedHttpServletRequest(@Nonnull final DDF ddf) {
        obj = Constraint.isNotNull(ddf, "DDF cannot be null");
    }
    
    /**
     * Gets the underlying object containing the remoted data.
     * 
     * @return remoted data object
     */
    @Nonnull public DDF getDDF() {
        return obj;
    }
    
    /** {@inheritDoc} */
    public Object getAttribute(final String name) {
        // TODO: might use this to expose certain "standard" pieces of information
        return null;
    }

    /** {@inheritDoc} */
    public Enumeration<String> getAttributeNames() {
        // TODO: adjust if we support the method above.
        return Collections.emptyEnumeration();
    }

    /** {@inheritDoc} */
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    /** {@inheritDoc} */
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public int getContentLength() {
        final Integer i = obj.getmember("content_length").integer();
        return i != null ? i : -1;
    }

    /** {@inheritDoc} */
    public long getContentLengthLong() {
        return getContentLength();
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return obj.getmember("content_type").string();
    }

    /** {@inheritDoc} */
    public ServletInputStream getInputStream() throws IOException {
        final String body = obj.getmember("body").string();
        if (body != null) {
            // The body is always assumed to be safely encoded for our use cases.
            return new BodyInputStream(body.getBytes("UTF-8"));
        }
        return new BodyInputStream(EMPTY_BODY);
    }

    /** {@inheritDoc} */
    public String getParameter(final String name) {
        final String[] values = getParameterMap().get(name);
        if (values != null) {
            return values[0];
        }
        
        return null;
    }

    /** {@inheritDoc} */
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    /** {@inheritDoc} */
    public String[] getParameterValues(final String name) {
        return getParameterMap().get(name);
    }

    /** {@inheritDoc} */
    public Map<String, String[]> getParameterMap() {
        if (parameters == null) {
            parameters = new HashMap<>();
            final Multimap<String,String> multimap = ArrayListMultimap.create();
            final String qs = getQueryString();
            if (qs != null) {
                final List<Pair<String,String>> qparams = URISupport.parseQueryString(qs);
                for (final Pair<String,String> p : qparams) {
                    multimap.put(p.getFirst(), p.getSecond());
                }
            }
            
            if ("application/x-www-form-urlencoded".equals(getContentType())) {
                final String body = obj.getmember("body").string();
                if (body != null) {
                    final List<Pair<String,String>> qparams = URISupport.parseQueryString(body);
                    for (final Pair<String,String> p : qparams) {
                        multimap.put(p.getFirst(), p.getSecond());
                    }
                }
            }
            
            for (final Map.Entry<String,Collection<String>> entry : multimap.asMap().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
            }
        }
        
        return parameters;
    }

    /** {@inheritDoc} */
    public String getProtocol() {
        final String protocol = obj.getmember("protocol").string();
        return protocol != null ? protocol : "HTTP/1.1";
    }

    /** {@inheritDoc} */
    public String getScheme() {
        return obj.getmember("scheme").string();
    }

    /** {@inheritDoc} */
    public String getServerName() {
        return decodeUnsafeString(obj.getmember("hostname").unsafe_string());
    }

    /** {@inheritDoc} */
    public int getServerPort() {
        final Integer i = obj.getmember("port").integer();
        return i != null ? i : -1;
    }

    /** {@inheritDoc} */
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(obj.getmember("body").string()));
    }

    /** {@inheritDoc} */
    public String getRemoteAddr() {
        return obj.getmember("client_addr").string();
    }

    /** {@inheritDoc} */
    public String getRemoteHost() {
        return obj.getmember("client_addr").string();
    }

    /** {@inheritDoc} */
    public void setAttribute(final String name, final Object o) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void removeAttribute(final String name) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        throw new UnsupportedOperationException();        
    }

    /** {@inheritDoc} */
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public boolean isSecure() {
        return "https".equals(getScheme());
    }

    /** {@inheritDoc} */
    public RequestDispatcher getRequestDispatcher(final String path) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getRealPath(final String path) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        // TODO: If we need this, should be configurable via the c'tor.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getLocalAddr() {
        // TODO: If we need this, should be configurable via the c'tor.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public int getLocalPort() {
        return getServerPort();
    }

    /** {@inheritDoc} */
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public boolean isAsyncStarted() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isAsyncSupported() {
        return false;
    }

    /** {@inheritDoc} */
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getAuthType() {
        return null;
    }

    /** {@inheritDoc} */
    public Cookie[] getCookies() {
        if (cookies == null) {
            final String header = getHeader("Cookie");
            if (header != null) {
                final String[] carray = header.split(";");
                if (carray != null) {
                    cookies = new ArrayList<>(carray.length);
                    for (final String c : carray) {
                        final String[] nvpair = c.split("=", -1);
                        if (nvpair.length == 2) {
                            final String name = nvpair[0].trim();
                            // This is a fallback cookie used for Safari to work around SameSite bugs.
                            if (name.endsWith("_fgwars")) {
                                name.substring(0, name.length() - 7);
                            }
                            cookies.add(new Cookie(name, nvpair[1]));
                        }
                    }
                } else {
                    cookies = Collections.emptyList();
                }
            } else {
                cookies = Collections.emptyList();
            }
        }
        
        if (cookies.isEmpty()) {
            return null;
        }
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    /** {@inheritDoc} */
    public long getDateHeader(final String name) {
        final DDF h = obj.getmember("headers").getmember(name);
        if (h.isstring()) {
            try {
                // TODO: there are a ton of valid formats but I don't think we really will need this anyway.
                final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                final Date d = formatter.parse(h.string());
                if (d != null) {
                    return d.getTime();
                }
            } catch (final Exception e) {
                // Ignore for now.
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    public String getHeader(final String name) {
        return decodeUnsafeString(obj.getmember("headers").getmember(name).unsafe_string());
    }

    /** {@inheritDoc} */
    public Enumeration<String> getHeaders(final String name) {
        final String s = decodeUnsafeString(obj.getmember("headers").getmember(name).unsafe_string());
        if (s != null) {
            return Collections.enumeration(Collections.singletonList(s));
        }
        return Collections.emptyEnumeration();
    }

    /** {@inheritDoc} */
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(obj.getmember("headers").asMap().keySet());
    }

    /** {@inheritDoc} */
    public int getIntHeader(final String name) {
        final DDF h = obj.getmember("headers").getmember(name);
        if (h.isstring()) {
            return Integer.parseInt(decodeUnsafeString(h.unsafe_string()));
        }
        return -1;
    }

    /** {@inheritDoc} */
    public String getMethod() {
        return obj.getmember("method").string();
    }

    /** {@inheritDoc} */
    public String getPathInfo() {
        // TODO: If we need this, should be configurable via the c'tor.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getPathTranslated() {
        return null;
    }

    /** {@inheritDoc} */
    public String getContextPath() {
        // TODO: If we need this, should be configurable via the c'tor.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getQueryString() {
        return obj.getmember("query").string();
    }

    /** {@inheritDoc} */
    public String getRemoteUser() {
        return obj.getmember("remote_user").string();
    }

    /** {@inheritDoc} */
    public boolean isUserInRole(final String role) {
        return false;
    }

    /** {@inheritDoc} */
    public Principal getUserPrincipal() {
        return null;
    }

    /** {@inheritDoc} */
    public String getRequestedSessionId() {
        return null;
    }

    /** {@inheritDoc} */
    public String getRequestURI() {
        return decodeUnsafeString(obj.getmember("uri").unsafe_string());
    }

    /** {@inheritDoc} */
    public StringBuffer getRequestURL() {
        final String url = decodeUnsafeString(obj.getmember("url").unsafe_string());
        return new StringBuffer(url != null ? url : "");
    }

    /** {@inheritDoc} */
    public String getServletPath() {
        // TODO: If we need this, should be configurable via the c'tor.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public HttpSession getSession(final boolean create) {
        if (create) {
            throw new UnsupportedOperationException();
        }
        return null;
    }

    /** {@inheritDoc} */
    public HttpSession getSession() {
        return null;
    }

    /** {@inheritDoc} */
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void login(final String username, final String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void logout() throws ServletException {
        
    }

    /** {@inheritDoc} */
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    /** {@inheritDoc} */
    public Part getPart(final String name) throws IOException, ServletException {
        return null;
    }

    /** {@inheritDoc} */
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Helper method to decode a byte buffer into either UTF-8 or ISO-8859-1.
     * 
     * @param buffer input buffer
     * 
     * @return encoded String form of the data
     */
    @Nullable private static String decodeUnsafeString(final byte[] buffer) {
        
        if (buffer == null) {
            return null;
        }
        
        final ByteBuffer wrapper = ByteBuffer.wrap(buffer);
        
        try {
            return UTF_8.decode(wrapper).toString();
        } catch (final CharacterCodingException e) {
            
        }
        
        try {
            return ISO_8859_1.decode(wrapper).toString();
        } catch (final CharacterCodingException e) {
            
        }
        
        return null;
    }

    /** Helper class cribbed from Spring. */
    private static class BodyInputStream extends ServletInputStream {

        /** Underlying stream. */
        @Nonnull private final InputStream delegate;

        /**
         * Constructor.
         *
         * @param body body data
         */
        public BodyInputStream(final byte[] body) {
            delegate = new ByteArrayInputStream(body);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isFinished() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isReady() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public void setReadListener(final ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        /** {@inheritDoc} */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return delegate.read(b, off, len);
        }

        /** {@inheritDoc} */
        @Override
        public int read(final byte[] b) throws IOException {
            return delegate.read(b);
        }

        /** {@inheritDoc} */
        @Override
        public long skip(final long n) throws IOException {
            return delegate.skip(n);
        }

        /** {@inheritDoc} */
        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws IOException {
            delegate.close();
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void mark(final int readlimit) {
            delegate.mark(readlimit);
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        /** {@inheritDoc} */
        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }

}