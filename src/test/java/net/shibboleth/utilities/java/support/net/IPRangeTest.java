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

import java.net.InetAddress;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IPRangeTest {
    
    @Test public void testValidV4Addresses() {
        IPRange.parseCIDRBlock("1.2.3.4/32");
        IPRange.parseCIDRBlock("0.0.0.0/8");
        IPRange.parseCIDRBlock("0.0.0.0/0");
    }

    @Test public void testValidV6Addresses() {
        IPRange.parseCIDRBlock("0:0:0:0:0:0:0:0/128");
        IPRange.parseCIDRBlock("0:0:0:0:0:0:0:0/0");
        IPRange.parseCIDRBlock("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:CCCC/128");
        IPRange.parseCIDRBlock("1234:5678::BBBB:CCCC/128");
        IPRange.parseCIDRBlock("2001:630:200::/48");
        IPRange.parseCIDRBlock("::0BAD:7/128");
    }

    @Test public void testInvalidJunkAddresses() {
        testInvalid(null);
        testInvalid("1.2.3.500/32");
        testInvalid("1.2.3.G/32");
        testInvalid("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:FFFFF/128");
        testInvalid("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:GHIJ/128");
        testInvalid("/32");
        testInvalid("f/32");
        testInvalid("1.2.3.4");
        testInvalid("1.2.3.4/32/1");
        
        try{
            new IPRange(new byte[]{1, 2 ,3}, 32);
            Assert.fail();
        }catch(IllegalArgumentException e){
            //expected this
        }
    }

    @Test public void testInvalidV4Addresses() {
        testInvalid("1/32");
        testInvalid("1.2/32");
        testInvalid("1.2.3/32");
        testInvalid("1.2.3.4/33");
        testInvalid("1.2.3.4/-3");
        testInvalid("1.2.3.4/wrong");
    }

    @Test public void testInvalidV6Addresses() {
        testInvalid("0:0/128");
        testInvalid("1:2:3:4:5:6:7/128");
        testInvalid("::0BAD::7/128");
        testInvalid("1:2:3:4:5:6:7:8/-5");
        testInvalid("1:2:3:4:5:6:7:8/129");
        testInvalid("1:2:3:4:5:6:7:8/wrong");
    }

    @Test public void testContains() throws Exception {
        // IPRange given a network address
        IPRange networkRange = IPRange.parseCIDRBlock("192.168.117.192/28");

        // IPRange given a host address
        IPRange hostRange = IPRange.parseCIDRBlock("192.168.117.199/28");
        
        // test for contain
        byte[] bytes = new byte[] {(byte) 192, (byte) 168, 117, (byte) 191};
        Assert.assertFalse(networkRange.contains(bytes));
        Assert.assertFalse(hostRange.contains(bytes));

        for (int host = 0; host < 16; host++) {
            bytes[3] = (byte) (192 + host);
            Assert.assertTrue(networkRange.contains(bytes));
            Assert.assertTrue(hostRange.contains(bytes));
        }

        bytes[3] = (byte) (192 + 16);
        Assert.assertFalse(networkRange.contains(bytes));
        Assert.assertFalse(hostRange.contains(bytes));
        
        Assert.assertFalse(networkRange.contains(new byte[] {1, 2, 3}));
        Assert.assertFalse(hostRange.contains(new byte[] {1, 2, 3}));
        
        Assert.assertFalse(networkRange.contains(InetAddress.getLocalHost()));
        Assert.assertFalse(hostRange.contains(InetAddress.getLocalHost()));
        
        // IPRange for V6
        IPRange v6Range = IPRange.parseCIDRBlock("2620:102:c000::1/48");
        InetAddress v6Addr = InetAddress.getByName("2620:102:c000:f10:d::29c1");
        Assert.assertTrue(v6Range.contains(v6Addr));
    }

    @Test public void testGetNetworkAddress() {
        IPRange v6a = IPRange.parseCIDRBlock("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:CCCC/128");
        byte[] expected6a =
                {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd,
                        (byte) 0xef, (byte) 0xff, (byte) 0xff, (byte) 0xaa, (byte) 0xaa, (byte) 0xbb, (byte) 0xbb,
                        (byte) 0xcc, (byte) 0xcc,};
        Assert.assertEquals(v6a.getNetworkAddress().getAddress(), expected6a);

        IPRange v6b = IPRange.parseCIDRBlock("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:CCCC/104");
        byte[] expected6b =
                {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd,
                        (byte) 0xef, (byte) 0xff, (byte) 0xff, (byte) 0xaa, (byte) 0xaa, (byte) 0xbb, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00,};
        Assert.assertEquals(v6b.getNetworkAddress().getAddress(), expected6b);

        IPRange v4a = IPRange.parseCIDRBlock("192.168.117.17/32");
        byte[] expected4a = {(byte) 192, (byte) 168, (byte) 117, (byte) 17};
        Assert.assertEquals(v4a.getNetworkAddress().getAddress(), expected4a);

        IPRange v4b = IPRange.parseCIDRBlock("192.168.117.17/16");
        byte[] expected4b = {(byte) 192, (byte) 168, (byte) 0, (byte) 0};
        Assert.assertEquals(v4b.getNetworkAddress().getAddress(), expected4b);
    }

    @Test public void testGetHostAddress() {
        IPRange v6a = IPRange.parseCIDRBlock("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:CCCC/128");
        Assert.assertNull(v6a.getHostAddress());

        IPRange v6b = IPRange.parseCIDRBlock("1234:5678:90ab:cdef::/64");
        Assert.assertNull(v6b.getHostAddress());

        IPRange v6c = IPRange.parseCIDRBlock("1234:5678:90ab:cdef:FfFf:AaAa:BBBB:CCCC/64");
        Assert.assertNotNull(v6c.getHostAddress());
        Assert.assertEquals(v6c.getHostAddress().getAddress(), v6a.getNetworkAddress().getAddress());

        IPRange v4a = IPRange.parseCIDRBlock("192.168.117.17/32");
        Assert.assertNull(v4a.getHostAddress());

        IPRange v4b = IPRange.parseCIDRBlock("192.168.0.0/16");
        Assert.assertNull(v4b.getHostAddress());

        IPRange v4c = IPRange.parseCIDRBlock("192.168.117.17/16");
        Assert.assertNotNull(v4c.getHostAddress());
        Assert.assertEquals(v4c.getHostAddress().getAddress(), v4a.getNetworkAddress().getAddress());
    }
    
    private void testInvalid(final String address) {
        try {
            IPRange.parseCIDRBlock(address);
            Assert.fail("address should have been invalid: " + address);
        } catch (IllegalArgumentException e) {
            // expected behaviour
            return;
        }
    }
}