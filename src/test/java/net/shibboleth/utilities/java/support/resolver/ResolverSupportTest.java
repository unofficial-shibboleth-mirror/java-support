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

import java.util.Set;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class ResolverSupportTest {
    
    @Test
    public void testGetPredicates() throws ResolverException {
        final CriterionPredicateRegistry<Foo> registry = new CriterionPredicateRegistry<>();
        registry.register(TestCriterion.class, FooPredicate.class);
        
        final EvaluableTestFooCriterion evaluableCriterion = new EvaluableTestFooCriterion();
        
        Set<Predicate<Foo>> predicates;
        
        // Null criteria
        predicates = ResolverSupport.getPredicates(null, EvaluableFooCriterion.class, registry);
        Assert.assertNotNull(predicates);
        Assert.assertEquals(predicates.size(), 0);
        
        final CriteriaSet criteria = new CriteriaSet();
        
        predicates = ResolverSupport.getPredicates(criteria, EvaluableFooCriterion.class, registry);
        Assert.assertNotNull(predicates);
        Assert.assertEquals(predicates.size(), 0);
        
        criteria.clear();
        criteria.add(evaluableCriterion);
        predicates = ResolverSupport.getPredicates(criteria, EvaluableFooCriterion.class, registry);
        Assert.assertNotNull(predicates);
        Assert.assertEquals(predicates.size(), 1);
        Assert.assertTrue(predicates.contains(evaluableCriterion));
        
        criteria.clear();
        criteria.add(new TestCriterion());
        predicates = ResolverSupport.getPredicates(criteria, EvaluableFooCriterion.class, registry);
        Assert.assertNotNull(predicates);
        Assert.assertEquals(predicates.size(), 1);
        Assert.assertTrue(FooPredicate.class.isInstance(predicates.iterator().next()));
        
    }
    
    @Test
    public void testGetFilteredIterable() {
        final Foo foo1 = new Foo();
        final Foo foo2 = new Foo();
        
        Iterable<Foo> result;
        Set<Foo> resultSet;
        
        //Null candidates
        result = ResolverSupport.getFilteredIterable(null, Sets.newHashSet(new EvaluableTestFooCriterion(true)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
        //Empty candidates
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(), Sets.newHashSet(new EvaluableTestFooCriterion(true)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
        // Single predicate tests
        
        // predicate = true
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2),
                Sets.newHashSet(new EvaluableTestFooCriterion(true)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 2);
        Assert.assertTrue(resultSet.contains(foo1));
        Assert.assertTrue(resultSet.contains(foo2));
        
        // predicate = false
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), 
                Sets.newHashSet(new EvaluableTestFooCriterion(false)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
        // Multiple predicate tests
        
        // satisfyAny = false, predicates all true
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), 
                Sets.newHashSet(new EvaluableTestFooCriterion(true), new EvaluableTestFooCriterion(true)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 2);
        Assert.assertTrue(resultSet.contains(foo1));
        Assert.assertTrue(resultSet.contains(foo2));
        
        // satisfyAny = false, predicates true + false
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), 
                Sets.newHashSet(new EvaluableTestFooCriterion(true), new EvaluableTestFooCriterion(false)),
                false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
        // satisfyAny = true, predicates true + false
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), 
                Sets.newHashSet(new EvaluableTestFooCriterion(true), new EvaluableTestFooCriterion(false)),
                true, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 2);
        Assert.assertTrue(resultSet.contains(foo1));
        Assert.assertTrue(resultSet.contains(foo2));
        
        // Empty predicates tests
        
        // onEmptyPredicatesReturnEmpty = false
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), Sets.newHashSet(), false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 2);
        Assert.assertTrue(resultSet.contains(foo1));
        Assert.assertTrue(resultSet.contains(foo2));
        
        // onEmptyPredicatesReturnEmpty = false, predicates = null
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), null, false, false);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 2);
        Assert.assertTrue(resultSet.contains(foo1));
        Assert.assertTrue(resultSet.contains(foo2));
        
        // onEmptyPredicatesReturnEmpty = true
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), Sets.newHashSet(), false, true);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
        // onEmptyPredicatesReturnEmpty = true, predicates = null
        result = ResolverSupport.getFilteredIterable(Sets.newHashSet(foo1, foo2), null, false, true);
        Assert.assertNotNull(result);
        resultSet = Sets.newHashSet(result);
        Assert.assertEquals(resultSet.size(), 0);
        
    }

}
