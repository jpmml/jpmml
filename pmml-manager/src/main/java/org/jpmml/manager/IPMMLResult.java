package org.jpmml.manager;

import java.util.NoSuchElementException;

import org.dmg.pmml.FieldName;

/**
 * This Interface handles the results of an evaluation.
 *
 * @author tbadie
 *
 */
public interface IPMMLResult {
	/**
	 * Get the value associated to the key.
	 *
	 * We use exception to differentiate the case where the
	 * associated value is null and the case when there is no
	 * such key.
	 *
	 * @param key The result we are interested in.
	 * @return The value associated with the key.
	 * @throws NoSuchElementException If the key does not exist.
	 */
	public Object getValue(FieldName key) throws NoSuchElementException;

	/**
	 * Return true if there is not result.
	 */
	public Boolean isEmpty();

	/**
	 * Most of the time, there is only one return value to a model.
	 * This result is what you get by calling this function.
	 *
	 * @return The result wanted.
	 * @throws NoSuchElementException If there is more than one result or
	 * if there is none.
	 */
	public Object getResult() throws NoSuchElementException;

}
