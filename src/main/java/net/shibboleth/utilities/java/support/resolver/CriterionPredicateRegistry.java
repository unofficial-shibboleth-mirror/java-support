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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry which manages mappings from types of {@link Criterion} to types of {@link Predicate}
 * which can evaluate that criterion's data against a particular target type.
 *
 * <p>
 * Each predicate's implementation that is registered <strong>MUST</strong> implement a 
 * single-arg constructor which takes an instance of the {@link Criterion} to be evaluated.
 * The predicate instance is instantiated reflectively based on this requirement.
 * </p>
 * 
 * @param <T> the target type which the returned predicates evaluate
 */
public class CriterionPredicateRegistry<T> {

    /** Logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(CriterionPredicateRegistry.class);

    /** Storage for the registry mappings. */
    @Nonnull private Map<Class<? extends Criterion>, Class<? extends Predicate<T>>> registry;
    
    /** Constructor. */
    public CriterionPredicateRegistry() {
        registry = new HashMap<>();
    }

    /**
     * Get an instance of {@link Predicate} which can evaluate the supplied criterion's
     * requirements against a target of the specified type.
     * 
     * @param criterion the criterion to be evaluated
     * @return an predicate instance representing the specified criterion's requirements
     * @throws ResolverException thrown if there is an error reflectively instantiating a new instance of
     *             the predicate type based on class information stored in the registry
     */
    @Nullable public Predicate<T> getPredicate(@Nonnull final Criterion criterion) throws ResolverException {
        Constraint.isNotNull(criterion, "Criterion to map cannot be null");
        
        final Class<? extends Predicate<T>> predicateClass = lookup(criterion.getClass());

        if (predicateClass != null) {
            log.debug("Registry located Predicate class {} for Criterion class {}", predicateClass.getName(),
                    criterion.getClass().getName());

            try {
                final Constructor<? extends Predicate<T>> constructor = 
                        predicateClass.getConstructor(new Class[] { criterion.getClass() });

                return constructor.newInstance(new Object[] { criterion });

            } catch (final SecurityException | InstantiationException | IllegalAccessException 
                    | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Error instantiating new Predicate instance", e);
                throw new ResolverException("Could not create new Predicate instance", e);
            }
        }
        
        log.debug("Registry did not locate Predicate implementation registered for Criterion class {}", 
                criterion.getClass().getName());
        return null;
    }
    

    /**
     * Lookup the predicate class type which is registered for the specified Criterion class.
     * 
     * @param clazz the Criterion class subtype to lookup
     * @return the registered predicate class type
     */
    @Nullable protected Class<? extends Predicate<T>> lookup(@Nonnull final Class<? extends Criterion> clazz) {
        Constraint.isNotNull(clazz, "Criterion class to lookup cannot be null");
        return registry.get(clazz);
    }

    /**
     * Register a {@link Predicate} class for a criterion class.
     * 
     * @param criterionClass class subtype of {@link Criterion}
     * @param predicateClass the predicate class type
     */
    public void register(@Nonnull final Class<? extends Criterion> criterionClass,
            @Nonnull final Class<? extends Predicate<T>> predicateClass) {
        Constraint.isNotNull(criterionClass, "Criterion class to register cannot be null");
        Constraint.isNotNull(predicateClass, "Predicate class to register cannot be null");
        
        log.debug("Registering class {} as Predicate for Criterion class {}", 
                predicateClass.getName(), criterionClass.getName());

        registry.put(criterionClass, predicateClass);
    }

    /**
     * Deregister a criterion-evaluator mapping.
     * 
     * @param criterionClass class subtype of {@link Criterion}
     */
    public void deregister(@Nonnull final Class<? extends Criterion> criterionClass) {
        Constraint.isNotNull(criterionClass, "Criterion class to unregister cannot be null");
        
        log.debug("Deregistering Predicate for Criterion class {}", criterionClass.getName());
        registry.remove(criterionClass);
    }

    /**
     * Clear all mappings from the registry.
     */
    public void clearRegistry() {
        log.debug("Clearing Criterion Predicate registry");
        registry.clear();
    }

//CheckStyle: ReturnCount OFF
    /**
     * Load criterion -&gt; predicate mappings from a classpath resource.
     * 
     * @param classpathResource the classpath resource path from which to load mapping properites
     */
    public void loadMappings(@Nonnull final String classpathResource) {
        final String resource = Constraint.isNotNull(StringSupport.trimOrNull(classpathResource),
                "Classpath resource was null or empty");
        try (final InputStream inStream = this.getClass().getResourceAsStream(resource)) {
            if (inStream == null) {
                log.error("Could not open resource stream from resource '{}'", resource);
                return;
            }
            final Properties mappings = new Properties();
            mappings.load(inStream);
            loadMappings(mappings);
        } catch (final IOException e) {
            log.error("Error load mappings from resource '{}'", resource, e);
            return;
        }
    }
  //CheckStyle: ReturnCount ON

    /**
     * Load a set of criterion -&gt; predicate mappings from the supplied properties set.
     * 
     * @param mappings properties set where the key is the criterion class name, the value is the predicate class name
     */
    public void loadMappings(@Nonnull final Properties mappings) {
        Constraint.isNotNull(mappings, "Mappings to load cannot be null");
        
        for (final Object key : mappings.keySet()) {
            if (!(key instanceof String)) {
                log.error("Properties key was not an instance of String, was '{}', skipping...", 
                        key.getClass().getName());
                continue;
            }
            final String criterionName = (String) key;
            final String predicateName = mappings.getProperty(criterionName);

            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<? extends Criterion> criterionClass = null;
            try {
                criterionClass = classLoader.loadClass(criterionName).asSubclass(Criterion.class);
            } catch (final ClassNotFoundException e) {
                log.error("Could not find Criterion class '{}', skipping registration", criterionName);
                continue;
            }

            Class<? extends Predicate<T>> predicateClass = null;
            try {
                predicateClass = (Class<? extends Predicate<T>>) classLoader.loadClass(
                        predicateName).asSubclass(Predicate.class);
            } catch (final ClassNotFoundException e) {
                log.error("Could not find Predicate class '{}', skipping registration", criterionName);
                continue;
            }

            register(criterionClass, predicateClass);
        }

    }
    
}
