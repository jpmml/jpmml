/*
 * Copyright (c) 2012 University of Tartu
 */
package org.dmg.pmml;

import java.math.*;
import java.util.*;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class SparseArray extends PMMLObject {

	abstract
	public List<Integer> getIndices();

	abstract
	public BigInteger getN();

	abstract
	public void setN(BigInteger n);
}