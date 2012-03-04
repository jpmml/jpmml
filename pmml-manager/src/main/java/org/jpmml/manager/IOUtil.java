/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;

import org.dmg.pmml.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class IOUtil {

	private IOUtil(){
	}

	static
	public Source createImportSource(InputSource source) throws SAXException {
		XMLReader reader = XMLReaderFactory.createXMLReader();

		ImportFilter filter = new ImportFilter(reader);

		return new SAXSource(filter, source);
	}
}