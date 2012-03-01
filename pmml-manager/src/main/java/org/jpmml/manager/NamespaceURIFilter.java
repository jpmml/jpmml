/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class NamespaceURIFilter extends XMLFilterImpl {

	private Map<String, String> mappings = new HashMap<String, String>();


	public NamespaceURIFilter(){
	}

	public NamespaceURIFilter(XMLReader parent){
		super(parent);
	}

	public void initDefaultMappings(){
		setMapping(Version.PMML_3_0.getURI(), Version.PMML_4_1.getURI());
		setMapping(Version.PMML_3_1.getURI(), Version.PMML_4_1.getURI());
		setMapping(Version.PMML_3_2.getURI(), Version.PMML_4_1.getURI());
		setMapping(Version.PMML_4_0.getURI(), Version.PMML_4_1.getURI());

		// Missing XML namespace declaration
		setMapping("", Version.PMML_4_1.getURI());
	}

	public void setMapping(String fromNsURI, String toNsURI){
		this.mappings.put(fromNsURI, toNsURI);
	}

	public Map<String, String> getMappings(){
		return this.mappings;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException{
		super.startElement(filterNamespaceURI(namespaceURI), localName, qualifiedName, attributes);
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		super.endElement(filterNamespaceURI(namespaceURI), localName, qualifiedName);
	}

	private String filterNamespaceURI(String fromNsURI){
		String toNsURI = this.mappings.get(fromNsURI);

		return (toNsURI != null ? toNsURI : fromNsURI);
	}
}