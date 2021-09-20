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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Uses a {@link DDF} object to reflect an HTTP response back to a remote caller.
 */
@NotThreadSafe
public class RemotedHttpServletResponse implements HttpServletResponse {

    /** Underlying object for remoted data. */
    @Nonnull private final DDF obj;
    
    /** Size of each character buffer for output. */
    private int bufferSize;
    
    /** Tracks committing of response. */
    private boolean committed;
    
    /** A materialized output stream. */
    @Nullable private BodyOutputStream outputStream;

    /**
     * Constructor.
     *
     * @param ddf object to capture response
     */
    public RemotedHttpServletResponse(final DDF ddf) {
        obj = Constraint.isNotNull(ddf, "DDF cannot be null");
        obj.structure();
        bufferSize = 1024;
    }
    
    /** {@inheritDoc} */
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return getHeader("Content-Type");
    }

    /** {@inheritDoc} */
    public ServletOutputStream getOutputStream() throws IOException {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        if (outputStream == null) {
            outputStream = new BodyOutputStream();
        }
        return outputStream;
    }

    /** {@inheritDoc} */
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream(), false, Charset.forName(getCharacterEncoding()));
    }

    /** {@inheritDoc} */
    public void setCharacterEncoding(final String charset) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void setContentLength(final int len) {
        setIntHeader("Content-Length", len);
    }

    /** {@inheritDoc} */
    public void setContentLengthLong(final long len) {
        setHeader("Content-Length", Long.toString(len));
    }

    /** {@inheritDoc} */
    public void setContentType(final String type) {
        setHeader("Content-Type", type);
    }

    /** {@inheritDoc} */
    public void setBufferSize(final int size) {
        bufferSize = size;
    }

    /** {@inheritDoc} */
    public int getBufferSize() {
        return bufferSize;
    }

    /** {@inheritDoc} */
    public void flushBuffer() throws IOException {
        // Just mark as committed to signal that status and headers are frozen.
        committed = true;
    }

    /** {@inheritDoc} */
    public void resetBuffer() {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        
        if (outputStream != null) {
            outputStream.reset();
        }
    }

    /** {@inheritDoc} */
    public boolean isCommitted() {
        return committed;
    }

    /** {@inheritDoc} */
    public void reset() {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        outputStream = null;
        obj.structure();
    }

    /** {@inheritDoc} */
    public void setLocale(final Locale loc) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void addCookie(final Cookie cookie) {
        // The C++ side already manages SameSite independently, so we'll likely continue that.
        final StringBuffer buffer = new StringBuffer(cookie.getName()).append('=');
        if (cookie.getValue() != null) {
            buffer.append(cookie.getValue());
        }
        if (cookie.getMaxAge() >= 0) {
            buffer.append("; MaxAge=").append(cookie.getMaxAge());
        }
        if (cookie.getPath() != null) {
            buffer.append("; ").append("Path=").append(cookie.getPath());
        }
        if (cookie.getDomain() != null) {
            buffer.append("; ").append("Domain=").append(cookie.getDomain());
        }
        if (cookie.getSecure()) {
            buffer.append("; Secure");
        }
        if (cookie.isHttpOnly()) {
            buffer.append("; HttpOnly");
        }
        addHeader("Set-Cookie", buffer.toString());
    }

    /** {@inheritDoc} */
    public boolean containsHeader(final String name) {
        for (final DDF header : obj.getmember("headers").asList()) {
            if (name.equals(header.name())) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public String encodeURL(final String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeRedirectURL(final String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeUrl(final String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeRedirectUrl(final String url) {
        return url;
    }

    /** {@inheritDoc} */
    public void sendError(final int sc, final String msg) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void sendError(final int sc) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void sendRedirect(final String location) throws IOException {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        obj.getmember("response").remove();
        obj.addmember("redirect").string(location);
        committed = true;
        outputStream = null;
    }

    /** {@inheritDoc} */
    public void setDateHeader(final String name, final long date) {
        unsetHeader(name);
        addDateHeader(name, date);
    }

    /** {@inheritDoc} */
    public void addDateHeader(final String name, final long date) {
        final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        addHeader(name, formatter.format(Date.from(Instant.ofEpochMilli(date))));
    }

    /** {@inheritDoc} */
    public void setHeader(final String name, final String value) {
        unsetHeader(name);
        addHeader(name, value);
    }

    /** {@inheritDoc} */
    public void addHeader(final String name, final String value) {
        getHeaderList().add(new DDF(name).string(value));
    }

    /** {@inheritDoc} */
    public void setIntHeader(final String name, final int value) {
        unsetHeader(name);
        addIntHeader(name, value);
    }

    /** {@inheritDoc} */
    public void addIntHeader(final String name, final int value) {
        getHeaderList().add(new DDF(name).integer(value));
    }

    /** {@inheritDoc} */
    public void setStatus(final int sc) {
        obj.addmember("response.status").integer(sc);
    }

    /** {@inheritDoc} */
    public void setStatus(final int sc, final String sm) {
        setStatus(sc);
        obj.addmember("response.status_message").string(sm);
    }

    /** {@inheritDoc} */
    public int getStatus() {
        final Integer i = obj.getmember("response.status").integer();
        return i != null ? i : -1;
    }

    /** {@inheritDoc} */
    public String getHeader(final String name) {
        final Optional<DDF> header =
                obj.getmember("headers").asList()
                    .stream()
                    .filter(ddf -> name.equalsIgnoreCase(ddf.name()))
                    .findFirst();
        if (header.isPresent()) {
            if (header.orElseThrow().isstring()) {
                return header.orElseThrow().string();
            }
            return header.orElseThrow().integer().toString();
        }
        
        return null;
    }

    /** {@inheritDoc} */
    public Collection<String> getHeaders(final String name) {
        return obj.getmember("headers").asList()
            .stream()
            .filter(ddf -> name.equalsIgnoreCase(ddf.name()))
            .map(DDF::string)
            .collect(Collectors.toUnmodifiableList());
    }

    /** {@inheritDoc} */
    public Collection<String> getHeaderNames() {
        return obj.getmember("headers").asList()
                .stream()
                .map(DDF::name)
                .collect(Collectors.toUnmodifiableSet());
    }
    
    /**
     * Removes any existing header(s) of this type.
     * 
     * @param name name of header to remove
     */
    private void unsetHeader(final @Nonnull @NotEmpty String name) {
        
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        
        // This is safe because the asList copy is divorced from the original list
        // but the DDF child objects are the same.
        obj.getmember("headers").asList()
            .stream()
            .filter(ddf -> name.equalsIgnoreCase(ddf.name()))
            .forEach(DDF::remove);
    }
    
    /**
     * Get the list node to which headers should be added.
     * 
     * @return a pre-existing list or a new one for mutation.
     */
    @Nonnull private DDF getHeaderList() {
        
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        
        final DDF headers = obj.getmember("headers");
        if (headers.islist()) {
            return headers;
        }
        return obj.addmember("headers").list();
    }

    /** Wrapper allowing use of containers of arrays. */
    private static final class ByteArrayWrapper {
        
        /** Current write position. */
        private int offset;
        
        /** Wrapped array. */
        private final byte[] buffer;
        
        private ByteArrayWrapper(final int size) {
            buffer = new byte[size];
            offset = 0;
        }
        
        @Nonnull private byte[] getBuffer() {
            return buffer;
        }
        
        private int getOffset() {
            return offset;
        }
        
        private boolean write(final int b) {
            if (offset < buffer.length) {
                buffer[offset++] = Integer.valueOf(b).byteValue();
                return true;
            }
            
            return false;
        }
    }

    private class BodyOutputStream extends ServletOutputStream {

        
        /** Internal buffers. */
        @Nonnull @NonnullElements private final ArrayList<ByteArrayWrapper> bufferList;
        
        /** The currently filling buffer. */
        @Nonnull private ByteArrayWrapper currentBuffer;
        
        /** Constructor. */
        public BodyOutputStream() {
            currentBuffer = new ByteArrayWrapper(bufferSize);
            bufferList = new ArrayList<>(1);
            bufferList.add(currentBuffer);
        }
        
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(final WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final int b) throws IOException {
            if (!currentBuffer.write(b)) {
                currentBuffer = new ByteArrayWrapper(bufferSize);
                bufferList.add(currentBuffer);
                Constraint.isTrue(currentBuffer.write(b), "Fresh buffer cannot fail to accept data");
            }
        }

        @Override
        public void flush() throws IOException {
            
            int offset = 0;
            final byte[] copy = new byte[((bufferList.size() - 1) * bufferSize) +
                                         bufferList.get(bufferList.size() - 1).getOffset()];

            for (final ByteArrayWrapper b : bufferList) {
                System.arraycopy(b.getBuffer(), 0, copy, offset, b.getOffset());
                offset += b.getOffset();
            }
            obj.addmember("response.data").unsafe_string(copy);
            committed = true;
        }

        @Override
        public void close() throws IOException {
            flush();
        }

        /** Clear all data written. */
        private void reset() {
            currentBuffer = new ByteArrayWrapper(bufferSize);
            bufferList.clear();
            bufferList.add(currentBuffer);
        }
    }

}