/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import com.sun.xml.bind.*;

import org.xml.sax.*;

public class PMMLException extends RuntimeException {

	private Locatable locatable = null;


	public PMMLException(){
		super();
	}

	public PMMLException(String message){
		super(message);
	}

	public PMMLException(Locatable locatable){
		super();

		setLocatable(locatable);
	}

	public PMMLException(String message, Locatable locatable){
		super(message);

		setLocatable(locatable);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());

		Locatable locatable = getLocatable();
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

	public Locatable getLocatable(){
		return this.locatable;
	}

	private void setLocatable(Locatable locatable){
		this.locatable = locatable;
	}
}