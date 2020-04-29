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

package net.shibboleth.utilities.java.support.plugin;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.resource.Resource;

/**
 * This class is exported (via the service API) by every plugin.
 */
public abstract class PluginInstaller {
    
    /** Return the unique identifier for the plugin.  This name <em>MUST</em> be
     * <ul>
     * <li> renderable in all file systems (for instance alphanumerics, '-' and '.' only)</li>
     * <li> unique.  This is best done using java module guidance</li>
     * </ul>
     * For instance <code>org.example.plugins.myplugin</code>
     *
     * @return The id of this plugin.
     */
    @Nonnull @NotEmpty public abstract String getPluginId();

    /** Return the list of file names to be appended to
     * <code>idp.additional.properties</code>.
     * @return The list of names, potentially empty.
     */
    @Nonnull public List<String> getAdditionalPropertyFiles() {
        return Collections.emptyList();
    }
    
    /** Return the list of (idp.home) relative paths (of files, <em>not directories </em>) 
     * to copy from the distribution into the IdP installation.<p>
     * 
     * These files are copied non-destructively (if the file already exists
     * then it is not copied).  Some paths are disallowed (for instance dist and system).</p>
     * <p>
     * The dist folder is always copied, so no files from it should be included.</p>
     * @return The list of paths.
     */
    @Nonnull public List<Path> getFilePathsToCopy() {
        return Collections.emptyList();
    }
    
    /** Return the places to look for updates for this plugin package.
     * The format of the paths below this point is fixed.
     * 
     * 
     * @return At least one Resource.
     */
    @Nonnull @NotEmpty @NonnullElements public abstract List<Resource> getUpdateResources();
    
    /** Return this properties that require list merging.  
     * 
     * <p>Given
     * an property called (say) <code>idp.service.foo</code> in file <code>services.property</code>
     * and an init setting of <code>idp.service.foo.bar</code>, the output would be:</p>
     * <pre> idp.service.foo = BEAN.idp.service.foo.plugin-id.net.shibboleth.foo
     * idp.service.foo.OLD.plugin-id = shibboleth.AttributeFilterResources</pre>
     *
     * @return A list of pairs, the first element of the pair is the (relative) path of the property file
     * and the second is a list of property names to edit within that file.
     */
    @Nonnull public List<Pair<Path, List<String>>> getPropertyMerges() {
        return Collections.emptyList();
    }
    
    /** Return the major version, (as defined by the 
     * <a href="https://wiki.shibboleth.net/confluence/display/DEV/Java+Product+Version+Policy">
     * Java Product Version Policy</a>.
     * @return The major version.
     */
    @Positive public abstract int getMajorVersion();

    /** Return the minor version, (as defined by the 
     * <a href="https://wiki.shibboleth.net/confluence/display/DEV/Java+Product+Version+Policy">
     * Java Product Version Policy</a>.
     * @return The minor version.
     */
    @Nonnegative public abstract int getMinorVersion();
    
    /** Return The patch version, (as defined by the 
     * <a href="https://wiki.shibboleth.net/confluence/display/DEV/Java+Product+Version+Policy">
     * Java Product Version Policy</a>.
     * @return The patch version.
     */
    @Nonnegative public int getPatchVersion() {
        return 0;
    }
}
