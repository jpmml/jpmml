library("pmml")
library("rpart")

data = readCsv("csv/Iris.csv")

rpart = rpart(Species ~ ., data = data)
saveXML(pmml(rpart), "pmml/DecisionTreeIris.pmml")

classes = predict(rpart, type = "class")
probabilities = predict(rpart, type = "prob")
writeCsv(formatIris(classes, probabilities), "csv/DecisionTreeIris.csv")