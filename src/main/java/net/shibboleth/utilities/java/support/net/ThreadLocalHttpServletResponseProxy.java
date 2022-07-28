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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An implementation of {@link HttpServletResponse} which serves as a proxy for the 
 * current thread-local servlet response obtained from {@link HttpServletRequestResponseContext}.
 */
public class ThreadLocalHttpServletResponseProxy implements HttpServletResponse {

    /** {@inheritDoc} */
    public String getCharacterEncoding() {
        return getCurrent().getCharacterEncoding();
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return getCurrent().getContentType();
    }

    /** {@inheritDoc} */
    public ServletOutputStream getOutputStream() throws IOException {
        return getCurrent().getOutputStream();
    }

    /** {@inheritDoc} */
    public PrintWriter getWriter() throws IOException {
        return getCurrent().getWriter();
    }

    /** {@inheritDoc} */
    public void setCharacterEncoding(final String charset) {
        getCurrent().setCharacterEncoding(charset);
    }

    /** {@inheritDoc} */
    public void setContentLength(final int len) {
        getCurrent().setContentLength(len);
    }

    /** {@inheritDoc} */
    public void setContentType(final String type) {
        getCurrent().setContentType(type);
    }

    /** {@inheritDoc} */
    public void setBufferSize(final int size) {
        getCurrent().setBufferSize(size);
    }

    /** {@inheritDoc} */
    public int getBufferSize() {
        return getCurrent().getBufferSize();
    }

    /** {@inheritDoc} */
    public void flushBuffer() throws IOException {
        getCurrent().flushBuffer();
    }

    /** {@inheritDoc} */
    public void resetBuffer() {
        getCurrent().resetBuffer();
    }

    /** {@inheritDoc} */
    public boolean isCommitted() {
        return getCurrent().isCommitted();
    }

    /** {@inheritDoc} */
    public void reset() {
        getCurrent().reset();
    }

    /** {@inheritDoc} */
    public void setLocale(final Locale loc) {
        getCurrent().setLocale(loc);
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        return getCurrent().getLocale();
    }

    /** {@inheritDoc} */
    public void addCookie(final Cookie cookie) {
        getCurrent().addCookie(cookie);
    }

    /** {@inheritDoc} */
    public boolean containsHeader(final String name) {
        return getCurrent().containsHeader(name);
    }

    /** {@inheritDoc} */
    public String encodeURL(final String url) {
        return getCurrent().encodeURL(url);
    }

    /** {@inheritDoc} */
    public String encodeRedirectURL(final String url) {
        return getCurrent().encodeRedirectURL(url);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public String encodeUrl(final String url) {
        return getCurrent().encodeUrl(url);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public String encodeRedirectUrl(final String url) {
        return getCurrent().encodeRedirectUrl(url);
    }

    /** {@inheritDoc} */
    public void sendError(final int sc, final String msg) throws IOException {
        getCurrent().sendError(sc, msg);
    }

    /** {@inheritDoc} */
    public void sendError(final int sc) throws IOException {
        getCurrent().sendError(sc);
    }

    /** {@inheritDoc} */
    public void sendRedirect(final String location) throws IOException {
        getCurrent().sendRedirect(location);
    }

    /** {@inheritDoc} */
    public void setDateHeader(final String name, final long date) {
        getCurrent().setDateHeader(name, date);
    }

    /** {@inheritDoc} */
    public void addDateHeader(final String name, final long date) {
        getCurrent().addDateHeader(name, date);
    }

    /** {@inheritDoc} */
    public void setHeader(final String name, final String value) {
        getCurrent().setHeader(name, value);
    }

    /** {@inheritDoc} */
    public void addHeader(final String name, final String value) {
        getCurrent().addHeader(name, value);
    }

    /** {@inheritDoc} */
    public void setIntHeader(final String name, final int value) {
        getCurrent().setIntHeader(name, value);
    }

    /** {@inheritDoc} */
    public void addIntHeader(final String name, final int value) {
        getCurrent().addIntHeader(name, value);
    }

    /** {@inheritDoc} */
    public void setStatus(final int sc) {
        getCurrent().setStatus(sc);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public void setStatus(final int sc, final String sm) {
        getCurrent().setStatus(sc, sm);
    }
    
    /** {@inheritDoc} */
    public int getStatus() {
        return getCurrent().getStatus();
    }

    /** {@inheritDoc} */
    public String getHeader(final String name) {
        return getCurrent().getHeader(name);
    }

    /** {@inheritDoc} */
    public Collection<String> getHeaders(final String name) {
        return getCurrent().getHeaders(name);
    }

    /** {@inheritDoc} */
    public Collection<String> getHeaderNames() {
        return getCurrent().getHeaderNames();
    }

    /** {@inheritDoc} */
    public void setContentLengthLong(final long len) {
        getCurrent().setContentLengthLong(len);
    }

    /**
     * Get the current HttpServletResponse from ThreadLocal storage.
     * 
     * @return the current response
     */
    protected HttpServletResponse getCurrent() {
        return Constraint.isNotNull(HttpServletRequestResponseContext.getResponse(),
                "Current HttpServletResponse has not been loaded via HttpServletRequestResponseContext");
    }
}
