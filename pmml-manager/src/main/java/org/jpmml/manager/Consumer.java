/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.io.*;
import java.util.*;

import org.dmg.pmml.*;

public interface Consumer extends Serializable {

	/**
	 * Returns a short description of the underlying {@link Model}
	 */
	String getSummary();

	/**
	 * Gets the definition of a field from the {@link DataDictionary}.
	 *
	 * @see PMMLManager#getDataField(FieldName)
	 */
	DataField getDataField(FieldName name);

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
	 * Gets the definition of a field from the {@link MiningSchema}.
	 *
	 * @see #getActiveFields()
	 * @see #getPredictedFields()
	 *
	 * @see ModelManager#getMiningField(FieldName)
	 */
	MiningField getMiningField(FieldName name);

	/**
	 * Gets the output fields of a {@link Model} from its {@link Output}.
	 *
	 * @see ModelManager#getOutputFields()
	 */
	List<FieldName> getOutputFields();

	/**
	 * Gets the definition of a field from the {@link Output}
	 *
	 * @see #getOutputFields()
	 */
	OutputField getOutputField(FieldName name);
}