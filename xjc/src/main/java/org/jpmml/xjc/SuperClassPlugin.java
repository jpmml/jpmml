/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.xjc;

import java.util.*;

import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.*;

import org.xml.sax.*;

public class SuperClassPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "XsuperClass";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public List<String> getCustomizationURIs(){
		return Collections.singletonList(JAVA_URI);
	}

	@Override
	public boolean isCustomizationTagName(String nsUri, String localName){
		return nsUri.equals(JAVA_URI) && localName.equals("superClass");
	}

	@Override
	public void postProcessModel(Model model, ErrorHandler errorHandler){
		super.postProcessModel(model, errorHandler);

		Collection<CClassInfo> classInfos = (model.beans()).values();
		for(CClassInfo classInfo : classInfos){
			CPluginCustomization customization = classInfo.getCustomizations().find(JAVA_URI, "superClass");
			if(customization == null){
				continue;
			}

			String name = customization.element.getAttribute("name");
			if(name == null){
				continue;
			}

			CClassRef superClass = new CClassRef(model, null, createBIClass(name), null);
			classInfo.setBaseClass(superClass);

			customization.markAsAcknowledged();
		}
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		return true;
	}

	static
	private BIClass createBIClass(String name){
		try {
			java.lang.reflect.Constructor<? extends BIClass> constructor = BIClass.class.getDeclaredConstructor();
			if(!constructor.isAccessible()){
				constructor.setAccessible(true);
			}

			BIClass biClass = constructor.newInstance();

			java.lang.reflect.Field field = BIClass.class.getDeclaredField("ref");
			if(!field.isAccessible()){
				field.setAccessible(true);
			}

			field.set(biClass, name);

			return biClass;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private static final String JAVA_URI = "http://java.sun.com/java";
}