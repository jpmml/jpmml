package org.jpmml.translator;

import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.DataField;
import org.dmg.pmml.False;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.True;
import org.jpmml.manager.ModelManager;

public class PredicateTranslationUtil {
	static public final int TRUE = 1;
	static public final int FALSE = 2;
	static public final int UNKNOWN = 3;
	
	static public String generateCode(Predicate predicate, ModelManager<?> modelManager, TranslationContext context) throws TranslationException {
		if (predicate instanceof SimplePredicate) {
			return generateCodeForSimplePredicate((SimplePredicate)predicate, modelManager, context);
		}
		else if (predicate instanceof CompoundPredicate) {
			return generateCodeForCompoundPredicate((CompoundPredicate)predicate, modelManager, context);
		}
		else if (predicate instanceof SimpleSetPredicate) {
			return generateCodeForSimpleSetPredicate((SimpleSetPredicate)predicate, modelManager, context);
		}
		else if (predicate instanceof True) {
			return String.valueOf(PredicateTranslationUtil.TRUE);
		}
		else if (predicate instanceof False) {
			return String.valueOf(PredicateTranslationUtil.FALSE);
		}
		throw new TranslationException("Unknown predicate type: "+predicate.getClass().getName());
	}
	
	static private String operationWrapper(String operation, String variableString, String constant, TranslationContext context) {
		return String.format("%s == %s? %s : ((%s %s %s)? %s : %s)",
				variableString, context.getMissingValue(OpType.CONTINUOUS),
				PredicateTranslationUtil.UNKNOWN,
				variableString, operation, constant,
				PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
	} 

	static private String generateCodeForSimplePredicate(SimplePredicate predicate, 
			ModelManager<?> modelManager, 
			TranslationContext context) throws TranslationException {
		
		String code = null; 
		
		DataField dataField = modelManager.getDataField(predicate.getField());

		String variableString = context.formatVariableName(modelManager, predicate.getField());
		String constant = context.formatConstant(modelManager, predicate.getField(), predicate.getValue());

		if (dataField.getOptype() == OpType.CATEGORICAL) {
			switch(predicate.getOperator()) {
				case EQUAL: 
					code = String.format("%s == %s? %s : (%s.equals(\"%s\") ? %s : %s)",
											variableString, context.getMissingValue(OpType.CATEGORICAL),
											PredicateTranslationUtil.UNKNOWN,
											variableString, constant,
											PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
					break;
				case NOT_EQUAL: 
					code = String.format("%s == %s? %s : (%s.equals(\"%s\") ? %s : %s)",
							variableString, context.getMissingValue(OpType.CATEGORICAL),
							PredicateTranslationUtil.UNKNOWN,
							variableString, constant,
							PredicateTranslationUtil.FALSE, PredicateTranslationUtil.TRUE);
					break;
				case IS_MISSING: 
					code = String.format("%s == %s? %s : %s", 
							variableString, context.getMissingValue(OpType.CATEGORICAL),
							PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
					break;
				case IS_NOT_MISSING: 
					code = String.format("%s!=%s? %s : %s", 
							variableString, context.getMissingValue(OpType.CATEGORICAL),
							PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
					break;
				case LESS_THAN: 
				case LESS_OR_EQUAL: 
				case GREATER_THAN: 
				case GREATER_OR_EQUAL:
					throw new TranslationException("Invalid operator for categorical variable: "
								+ predicate.getField() + "; operator: " + predicate.getOperator());
				default:
					throw new TranslationException("Unknown operator: "+predicate.getOperator());

			}
		}
		else {
			switch(predicate.getOperator()) {
				case EQUAL: 
					code = operationWrapper("==", variableString, constant, context);
					break;
				case NOT_EQUAL: 
					code = operationWrapper("!=", variableString, constant, context);
					break;
				case LESS_THAN: 
					code = operationWrapper("<", variableString, constant, context);
					break;
				case LESS_OR_EQUAL: 
					code = operationWrapper("<=", variableString, constant, context);
					break;
				case GREATER_THAN:
					code = operationWrapper(">", variableString, constant, context);
					break;
				case GREATER_OR_EQUAL:
					code = operationWrapper(">", variableString, constant, context);
					break;
				case IS_MISSING: 
					code = String.format("%s == %s? %s : %s", variableString, context.getMissingValue(OpType.CONTINUOUS),
							PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
					break;
				case IS_NOT_MISSING: 
					code = String.format("%s != %s? %s : %s", variableString, context.getMissingValue(OpType.CONTINUOUS),
							PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
					break;
				default:
					throw new TranslationException("Unknown operator: "+predicate.getOperator());
			}			
		}
		
		return code;
		
	}

	static private String generateCodeForCompoundPredicate(
			CompoundPredicate predicate, 
			ModelManager<?> modelManager, 
			TranslationContext context) throws TranslationException {
		context.addRequiredImport("org.jpmml.translator.PredicateTranslationUtil");
		context.addRequiredImport("org.dmg.pmml.CompoundPredicate.BooleanOperator");
		
		StringBuilder code = new StringBuilder();
		
		code.append("PredicateTranslationUtil.evaluateCompoundPredicate(");
		code.append("BooleanOperator.")
			.append(predicate.getBooleanOperator().toString());
		
		for (Predicate innerPredicate : predicate.getContent()) {
			String predicateCode = PredicateTranslationUtil.generateCode(innerPredicate, modelManager, context);
			code.append(',');
			code.append(predicateCode);
		}
		code.append(')');

		return code.toString();

	}

	static private String generateCodeForSimpleSetPredicate(
			SimpleSetPredicate predicate, 
			ModelManager<?> modelManager, 
			TranslationContext context) {
		
		
		
		return null;
	}
	
	static public int evaluateCompoundPredicate(CompoundPredicate.BooleanOperator operator, int ... predicateResults) {
	
		int result = PredicateTranslationUtil.UNKNOWN;
		
		switch(operator) {
			case SURROGATE:
				for (int i=0;i<predicateResults.length;i++) {
					if (predicateResults[i]!=PredicateTranslationUtil.UNKNOWN) {
						result = predicateResults[i];
						break;							
					}
				}
				break;
			case OR:
				
				for (int i=0;i<predicateResults.length;i++) {
					if (i==0) {
						result = predicateResults[i];
					}
					else {
						// regular OR; if at least one is not missing and TRUE - return TRUE
						if (result!=PredicateTranslationUtil.UNKNOWN && predicateResults[i]!=PredicateTranslationUtil.UNKNOWN) {
							result = ((result==PredicateTranslationUtil.TRUE) || (predicateResults[i]==PredicateTranslationUtil.TRUE))? 
								PredicateTranslationUtil.TRUE : PredicateTranslationUtil.FALSE;						
						}
						else if ((result==PredicateTranslationUtil.TRUE) || (predicateResults[i]==PredicateTranslationUtil.TRUE)) {
							result = PredicateTranslationUtil.TRUE;
						}
						else { 
							result = PredicateTranslationUtil.UNKNOWN;
						}
					}
				}
				break;
			case AND:
				for (int i=0;i<predicateResults.length;i++) {
					if (i==0) {
						result = predicateResults[i];
					}
					else {
						// regular AND; if at least one is not missing and FALSE - return FALSE
						if (result!=PredicateTranslationUtil.UNKNOWN && predicateResults[i]!=PredicateTranslationUtil.UNKNOWN) {
							result = ((result==PredicateTranslationUtil.TRUE) && (predicateResults[i]==PredicateTranslationUtil.TRUE))? 
								PredicateTranslationUtil.TRUE : PredicateTranslationUtil.FALSE;						
						}
						else if ((result==PredicateTranslationUtil.FALSE) || (predicateResults[i]==PredicateTranslationUtil.FALSE)) {
							result = PredicateTranslationUtil.FALSE;
						}
						else { 
							result = PredicateTranslationUtil.UNKNOWN;
						}
					}
				}
				break;
			case XOR:
				for (int i=0;i<predicateResults.length;i++) {
					if (i==0) {
						result = predicateResults[i];
					}
					else {
						// regular XOR; return null if at least is missing
						if (result!=PredicateTranslationUtil.UNKNOWN && predicateResults[i]!=PredicateTranslationUtil.UNKNOWN) {
							result = ((result==PredicateTranslationUtil.TRUE) ^ (predicateResults[i]==PredicateTranslationUtil.TRUE))? 
								PredicateTranslationUtil.TRUE : PredicateTranslationUtil.FALSE;						
						}
						else { 
							result = PredicateTranslationUtil.UNKNOWN;
						}
					}
				}
				
				break;
		}
		return result;
	}

}
