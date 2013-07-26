/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

public class FieldName {

	private String value = null;


	public FieldName(String value){
		setValue(value);
	}

	@Override
	public int hashCode(){
		return getValue().hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof FieldName){
			FieldName that = (FieldName)object;

			return (this.getValue()).equals(that.getValue());
		}

		return super.equals(object);
	}

	@Override
	public String toString(){
		return getValue();
	}

	public String getValue(){
		return this.value;
	}

	private void setValue(String value){

		if(value == null){
			throw new NullPointerException();
		}

		this.value = value;
	}

	static
	public FieldName unmarshal(String value){
		return new FieldName(value);
	}

	static
	public String marshal(FieldName name){
		return name.getValue();
	}
}