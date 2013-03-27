package org.jpmml.translator;

import java.util.List;

import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.NoTrueChildStrategyType;
import org.dmg.pmml.Node;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.TreeModel;
import org.jpmml.manager.TreeModelManager;

/**
 * Translate tree model into java code
 * 
 * @author asvirsky
 *
 */
public class TreeModelTranslator extends TreeModelManager implements Translator {	
	public TreeModelTranslator(PMML pmml){
		super(pmml);
	}

	public TreeModelTranslator(PMML pmml, TreeModel treeModel){
		super(pmml, treeModel);
	}

	public TreeModelTranslator(TreeModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	public String translate(TranslationContext context) throws TranslationException {

		String outputVariableName = null;
		List<FieldName> predictedFields = getPredictedFields();
		if (predictedFields!=null && !predictedFields.isEmpty()) {
			outputVariableName = predictedFields.get(0).getValue();
		}
		if (outputVariableName==null) {
			throw new TranslationException("Predicted variable is not defined");
		}
		
		DataField outputField = getDataField(new FieldName(outputVariableName));		
		if (outputField==null || outputField.getDataType()==null) {
			throw new TranslationException("Predicted variable ["+outputVariableName+"] does not have type defined");
		}
		
		Node rootNode = getOrCreateRoot();
		StringBuilder sb = new StringBuilder();		
		generateCodeForNode(rootNode, context, sb, outputField);
		
		return sb.toString();
	}
	
	private OpType findOutputVariableType() {
		OpType type = OpType.CONTINUOUS;
		for (MiningField miningField : getMiningSchema().getMiningFields()) {
			if (miningField.getUsageType()==FieldUsageType.PREDICTED) {
				DataField dataField = this.getDataField(miningField.getName());
				if (dataField!=null) {
					type = dataField.getOptype();
					break;
				}
			}
		}
		return type;
	}
	
	private Node getChildById(Node node, String id) {
		Node result = null;
		if (id!=null) {
			for (Node child : node.getNodes()) {
				if (id.equals(child.getId())) {
					result = child;
					break;
				}
			}
		}
		return result;
	}
	
	private void generateCodeForNode(Node node, TranslationContext context, StringBuilder code, DataField outputVariable) throws TranslationException {
		
		TranslatorUtil.assignOutputVariable(code, node.getScore(), context, outputVariable);

		if (context.getModelResultTrackingVariable()!=null) {
				code.append(context.getIndentation())
					.append(context.getModelResultTrackingVariable()).append(" = \"").append(node.getId()).append("\"")
					.append(";\n");
		}


		if (node.getNodes()==null || node.getNodes().isEmpty()) return;

		String succVariable = context.generateLocalVariableName("succ");
		code.append(context.getIndentation()).append("boolean ")
			.append(succVariable).append(" = false;\n");
		
		//List<Node> children = node.getNodes();
		for (Node child : node.getNodes()) {				
			
			//Node child = children.get(i);
			Predicate predicate = child.getPredicate();
			if (predicate==null) {
				throw new TranslationException("No predicate for node: "+child.getId());
			}
			
			code.append(context.getIndentation())
				.append("if (!").append(succVariable).append(") {\n");
			context.incIndentation();
			
			String predicateValue = context.generateLocalVariableName("predicateValue");
			String predicateCode = PredicateTranslationUtil.generateCode(predicate, this, context);
			
			// evaluate predicate and store value into "predicateValue" variable
			code.append(context.getIndentation())
				.append("int ").append(predicateValue).append(" = ").append(predicateCode).append(";\n");
			
			code.append(context.getIndentation())
				.append("if (").append(predicateValue).append("==").append(PredicateTranslationUtil.TRUE)
				.append(") {\n");
			context.incIndentation();
			
			code.append(context.getIndentation()).append(succVariable).append(" = true;\n");
			// predicate is true - insert code for nested nodes 
			generateCodeForNode(child, context, code, outputVariable);

			context.decIndentation();
			code.append(context.getIndentation()).append("}\n");
			
			code.append(context.getIndentation())
				.append("else if (").append(predicateValue).append("==").append(PredicateTranslationUtil.UNKNOWN)
				.append(") {\n");
			context.incIndentation();
			// predicate is unknown
			
			switch (this.getModel().getMissingValueStrategy()) {
				case NONE: 
					// same as FALSE for current predicate
					// do nothing 
					break;
	
				case LAST_PREDICTION:
					// assume this node evaluated to true, but ignore its value
					// take last prediction instead
					code.append(context.getIndentation()).append(succVariable).append(" = true;\n");
					break;
					
				case NULL_PREDICTION:
					// same as above, but reset prediction to null
					code.append(context.getIndentation()).append(succVariable).append(" = true;\n");
					// this.getDataField(context.getO)
					code.append(context.getIndentation())
						.append(context.formatOutputVariable(outputVariable.getName().getValue()))
						.append(" = ")
						.append(context.getNullValueForVariable(findOutputVariableType()))
						.append(";\n");
					break;
				
				case DEFAULT_CHILD:
					// use default node 
					// can't generate code if default child is undefined
					// (this seems to be expensive option in terms of amount of code generated...)
					Node defaultNode = getChildById(child, child.getDefaultChild());
					if (defaultNode==null) {
						throw new TranslationException("No default child defined for nodeId: "+child.getId());
					}
					generateCodeForNode(defaultNode, context, code, outputVariable);
					break;
				default:
					throw new TranslationException("Unsupported strategy: " + getModel().getMissingValueStrategy());
			}

			context.decIndentation();
			code.append(context.getIndentation()).append("}\n");

			context.decIndentation();
			code.append(context.getIndentation()).append("}\n");
		}
		
		if (getModel().getNoTrueChildStrategy()==NoTrueChildStrategyType.RETURN_NULL_PREDICTION) {
			code.append(context.getIndentation()).append("if (!").append(succVariable).append(") {\n") ;

			context.incIndentation();

			code.append(context.getIndentation())
				.append(context.formatOutputVariable(outputVariable.getName().getValue()))
				.append(" = ").append(context.getNullValueForVariable(findOutputVariableType()))
				.append(";\n");

			context.decIndentation();
			
			code.append(context.getIndentation()).append("}\n");
		}
		
	}

}
