package org.jpmml.translator;

import java.util.List;

import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NoTrueChildStrategyType;
import org.dmg.pmml.Node;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.TreeModel;
import org.jpmml.manager.TreeModelManager;
import org.jpmml.translator.Variable.VariableType;

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

		return translate(context, outputField);
	}

	public String translate(TranslationContext context, DataField outputField) throws TranslationException {
		Node rootNode = getOrCreateRoot();
		StringBuilder sb = new StringBuilder();
		CodeFormatter cf = new StandardCodeFormatter();
		generateCodeForNode(rootNode, context, sb, outputField, cf);

		return sb.toString();
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

	private void generateCodeForNode(Node node, TranslationContext context, StringBuilder code, DataField outputVariable, CodeFormatter cf) throws TranslationException {
		TranslatorUtil.assignOutputVariable(code, node.getScore(), context, outputVariable);


		if (context.getModelResultTrackingVariable() != null && node.getId() != null) {
			cf.affectVariable(code, context, context.getModelResultTrackingVariable(), cf.stringify(node.getId()));
		}


		if (node.getNodes() == null || node.getNodes().isEmpty()) {
			return;
		}

		String succVariable = context.generateLocalVariableName("succ");
		
		cf.addDeclarationVariable(code, context, new Variable(VariableType.BOOLEAN, succVariable));
		
		for (Node child : node.getNodes()) {				
			
			Predicate predicate = child.getPredicate();
			if (predicate==null) {
				throw new TranslationException("No predicate for node: "+child.getId());
			}
			
			cf.beginControlFlowStructure(code, context, "if", "!" + succVariable);
			
			String predicateValue = context.generateLocalVariableName("predicateValue");
			String predicateCode = PredicateTranslationUtil.generateCode(predicate, this, context);
			
			// evaluate predicate and store value into "predicateValue" variable
			cf.addDeclarationVariable(code, context, new Variable(VariableType.INTEGER, predicateValue), predicateCode);
			
			cf.beginControlFlowStructure(code, context, "if", predicateValue + " == " + PredicateTranslationUtil.TRUE);

			cf.affectVariable(code, context, succVariable, "true");

			// predicate is true - insert code for nested nodes 
			generateCodeForNode(child, context, code, outputVariable, cf);

			cf.endControlFlowStructure(code, context);

			cf.beginControlFlowStructure(code, context, "else if", predicateValue + " == " + PredicateTranslationUtil.UNKNOWN);
			// predicate is unknown
			
			switch (this.getModel().getMissingValueStrategy()) {
				case NONE: 
					// same as FALSE for current predicate
					// do nothing 
					break;
	
				case LAST_PREDICTION:
					// assume this node evaluated to true, but ignore its value
					// take last prediction instead
					cf.affectVariable(code, context, succVariable, "true");
					break;
					
				case NULL_PREDICTION:
					// same as above, but reset prediction to null
					cf.affectVariable(code, context, succVariable, "true");

					cf.affectVariableToNullValue(code, context, new Variable(outputVariable));
					break;
				
				case DEFAULT_CHILD:
					// use default node 
					// can't generate code if default child is undefined
					// (this seems to be expensive option in terms of amount of code generated...)
					Node defaultNode = getChildById(child, child.getDefaultChild());
					if (defaultNode==null) {
						throw new TranslationException("No default child defined for nodeId: "+child.getId());
					}
					generateCodeForNode(defaultNode, context, code, outputVariable, cf);
					break;
				default:
					throw new TranslationException("Unsupported strategy: " + getModel().getMissingValueStrategy());
			}


			cf.endControlFlowStructure(code, context);
			cf.endControlFlowStructure(code, context);
		}
		
		if (getModel().getNoTrueChildStrategy()==NoTrueChildStrategyType.RETURN_NULL_PREDICTION) {
			cf.beginControlFlowStructure(code, context, "if", "!" + succVariable);
			
			cf.affectVariableToNullValue(code, context, new Variable(outputVariable));


			cf.affectVariable(code, context,
					context.getModelResultTrackingVariable(),
					context.getNullValueForVariable(OpType.CATEGORICAL));
			
			cf.endControlFlowStructure(code, context);
		}	
	}

}