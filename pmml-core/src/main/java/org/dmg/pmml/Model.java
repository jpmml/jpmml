/*
 * Copyright (c) 2009 University of Tartu
 */
package org.dmg.pmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Model extends PMMLObject {

	abstract
	public String getModelName();

	abstract
	public void setModelName(String modelName);

	abstract
	public MiningFunctionType getFunctionName();

	abstract
	public void setFunctionName(MiningFunctionType functionName);

	abstract
	public MiningSchema getMiningSchema();

	abstract
	public void setMiningSchema(MiningSchema miningSchema);

	abstract
	public LocalTransformations getLocalTransformations();

	abstract
	public void setLocalTransformations(LocalTransformations localTransformations);

	abstract
	public boolean isIsScorable();

	abstract
	public void setIsScorable(Boolean isScorable);

	abstract
	public ModelStats getModelStats();

	abstract
	public void setModelStats(ModelStats modelStats);
}