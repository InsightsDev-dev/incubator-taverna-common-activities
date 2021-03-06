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

package org.apache.taverna.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XMLInputSplitter {

	private TypeDescriptor typeDescriptor;
	private String[] outputNames;
	private String[] inputNames;
	private String[] inputTypes;

	public XMLInputSplitter(TypeDescriptor typeDescriptor, String inputNames[],
			String inputTypes[], String[] outputNames) {
		this.typeDescriptor = typeDescriptor;
		this.outputNames = outputNames;
		this.inputTypes = inputTypes;
		this.inputNames = inputNames;
	}

	public Map<String, String> execute(Map<String, Object> inputMap)
			throws JDOMException, IOException {
		Map<String, String> result = new HashMap<String, String>();
		Element outputElement = (this.typeDescriptor.getName().length() > 0 ? new Element(
				this.typeDescriptor.getName())
				: new Element(this.typeDescriptor.getType()));

		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			executeForComplexType(inputMap, result, outputElement);

		} else {
			for (String key : inputMap.keySet()) {
				Object dataObject = inputMap.get(key);

				if (dataObject instanceof List) {
					Element dataElement = buildElementFromObject(key, "");
					for (Object dataItem : ((List<?>) dataObject)) {
						Element itemElement = buildElementFromObject(key,
								dataItem);
						dataElement.addContent(itemElement);
					}

					XMLOutputter outputter = new XMLOutputter();
					String xmlText = outputter.outputString(dataElement);

					result.put(outputNames[0], xmlText);
				} else {
					Element dataElement = buildElementFromObject(key,
							dataObject);
					outputElement.addContent(dataElement);
					XMLOutputter outputter = new XMLOutputter();
					String xmlText = outputter.outputString(outputElement);
					result.put(outputNames[0], xmlText);
				}

			}
		}

		return result;

	}

	private void executeForComplexType(Map<String, Object> inputMap,
			Map<String, String> result, Element outputElement)
			throws JDOMException, IOException {
		ComplexTypeDescriptor complexDescriptor = (ComplexTypeDescriptor) typeDescriptor;
		for (TypeDescriptor elementType : complexDescriptor.getElements()) {
			String key = elementType.getName();
			Object dataObject = inputMap.get(key);			
			if (dataObject==null) {
				if (elementType.isOptional()) {
					continue;
				} if (elementType.isNillable()) {
					dataObject = "xsi:nil";
				} else {
					dataObject="";
				}
			}

			if (dataObject instanceof List) {
				Element arrayElement = buildElementFromObject(key, "");

				String itemkey = "item";
				boolean wrapped = false;
				if (elementType instanceof ArrayTypeDescriptor) {
					wrapped = ((ArrayTypeDescriptor) elementType).isWrapped();
					TypeDescriptor arrayElementType = ((ArrayTypeDescriptor) elementType)
							.getElementType();
					if (!wrapped) {
						itemkey = elementType.getName();
					} else {
						if (arrayElementType.getName() != null
								&& arrayElementType.getName().length() > 0) {
							itemkey = arrayElementType.getName();
						} else {
							itemkey = arrayElementType.getType();
						}
					}

				}

				for (Object itemObject : ((List<?>)dataObject)) {

					Element dataElement = buildElementFromObject(itemkey,
							itemObject);
					dataElement.setNamespace(Namespace.getNamespace(elementType
							.getNamespaceURI()));
					if (!wrapped) {
						dataElement.setName(itemkey);
						outputElement.addContent(dataElement);
					} else {
						arrayElement.addContent(dataElement);
					}
				}
				if (wrapped)
					outputElement.addContent(arrayElement);
			} else {
				Element dataElement = buildElementFromObject(key, dataObject);
				outputElement.addContent(dataElement);
			}
		}
		for (TypeDescriptor attribute : complexDescriptor.getAttributes()) {
			String key = attribute.getName();
			Object dataObject = inputMap.get("1" + key);
			if (dataObject == null) {
				dataObject = inputMap.get(key);
			}
			if (dataObject != null) {
				outputElement.setAttribute(key, dataObject.toString(), Namespace
						.getNamespace(attribute.getQname().getPrefix(), attribute.getNamespaceURI()));
			}
		}
		
		outputElement.setNamespace(Namespace.getNamespace(typeDescriptor
				.getNamespaceURI()));
		XMLOutputter outputter = new XMLOutputter();
		String xmlText = outputter.outputString(outputElement);
		result.put(outputNames[0], xmlText);
	}

	private Element buildElementFromObject(String key, Object dataObject)
			throws JDOMException, IOException {

		Element dataElement = null;

		if (isXMLInput(key)) {
			dataElement = createDataElementForXMLInput(dataObject, key);
		} else {
			dataElement = new Element(key);
			setDataElementNamespace(key, dataElement);
			Namespace xsiNs = org.jdom.Namespace
				.getNamespace("xsi",
						"http://www.w3.org/2001/XMLSchema-instance");
			if (dataObject.toString().equals("xsi:nil")) {
				dataElement.setAttribute("nil", "true", xsiNs); // changes nil value
			} else {
				if (dataObject instanceof byte[]) {
				
					dataElement
							.setAttribute(
									"type",
									"xsd:base64Binary",
									xsiNs);
					dataObject = DatatypeConverter.printBase64Binary((byte[]) dataObject);
				}
				dataElement.setText(dataObject.toString());
			}

		}
		return dataElement;
	}

	private Element createDataElementForXMLInput(Object dataObject, String key)
			throws JDOMException, IOException {
		Element dataElement = null;
		String xml = dataObject.toString();
		if (xml.length() > 0) {
			Document doc = new SAXBuilder().build(new StringReader(xml));
			dataElement = doc.getRootElement();
			dataElement.detach();
		} else {
			dataElement = new Element(key);
		}

		setDataElementNamespace(key, dataElement);
		return dataElement;
	}

	// set the namespace if it can be determined from the element TypeDescriptor
	// by the key
	private void setDataElementNamespace(String key, Element dataElement) {
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			TypeDescriptor elementTypeDescriptor = ((ComplexTypeDescriptor) typeDescriptor)
					.elementForName(key);
			if (elementTypeDescriptor != null) {
				String nsURI = null;
				if (elementTypeDescriptor instanceof BaseTypeDescriptor) {
					nsURI = elementTypeDescriptor.getNamespaceURI();
					// this is some protective code against old workflows that
					// had the base element namespace incorrectly
					// declared (it was using the type NS, rather than the
					// element NS.
					if (nsURI.contains("XMLSchema")
							&& nsURI.contains("http://www.w3.org")) {
						nsURI = typeDescriptor.getNamespaceURI();
					}
				} else {
					nsURI = elementTypeDescriptor.getNamespaceURI();
				}
				if (nsURI != null && nsURI.length() > 0) {
					updateElementNamespace(dataElement, nsURI);
				}
			}
		}
	}

	/**
	 * Updates the element namespace, and also iterates all descendant elements.
	 * If these elements have no default namespace, or is blank then it is also
	 * set to namespaceURI (JDOM by default will not set the child elements to
	 * the same namespace as the element modified but will override them with
	 * blank namespaces).
	 * 
	 * @param dataElement
	 * @param namespaceURI
	 */
	private void updateElementNamespace(Element dataElement, String namespaceURI) {
		dataElement.setNamespace(Namespace.getNamespace(namespaceURI));
		Iterator<?> iterator = dataElement.getDescendants();
		while (iterator.hasNext()) {
			Object descendantObject = iterator.next();
			if (descendantObject instanceof Element) {
				Element childElement = (Element) descendantObject;
				if (childElement.getNamespaceURI() == null) {
					childElement.setNamespace(Namespace
							.getNamespace(namespaceURI));
				}
			}
		}
	}

	private boolean isXMLInput(String key) {
		boolean result = false;
		for (int i = 0; i < inputNames.length; i++) {
			if (inputNames[i].equals(key)) {
				result = inputTypes[i].indexOf("'text/xml'") != -1;
			}
		}
		return result;
	}

}
