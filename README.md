Java API for producing and scoring models in Predictive Model Markup Language (PMML).

# Features #

### Class model ###

* Full support for PMML 3.0, 3.1, 3.2, 4.0 and 4.1 schemas:
  * Class hierarchy.
  * Schema version annotations.
* Fluent API:
  * Value constructors.
* SAX Locator information
* [Visitor pattern] (http://en.wikipedia.org/wiki/Visitor_pattern):
  * Validation agents.
  * Optimization and transformation agents.

### Evaluation engine ###

* Full support for [DataDictionary] (http://www.dmg.org/v4-1/DataDictionary.html) and [MiningSchema] (http://www.dmg.org/v4-1/MiningSchema.html) elements:
  * Complete data type system.
  * Complete operational type system. For example, continuous integers, categorical integers and ordinal integers are handled differently in equality check and comparison operations.
  * Detection and treatment of outlier, missing and invalid values.
* Full support for [transformations] (http://www.dmg.org/v4-1/Transformations.html) and [functions] (http://www.dmg.org/v4-1/Functions.html):
  * Built-in functions.
  * User defined functions (PMML, Java).
* Full support for [Targets] (http://www.dmg.org/v4-1/Targets.html) and [Output] (http://www.dmg.org/v4-1/Output.html) elements.
* Fully supported model elements:
  * [Association rules] (http://www.dmg.org/v4-1/AssociationRules.html)
  * [Cluster model] (http://www.dmg.org/v4-1/ClusteringModel.html)
  * [General regression] (http://www.dmg.org/v4-1/GeneralRegression.html)
  * [Naive Bayes] (http://www.dmg.org/v4-1/NaiveBayes.html)
  * [k-Nearest neighbors] (http://www.dmg.org/v4-1/KNN.html)
  * [Neural network] (http://www.dmg.org/v4-1/NeuralNetwork.html)
  * [Regression] (http://www.dmg.org/v4-1/Regression.html)
  * [Rule set] (http://www.dmg.org/v4-1/RuleSet.html)
  * [Scorecard] (http://www.dmg.org/v4-1/Scorecard.html)
  * [Support Vector Machine] (http://www.dmg.org/v4-1/SupportVectorMachine.html)
  * [Tree model] (http://www.dmg.org/v4-1/TreeModel.html)
  * [Ensemble model] (http://www.dmg.org/v4-1/MultipleModels.html)
* Fully interoperable with popular open source software:
  * [R] (http://www.r-project.org/) and [Rattle] (http://rattle.togaware.com/)
  * [KNIME] (http://www.knime.org/)
  * [RapidMiner] (http://rapid-i.com/content/view/181/190/)

# Installation #

JPMML library JAR files (together with accompanying Java source and Javadocs JAR files) are released via [Maven Central Repository] (http://repo1.maven.org/maven2/org/jpmml/). Please join the [JPMML mailing list] (https://groups.google.com/forum/#!forum/jpmml) for release announcements.

The current version is **1.0.21** (31 October, 2013).

### Class model ###

```xml
<!-- Class model classes -->
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-model</artifactId>
	<version>${jpmml.version}</version>
</dependency>
<!-- Class model annotations -->
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-schema</artifactId>
	<version>${jpmml.version}</version>
</dependency>
```

### Evaluation engine ###

```xml
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-evaluator</artifactId>
	<version>${jpmml.version}</version>
</dependency>
```

# Usage #

### Class model ###

The class model consists of two types of classes. There is a small number of manually crafted classes that are used for structuring the class hierarchy. They are permanently stored in the Java sources directory `/pmml-model/src/main/java`. Additionally, there is a much greater number of automatically generated classes that represent actual PMML elements. They can be found in the generated Java sources directory `/pmml-model/target/generated-sources/xjc` after a successful build operation.

All class model classes descend from class `org.dmg.pmml.PMMLObject`. Additional class hierarchy levels, if any, represent common behaviour and/or features. For example, all model classes descend from class `org.dmg.pmml.Model`.

There is not much documentation accompanying class model classes. The application developer should consult with the [PMML specification] (http://www.dmg.org/v4-1/GeneralStructure.html) about individual PMML elements and attributes.

##### Example applications #####

* Copying an existing `org.dmg.pmml.PMML` instance from one file to another file: [CopyExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/CopyExample.java)
* Building a new `org.dmg.pmml.PMML` instance for the "golfing" decision tree model: [TreeModelBuilderExample.java] (https://github.com/jpmml/jpmml-example/blob/master/src/main/java/org/jpmml/example/TreeModelBuilderExample.java)

### Evaluation engine ###

A model evaluator class can be instantiated directly when the contents of the PMML document is known:
```java
PMML pmml = ...;

ModelEvaluator<TreeModel> modelEvaluator = new TreeModelEvaluator(pmml);
```

Otherwise, a PMML manager class should be instantiated first, which will inspect the contents of the PMML document and instantiate the right model evaluator class later:
```java
PMML pmml = ...;

PMMLManager pmmlManager = new PMMLManager(pmml);
 
ModelEvaluator<?> modelEvaluator = pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
```

Model evaluator classes follow functional programming principles. Model evaluator instances are cheap enough to be created and discarded as needed (ie. not worth the pooling effort).

It is advisable for application code to work against the `org.jpmml.evaluator.Evaluator` interface:
```java
Evaluator evaluator = (Evaluator)modelEvaluator;
```

An evaluator instance can be queried for the definition of active (ie. independent), predicted (ie. primary dependent) and output (ie. secondary dependent) fields:
```java
List<FieldName> activeFields = evaluator.getActiveFields();
List<FieldName> predictedFields = evaluator.getPredictedFields();
List<FieldName> outputFields = evaluator.getOutputFields();
``` 

The PMML scoring operation must be invoked with valid arguments. Otherwise, the behaviour of the model evaluator class is unspecified.

The preparation of field values:
```java
Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();

List<FieldName> activeFields = evaluator.getActiveFields();
for(FieldName activeField : activeFields){
	// The raw (ie. user-supplied) value could be any Java primitive value
	Object rawValue = ...;

	// The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
	FieldValue activeValue = evaluator.prepare(activeField, rawValue);

	arguments.put(activeField, activeValue);
}
```

The scoring:
```java
Map<FieldName, ?> results = evaluator.evaluate(arguments);
```

Typically, a model has exactly one predicted field, which is called the target field:
```java
FieldName targetName = evaluator.getTargetField();
Object targetValue = results.get(targetName);
```

The target value is either a Java primitive value (as a wrapper object) or an instance of `org.jpmml.evaluator.Computable`:
```java
if(targetValue instanceof Computable){
	Computable computable = (Computable)targetValue;

	Object primitiveValue = computable.getResult();
}
```

The target value may implement interfaces that descend from interface `org.jpmml.evaluator.ResultFeature`:
```java
// Test for "entityId" result feature
if(targetValue instanceof HasEntityId){
	HasEntityId hasEntityId = (HasEntityId)targetValue;
	HasEntityRegistry<?> hasEntityRegistry = (HasEntityRegistry<?>)evaluator;
	BiMap<String, ? extends Entity> entities = hasEntityRegistry.getEntityRegistry();
	Entity winner = entities.get(hasEntityId.getEntityId());

	// Test for "probability" result feature
	if(targetValue instanceof HasProbability){
		HasProbability hasProbability = (HasProbability)targetValue;
		Double winnerProbability = hasProbability.getProbability(winner.getId());
	}
}
```

##### Example applications #####

* Evaluating a PMML file interactively: [EvaluationExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/EvaluationExample.java)
* Evaluating a PMML file non-interactively with CSV file input: [CsvEvaluationExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/CsvEvaluationExample.java)

# Contact and Support #

Please use the e-mail displayed at [GitHub profile page] (https://github.com/jpmml)