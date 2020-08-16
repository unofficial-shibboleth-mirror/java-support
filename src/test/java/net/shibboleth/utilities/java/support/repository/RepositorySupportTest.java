
package net.shibboleth.utilities.java.support.repository;

import java.io.IOException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;


public class RepositorySupportTest {

    @Test
    public void testBuildHTTPResourceURLTest() throws IOException {
        final String s = RepositorySupport.buildHTTPResourceURL("java-support",
                "src/test/java/net/shibboleth/utilities/java/support/repository/RepositorySupport.java", false);
        final URL u = new URL(s);
        Assert.assertNotNull(u);
        Assert.assertNotNull(u.getContent());
    }

    @Test
    public void testBuildHTTPSResourceURLTest() throws IOException {
        final String s = RepositorySupport.buildHTTPSResourceURL("java-support",
                "src/test/java/net/shibboleth/utilities/java/support/repository/RepositorySupport.java");
        final URL u = new URL(s);
        Assert.assertNotNull(u);
        // Can't dereference, as test.shibboleth.net has a non-commercial certificate
        // Real uses need to take this into account.
        // Assert.assertNotNull(u.getContent());
    }
}
