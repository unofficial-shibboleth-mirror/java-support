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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
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
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An implementation of {@link HttpServletRequest} which serves as a proxy for the 
 * current thread-local servlet request obtained from {@link HttpServletRequestResponseContext}.
 */
public class ThreadLocalHttpServletRequestProxy implements HttpServletRequest {

    /** {@inheritDoc} */
    public Object getAttribute(final String name) {
        return getCurrent().getAttribute(name);
    }

    /** {@inheritDoc} */
    public Enumeration<String> getAttributeNames() {
        return getCurrent().getAttributeNames();
    }

    /** {@inheritDoc} */
    public String getCharacterEncoding() {
        return getCurrent().getCharacterEncoding();
    }

    /** {@inheritDoc} */
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
        getCurrent().setCharacterEncoding(env);
    }

    /** {@inheritDoc} */
    public int getContentLength() {
        return getCurrent().getContentLength();
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return getCurrent().getContentType();
    }

    /** {@inheritDoc} */
    public ServletInputStream getInputStream() throws IOException {
        return getCurrent().getInputStream();
    }

    /** {@inheritDoc} */
    public String getParameter(final String name) {
        return getCurrent().getParameter(name);
    }

    /** {@inheritDoc} */
    public Enumeration<String> getParameterNames() {
        return getCurrent().getParameterNames();
    }

    /** {@inheritDoc} */
    public String[] getParameterValues(final String name) {
        return getCurrent().getParameterValues(name);
    }

    /** {@inheritDoc} */
    public Map<String,String[]> getParameterMap() {
        return getCurrent().getParameterMap();
    }

    /** {@inheritDoc} */
    public String getProtocol() {
        return getCurrent().getProtocol();
    }

    /** {@inheritDoc} */
    public String getScheme() {
        return getCurrent().getScheme();
    }

    /** {@inheritDoc} */
    public String getServerName() {
        return getCurrent().getServerName();
    }

    /** {@inheritDoc} */
    public int getServerPort() {
        return getCurrent().getServerPort();
    }

    /** {@inheritDoc} */
    public BufferedReader getReader() throws IOException {
        return getCurrent().getReader();
    }

    /** {@inheritDoc} */
    public String getRemoteAddr() {
        return getCurrent().getRemoteAddr();
    }

    /** {@inheritDoc} */
    public String getRemoteHost() {
        return getCurrent().getRemoteHost();
    }

    /** {@inheritDoc} */
    public void setAttribute(final String name, final Object o) {
        getCurrent().setAttribute(name, o);
    }

    /** {@inheritDoc} */
    public void removeAttribute(final String name) {
        getCurrent().removeAttribute(name);
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        return getCurrent().getLocale();
    }

    /** {@inheritDoc} */
    public Enumeration<Locale> getLocales() {
        return getCurrent().getLocales();
    }

    /** {@inheritDoc} */
    public boolean isSecure() {
        return getCurrent().isSecure();
    }

    /** {@inheritDoc} */
    public RequestDispatcher getRequestDispatcher(final String path) {
        return getCurrent().getRequestDispatcher(path);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public String getRealPath(final String path) {
        return getCurrent().getRealPath(path);
    }

    /** {@inheritDoc} */
    public int getRemotePort() {
        return getCurrent().getRemotePort();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return getCurrent().getLocalName();
    }

    /** {@inheritDoc} */
    public String getLocalAddr() {
        return getCurrent().getLocalAddr();
    }

    /** {@inheritDoc} */
    public int getLocalPort() {
        return getCurrent().getLocalPort();
    }

    /** {@inheritDoc} */
    public String getAuthType() {
        return getCurrent().getAuthType();
    }

    /** {@inheritDoc} */
    public Cookie[] getCookies() {
        return getCurrent().getCookies();
    }

    /** {@inheritDoc} */
    public long getDateHeader(final String name) {
        return getCurrent().getDateHeader(name);
    }

    /** {@inheritDoc} */
    public String getHeader(final String name) {
        return getCurrent().getHeader(name);
    }

    /** {@inheritDoc} */
    public Enumeration<String> getHeaders(final String name) {
        return getCurrent().getHeaders(name);
    }

    /** {@inheritDoc} */
    public Enumeration<String> getHeaderNames() {
        return getCurrent().getHeaderNames();
    }

    /** {@inheritDoc} */
    public int getIntHeader(final String name) {
        return getCurrent().getIntHeader(name);
    }

    /** {@inheritDoc} */
    public String getMethod() {
        return getCurrent().getMethod();
    }

    /** {@inheritDoc} */
    public String getPathInfo() {
        return getCurrent().getPathInfo();
    }

    /** {@inheritDoc} */
    public String getPathTranslated() {
        return getCurrent().getPathTranslated();
    }

    /** {@inheritDoc} */
    public String getContextPath() {
        return getCurrent().getContextPath();
    }

    /** {@inheritDoc} */
    public String getQueryString() {
        return getCurrent().getQueryString();
    }

    /** {@inheritDoc} */
    public String getRemoteUser() {
        return getCurrent().getRemoteUser();
    }

    /** {@inheritDoc} */
    public boolean isUserInRole(final String role) {
        return getCurrent().isUserInRole(role);
    }

    /** {@inheritDoc} */
    public Principal getUserPrincipal() {
        return getCurrent().getUserPrincipal();
    }

    /** {@inheritDoc} */
    public String getRequestedSessionId() {
        return getCurrent().getRequestedSessionId();
    }

    /** {@inheritDoc} */
    public String getRequestURI() {
        return getCurrent().getRequestURI();
    }

    /** {@inheritDoc} */
    public StringBuffer getRequestURL() {
        return getCurrent().getRequestURL();
    }

    /** {@inheritDoc} */
    public String getServletPath() {
        return getCurrent().getServletPath();
    }

    /** {@inheritDoc} */
    public HttpSession getSession(final boolean create) {
        return getCurrent().getSession(create);
    }

    /** {@inheritDoc} */
    public HttpSession getSession() {
        return getCurrent().getSession();
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdValid() {
        return getCurrent().isRequestedSessionIdValid();
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdFromCookie() {
        return getCurrent().isRequestedSessionIdFromCookie();
    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdFromURL() {
        return getCurrent().isRequestedSessionIdFromURL();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return getCurrent().isRequestedSessionIdFromUrl();
    }
    
    /** {@inheritDoc} */
    public ServletContext getServletContext() {
        return getCurrent().getServletContext();
    }

    /** {@inheritDoc} */
    public AsyncContext startAsync() {
        return getCurrent().startAsync();
    }

    /** {@inheritDoc} */
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        return getCurrent().startAsync(servletRequest, servletResponse);
    }

    /** {@inheritDoc} */
    public boolean isAsyncStarted() {
        return getCurrent().isAsyncStarted();
    }

    /** {@inheritDoc} */
    public boolean isAsyncSupported() {
        return getCurrent().isAsyncSupported();
    }

    /** {@inheritDoc} */
    public AsyncContext getAsyncContext() {
        return getCurrent().getAsyncContext();
    }

    /** {@inheritDoc} */
    public DispatcherType getDispatcherType() {
        return getCurrent().getDispatcherType();
    }

    /** {@inheritDoc} */
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        return getCurrent().authenticate(response);
    }

    /** {@inheritDoc} */
    public void login(final String username, final String password) throws ServletException {
        getCurrent().login(username, password);
    }

    /** {@inheritDoc} */
    public void logout() throws ServletException {
        getCurrent().logout();
    }

    /** {@inheritDoc} */
    public Collection<Part> getParts() throws IOException, ServletException {
        return getCurrent().getParts();
    }

    /** {@inheritDoc} */
    public Part getPart(final String name) throws IOException, ServletException {
        return getCurrent().getPart(name);
    }

    /** {@inheritDoc} */
    public long getContentLengthLong() {
        return getCurrent().getContentLengthLong();
    }

    /** {@inheritDoc} */
    public String changeSessionId() {
        return getCurrent().changeSessionId();
    }

    /** {@inheritDoc} */
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException {
        return getCurrent().upgrade(handlerClass);
    }
    
    /**
     * Get the current HttpServletRequest from ThreadLocal storage.
     * 
     * @return the current request
     */
    protected HttpServletRequest getCurrent() {
        return Constraint.isNotNull(HttpServletRequestResponseContext.getRequest(), 
                "Current HttpServletRequest has not been loaded via HttpServletRequestResponseContext");
    }
}
