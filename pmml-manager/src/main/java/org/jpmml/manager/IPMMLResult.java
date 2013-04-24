package org.jpmml.manager;

import java.util.Map;
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
	 * Associate key with value. If key already exists, the old value is
	 * overridden.
	 *
	 * @param key The key.
	 * @param value The value.
	 */
	public void put(FieldName key, Object value);


	/**
	 * Take a map and add all the content of the result to this map.
	 *
	 * @param m The map to fill.
	 * @return
	 */
	public void merge(Map<FieldName, Object> m);

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
