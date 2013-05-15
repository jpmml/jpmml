library("nnet")
library("pmml")

data = readCsv("csv/Ozone.csv")

nnet = nnet(O3 ~ temp + ibh + ibt, data = data, size = 4, decay = 1e-3, maxit = 10000, linout = TRUE)
saveXML(pmml(nnet), "pmml/NeuralNetworkOzone.pmml")

values = predict(nnet)
writeCsv(formatOzone(values), "csv/NeuralNetworkOzone.csv")