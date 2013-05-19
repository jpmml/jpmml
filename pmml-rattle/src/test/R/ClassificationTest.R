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

	classes = predict(nnet, type = "class")
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