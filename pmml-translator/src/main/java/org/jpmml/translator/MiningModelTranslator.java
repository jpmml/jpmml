package org.jpmml.translator;

import java.util.List;
import java.util.Set;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Segment;
import org.jpmml.manager.MiningModelManager;
import org.jpmml.manager.UnsupportedFeatureException;
import org.jpmml.translator.Variable.VariableType;

/**
 * Generate java code to manage MiningModel.
 *
 * @author tbadie
 *
 */
public class MiningModelTranslator extends MiningModelManager implements Translator {
	public MiningModelTranslator(PMML pmml){
		super(pmml);
	}

	public MiningModelTranslator(PMML pmml, MiningModel model){
		super(pmml, model);
	}

	public MiningModelTranslator(MiningModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * Return a string that is a java code able to evaluate the model on a set of parameters.
	 *
	 * @param context The translation context.
	 * @throws Exception
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

	public String translate(TranslationContext context, DataField outputField) throws TranslationException{
		StringBuilder sb = new StringBuilder();

		switch (getFunctionType()) {
		case CLASSIFICATION:
			translateClassification(context, sb, outputField.getName());
			break;
		case REGRESSION:
			throw new UnsupportedOperationException();
		default:
			assert false;
		}

		return sb.toString();
	}

	private String namify(String id) {
		return "modelId" + id;
	}

	private void translateClassification(TranslationContext context, StringBuilder code, FieldName outputFieldName) throws TranslationException {
		CodeFormatter cf = context.getFormatter();

		ModelTranslatorFactory factory = new ModelTranslatorFactory();

		// Now, here is a subtle hack to avoid evaluate everything where we
		// are only interested in getting the first segment. The idea is to
		// use a "goto end" after the first segment that is evaluated to true.
		// Java does not provide a goto, so we rely on a "do { ... } while (false);"
		// structure. The code inside the brackets is the evaluation of all the segment.
		// And after the first segment evaluated, we just "break".
		if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
			cf.addLine(code, context, "do {");
		}

		// FIXME: For now, classification is only working on string.
		for (Segment s : getSegment()) {
			cf.addDeclarationVariable(code, context, new Variable(VariableType.STRING, namify(s.getId())), "null");
			cf.beginControlFlowStructure(code, context, "if", "("
					+ PredicateTranslationUtil.generateCode(s.getPredicate(), this, context) + ") == " + PredicateTranslationUtil.TRUE);
			Translator t = (Translator) factory.getModelManager(getPmml(), s.getModel());
			code.append(t.translate(context, new DataField(new FieldName(namify(s.getId())), OpType.CATEGORICAL, DataType.STRING)));
			if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
				cf.affectVariable(code, context, outputFieldName.getValue(), namify(s.getId()));
				cf.addLine(code, context, "break;");
			}
			cf.endControlFlowStructure(code, context);
		}

		if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
			cf.addLine(code, context, "} while (false);");
		}

		// Now work on the multiple method.
		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// Already handled
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			throw new UnsupportedFeatureException("Missing implementation.");
		case MAJORITY_VOTE:
			context.addRequiredImport("java.util.TreeMap;");
			cf.addDeclarationVariable(code, context, new Variable(VariableType.OBJECT, "TreeMap<String, Double>", "nameToVote"));
			for (Segment s : getSegment()) {
				cf.beginControlFlowStructure(code, context, "if", namify(s.getId()) + " != null");
				// This segment has voted.
				cf.beginControlFlowStructure(code, context, "if", "nameToVote.containsKey(" + namify(s.getId()) + ")");
				cf.addLine(code, context, "nameToVote.put(" + namify(s.getId()) + ", nameToVote.get(" + namify(s.getId()) + ") + 1.0);");
				cf.endControlFlowStructure(code, context);
				cf.beginControlFlowStructure(code, context, "else", null);
				cf.addLine(code, context, "nameToVote.put(" + namify(s.getId()) + ", 1.0);");
				cf.endControlFlowStructure(code, context);
				cf.endControlFlowStructure(code, context);
				cf.addLine(code, context, getBetterKey(context, cf, "nameToVote", outputFieldName.getValue()));
			}
			break;

		default:
			throw new TranslationException("The method " + getMultipleMethodModel().value()
					+ " is not compatible with the classification.");
		}
	}

	/**
	 * Get an expression that store the key that has the biggest value into the outputVariableName.
	 *
	 * @param context The context of the translation.
	 * @param cf The formatter.
	 * @param mapName The name of the variable.
	 * @param outputVariableName The variable where we store the result.
	 * @return A valid expression.
	 */
	// FIXME: Enhance this function.
	private String getBetterKey(TranslationContext context, CodeFormatter cf,
						String mapName, String outputVariableName) {
		StringBuilder result = new StringBuilder();

		String maxVarName = context.generateLocalVariableName("max");
		cf.addDeclarationVariable(result, context, new Variable(VariableType.DOUBLE, maxVarName));
		cf.beginControlFlowStructure(result, context, "for", "Map.Entry<?, Double> tmpVariable : " + mapName + ".entrySet()");
		cf.beginControlFlowStructure(result, context, "if", "tmpVariable.getValue() > " + maxVarName);
		cf.affectVariable(result, context, outputVariableName, "(String) tmpVariable.getKey()");
		cf.affectVariable(result, context, maxVarName, "tmpVariable.getValue()");
		cf.endControlFlowStructure(result, context);

		cf.endControlFlowStructure(result, context);

		return result.toString();
	}

}