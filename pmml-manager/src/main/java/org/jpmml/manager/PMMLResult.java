package org.jpmml.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.dmg.pmml.FieldName;

/**
 * This is the mother class of the Result hierarchy.
 * There is some child classes that are used when some
 * particular information is needed, for example a node
 * id in a tree model.
 *
 * @author tbadie
 *
 */
public class PMMLResult implements IPMMLResult {
	HashMap<FieldName, Object> results;

	public PMMLResult() {
		results = new HashMap<FieldName, Object>();
	}

	public Object getValue(FieldName key) throws NoSuchElementException {
		if (!results.containsKey(key)) {
			throw new NoSuchElementException("There is no field " + key.getValue() + " in the result.");
		}

		return results.get(key);
	}

    /**
     * Associate key with value. If key already exists, the old value is
     * overridden.
     *
     * @param key The key.
     * @param value The value.
     */
	public void put(FieldName key, Object value) {
		results.put(key, value);
	}

	/**
	 * Take a map and add all the content of the result to this map.
	 *
	 * @param m The map to fill.
	 * @return
	 */
	public void merge(Map<FieldName, Object> m) {
		for (Map.Entry<FieldName, Object> e : results.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
	}

	public Object getResult() throws NoSuchElementException {
		if (results.size() == 1) {
			for (Map.Entry<FieldName, Object> e : results.entrySet()) {
				return e.getValue();
			}
		}
		else {
			if (!isEmpty())
				throw new NoSuchElementException("There is more than one result.");
			else
				throw new NoSuchElementException("There is no result.");
		}
		return null;
	}

	public Boolean isEmpty() {
		return results.isEmpty();
	}
}
