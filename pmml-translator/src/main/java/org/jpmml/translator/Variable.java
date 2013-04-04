package org.jpmml.translator;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.jpmml.manager.UnsupportedFeatureException;

/**
 * The aim of this class is to abstract the notion of variable
 * and to have a unified way to use them.
 * 
 * @author tbadie
 *
 */
public class Variable {
	
	public enum VariableType {
	    INTEGER("Integer"),
	    FLOAT("Float"),
	    DOUBLE("Double"),
	    BOOLEAN("Boolean"),
	    STRING("String"),
	    
	    // OBJECT is for handling any type.
		OBJECT("FIXME"),
		;
		
		private final String value;

	    VariableType(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

	    public static VariableType fromValue(String v) {
	        for (VariableType c: VariableType.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
	
	private VariableType type;
	public VariableType getType() {
		return type;
	}

	public String typeName;
	private String name;
	

	public Variable(DataField d) {
		type = dataTypeToVariableType(d.getDataType());
		name = d.getName().getValue();
		typeName = null;
	}
	
	public Variable(VariableType type,
					String name) {
		this.type = type;
		this.name = name;
		typeName = null;
	}

	public Variable(VariableType type,
					String typename,
					String name)
	{
		if (type != VariableType.OBJECT) {
			// Name given to the type which  is allowed only for Object type.
			assert false;
		}
		
		this.name = name;
		typeName = typename;
		this.type = type;
	}
	
	public Variable(String name) {
		this.name = name;
		type = VariableType.INTEGER;
	}
	
	public VariableType dataTypeToVariableType(DataType d) {
		VariableType res;
		
		switch (d) {
		case INTEGER:
			res = VariableType.INTEGER;
			break;
		case FLOAT:
			res = VariableType.FLOAT;
			break;
		case DOUBLE:
			res = VariableType.DOUBLE;
			break;
		case STRING:
			res = VariableType.STRING;
			break;
		case BOOLEAN:
			res = VariableType.BOOLEAN;
			break;
		default:
			throw new UnsupportedFeatureException(d);
		}

		return res;
	}
	
	public String getTypeName() {
		if (typeName == null) {
			return type.value;
		}
		// If typeName is not null, that means we are dealing with OBJECT.
		assert type == VariableType.OBJECT;
		return typeName;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}


}
