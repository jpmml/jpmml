library("pmml")

data = readCsv("csv/Ozone.csv")

lm = lm(O3 ~ temp + ibh + ibt, data = data)
saveXML(pmml(lm), "pmml/RegressionOzone.pmml")

values = predict.lm(lm)
writeCsv(formatOzone(values), "csv/RegressionOzone.csv")