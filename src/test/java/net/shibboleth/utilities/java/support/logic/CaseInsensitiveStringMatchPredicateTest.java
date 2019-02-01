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
import org.testng.annotations.Test;

/** {@link CaseInsensitiveStringMatchPredicate} unit test. */
public class CaseInsensitiveStringMatchPredicateTest {
    
    @Test public void testApply(){
        CaseInsensitiveStringMatchPredicate predicate = new CaseInsensitiveStringMatchPredicate("FoO");
        
        if(predicate.test(null)){
            Assert.fail();
        }
        
        if(predicate.test("")){
            Assert.fail();
        }
        
        if(predicate.test("  ")){
            Assert.fail();
        }
        
        if(!predicate.test("foo")){
            Assert.fail();
        }
        
        if(!predicate.test("FOO")){
            Assert.fail();
        }
        
        if(predicate.test("bar")){
            Assert.fail();
        }
    }
}