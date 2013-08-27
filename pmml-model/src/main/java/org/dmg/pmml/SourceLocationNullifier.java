/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

public class SourceLocationNullifier extends AbstractSimpleVisitor {

	@Override
	public VisitorAction visit(PMMLObject object){
		object.setSourceLocation(null);

		return VisitorAction.CONTINUE;
	}
}