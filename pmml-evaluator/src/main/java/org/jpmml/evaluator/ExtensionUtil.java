/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import org.w3c.dom.Node;

public class ExtensionUtil {

	private ExtensionUtil(){
	}

	static
	public <E extends PMMLObject> E getExtension(Extension extension, Class<? extends E> clazz){
		XmlRootElement rootElement = clazz.getAnnotation(XmlRootElement.class);
		if(rootElement == null){
			throw new IllegalArgumentException();
		}

		String name = rootElement.name();

		List<?> objects = extension.getContent();
		for(Object object : objects){

			if(object instanceof Node){
				Node node = (Node)object;

				if((name).equals(node.getLocalName())){
					Source source = new DOMSource(node);

					try {
						return clazz.cast(IOUtil.unmarshal(source));
					} catch(JAXBException je){
						throw new InvalidFeatureException(extension);
					}
				}
			}
		}

		return null;
	}
}