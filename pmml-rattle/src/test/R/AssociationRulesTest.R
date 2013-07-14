library("arules")
library("pmml")

shoppingData = readCsv("csv/Shopping.csv")

# Remove 2 duplicate rows
shoppingData = unique(shoppingData)

transactions = as(split(shoppingData[, "Product"], shoppingData[, "Transaction"]), "transactions")

apriori = apriori(transactions, parameter = list(support = 5 / 1000))

saveXML(pmml(apriori), "pmml/AssociationRulesShopping.pmml")