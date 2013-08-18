/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class PredictorList extends PMMLObject {

	abstract
	public List<Predictor> getPredictors();
}