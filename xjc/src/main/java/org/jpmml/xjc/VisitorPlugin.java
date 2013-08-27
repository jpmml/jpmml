/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.xjc;

import java.util.*;

import com.sun.codemodel.*;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.tools.xjc.util.*;
import com.sun.xml.bind.api.impl.*;

import org.xml.sax.*;

public class VisitorPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "Xvisitor";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		JCodeModel codeModel = outline.getCodeModel();

		CodeModelClassFactory clazzFactory = outline.getClassFactory();

		JClass objectClazz = codeModel.ref("org.dmg.pmml.PMMLObject");

		JPackage modelPackage = objectClazz._package();

		JDefinedClass visitor = clazzFactory.createInterface(modelPackage, JMod.PUBLIC, "Visitor", null);

		JDefinedClass abstractVisitor = clazzFactory.createClass(modelPackage, JMod.ABSTRACT | JMod.PUBLIC, "AbstractVisitor", null)._implements(visitor);
		JDefinedClass abstractSimpleVisitor = clazzFactory.createClass(modelPackage, JMod.ABSTRACT | JMod.PUBLIC, "AbstractSimpleVisitor", null)._implements(visitor);

		JMethod defaultMethod = abstractSimpleVisitor.method(JMod.ABSTRACT | JMod.PUBLIC, void.class, "visit");
		defaultMethod.param(objectClazz, "object");

		Set<JType> traversableTypes = new LinkedHashSet<JType>();

		Collection<? extends ClassOutline> clazzes = outline.getClasses();
		for(ClassOutline clazz : clazzes){
			JDefinedClass beanClazz = clazz.implClass;
			traversableTypes.add(beanClazz);

			JClass beanSuperClazz = beanClazz._extends();
			traversableTypes.add(beanSuperClazz);
		} // End for

		for(ClassOutline clazz : clazzes){
			JDefinedClass beanClazz = clazz.implClass;

			String parameterName = NameConverter.standard.toVariableName(beanClazz.name());
			if(!JJavaName.isJavaIdentifier(parameterName)){
				parameterName = ("_" + parameterName);
			}

			JMethod visitorVisit = visitor.method(JMod.PUBLIC, void.class, "visit");
			visitorVisit.param(beanClazz, parameterName);

			JMethod abstractVisitorVisit = abstractVisitor.method(JMod.PUBLIC, void.class, "visit");
			abstractVisitorVisit.annotate(Override.class);
			abstractVisitorVisit.param(beanClazz, parameterName);

			JClass beanSuperClass = beanClazz._extends();

			JMethod abstractSimpleVisitorVisit = abstractSimpleVisitor.method(JMod.PUBLIC, void.class, "visit");
			abstractSimpleVisitorVisit.annotate(Override.class);
			abstractSimpleVisitorVisit.param(beanClazz, parameterName);
			abstractSimpleVisitorVisit.body().add(JExpr.invoke(defaultMethod).arg(JExpr.cast(beanSuperClass, JExpr.ref(parameterName))));

			JMethod beanAccept = beanClazz.method(JMod.PUBLIC, void.class, "accept");
			beanAccept.annotate(Override.class);

			JVar visitorParameter = beanAccept.param(visitor, "visitor");

			JBlock body = beanAccept.body();
			body.add(JExpr.invoke(visitorParameter, "visit").arg(JExpr._this()));

			List<FieldOutline> beanFields = Arrays.asList(clazz.getDeclaredFields());
			for(FieldOutline beanField : beanFields){
				CPropertyInfo propertyInfo = beanField.getPropertyInfo();

				String fieldName = propertyInfo.getName(false);

				JFieldRef fieldRef = (JExpr._this()).ref(fieldName);

				JType fieldType = beanField.getRawType();

				// Collection of values
				if(propertyInfo.isCollection()){
					JClass classFieldType = (JClass)fieldType;

					List<JClass> elementTypes = classFieldType.getTypeParameters();

					JType fieldElementType = elementTypes.get(0);
					if(traversableTypes.contains(fieldElementType)){
						JForLoop forLoop = body._for();
						JVar var = forLoop.init(codeModel.INT, "i", JExpr.lit(0));
						forLoop.test(fieldRef.ne(JExpr._null()).cand(var.lt(fieldRef.invoke("size"))));
						forLoop.update(var.incr());
						forLoop.body().add((JExpr.invoke(fieldRef, "get").arg(var)).invoke("accept").arg(visitorParameter));
					}
				} else

				// Simple value
				{
					if(traversableTypes.contains(fieldType)){
						body._if(fieldRef.ne(JExpr._null()))._then().add(JExpr.invoke(fieldRef, "accept").arg(visitorParameter));
					}
				}
			}
		}

		return true;
	}
}