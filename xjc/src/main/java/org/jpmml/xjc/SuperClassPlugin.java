/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.xjc;

import java.lang.reflect.*;
import java.util.*;

import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.*;

import org.jvnet.jaxb2_commons.plugin.*;

import org.xml.sax.*;

public class SuperClassPlugin extends AbstractParameterizablePlugin {

	private String defaultName = null;


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

		CClassRef defaultSuperClass = null;

		String defaultName = getDefaultName();
		if(defaultName != null){
			defaultSuperClass = new CClassRef(model, null, createBIClass(defaultName), null);
		}

		Collection<CClassInfo> classInfos = (model.beans()).values();
		for(CClassInfo classInfo : classInfos){
			CPluginCustomization customization = classInfo.getCustomizations().find(JAVA_URI, "superClass");

			if(customization != null){
				String name = customization.element.getAttribute("name");
				if(name != null){
					CClassRef superClass = new CClassRef(model, null, createBIClass(name), null);
					classInfo.setBaseClass(superClass);
				}

				customization.markAsAcknowledged();
			} else

			{
				if(defaultSuperClass != null){
					classInfo.setBaseClass(defaultSuperClass);
				}
			}
		}
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		Model model = outline.getModel();

		try {
			Field customizationsField = (Model.class).getDeclaredField("customizations");
			if(!customizationsField.isAccessible()){
				customizationsField.setAccessible(true);
			}

			CCustomizations customizations = (CCustomizations)customizationsField.get(model);

			Field nextField = (CCustomizations.class).getDeclaredField("next");
			if(!nextField.isAccessible()){
				nextField.setAccessible(true);
			}

			while(customizations != null){
				Collection<CPluginCustomization> pluginCustomizations = customizations;
				for(CPluginCustomization pluginCustomization : pluginCustomizations){

					if(!isSuperClassPluginCustomization(pluginCustomization)){
						continue;
					}

					// XXX: Log a warning
					if(!pluginCustomization.isAcknowledged()){
						pluginCustomization.markAsAcknowledged();
					}
				}

				customizations = (CCustomizations)nextField.get(customizations);
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		return true;
	}

	private boolean isSuperClassPluginCustomization(CPluginCustomization pluginCustomization){
		return isCustomizationTagName(pluginCustomization.element.getNamespaceURI(), pluginCustomization.element.getLocalName());
	}

	public String getDefaultName(){
		return this.defaultName;
	}

	public void setDefaultName(String defaultName){
		this.defaultName = defaultName;
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