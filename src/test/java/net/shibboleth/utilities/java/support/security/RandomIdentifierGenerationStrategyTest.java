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

package net.shibboleth.utilities.java.support.security;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;

public class RandomIdentifierGenerationStrategyTest {

    /**
     * Test generateIdentifier by generating a large number of identifiers
     * and seeing whether each one is a valid XML ID, and that they are
     * all different.
     */
    @Test
    public void testGenerateIdentifier() {
      final Pattern ncNamePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_\\-\\.]+$");
      final IdentifierGenerationStrategy strat = new RandomIdentifierGenerationStrategy();
      final int howMany = 1000;
      final Set<String> values = new HashSet<>(1000);
      for (int iteration = 1; iteration<=howMany; iteration++) {
          final String value = strat.generateIdentifier();
          
          // we shouldn't see the same value twice
          if (values.contains(value)) {
              Assert.fail("duplicate value " + value + " on iteration " + iteration);
          }
          values.add(value);
          
          // values should be valid NCNames
          final Matcher match = ncNamePattern.matcher(value);
          if (!match.matches()) {
              Assert.fail("value " + value + " is not a valid NCName on iteration " + iteration);
          }
      }
  }
}
