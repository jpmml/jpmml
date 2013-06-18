Java API for producing and scoring models in Predictive Model Markup Language (PMML).

# Features #

Supported model types:
<table>
	<tr><th>Description</th><th>PMML element</th></tr>
	<tr><td>Linear regression</td><td>http://www.dmg.org/v4-1/Regression.html</td></tr>
	<tr><td>Decision tree</td><td>http://www.dmg.org/v4-1/TreeModel.html</td></tr>
	<tr><td>Neural network</td><td>http://www.dmg.org/v4-1/NeuralNetwork.html</td></tr>
	<tr><td>Ensembles models</td><td>http://www.dmg.org/v4-1/MultipleModels.html</td></tr>
</table>

# Usage #

JPMML library JAR files (together with accompanying Java source and Javadocs JAR files) are released via [Maven Central Repository] (http://repo1.maven.org/maven2/org/jpmml/).

Release history:
<table>
	<tr><th>Date</th><th>Version</th></tr>
	<tr><td>16 Jun, 2013</td><td>1.0.6</td></tr>
	<tr><td>5 Jun, 2013</td><td>1.0.5</td></tr>
	<tr><td>2 Jun, 2013</td><td>1.0.4</td></tr>
	<tr><td>27 May, 2013</td><td>1.0.3</td></tr>
	<tr><td>21 May, 2013</td><td>1.0.2</td></tr>
	<tr><td>20 May, 2013</td><td>1.0.1</td></tr>
	<tr><td>2 Dec, 2012</td><td>1.0.0</td></tr>
</table>

The latest versions of public API modules can be incorporated using the following dependency declarations:
```
<!-- low-level API -->
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-model</artifactId>
	<version>1.0.6</version>
</dependency>
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-schema</artifactId>
	<version>1.0.6</version>
</dependency>

<!-- medium-level API -->
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-manager</artifactId>
	<version>1.0.6</version>
</dependency>

<!-- high-level API -->
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-evaluator</artifactId>
	<version>1.0.6</version>
</dependency>
```
Please note that higher API levels depend on lower API levels.

# Modules #

## Class model (1/3) ##

Low-level API module. Provides JAXB-driven class model, which corresponds to the latest [PMML XML Schema version 4.1] (http://www.dmg.org/v4-1/pmml-4-1.xsd).

Different PMML XML Schema versions are compatible with one another. Older PMML version 3.X and 4.X documents can be converted to the latest PMML version 4.1 documents simply by performing XML namespace substitution. The `org.dmg.pmml.IOUtil` utility class performs this conversion automatically when unmarshalling.

Conversely, the latest PMML version 4.1 documents can be converted to older PMML version documents by similar means. The minimum supported version is determined by `org.dmg.pmml.Schema` class model annotations.

### Components

##### pmml-model (public)

Class models classes.

The class model consists of two types of classes. There is a small number of manually crafted classes that are used for structuring the class hierarchy. They are permanently stored in the Java sources directory of the module `src/main/java`. Additionally, there is a much greater number of automatically generated classes that represent actual PMML elements. They can be found in the generated Java sources directory of the module `target/generated-sources/xjc` after a successful build operation.

All class model classes descend from class `org.dmg.pmml.PMMLObject`. Additional class hierarchy levels, if any, represent common behaviour and/or features. For example, model classes descend from class `org.dmg.pmml.Model`.

There is not much documentation accompanying class model classes. The application developer should consult with the PMML specification about individual PMML elements and attributes.

##### pmml-schema (public)

Class model annotations.

##### xjc (internal)

JAXB compiler (XJC) plugins.

### Example applications

* Copying a live `org.dmg.pmml.PMML` instance from one file to another file: [CopyExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/CopyExample.java)


## PMML document and model manipulation (2/3) ##

Medium-level API module. Provides manager classes for dealing with class model classes.

### Components

##### pmml-manager (public)

Provides class `org.jpmml.manager.PMMLManager`. Additionally, provides a subclass of `org.jpmml.manager.ModelManager` for every supported model type.

PMML model producers must work with the specific model manager class. PMML model consumers may choose to work with interface `org.jpmml.manager.Consumer` instead.

### Example code

Constructing an instance of `org.jpmml.manager.TreeModelManager` for a PMML document that **is known to contain** a decision tree model:
```
PMML pmml = ...

TreeModelManager treeModelManager = new TreeModelManager(pmml);
```

Obtaining an instance of `org.jpmml.manager.ModelManager` for a PMML document whose contents is unknown:
```
PMML pmml = ...

PMMLManager pmmlManager = new PMMLManager(pmml);

// A single PMML document may contain multiple models in which case it will be necessary to provide the name of the model (instead of null)
ModelManager modelManager = pmmlManager.getModelManager(null, ModelManagerFactory.getInstance());
```

### Example applications

* Printing the control structure of a TreeModel in a Java-like pseudocode: [TreeModelTranslationExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/TreeModelTranslationExample.java)


## PMML model evaluation (3/3) ##

High-level API module. Provides evaluator classes for scoring models in "interpreted mode".

### Components

##### pmml-evaluator (public)

Provides a subclass of `org.jpmml.manager.ModelManager` for every supported model type.

PMML model consumers may choose to work with interface `org.jpmml.evaluator.Evaluator` instead.

##### pmml-knime (internal)

Functional tests for [KNIME] (http://www.knime.org/) open source software.

Tested model types:
<table>
	<tr><th>Description</th></tr>
	<tr><td>Linear regression</td></tr>
	<tr><td>Decision tree</td></tr>
	<tr><td>Neural network</td></tr>
</table>

##### pmml-rattle (internal)

Functional tests for R (http://www.r-project.org/) and Rattle (http://rattle.togaware.com/) open source software.

Tested model types:
<table>
	<tr><th>Description</th><th>R function(s)</th></tr>
	<tr><td>Linear regression</td><td>`lm()` and `multinom()`</td></tr>
	<tr><td>Decision tree</td><td>`rpart()` (package `rpart`)</td></tr>
	<tr><td>Neural network</td><td>`nnet()` (package `nnet`)</td></tr>
	<tr><td>Random forest</td><td>`randomForest()` (package `randomForest`)</td></tr>
</table>

### Example code

Constructing an instance of `org.jpmml.evaluator.TreeModelEvaluator` for a PMML document that **is known to contain** a decision tree model:
```
PMML pmml = ...

TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(pmml);
```

Obtaining an instance of `org.jpmml.manager.ModelManager`, which is then immediately cast to an instance of `org.jpmml.evaluator.Evaluator`, for a PMML document whose contents is unknown:
```
PMML pmml = ...

PMMLManager pmmlManager = new PMMLManager(pmml);

ModelManager modelManager = pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

// This cast is sound, because the ModelManager was obtained from ModelEvaluatorFactory (not ModelManagerFactory as in the previous section)
Evaluator evaluator = (Evaluator)modelManager;
```

Using the instance of `org.jpmml.evaluator.Evaluator` for the preparation of input values and scoring:
```
Evaluator evaluator = ...

Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

List<FieldName> activeFields = evaluator.getActiveFields();
for(FieldName activeField : activeFields){
	// The raw (ie. user-supplied) value could be any Java primitive value
	Object rawValue = ...

	// The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
	Object activeValue = evaluator.prepare(activeField, rawValue);

	parameters.put(activeField, activeValue);
}

Map<FieldName, ?> result = evaluator.evaluate(parameters);
```

### Example applications

* Evaluating a PMML file interactively: [EvaluationExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/EvaluationExample.java)
* Evaluating a PMML file non-interactively with CSV file input: [CsvEvaluationExample.java] (https://github.com/jpmml/jpmml-example/tree/master/src/main/java/org/jpmml/example/CsvEvaluationExample.java)

# Contact and Support #

Please use the e-mail displayed at [GitHub profile page] (https://github.com/jpmml)