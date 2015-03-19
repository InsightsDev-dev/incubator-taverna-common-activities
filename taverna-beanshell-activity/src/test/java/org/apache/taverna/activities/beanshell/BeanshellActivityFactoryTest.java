/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.beanshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author David Withers
 */
public class BeanshellActivityFactoryTest {

	private BeanshellActivityFactory factory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		factory = new BeanshellActivityFactory();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.beanshell.BeanshellActivityFactory#createActivity()}.
	 */
	@Test
	public void testCreateActivity() {
		BeanshellActivity createActivity = factory.createActivity();
		assertNotNull(createActivity);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.beanshell.BeanshellActivityFactory#getActivityType()}.
	 */
	@Test
	public void testGetActivityURI() {
		assertEquals(URI.create(BeanshellActivity.URI), factory.getActivityType());
	}

}
