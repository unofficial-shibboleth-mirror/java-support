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

package net.shibboleth.utilities.java.support.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CriterionPredicateRegistryTest {
    
    private TestCriterion fooCriterion;
    
    private Class<TestCriterion> testCriterionClass = TestCriterion.class;
    private Class<FooPredicate> fooPredicateClass = FooPredicate.class;
    
    @BeforeMethod
    public void setUp() {
        fooCriterion = new TestCriterion();
    }
    
    @Test
    public void testExplictRegisterDeregister() throws ResolverException {
        final CriterionPredicateRegistry<Foo> registry = new CriterionPredicateRegistry<>();
        Predicate<Foo> predicate;
        
        Assert.assertNull(registry.getPredicate(fooCriterion));
        
        registry.register(testCriterionClass, fooPredicateClass);
        predicate = registry.getPredicate(fooCriterion);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(fooPredicateClass.isInstance(predicate));
        
        registry.deregister(testCriterionClass);
        Assert.assertNull(registry.getPredicate(fooCriterion));
        
        registry.register(testCriterionClass, fooPredicateClass);
        predicate = registry.getPredicate(fooCriterion);
        Assert.assertNotNull(predicate);
        registry.clearRegistry();
        predicate = registry.getPredicate(fooCriterion);
        Assert.assertNull(predicate);
        
    }
    
    @Test
    public void testRelativeClassPathResourceLoad() throws ResolverException {
        final CriterionPredicateRegistry<Foo> registry = new CriterionPredicateRegistry<>();
        
        Assert.assertNull(registry.getPredicate(fooCriterion));
        
        registry.loadMappings("test-criterion-predicate-mappings.properties");
        
        final Predicate<Foo> predicate = registry.getPredicate(fooCriterion);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(fooPredicateClass.isInstance(predicate));
    }
    
    @Test
    public void testAbsoluteClassPathResourceLoad() throws ResolverException {
        final CriterionPredicateRegistry<Foo> registry = new CriterionPredicateRegistry<>();
        
        Assert.assertNull(registry.getPredicate(fooCriterion));
        
        registry.loadMappings("/net/shibboleth/utilities/java/support/resolver/test-criterion-predicate-mappings.properties");
        
        final Predicate<Foo> predicate = registry.getPredicate(fooCriterion);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(fooPredicateClass.isInstance(predicate));
    }
    
    @Test
    public void testPropertiesLoad() throws ResolverException, IOException {
        final CriterionPredicateRegistry<Foo> registry = new CriterionPredicateRegistry<>();
        
        Assert.assertNull(registry.getPredicate(fooCriterion));
        
        final Properties properties = new Properties();
        try (InputStream is = this.getClass().getResourceAsStream("test-criterion-predicate-mappings.properties")) {
            properties.load(is);;
        }
        registry.loadMappings(properties);
        
        final Predicate<Foo> predicate = registry.getPredicate(fooCriterion);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(fooPredicateClass.isInstance(predicate));
    }

}
