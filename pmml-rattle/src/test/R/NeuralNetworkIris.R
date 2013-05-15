library("nnet")

data = readCsv("csv/Iris.csv")

nnet = nnet(Species ~ ., data = data, size = 5)
saveXML(pmml(nnet), "pmml/NeuralNetworkIris.pmml")

classes = predict(nnet, type = "class")
probabilities = predict(nnet, type = "raw")
writeCsv(formatIris(classes, probabilities), "csv/NeuralNetworkIris.csv")