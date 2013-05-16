library("nnet")
library("pmml")
library("rpart")

data = readCsv("csv/Iris.csv")

writeIris = function(classes, probabilities, file){
	result = data.frame(classes, probabilities)
	names(result) = c("Species", "Probability_setosa", "Probability_versicolor", "Probability_virginica")

	writeCsv(result, file)
}

generateDecisionTreeIris = function(){
	rpart = rpart(Species ~ ., data = data)
	saveXML(pmml(rpart), "pmml/DecisionTreeIris.pmml")

	classes = predict(rpart, type = "class")
	probabilities = predict(rpart, type = "prob")

	writeIris(classes, probabilities, "csv/DecisionTreeIris.csv")
}

generateNeuralNetworkOzone = function(){
	nnet = nnet(Species ~ ., data = data, size = 5)
	saveXML(pmml(nnet), "pmml/NeuralNetworkIris.pmml")

	classes = predict(nnet, type = "class")
	probabilities = predict(nnet, type = "raw")
	writeIris(classes, probabilities, "csv/NeuralNetworkIris.csv")
}

generateDecisionTreeIris()
generateNeuralNetworkOzone()