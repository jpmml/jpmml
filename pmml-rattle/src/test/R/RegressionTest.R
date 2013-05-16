library("nnet")
library("pmml")

data = readCsv("csv/Ozone.csv")

writeOzone = function(values, file){
	result = data.frame(values)
	names(result) = c("O3")

	writeCsv(result, file)
}

generateNeuralNetworkOzone = function(){
	nnet = nnet(O3 ~ temp + ibh + ibt, data = data, size = 4, decay = 1e-3, maxit = 10000, linout = TRUE)
	saveXML(pmml(nnet), "pmml/NeuralNetworkOzone.pmml")

	writeOzone(predict(nnet), "csv/NeuralNetworkOzone.csv")
}

generateRegressionOzone = function(){
	lm = lm(O3 ~ temp + ibh + ibt, data = data)
	saveXML(pmml(lm), "pmml/RegressionOzone.pmml")
	
	writeOzone(predict(lm), "csv/RegressionOzone.csv")
}

generateNeuralNetworkOzone()
generateRegressionOzone()