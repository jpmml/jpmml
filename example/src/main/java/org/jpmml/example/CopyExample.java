/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.transform.sax.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class CopyExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 2){
			System.err.println("Usage: java " + CopyExample.class.getName() + " <Source file> <Destination file>");

			System.exit(-1);
		}

		File srcFile = new File(args[0]);
		File destFile = new File(args[1]);

		copyPmml(srcFile, destFile);
	}

	static
	public void copyPmml(File srcFile, File destFile) throws Exception {
		writePmml(readPmml(srcFile), destFile);
	}

	static
	public PMML readPmml(File file) throws Exception {
		InputStream is = new FileInputStream(file);

		try {
			InputSource source = new InputSource(is);

			XMLReader reader = XMLReaderFactory.createXMLReader();

			NamespaceURIFilter filteredReader = new NamespaceURIFilter();
			filteredReader.initDefaultMappings();

			filteredReader.setParent(reader);

			SAXSource filteredSource = new SAXSource(filteredReader, source);

			Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
			return (PMML)unmarshaller.unmarshal(filteredSource);
		} finally {
			is.close();
		}
	}

	static
	public void writePmml(PMML pmml, File file) throws Exception {
		OutputStream os = new FileOutputStream(file);

		try {
			Marshaller marshaller = getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.marshal(pmml, os);
		} finally {
			os.close();
		}
	}

	static
	private JAXBContext getJAXBContext() throws JAXBException {

		if(CopyExample.jaxbCtx == null){
			CopyExample.jaxbCtx = JAXBContext.newInstance(ObjectFactory.class);
		}

		return CopyExample.jaxbCtx;
	}

	private static JAXBContext jaxbCtx = null;
}