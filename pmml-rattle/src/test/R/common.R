readCsv = function(file){
	return (read.csv(file = file, header = TRUE))
}

writeCsv = function(data, file){
	write.table(data, file = file, sep = ",", quote = FALSE, row.names = FALSE, col.names = TRUE)
}

formatIris = function(classes, probabilities){
	result = data.frame(classes, probabilities)
	names(result) = c("Species", "Probability_setosa", "Probability_versicolor", "Probability_virginica")

	return (result)
}

formatOzone = function(values){
	result = data.frame(values)
	names(result) = c("O3")

	return (result)
}