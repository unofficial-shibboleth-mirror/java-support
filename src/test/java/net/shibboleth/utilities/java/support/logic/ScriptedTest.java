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

package net.shibboleth.utilities.java.support.logic;

import org.testng.Assert;

import javax.script.ScriptException;

import org.testng.annotations.Test;

/**
 * Tests for the {@link ScriptedFunction} and {@link ScriptedPredicate}.
 */
public class ScriptedTest {

    static final private String returnSelf="input";
    static final private String returnSelfString="input.toString()";
    static final private String returnCustom="custom";

    @Test public void testPredicate() throws ScriptException {

        ScriptedPredicate test = ScriptedPredicate.inlineScript(returnSelf);

        Assert.assertTrue(test.apply(Boolean.TRUE));
        Assert.assertFalse(test.apply(Boolean.FALSE));
        Assert.assertFalse(test.apply(new Integer(1)));
        test.setReturnOnError(true);
        Assert.assertTrue(test.apply(new Integer(1)));
    }

    @Test public void testPredicateCustom() throws ScriptException {

        ScriptedPredicate test = ScriptedPredicate.inlineScript(returnCustom);

        test.setCustomObject(Boolean.TRUE);
        Assert.assertTrue(test.apply(Boolean.FALSE));
        test.setCustomObject(Boolean.FALSE);
        Assert.assertFalse(test.apply(Boolean.TRUE));
        test.setCustomObject(new Integer(1));
        Assert.assertFalse(test.apply("true"));
        test.setReturnOnError(true);
        Assert.assertTrue(test.apply("false"));
    }

    @Test public void testBadScriptPredicate() throws ScriptException {

        final ScriptedPredicate test = ScriptedPredicate.inlineScript(returnSelfString);

        test.setHideExceptions(true);
        test.setReturnOnError(true);
        Assert.assertTrue(test.apply(null));
        test.setReturnOnError(false);
        Assert.assertFalse(test.apply(null));

        test.setHideExceptions(false);
        try {
            Assert.assertFalse(test.apply(null));
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), ScriptException.class);
            // nothing
        }
    }

    @Test public void testFunction() throws ScriptException {

        ScriptedFunction test = ScriptedFunction.inlineScript(returnSelf);

        Assert.assertEquals(test.apply(Boolean.FALSE), Boolean.FALSE);
        Assert.assertEquals(test.apply(Boolean.TRUE), Boolean.TRUE);
        Assert.assertEquals(test.apply(new Integer(1)), new Integer(1));
        test.setOutputType(Boolean.class);
        Assert.assertEquals(test.apply(Boolean.FALSE), Boolean.FALSE);
        Assert.assertEquals(test.apply(Boolean.TRUE), Boolean.TRUE);
        Assert.assertNotEquals(test.apply(new Integer(1)), new Integer(1));
        test.setReturnOnError(Boolean.TRUE);
        Assert.assertEquals(test.apply(new Integer(1)), Boolean.TRUE);

        test.setReturnOnError(Boolean.TRUE);
        test.setOutputType(Integer.class);
        test.setInputType(Integer.class);
        Assert.assertEquals(test.apply(Boolean.FALSE), Boolean.TRUE);
    }

    @Test public void testFunctionCustom() throws ScriptException {

        ScriptedFunction test = ScriptedFunction.inlineScript(returnCustom);

        test.setReturnOnError(new Integer(99));
        test.setOutputType(Integer.class);
        test.setInputType(Integer.class);
        test.setCustomObject(12);
        Assert.assertEquals(test.apply(false), 99);
        Assert.assertEquals(test.apply(1), 12);
        test.setCustomObject(false);
        Assert.assertEquals(test.apply(false), 99);
    }

    @Test public void testBadScriptFunction() throws ScriptException {

        ScriptedFunction test = ScriptedFunction.inlineScript(returnSelfString);
        test.setOutputType(Boolean.class);
        test.setInputType(Boolean.class);

        test.setHideExceptions(true);
        test.setReturnOnError(true);
        Assert.assertEquals(test.apply(null), true);
        test.setReturnOnError(false);
        Assert.assertEquals(test.apply(null), false);

        test.setHideExceptions(false);
        try {
            Assert.assertEquals(test.apply(null), true);
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), ScriptException.class);
            // nothing
        }
    }
}
