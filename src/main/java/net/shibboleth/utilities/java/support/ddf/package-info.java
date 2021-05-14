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

/**
 * Implementation of the Dynamic Dataflow abstraction used in the
 * Service Provider for interprocess communication.
 * 
 * <p>DDFs are somewhat JSON-like, but with a (subjectively) friendlier
 * API and allow for arbitrary serialization. They support dynamic RPC
 * interfaces that don't require pre-definition of a data contract or
 * compilation of client/server stubs.</p>
 */

package net.shibboleth.utilities.java.support.ddf;