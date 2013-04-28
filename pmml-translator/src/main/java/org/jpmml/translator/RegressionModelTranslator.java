package org.jpmml.translator;

import java.util.List;
import java.util.TreeMap;

import org.dmg.pmml.CategoricalPredictor;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionNormalizationMethodType;
import org.dmg.pmml.RegressionTable;
import org.jpmml.manager.RegressionModelManager;
import org.jpmml.translator.CodeFormatter.Operator;
import org.jpmml.translator.Variable.VariableType;

/**
 * Translate regression model into java code.
 * @see RegressionModelManager.
 *
 * @author tbadie
 *
 */
public class RegressionModelTranslator extends RegressionModelManager implements Translator {	
	public RegressionModelTranslator(PMML pmml){
		super(pmml);
	}

	public RegressionModelTranslator(PMML pmml, RegressionModel regressionModel){
		super(pmml, regressionModel);
	}

	public RegressionModelTranslator(RegressionModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * Return a string that is a java code able to evaluate the model on a set of parameters.
	 * 
	 * @param context The translation context. 
	 */
	public String translate(TranslationContext context) throws TranslationException {
		String outputVariableName = null;
		List<FieldName> predictedFields = getPredictedFields();
		// Get the predicted field. If there is none, it is an error.
		if (predictedFields != null && predictedFields.size() > 0) {
			outputVariableName = predictedFields.get(0).getValue();
		}
		if (outputVariableName == null) {
			throw new TranslationException("Predicted variable is not defined");
		}

		DataField outputField = getDataField(new FieldName(outputVariableName));
		if (outputField == null || outputField.getDataType() == null) {
			throw new TranslationException("Predicted variable [" +
					outputVariableName + "] does not have type defined");
		}
		
		return translate(context, outputField);
	}

	
	public String translate(TranslationContext context, DataField outputField) {
		StringBuilder sb = new StringBuilder();

		switch (getFunctionName()) {
		case REGRESSION:
			translateRegression(sb, context, outputField);
			break;
		case CLASSIFICATION:
			translateClassification(sb, context, outputField);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		return sb.toString();
	}
	
	/**
	 * Translate the regression.
	 * 
	 * @param sb The string builder we are working with.
	 * @param context The context of the translation.
	 * @param outputField The name of the output variable.
	 */
	private void translateRegression(StringBuilder sb, TranslationContext context, DataField outputField) {
		RegressionTable rt = getOrCreateRegressionTable();
		CodeFormatter cf = context.getFormatter();

		translateRegressionTable(sb, context, outputField.getName().getValue(), rt, cf, true);
		translateNormalizationRegression(sb, context, outputField, cf);
	}
	
	/** 
	 * Translate the classification.
	 * 
	 * @param sb The string builder we are working with.
	 * @param context The context of the translation.
	 * @param outputField The name of the output variable.
	 */
	private void translateClassification(StringBuilder sb, TranslationContext context, DataField outputField) {
		CodeFormatter cf = context.getFormatter();
		String targetCategoryToScoreVariable = context.generateLocalVariableName("targetCategoryToScore");
		context.requiredImports.add("import java.util.TreeMap;");
		cf.addLine(sb, context, "TreeMap<String, Double> " + targetCategoryToScoreVariable
				+ " = new TreeMap<String, Double>();");

		TreeMap<String, String> categoryNameToVariable = new TreeMap<String, String>();
		for (RegressionTable rt : getOrCreateRegressionTables()) {
			categoryNameToVariable.put(rt.getTargetCategory(),
					translateRegressionTable(sb, context, targetCategoryToScoreVariable, rt, cf, false));
		}

		// Apply the normalization:
		String scoreToCategoryVariable = context.generateLocalVariableName("scoreToCategory");
		cf.addLine(sb, context, "TreeMap<Double, String> " + scoreToCategoryVariable
				+ " = new TreeMap<Double, String>();");
		switch (getNormalizationMethodType()) {
		case NONE:
			// Pick the category with top score.
			String entryName = context.generateLocalVariableName("entry");
			cf.declareVariable(sb, context, new Variable(VariableType.DOUBLE, entryName));
			for (RegressionTable rt : getOrCreateRegressionTables()) {
				cf.assignVariable(sb, context, entryName, categoryNameToVariable.get(rt.getTargetCategory()));
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
						entryName + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		case LOGIT:
			// pick the max of pj = 1 / ( 1 + exp( -yj ) )
			for (RegressionTable rt : getOrCreateRegressionTables()) {
				String expression = "1.0 / (1.0 + Math.exp(" + categoryNameToVariable.get(rt.getTargetCategory()) + "))";
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
						expression + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		case EXP:
			// pick the max of exp(yj) 
			for (RegressionTable rt : getOrCreateRegressionTables()) {
				String expression = "Math.exp(" + categoryNameToVariable.get(rt.getTargetCategory()) + ")";
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
						expression + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		case SOFTMAX:
			// pj = exp(yj) / (Sum[i = 1 to N](exp(yi) ) )
			String sumName = context.generateLocalVariableName("sum");
			cf.declareVariable(sb, context, new Variable(Variable.VariableType.DOUBLE, sumName));
			for (RegressionTable rt : getOrCreateRegressionTables()) {
				cf.assignVariable(sb, context, Operator.PLUS_EQUAL, sumName, "Math.exp(" 
						+ categoryNameToVariable.get(rt.getTargetCategory()) + ")");
			}

			for (RegressionTable rt : getOrCreateRegressionTables()) {
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(Math.exp(" 
						+ categoryNameToVariable.get(rt.getTargetCategory()) + ") / "
						+ sumName + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		case CLOGLOG:
			// pick the max of pj = 1 - exp( -exp( yj ) ) 

			for (RegressionTable rt : getOrCreateRegressionTables()) {
				String expression = "1.0 - Math.exp(-Math.exp("
							+ categoryNameToVariable.get(rt.getTargetCategory()) + "))";
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
						expression + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		case LOGLOG:
			// pick the max of pj = exp( -exp( -yj ) )
			for (RegressionTable rt : getOrCreateRegressionTables()) {
				String expression = "Math.exp(-Math.exp(-"
							+ categoryNameToVariable.get(rt.getTargetCategory()) + "))";
				cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
						expression + ", \"" + rt.getTargetCategory() + "\");");
			}
			break;
		default:
			cf.addLine(sb, context, "return null;");
			break;
	}

		
		cf.assignVariable(sb, context, outputField.getName().getValue(),
						scoreToCategoryVariable + ".lastEntry().getValue()");
	}
	

	/**
	 * Produce a code that evaluates a regressionTable.
	 * 
	 * @param sb The string builder we are working with.
	 * @param context The context of the translation.
	 * @param variableName The name of the variable we wa
	 * @param rt 
	 * @param cf
	 * @param storeResultInVariable True if we want to affect the result to
	 * the output variable. False Otherwise.
	 * @return The name of the variable that contains the evaluation of the
	 * table.
	 */
	private String translateRegressionTable(StringBuilder sb, TranslationContext context, String variableName,
			RegressionTable rt, CodeFormatter cf, boolean storeResultInVariable) {
		List<NumericPredictor> lnp = getNumericPredictors(rt);
		List<CategoricalPredictor> lcp = getCategoricalPredictors(rt);

		String categoryVariableName = context.generateLocalVariableName(rt.getTargetCategory());
		cf.declareVariable(sb, context, new Variable(Variable.VariableType.DOUBLE,
				categoryVariableName), getIntercept(rt).toString());

		for (NumericPredictor np : lnp) {
			translateNumericPredictor(sb, context, categoryVariableName, np, cf);
		}

		for (CategoricalPredictor cp : lcp) {
			translateCategoricalPredictor(sb, context, categoryVariableName, cp, cf);
		}
		
		if (storeResultInVariable) {
			cf.assignVariable(sb, context, variableName, categoryVariableName);
		}

		return categoryVariableName;
	}
	
	
	/**
	 * Produce the code for the normalization for the regression.
	 * 
	 * @param code The string builder we are working with.
	 * @param context The context of the translation.
	 * @param outputVariable The variable where we have to put the result.
	 * @param cf The code formatter.
	 */
	private void translateNormalizationRegression(StringBuilder code, TranslationContext context,
			DataField outputVariable, CodeFormatter cf) {
		RegressionNormalizationMethodType normalizationMethod = getNormalizationMethodType();
		switch (normalizationMethod) {
			case NONE:
				// Do nothing.
				break;
			case SOFTMAX:
			case LOGIT:
				cf.assignVariable(code, context, outputVariable.getName().getValue(), "1.0 / (1.0 + Math.exp(-"
						+ outputVariable.getName().getValue() + "))");
				// result = 1.0 / (1.0 + Math.exp(-result));
				break;
			case EXP:
				cf.assignVariable(code, context, outputVariable.getName().getValue(), "Math.exp("
						+ outputVariable.getName().getValue() + ")");
				// result = Math.exp(result);
				break;
			default:
				// We should never be here.
				assert false;
				break;
		}
	}
	
	/**
	 * Produce the code for the evaluation of a particular numeric predictor.
	 * 
	 * @param code The string builder we are working with.
	 * @param context The context of the translation.
	 * @param outputVariable The variable where we have to put the result.
	 * @param numericPredictor The numeric predictor we translate.
	 * @param cf The code formatter.
	 */
	private void translateNumericPredictor(StringBuilder code, TranslationContext context, String outputVariableName,
			NumericPredictor numericPredictor, CodeFormatter cf) {

		cf.beginControlFlowStructure(code, context, "if", numericPredictor.getName().getValue() + " == null");
		cf.addLine(code, context,
				 // FIXME: What exception would be useful instead of Exception?
					"throw new Exception(\"Missing parameter "
					+ numericPredictor.getName().getValue() + "\");");
		cf.endControlFlowStructure(code, context);
		cf.beginControlFlowStructure(code, context, "else", null);
		cf.assignVariable(code, context, Operator.PLUS_EQUAL, outputVariableName,
				numericPredictor.getCoefficient()
				+ " * Math.pow(" + numericPredictor.getName().getValue() + ", "
				+ numericPredictor.getExponent().doubleValue() + ")");
		cf.endControlFlowStructure(code, context);
	}

	/**
	 * Produce the code for the evaluation of a particular categorical predictor.
	 * 
	 * @param code The string builder we are working with.
	 * @param context The context of the translation.
	 * @param outputVariable The variable where we have to put the result.
	 * @param categoricalPredictor The categorical predictor we translate.
	 * @param cf The code formatter.
	 */
	private void translateCategoricalPredictor(StringBuilder code, TranslationContext context, String outputVariableName,
			CategoricalPredictor categoricalPredictor, CodeFormatter cf) {
		
		cf.beginControlFlowStructure(code, context, "if", categoricalPredictor.getName().getValue() + " != null");
		cf.assignVariable(code, context, Operator.PLUS_EQUAL, outputVariableName,
				categoricalPredictor.getCoefficient() + " * (("
				+ generateEqualityExpression(categoricalPredictor) + ") ? 1 : 0)");
		cf.endControlFlowStructure(code, context);
	}
	
	/**
	 * Produce the code for an equality expression. The code is different between
	 * string and numbers type.
	 * 
	 * @param categoricalPredictor The categorical predictor we translate. 
	 * @return The code corresponding to an is equal statement.
	 */
	private String generateEqualityExpression(CategoricalPredictor categoricalPredictor) {
		
		for (DataField df : getDataDictionary().getDataFields()) {
			if (df.getName().getValue().equals(categoricalPredictor.getName().getValue())) {
				switch (df.getDataType()) {
				case STRING:
					return "" + categoricalPredictor.getName().getValue() + ".equals(\"" + categoricalPredictor.getValue()
							+ "\")";
				case FLOAT:
				case DOUBLE:
				case BOOLEAN:
				case INTEGER:
					return "" + categoricalPredictor.getName().getValue() + " == " + categoricalPredictor.getValue();
				default:
					throw new UnsupportedOperationException();
				}
			}
		}
		
		
		return "false";
	}
	
}