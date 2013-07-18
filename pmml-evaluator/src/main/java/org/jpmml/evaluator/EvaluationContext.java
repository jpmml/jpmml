/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

abstract
public class EvaluationContext {

	private Deque<Map<FieldName, ?>> stack = Queues.newArrayDeque();


	public EvaluationContext(){
	}

	public EvaluationContext(Map<FieldName, ?> arguments){
		pushFrame(arguments);
	}

	abstract
	public DerivedField resolveField(FieldName name);

	abstract
	public DefineFunction resolveFunction(String name);

	public Map<FieldName, ?> getArguments(){
		Map<FieldName, Object> result = Maps.newLinkedHashMap();

		Deque<Map<FieldName, ?>> stack = getStack();

		// Iterate from last (ie. oldest) to first (ie. newest)
		Iterator<Map<FieldName, ?>> it = stack.descendingIterator();
		while(it.hasNext()){
			Map<FieldName, ?> frame = it.next();

			result.putAll(frame);
		}

		return result;
	}

	public Object getArgument(FieldName name){
		Deque<Map<FieldName, ?>> stack = getStack();

		// Iterate from first to last
		Iterator<Map<FieldName, ?>> it = stack.iterator();
		while(it.hasNext()){
			Map<FieldName, ?> frame = it.next();

			if(frame.containsKey(name)){
				return frame.get(name);
			}
		}

		return null;
	}

	public Map<FieldName, ?> popFrame(){
		return getStack().pop();
	}

	public void pushFrame(Map<FieldName, ?> frame){
		getStack().push(frame);
	}

	Deque<Map<FieldName, ?>> getStack(){
		return this.stack;
	}
}