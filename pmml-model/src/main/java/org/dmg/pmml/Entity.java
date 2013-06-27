/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Entity extends PMMLObject {

	abstract
	public String getId();

	abstract
	public void setId(String id);
}