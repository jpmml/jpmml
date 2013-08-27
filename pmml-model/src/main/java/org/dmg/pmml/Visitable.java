/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

public interface Visitable {

	void accept(Visitor visitor);
}