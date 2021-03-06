
package net.shibboleth.utilities.java.support.security;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.security.impl.FixedStringIdentifierGenerationStrategy;

public class FixedStringIdentifierGenerationStrategyTest {

    @Test
    public void generateIdentifier() {
        final FixedStringIdentifierGenerationStrategy f = new FixedStringIdentifierGenerationStrategy("aaa");
        Assert.assertEquals(f.generateIdentifier(), "aaa");
        Assert.assertEquals(f.generateIdentifier(), "aaa");
    }

    @Test
    public void generateIdentifierboolean() {
        final FixedStringIdentifierGenerationStrategy f = new FixedStringIdentifierGenerationStrategy("bbb");
        Assert.assertEquals(f.generateIdentifier(true), "bbb");
        Assert.assertEquals(f.generateIdentifier(false), "bbb");
    }
}
