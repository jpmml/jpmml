/*
 * Copyright (c) 2012 University of Tartu
 */
package org.dmg.pmml;

public enum Version {
	PMML_3_0("http://www.dmg.org/PMML-3_0"),
	PMML_3_1("http://www.dmg.org/PMML-3_1"),
	PMML_3_2("http://www.dmg.org/PMML-3_2"),
	PMML_4_0("http://www.dmg.org/PMML-4_0"),
	;

	private String uri = null;


	private Version(String uri){
		setURI(uri);
	}

	public String getURI(){
		return this.uri;
	}

	private void setURI(String uri){
		this.uri = uri;
	}
}