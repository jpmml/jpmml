/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class TypeDefinitionField extends Field {

	abstract
	public List<Value> getValues();
}