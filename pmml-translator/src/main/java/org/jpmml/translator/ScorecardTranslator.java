package org.jpmml.translator;

import java.util.List;

import org.dmg.pmml.Attribute;
import org.dmg.pmml.Characteristic;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.Scorecard;
import org.jpmml.manager.ScoreCardModelManager;

/**
 * Translate score card model into java code.
 * 
 * @author tbadie
 *
 */
public class ScorecardTranslator extends ScoreCardModelManager implements Translator {	
	public ScorecardTranslator(PMML pmml){
		super(pmml);
	}

	public ScorecardTranslator(PMML pmml, Scorecard scorecard){
		super(pmml, scorecard);
	}

	public ScorecardTranslator(ScoreCardModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	// The main method. It takes a context, and print the code corresponding to the scorecard.
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
	
		List<Characteristic> cl
		= scorecard.getCharacteristics().getCharacteristics();
		
		String diffToReasonCodeVariable = context.generateLocalVariableName("diffToReasonCode");
		
		context.requiredImports.add("import java.util.TreeMap;");
		sb.append(context.getIndentation()).append("TreeMap<Double, String> ")
			.append(diffToReasonCodeVariable).append(" = new TreeMap<Double, String>();\n");


		// Analyze each characteristic and print the corresponding code. 
		for (Characteristic c : cl) {
			translateCharacteristic(c, context, sb, outputField, diffToReasonCodeVariable);
		}

		// Store the result of the reason code. It is currently stored in diffToReasonCode.lastEntry().getValue().
		if (context.getModelResultTrackingVariable() != null) {
			sb.append(context.getIndentation())
				.append(context.getModelResultTrackingVariable()).append(" = ").append(diffToReasonCodeVariable).append(".lastEntry().getValue()")
				.append(";\n");
		}
		return sb.toString();
	}

	// Method that takes a characteristics, and update the code. 
	private void translateCharacteristic(Characteristic c, TranslationContext context, StringBuilder code, DataField outputVariable, String diffToReasonCodeVariable) throws TranslationException {
		// Run through each characteristic.
		// first is useful to know if we are the first case, or not. Depending on that, we add an 'else' before the if.
		Boolean first = true;

		// Put some space between the different characteristics.
		code.append("\n");
		
		for (Attribute a: c.getAttributes()) {
			Predicate p = a.getPredicate();
			if (p == null) {
				throw new TranslationException("Missing attribute predicate for characteristic: " + c.getName());
			}

			String predicateCode = PredicateTranslationUtil.generateCode(p, this, context);

			// Evaluate the predicate. If it is true, update the score. If it is not the first if of the list,
			// Change it in a else if.
			code.append(context.getIndentation()).append(first ? "" : "else ")
			.append("if ((").append(predicateCode).append(") == ").append(PredicateTranslationUtil.TRUE)
			.append(") {\n");
			context.incIndentation();
			
			// Update the outputVariable with the corresponding partial score.
			code.append(context.getIndentation()).append(outputVariable.getName().getValue()).append(" += ")
			.append(a.getPartialScore()).append(";\n");
			
			// Compute the diff, include the result in the generated code.
			double diff = 0;
			switch (reasonCodeAlgorithm) {
			case POINTS_BELOW:
				diff = c.getBaselineScore() - a.getPartialScore();
				break;
			case POINTS_ABOVE:
				diff = a.getPartialScore() - c.getBaselineScore();
				break;
			default:
				// We should never be there.
				assert false;
				break;
			}
			//code.append(context.getIndentation()).append(diffVariable).append(" = ").append(diff).append(";\n");

			// If there is a reason code at the attribute level, use it. Otherwise use the one in the
			// characteristic level.
			code.append(context.getIndentation()).append(diffToReasonCodeVariable).append(".put(").append(diff).append(", \"");
			if (a.getReasonCode()!=null && !a.getReasonCode().isEmpty()) {
				code.append(a.getReasonCode());
			}
			else {
				code.append(c.getReasonCode());
			}
			code.append("\");\n");
			context.decIndentation();
			code.append(context.getIndentation()).append("}\n");
			first = false;
		}
	}
}
