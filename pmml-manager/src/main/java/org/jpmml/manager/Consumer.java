/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public interface Consumer {

	/**
	 * Returns a short description of the underlying {@link Model}
	 */
	String getSummary();

	/**
	 * Gets the independent (ie. input) fields of a {@link Model} from its {@link MiningSchema}.
	 *
	 * @see ModelManager#getActiveFields()
	 */
	List<FieldName> getActiveFields();

	/**
	 * Gets the dependent (ie. output) field(s) of a {@link Model} from its {@link MiningSchema}.
	 *
	 * @see ModelManager#getPredictedFields()
	 */
	List<FieldName> getPredictedFields();

	/**
	 * Gets the data definition of a field from the {@link DataDictionary}.
	 *
	 * @see PMMLManager#getDataField(FieldName)
	 */
	DataField getDataField(FieldName fieldName);
}