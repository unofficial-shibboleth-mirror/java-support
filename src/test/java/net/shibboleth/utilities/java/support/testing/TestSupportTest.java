
package net.shibboleth.utilities.java.support.testing;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSupportTest {

    @Test
    public void isJavaV7OrLater() {
        // answer for current runtime can be true or false, just not an exception
        TestSupport.isJavaV7OrLater();
    }

    @Test
    public void isJavaV8OrLater() {
        // answer for current runtime can be true or false, just not an exception
        TestSupport.isJavaV8OrLater();
    }

    @Test
    public void isJavaV9OrLater() {
        // answer for current runtime can be true or false, just not an exception
        TestSupport.isJavaV9OrLater();
    }

    @Test
    public void isJavaV11OrLater() {
        // answer for current runtime can be true or false, just not an exception
        TestSupport.isJavaV11OrLater();
    }

    @Test
    public void getJavaVersion() {
        // test against some real versions
        Assert.assertEquals(TestSupport.getJavaVersion("1.6.0_65-b14-468"), 6);
        Assert.assertEquals(TestSupport.getJavaVersion("1.7.0_51"), 7);
        Assert.assertEquals(TestSupport.getJavaVersion("1.8.0_144"), 8);
        Assert.assertEquals(TestSupport.getJavaVersion("9"), 9);
        Assert.assertEquals(TestSupport.getJavaVersion("9.0.1"), 9);
        Assert.assertEquals(TestSupport.getJavaVersion("10+43"), 10); // Java 10 RC
        Assert.assertEquals(TestSupport.getJavaVersion("11.0.2"), 11);
    }

}
