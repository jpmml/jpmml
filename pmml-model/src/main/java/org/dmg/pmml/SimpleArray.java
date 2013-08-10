/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class SimpleArray extends PMMLObject {

	@XmlTransient
	private List<String> content = null;


	/**
	 * Gets the raw value.
	 */
	abstract
	public String getValue();

	/**
	 * Sets the raw value.
	 */
	abstract
	public void setValue(String value);

	/**
	 * Gets the parsed sequence of values.
	 *
	 * @see #getValue()
	 */
	public List<String> getContent(){
		return this.content;
	}

	/**
	 * Sets the parsed sequence of values.
	 *
	 * It is the responsibility of application developer to maintain the consistency between the raw value and the parsed sequence of values.
	 *
	 * @see #setValue(String)
	 */
	public void setContent(List<String> content){
		this.content = content;
	}
}