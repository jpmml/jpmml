/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.xjc;

import java.util.*;

import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.xml.bind.v2.model.core.*;

import org.xml.sax.ErrorHandler;

public class PMMLPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "Xpmml";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public void postProcessModel(Model model, ErrorHandler errorHandler){
		super.postProcessModel(model, errorHandler);

		Collection<CClassInfo> classInfos = (model.beans()).values();
		for(CClassInfo classInfo : classInfos){
			CTypeRef extension = null;

			Collection<CPropertyInfo> propertyInfos = classInfo.getProperties();
			for(CPropertyInfo propertyInfo : propertyInfos){

				if(propertyInfo.isCollection() && (propertyInfo.getName(false)).contains("And")){
					propertyInfo.setName(true, "Content");
					propertyInfo.setName(false, "content");

					if(propertyInfo instanceof CElementPropertyInfo){
						CElementPropertyInfo elementPropertyInfo = (CElementPropertyInfo)propertyInfo;

						Iterator<? extends CTypeRef> types = (elementPropertyInfo.getTypes()).iterator();
						while(types.hasNext()){
							CTypeRef type = types.next();

							CNonElement typeUse = type.getTarget();
							if(typeUse instanceof CClassInfo){
								CClassInfo classUse = (CClassInfo)typeUse;

								if((classUse.fullName()).equals("org.dmg.pmml.Extension")){
									extension = type;

									types.remove();
								}
							}
						}
					}
				} else

				if(propertyInfo.isCollection() && (propertyInfo.getName(false)).equals("arraies")){
					propertyInfo.setName(true, "Arrays");
					propertyInfo.setName(false, "arrays");
				}
			}

			if(extension != null){
				CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo("Extensions", CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT, ID.NONE, null, null, null, null, false);
				elementPropertyInfo.getTypes().add(extension);

				// Insert into the first position
				classInfo.getProperties().add(0, elementPropertyInfo);
			}
		}
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		return true;
	}
}