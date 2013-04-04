package org.jpmml.translator;

public interface CodeFormatter {
	
	public enum Operator {
		EQUAL("="),
		PLUS_EQUAL("+="),
		MINUS_EQUAL("-="),
		MULT_EQUAL("*="),
		DIV_EQUAL("/="),
		XOR_EQUAL("^="),
		;

	    private final String value;

	    Operator(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

	    public static Operator fromValue(String v) {
	        for (Operator c: Operator.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }

	}
	
	/**
	 * Allow to add a declaration of a new variable in the code.
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param variable The variable.
	 * @param initializer The initial value.
	 */
	public void addDeclarationVariable(StringBuilder code, TranslationContext context,
					Variable variable, String initializer);
	
	/**
	 * Allow to add a declaration of a new variable in the code. The variable
	 * is initialized with a value corresponding to a standard initial value for its type.
	 * 0 for Integer, "new String()" for String, ...
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param variable The variable.
	 */
	public void addDeclarationVariable(StringBuilder code, TranslationContext context,
					Variable variable);
	
	/**
	 * Add a line in the code. Don't append a '\n' at the end of the line.
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param line The line to add.
	 */
	public void addLine(StringBuilder code, TranslationContext context, String line);
	
	/**
	 * Affect the variable to the "null" value corresponding to its type.
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation. Define the null value.
	 * @param variableType The type of the variable.
	 * @param variable The name of the variable.
	 */
	public void affectVariableToNullValue(StringBuilder code, TranslationContext context,
										Variable variable);
	
	/**
	 * Affect a variable.
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param op The operation on the affectation.
	 * @param variable The variable we affect.
	 * @param expression The expression on the right side of the affectation.
	 */
	public void affectVariable(StringBuilder code, TranslationContext context,
			Operator op, Variable variable, String expression);

	/**
	 * Affect a variable.
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param op The operation on the affectation.
	 * @param variableName The name of the variable we affect.
	 * @param expression The expression on the right side of the affectation.
	 */
	public void affectVariable(StringBuilder code, TranslationContext context,
			Operator op, String variableName, String expression);
	
	/**
	 * Affect a variable with the operator "=".
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param variable The variable we affect.
	 * @param expression The expression on the right side of the affectation.
	 */
	public void affectVariable(StringBuilder code, TranslationContext context,
			Variable variable, String expression);
	
	/**
	 * Affect a variable with the operator "=".
	 * 
	 * @param code The code on which we append.
	 * @param context The context of the translation.
	 * @param variableName The name of the variable we affect.
	 * @param expression The expression on the right side of the affectation.
	 */
	public void affectVariable(StringBuilder code, TranslationContext context,
			String variableName, String expression);
	
	
	/**
	 * 
	 * Start a control flow structure, like a if. For example to generate
	 * "if (a == b) {
	 *  	a = b + 1;
	 *  }"
	 *  the developer may write:
	 *  
	 *  beginControlFlowStructure(code, context, "if", "a == b");
	 *  addLine(code, context, "a = b + 1;");
	 *  endControlFlowStructure(code, context); 
	 * 
	 * @param code ...
	 * @param context ...
	 * @param keyword The keyword of the structure. Not verified to be a good value.
	 * @param conditionnalExpression The expression to decide the flow of the program.
	 */
	public void beginControlFlowStructure(StringBuilder code, TranslationContext context,
			String keyword, String conditionnalExpression);
	
	/**
	 * End the control flow.
	 * See {@link CodeFormatter#beginControlFlowStructure(StringBuilder, TranslationContext, String, String)}
	 * @param code ...
	 * @param context ...
	 */
	public void endControlFlowStructure(StringBuilder code, TranslationContext context);
}
