package org.jpmml.translator;

import org.dmg.pmml.DataField;

/**
 * A class that contains some useful functions for the translation PMML/Java. 
 * 
 * @author tbadie
 *
 */
public class TranslatorUtil {

	
	/**
	 * A method to assign a value to a variable in java. Handle Integer,
	 * Float, Double and String.
	 * 
	 * @param code The code where we will append the assignment.
	 * @param value The rvalue we will assign.
	 * @param context The current state of the translation.
	 * @param outputVariable The variable to be assigned.
	 * @throws TranslationException
	 */
	static public void assignOutputVariable(StringBuilder code,
											String value,
											TranslationContext context,
											DataField outputVariable)
			throws TranslationException {
		
			switch(outputVariable.getDataType()) {
				case INTEGER:
				case FLOAT:
				case DOUBLE:
					code.append(context.getIndentation())
					.append(context.formatOutputVariable(outputVariable.getName().getValue())).append(" = ").append(value)
					.append(";\n");
					break;
				case STRING:
					code.append(context.getIndentation())
					.append(context
							.formatOutputVariable(outputVariable.getName()
													.getValue()))
							.append(" = \"").append(value)
					.append("\";\n");
					break;
				default:
					throw new
						TranslationException("Unsupported data type for output"
								+ "variable: " +outputVariable.getDataType());
			}
		}
}
