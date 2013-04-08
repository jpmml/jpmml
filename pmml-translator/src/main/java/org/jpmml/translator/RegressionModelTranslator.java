package org.jpmml.translator;

import java.util.List;

import org.dmg.pmml.CategoricalPredictor;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionNormalizationMethodType;
import org.jpmml.manager.RegressionModelManager;
import org.jpmml.translator.CodeFormatter.Operator;

/**
 * Translate regression model into java code.
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

	// The main method. It takes a context, and print the code corresponding to the regression model.
	public String translate(TranslationContext context) throws TranslationException {

		String outputVariableName = null;
		List<FieldName> predictedFields = getPredictedFields();
		// Get the predicted field. If there is none, it is an error.
		if (predictedFields!=null && predictedFields.size()>0) {
			outputVariableName = predictedFields.get(0).getValue();
		}
		if (outputVariableName==null) {
			throw new TranslationException("Predicted variable is not defined");
		}
		
		DataField outputField = getDataField(new FieldName(outputVariableName));		
		if (outputField==null || outputField.getDataType()==null) {
			throw new TranslationException("Predicted variable [" +
					outputVariableName + "] does not have type defined");
		}
		
		StringBuilder sb = new StringBuilder();
	
		List<NumericPredictor> lnp = getNumericPredictors();
		List<CategoricalPredictor> lcp = getCategoricalPredictors();
		CodeFormatter cf = context.getFormatter();

		cf.affectVariable(sb, context, outputVariableName, getIntercept().toString());
		
		for (NumericPredictor np : lnp) {
			translateNumericPredictor(sb, context, outputField, np, cf);
		}

		for (CategoricalPredictor cp : lcp) {
			translateCategoricalPredictor(sb, context, outputField, cp, cf);
		}

		translateNormalizationRegression(sb, context, outputField, cf);
		
		
		return sb.toString();
	}

	
	private void translateNormalizationRegression(StringBuilder code, TranslationContext context,
			DataField outputVariable, CodeFormatter cf) {
		RegressionNormalizationMethodType normalizationMethod = getNormalizationMethodType();
		switch (normalizationMethod) {
			case NONE:
				// Do nothing.
				break;
			case SOFTMAX:
			case LOGIT:
				cf.affectVariable(code, context, outputVariable.getName().getValue(), "1.0 / (1.0 + Math.exp(-"
						+ outputVariable.getName().getValue() + "))");
				// result = 1.0 / (1.0 + Math.exp(-result));
				break;
			case EXP:
				cf.affectVariable(code, context, outputVariable.getName().getValue(), "Math.exp("
						+ outputVariable.getName().getValue() + ")");
				// result = Math.exp(result);
				break;
			default:
				// We should never be here.
				assert false;
				break;
		}
	}
	
	public void translateNumericPredictor(StringBuilder code, TranslationContext context, DataField outputVariable,
			NumericPredictor numericPredictor, CodeFormatter cf) {

		cf.beginControlFlowStructure(code, context, "if", numericPredictor.getName().getValue() + " == null");
		cf.addLine(code, context,
				 // FIXME: What exception would be useful instead of Exception?
					"throw new Exception(\"Missing parameter "
					+ numericPredictor.getName().getValue() + "\");");
		cf.endControlFlowStructure(code, context);
		cf.beginControlFlowStructure(code, context, "else", null);
		cf.affectVariable(code, context, Operator.PLUS_EQUAL, outputVariable.getName().getValue(),
				numericPredictor.getCoefficient()
				+ " * Math.pow(" + numericPredictor.getName().getValue() + ", "
				+ numericPredictor.getExponent().doubleValue() + ")");
		cf.endControlFlowStructure(code, context);
	}

	public void translateCategoricalPredictor(StringBuilder code, TranslationContext context, DataField outputVariable,
			CategoricalPredictor categoricalPredictor, CodeFormatter cf) {

		cf.beginControlFlowStructure(code, context, "if", categoricalPredictor.getName().getValue() + " != null");
		cf.affectVariable(code, context, Operator.PLUS_EQUAL, outputVariable.getName().getValue(),
				categoricalPredictor.getCoefficient() + " * (" + categoricalPredictor.getName().getValue()
					+ ".equals(\"" + categoricalPredictor.getValue() + "\") ? 1 : 0)");
		cf.endControlFlowStructure(code, context);
	}
	
}