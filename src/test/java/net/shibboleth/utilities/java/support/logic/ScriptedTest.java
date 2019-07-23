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

        ScriptedPredicate<Object> test = ScriptedPredicate.inlineScript(returnSelf);

        Assert.assertTrue(test.test(Boolean.TRUE));
        Assert.assertFalse(test.test(Boolean.FALSE));
        Assert.assertFalse(test.test(Integer.valueOf(1)));
        test.setReturnOnError(true);
        Assert.assertTrue(test.test(Integer.valueOf(1)));
    }

    @Test public void testPredicateCustom() throws ScriptException {

        ScriptedPredicate<Object> test = ScriptedPredicate.inlineScript(returnCustom);

        test.setCustomObject(Boolean.TRUE);
        Assert.assertTrue(test.test(Boolean.FALSE));
        test.setCustomObject(Boolean.FALSE);
        Assert.assertFalse(test.test(Boolean.TRUE));
        test.setCustomObject(Integer.valueOf(1));
        Assert.assertFalse(test.test("true"));
        test.setReturnOnError(true);
        Assert.assertTrue(test.test("false"));
    }

    @Test public void testBadScriptPredicate() throws ScriptException {

        final ScriptedPredicate<Object> test = ScriptedPredicate.inlineScript(returnSelfString);

        test.setHideExceptions(true);
        test.setReturnOnError(true);
        Assert.assertTrue(test.test(null));
        test.setReturnOnError(false);
        Assert.assertFalse(test.test(null));

        test.setHideExceptions(false);
        try {
            Assert.assertFalse(test.test(null));
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), ScriptException.class);
            // nothing
        }
    }

    @Test public void testFunction() throws ScriptException {

        ScriptedFunction<Object,Object> test = ScriptedFunction.inlineScript(returnSelf);

        Assert.assertEquals(test.apply(Boolean.FALSE), Boolean.FALSE);
        Assert.assertEquals(test.apply(Boolean.TRUE), Boolean.TRUE);
        Assert.assertEquals(test.apply(Integer.valueOf(1)), Integer.valueOf(1));
        test.setOutputType(Boolean.class);
        Assert.assertEquals(test.apply(Boolean.FALSE), Boolean.FALSE);
        Assert.assertEquals(test.apply(Boolean.TRUE), Boolean.TRUE);
        Assert.assertNotEquals(test.apply(Integer.valueOf(1)), Integer.valueOf(1));
        test.setReturnOnError(Boolean.TRUE);
        Assert.assertEquals(test.apply(Integer.valueOf(1)), Boolean.TRUE);
    }

    @Test public void testBadScriptFunction() throws ScriptException {

        ScriptedFunction<Boolean,Boolean> test = ScriptedFunction.inlineScript(returnSelfString);
        test.setOutputType(Boolean.class);
        test.setInputType(Boolean.class);

        test.setHideExceptions(true);
        test.setReturnOnError(true);
        Assert.assertEquals(test.apply(null), Boolean.TRUE);
        test.setReturnOnError(false);
        Assert.assertEquals(test.apply(null), Boolean.FALSE);

        test.setHideExceptions(false);
        try {
            Assert.assertEquals(test.apply(null), Boolean.TRUE);
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), ScriptException.class);
            // nothing
        }
    }
}
