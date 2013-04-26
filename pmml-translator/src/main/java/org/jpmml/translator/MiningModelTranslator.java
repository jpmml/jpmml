package org.jpmml.translator;

import java.util.HashMap;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Segment;
import org.jpmml.manager.MiningModelManager;
import org.jpmml.manager.ModelManager;
import org.jpmml.manager.UnsupportedFeatureException;
import org.jpmml.translator.CodeFormatter.Operator;
import org.jpmml.translator.Variable.VariableType;

/**
 * Generate java code to manage MiningModel.
 *
 * @author tbadie
 *
 */
public class MiningModelTranslator extends MiningModelManager implements Translator {
	private HashMap<Segment, Integer> segmentToId = new HashMap<Segment, Integer>();

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
		try {
			return translate(context, getOutputField(this));
		}
		catch (Exception e) {
			throw new TranslationException(e.getMessage());
		}
	}

	public String translate(TranslationContext context, DataField outputField) throws TranslationException {
		StringBuilder sb = new StringBuilder();
		try	{
			switch (getFunctionType()) {
			case CLASSIFICATION:
				translateClassification(context, sb, outputField);
				break;
			case REGRESSION:
				translateRegression(context, sb, outputField);
				break;
			default:
				throw new UnsupportedOperationException();
			}

			return sb.toString();
		}
		catch (Exception e) {
			throw new TranslationException(e.getMessage());
		}
	}

	private String namify(Segment s) {
		if (!segmentToId.containsKey(s)) {
			segmentToId.put(s, segmentToId.size());
		}

		return "segmentNumber" + segmentToId.get(s);
	}

	private void runModels(TranslationContext context, StringBuilder code, DataField outputField,
			CodeFormatter cf) throws Exception {

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

		// FIXME: Here we are in trouble because there is two Predicted results.
		for (Segment s : getSegment()) {
			Translator t = (Translator) factory.getModelManager(getPmml(), s.getModel());
			DataField out = getOutputField((ModelManager<?>) t);
			OpType op = out.getOptype();
			DataType dt = out.getDataType();


			cf.addDeclarationVariable(code, context, new Variable(dt, namify(s)), "null");
			cf.beginControlFlowStructure(code, context, "if", "("
					+ PredicateTranslationUtil.generateCode(s.getPredicate(), this, context)
					+ ") == " + PredicateTranslationUtil.TRUE);
			code.append(t.translate(context, new DataField(new FieldName(namify(s)), op, dt)));


			if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
				cf.affectVariable(code, context, outputField.getName().getValue(), namify(s));
				cf.addLine(code, context, "break;");
			}

			if (getMultipleMethodModel() == MultipleModelMethodType.MODEL_CHAIN) {
				cf.affectVariable(code, context, getOutputField((ModelManager<?>) t)
						.getName().getValue(), namify(s));
			}
			cf.endControlFlowStructure(code, context);
		}

		if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
			cf.addLine(code, context, "} while (false);");
		}



	}

	private void translateRegression(TranslationContext context, StringBuilder code, DataField outputField) throws Exception {
		CodeFormatter cf = context.getFormatter();
		runModels(context, code, outputField, cf);

		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// result already have the right value.
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			break;
		case AVERAGE:
		case WEIGHTED_AVERAGE:
			Boolean weighted =
				getMultipleMethodModel() == MultipleModelMethodType.WEIGHTED_AVERAGE;

			String sumName = context.generateLocalVariableName("sum");
			String sumWeightName = context.generateLocalVariableName("sumWeight");

			cf.addDeclarationVariable(code, context, new Variable(VariableType.DOUBLE, sumName));
			if (weighted) {
				cf.addDeclarationVariable(code, context, new Variable(VariableType.DOUBLE, sumWeightName));
			}

			String counterName = context.generateLocalVariableName("counter");
			cf.addDeclarationVariable(code, context, new Variable(VariableType.INTEGER, counterName));

			for (Segment s : getSegment()) {
				// This following line is equivalent to add this to the code:
				// 'result += value == null ? value * weight : 0;' Where
				// the '* weight' is only done when we weighted is true.

				cf.beginControlFlowStructure(code, context, "if", namify(s) + " != null");
				cf.affectVariable(code, context, Operator.PLUS_EQUAL, new Variable(outputField), namify(s)
						+ (weighted ? " * " + s.getWeight() : ""));
				cf.addLine(code, context, "++" + counterName + ";");

				if (weighted) {
					// Little hack to transform the weight into a string without creating (explicitly) a Double, and call
					// ToString on it.
					cf.affectVariable(code, context, Operator.PLUS_EQUAL, sumWeightName, "" + s.getWeight());
				}
				cf.endControlFlowStructure(code, context);
			}

			cf.beginControlFlowStructure(code, context, "if", (weighted ? sumWeightName : counterName) + " != 0.0");
			cf.affectVariable(code, context, Operator.DIV_EQUAL, outputField.getName().getValue(),
							weighted ? sumWeightName : "" + counterName);

			cf.endControlFlowStructure(code, context);
			break;

		case MEDIAN:
			context.addRequiredImport("java.util.ArrayList");
			context.addRequiredImport("java.util.Collections");
			String listName = context.generateLocalVariableName("list");
			cf.addLine(code, context, "ArrayList<Double>" + listName + " = new ArrayList<Double>(" + getSegment().size() + ");");
			for (Segment s : getSegment()) {
				cf.beginControlFlowStructure(code, context, "if", namify(s) + "!= null");
				cf.addLine(code, context, listName + ".add(" + namify(s) + ");");
				cf.endControlFlowStructure(code, context);
			}
			cf.addLine(code, context, "Collections.sort(" + listName + ");");
			cf.affectVariable(code, context, outputField.getName().getValue(), listName + ".get("
					+ listName + ".size() / 2);");
			break;
		default:
			throw new TranslationException("The method " + getMultipleMethodModel().value()
					+ " is not compatible with the regression.");
		}

	}


	private void translateClassification(TranslationContext context, StringBuilder code, DataField outputField) throws Exception {
		CodeFormatter cf = context.getFormatter();
		runModels(context, code, outputField, cf);

		// Now work on the multiple method.
		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// Already handled
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			throw new UnsupportedFeatureException("Missing implementation.");
		case MAJORITY_VOTE:
		case WEIGHTED_MAJORITY_VOTE:
			context.addRequiredImport("java.util.TreeMap;");
			cf.addDeclarationVariable(code, context, new Variable(VariableType.OBJECT, "TreeMap<String, Double>", "nameToVote"));
			for (Segment s : getSegment()) {
				Double weight = getMultipleMethodModel() == MultipleModelMethodType.WEIGHTED_MAJORITY_VOTE
						? s.getWeight()
						: 1.0;
				cf.beginControlFlowStructure(code, context, "if", namify(s) + " != null");
				// This segment has voted.
				cf.beginControlFlowStructure(code, context, "if", "nameToVote.containsKey(" + namify(s) + ")");
				cf.addLine(code, context, "nameToVote.put(" + namify(s)
						+ ", nameToVote.get(" + namify(s) + ") + " + weight + ");");
				cf.endControlFlowStructure(code, context);
				cf.beginControlFlowStructure(code, context, "else", null);
				cf.addLine(code, context, "nameToVote.put(" + namify(s) + ", " + weight + ");");
				cf.endControlFlowStructure(code, context);
				cf.endControlFlowStructure(code, context);
				cf.addLine(code, context, getBetterKey(context, cf, "nameToVote", outputField.getName().getValue()));
			}
			break;
		case AVERAGE:
		case WEIGHTED_AVERAGE:
		case MEDIAN:
		case MAX:
			throw new UnsupportedFeatureException("Missing implementation.");
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