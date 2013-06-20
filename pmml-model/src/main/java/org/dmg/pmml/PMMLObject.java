/*
 * Copyright (c) 2009 University of Tartu
 */
package org.dmg.pmml;

import java.io.*;

import javax.xml.bind.annotation.*;

import com.sun.xml.bind.*;

import org.jvnet.jaxb2_commons.lang.*;
import org.jvnet.jaxb2_commons.locator.*;

@XmlTransient
abstract
public class PMMLObject implements Equals, HashCode, Locatable, ToString, Serializable {

	public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object that, EqualsStrategy equalsStrategy){
		return true;
	}

	public int hashCode(ObjectLocator locator, HashCodeStrategy hashCodeStrategy){
		return 1;
	}

	public StringBuilder append(ObjectLocator locator, StringBuilder builder, ToStringStrategy toStringStrategy){
		return builder;
	}

	public StringBuilder appendFields(ObjectLocator locator, StringBuilder builder, ToStringStrategy toStringStrategy) {
		return builder;
	}
}