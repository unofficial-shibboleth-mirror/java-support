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

package net.shibboleth.utilities.java.support.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.annotation.Nonnull;

/**
 * An interface representing a data resource.
 *
 * <p>This is API-compatible with the
 * <a href="http://docs.spring.io/spring/docs/2.5.x/api/org/springframework/core/io/Resource.html">Spring
 * <code>Resource</code></a> but allows <code>Resource</code>-consuming code to not require Spring.</p>
 *
 * <p>Shibboleth components only implement this interface if they also implement the Spring <code>Resource</code>
 * interface. Refer to the Spring documentation to implement other versions of this in a Spring-free environment.</p>
 * */
public interface Resource {

    /**
     * Return whether this resource actually exists in physical form.
     * <p>
     * This method performs a definitive existence check, whereas the existence of a {@code Resource} handle only
     * guarantees a valid descriptor handle.
     * 
     * @return whether this resource actually exists in physical form.
     */
    boolean exists();

    /**
     * Return whether the contents of this resource can be read, e.g. via {@link #getInputStream()} or
     * {@link #getFile()}.
     * <p>
     * Will be {@code true} for typical resource descriptors; note that actual content reading may still fail when
     * attempted. However, a value of {@code false} is a definitive indication that the resource content cannot be read.
     * 
     * @see #getInputStream()
     * @return whether the contents of this resource can be read.
     */
    boolean isReadable();

    /**
     * Return whether this resource represents a handle with an open stream. If true, the InputStream cannot be read
     * multiple times, and must be read and closed to avoid resource leaks.
     * <p>
     * Will be {@code false} for typical resource descriptors.
     * 
     * @return whether this resource represents a handle with an open stream.
     */
    boolean isOpen();

    /**
     * Return a URL handle for this resource.
     * 
     * @throws IOException if the resource cannot be resolved as URL, i.e. if the resource is not available as
     *             descriptor
     * @return a URL handle for this resource.
     */
    URL getURL() throws IOException;

    /**
     * Return a URI handle for this resource.
     * 
     * @throws IOException if the resource cannot be resolved as URI, i.e. if the resource is not available as
     *             descriptor
     * @return a URI handle for this resource.
     */
    URI getURI() throws IOException;

    /**
     * Return a File handle for this resource.
     * 
     * @throws IOException if the resource cannot be resolved as absolute file path, i.e. if the resource is not
     *             available in a file system
     * @return a File handle for this resource.
     */
    File getFile() throws IOException;

    /**
     * Return an {@link InputStream}.
     * <p>
     * It is expected that each call creates a <i>fresh</i> stream.
     * <p>
     * This requirement is particularly important when you consider an API such as JavaMail, which needs to be able to
     * read the stream multiple times when creating mail attachments. For such a use case, it is <i>required</i> that
     * each {@code getInputStream()} call returns a fresh stream.
     * 
     * @return the input stream for the underlying resource (must not be {@code null})
     * @throws IOException if the stream could not be opened
     */
    @Nonnull InputStream getInputStream() throws IOException;

    /**
     * Determine the content length for this resource.
     * 
     * @throws IOException if the resource cannot be resolved (in the file system or as some other known physical
     *             resource type)
     * @return the content length for this resource.
     */
    long contentLength() throws IOException;

    /**
     * Determine the last-modified timestamp for this resource.
     * 
     * @return the last-modified timestamp for this resource.
     * @throws IOException if the resource cannot be resolved (in the file system or as some other known physical
     *             resource type)
     */
    long lastModified() throws IOException;

    /**
     * Create a resource relative to this resource.
     * 
     * @param relativePath the relative path (relative to this resource)
     * @return the resource handle for the relative resource
     * @throws IOException if the relative resource cannot be determined
     */
    Resource createRelativeResource(String relativePath) throws IOException;

    /**
     * Determine a filename for this resource, i.e. typically the last part of the path: for example, "myfile.txt".
     * 
     * @return {@code null} if this type of resource does not have a filename, otherwise the file name.
     * 
     */
    String getFilename();

    /**
     * Return a description for this resource, to be used for error output when working with the resource.
     * <p>
     * Implementations are also encouraged to return this value from their {@code toString} method.
     * 
     * @see Object#toString()
     * @return a description for this resource.
     */
    String getDescription();

}