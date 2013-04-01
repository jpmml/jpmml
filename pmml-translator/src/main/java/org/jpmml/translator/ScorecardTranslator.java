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

	// The main method. It takes a context, and print the code corresponding tot he scorecard.
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
		
		// Analyze each characteristic and print the corresponding code. 
		for (Characteristic c : cl) {
			translateCharacteristic(c, context, sb, outputField);
		}
		
		return sb.toString();
	}

	// Method that takes a characteristics, and update the code. 
	private void translateCharacteristic(Characteristic c, TranslationContext context, StringBuilder code, DataField outputVariable) throws TranslationException {
		// Run through each characteristic.
		for (Attribute a: c.getAttributes()) {
			Predicate p = a.getPredicate();
			if (p==null) {
				throw new TranslationException("No predicate for attribute: " + c.getName());
			}

			String predicateValue = context.generateLocalVariableName("predicateValue");
			String predicateCode = PredicateTranslationUtil.generateCode(p, this, context);

			// Declare and affect the predicateValue with the predicateCode.
			code.append(context.getIndentation())
			.append("int ").append(predicateValue).append(" = ").append(predicateCode).append(";\n");
			
			// If the predicate is true,
			code.append(context.getIndentation())
			.append("if (").append(predicateValue).append(" == ").append(PredicateTranslationUtil.TRUE)
			.append(") {\n");
			context.incIndentation();
			// update the outputVariable with the corresponding partial score.
			code.append(context.getIndentation()).append(outputVariable.getName().getValue()).append(" += ")
			.append(a.getPartialScore()).append(";\n");
			context.decIndentation();
			code.append(context.getIndentation()).append("}\n\n");
		}
	}
}
