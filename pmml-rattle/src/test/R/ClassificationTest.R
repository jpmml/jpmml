library("nnet")
library("pmml")
library("rpart")

irisData = readCsv("csv/Iris.csv")
irisFormula = formula(Species ~ .)

writeIris = function(classes, probabilities, file){
	result = data.frame(classes, probabilities)
	names(result) = c("Species", "Probability_setosa", "Probability_versicolor", "Probability_virginica")

	writeCsv(result, file)
}

generateDecisionTreeIris = function(){
	rpart = rpart(irisFormula, irisData)
	saveXML(pmml(rpart), "pmml/DecisionTreeIris.pmml")

	classes = predict(rpart, type = "class")
	probabilities = predict(rpart, type = "prob")
	writeIris(classes, probabilities, "csv/DecisionTreeIris.csv")
}

generateNeuralNetworkIris = function(){
	nnet = nnet(irisFormula, irisData, size = 5)
	saveXML(pmml(nnet), "pmml/NeuralNetworkIris.pmml")

	classes = predict(nnet, type = "class", decay = 1e-3, maxit = 10000)
	probabilities = predict(nnet, type = "raw")
	writeIris(classes, probabilities, "csv/NeuralNetworkIris.csv")
}

generateRegressionIris = function(){
	multinom = multinom(irisFormula, irisData)
	saveXML(pmml(multinom), "pmml/RegressionIris.pmml")

	classes = predict(multinom)
	probabilities = predict(multinom, type = "probs")
	writeIris(classes, probabilities, "csv/RegressionIris.csv")
}

generateDecisionTreeIris()
generateNeuralNetworkIris()
generateRegressionIris()

auditData = readCsv("csv/Audit.csv")
auditData[, "Adjusted"] = as.factor(auditData[, "Adjusted"])
auditFormula = formula(Adjusted ~ Employment + Education + Marital + Occupation + Income + Gender + Deductions + Hours)

writeAudit = function(classes, probabilities, file){
	result = NULL

	if(is.null(probabilities)){
		result = data.frame(classes)
		names(result) = c("Adjusted")
	} else

	{
		result = data.frame(classes, probabilities)
		names(result) = c("Adjusted", "Probability_0", "Probability_1")
	}

	writeCsv(result, file)
}

generateDecisionTreeAudit = function(){
	rpart = rpart(auditFormula, auditData, method = "class")
	saveXML(pmml(rpart), "pmml/DecisionTreeAudit.pmml")

	classes = predict(rpart, type = "class")
	probabilities = predict(rpart, type = "prob")
	writeAudit(classes, probabilities, "csv/DecisionTreeAudit.csv")
}

generateNeuralNetworkAudit = function(){
	nnet = nnet(auditFormula, auditData, size = 9, decay = 1e-3, maxit = 10000)
	saveXML(pmml(nnet), "pmml/NeuralNetworkAudit.pmml")

	classes = predict(nnet, type = "class")	
	writeAudit(classes, NULL, "csv/NeuralNetworkAudit.csv")
}

generateDecisionTreeAudit()
generateNeuralNetworkAudit()