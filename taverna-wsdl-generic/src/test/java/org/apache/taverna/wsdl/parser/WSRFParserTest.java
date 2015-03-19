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

package org.apache.taverna.wsdl.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Check that WSDLParser can detect WSRF.
 * Tests {@link WSDLParser#checkWSRF()}
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class WSRFParserTest {
	
	private URL counterServiceWSDL;
	private WSDLParser wsdlParser;

	@Before
	public void findWSDL() {
		String path = "wsrf/counterService/CounterService_.wsdl";
		counterServiceWSDL = getClass().getResource(path);	
		assertNotNull("Coult not find test WSDL " + path, counterServiceWSDL);
	}
	
	@Test
	public void isWSRF() throws Exception {
		wsdlParser = new WSDLParser(counterServiceWSDL.toExternalForm());
		assertTrue("Not recognized as WSRF service", wsdlParser.isWsrfService());
	}

	
}
