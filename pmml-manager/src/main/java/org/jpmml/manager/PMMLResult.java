package org.jpmml.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.dmg.pmml.FieldName;

public class PMMLResult implements IPMMLResult {
	HashMap<FieldName, Object> results;

	public PMMLResult() {
		results = new HashMap<FieldName, Object>();
	}

	public Object getValue(FieldName key) throws NoSuchElementException {
		if (!results.containsKey(key)) {
			throw new NoSuchElementException();
		}


		return results.get(key);
	}

	public void put(FieldName key, Object value) {
		results.put(key, value);
	}

	public void merge(Map<FieldName, Object> m) {
		for (Map.Entry<FieldName, Object> e : results.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
	}


	public Boolean isEmpty() {
		return results.isEmpty();
	}
}
