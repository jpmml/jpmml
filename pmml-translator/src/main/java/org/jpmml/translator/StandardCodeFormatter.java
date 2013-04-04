package org.jpmml.translator;

import org.jpmml.manager.UnsupportedFeatureException;

public class StandardCodeFormatter implements CodeFormatter {

	public void addDeclarationVariable(StringBuilder code,
			TranslationContext context, Variable variable,
			String initializer) {
		code.append(context.getIndentation())
			.append(variable.getTypeName()).append(" ")
			.append(variable.getName()).append(" = ")
			.append(initializer).append(";\n");
	}

	public void addDeclarationVariable(StringBuilder code,
			TranslationContext context, Variable variable) {
		String initializer = null;

		switch (variable.getType()) {
		case INTEGER:
			initializer = "0";
			break;
		case DOUBLE:
		case FLOAT:
			initializer = "0.0";
			break;
		case STRING:
		case OBJECT:
			initializer = "new " + variable.getTypeName() + "()";
			break;
		default:
			throw new UnsupportedFeatureException(variable.getType());
		}

		addDeclarationVariable(code, context, variable, initializer);
	}

	public void addLine(StringBuilder code, TranslationContext context,
			String line) {
		code.append(context.getIndentation()).append(line).append("\n");
	}

	public void affectVariableToNullValue(StringBuilder code,
			TranslationContext context, Variable variable) {
		String initializer = null;

		switch (variable.getType()) {
		case INTEGER:
			initializer = "0";
			break;
		case DOUBLE:
		case FLOAT:
			initializer = "0.0";
			break;
		case OBJECT:
		case STRING:
			initializer = "null";
			break;
		default:
			throw new UnsupportedFeatureException(variable.getType());
		}
		
		affectVariable(code, context, Operator.EQUAL, variable, initializer);
	}

	public void affectVariable(StringBuilder code, TranslationContext context,
			Operator op, Variable variable, String expression) {
		affectVariable(code, context, op, variable.getName(), expression);
	}

	public void beginControlFlowStructure(StringBuilder code,
			TranslationContext context, String keyword,
			String conditionnalExpression) {
		code.append(context.getIndentation()).append(keyword)
			.append(" (").append(conditionnalExpression).append(") {\n");
		
		context.incIndentation();
		
	}

	public void endControlFlowStructure(StringBuilder code,
			TranslationContext context) {
		context.decIndentation();
		code.append(context.getIndentation()).append("}\n");
	}

	public void affectVariable(StringBuilder code, TranslationContext context,
			Variable variable, String expression) {
		affectVariable(code, context, Operator.EQUAL, variable, expression);
		
	}

	public void affectVariable(StringBuilder code, TranslationContext context,
			String variableName, String expression) {
		// TODO Auto-generated method stub
		code.append(context.getIndentation()).append(variableName)
		.append(" = ").append(expression)
		.append(";\n");
		
	}

	public void affectVariable(StringBuilder code, TranslationContext context,
			Operator op, String variableName, String expression) {
		code.append(context.getIndentation()).append(variableName)
		.append(" ").append(op.value()).append(expression)
		.append(";\n");
	}

}
