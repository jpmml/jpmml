/*
 * Copyright (c) 2012 University of Tartu
 */
package org.dmg.pmml;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class SparseArray<E extends Number> extends PMMLObject {

	@XmlTransient
	private SortedMap<Integer, E> content = null;


	abstract
	public Integer getN();

	abstract
	public void setN(Integer n);

	abstract
	public List<Integer> getIndices();

	abstract
	public List<E> getEntries();

	public SortedMap<Integer, E> getContent(){
		return this.content;
	}

	public void setContent(SortedMap<Integer, E> content){
		this.content = content;
	}
}