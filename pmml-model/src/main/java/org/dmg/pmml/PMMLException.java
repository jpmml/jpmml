/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import com.sun.xml.bind.*;

import org.xml.sax.*;

abstract
public class PMMLException extends RuntimeException {

	private PMMLObject context = null;


	public PMMLException(){
		super();
	}

	public PMMLException(String message){
		super(message);
	}

	public PMMLException(PMMLObject context){
		super();

		setContext(context);
	}

	public PMMLException(String message, PMMLObject context){
		super(message);

		setContext(context);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());

		Locatable locatable = getContext();
		if(locatable != null){
			Locator locator = locatable.sourceLocation();

			sb.append(" ").append("(at around line ").append(locator.getLineNumber()).append(")");
		} // End if

		String message = getLocalizedMessage();
		if(message != null){
			sb.append(":");

			sb.append(" ").append(message);
		}

		return sb.toString();
	}

	public PMMLObject getContext(){
		return this.context;
	}

	private void setContext(PMMLObject context){
		this.context = context;
	}
}