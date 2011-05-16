/*
 * Copyright (c) 2009 University of Tartu
 */
package org.dmg.pmml;

import java.io.*;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang.builder.*;

import org.jvnet.jaxb2_commons.lang.*;

@XmlTransient
abstract
public class PMMLObject implements Equals, HashCode, Serializable {

	public void equals(Object object, EqualsBuilder equalsBuilder){
	}

	public void hashCode(HashCodeBuilder hashCodeBuilder){
	}
}