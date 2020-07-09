/*
Basic Spark setup

The SparkSession object you create here is used as your connection between regular Scala
code and objects managed by Spark, potentially on a cluster
*/
val session = SparkSession.builder.appName("Simple ModelExample").getOrCreate()
import session.implicits._ // imports serializers for primitive datatypes in session

/*
Handling the data path
*/
val inputBasePath = "example_data"
val outputBasePath = "."
val trainingDataPath = inputBasePath + "/training.txt"
val testingDataPath = inputBasePath + "/testing.txt"
val currentOutputPath = outputBasePath + System.currentTimeMillis()

/*
Loading training and testing data

Next, you need to load these files into Spark’s in-memory representation of
datasets, called resilient distributed datasets (RDDs). RDDs are Spark’s core
abstraction. They provide enormous amounts of data in memory, spread across a 
cluster without you having to explicitly implement that distribution. In fact, 
RDDs can even handle what happens when some data disappears due to cluster nodes 
failing, again without you having to concern yourself with handling this failure.

- higer-order function
  - Spark will literally serialize this function and send it to all the nodes storing
    the data in the RDD. Shifting from sending data to functions to sending functions
    to data is one of the changes you make when using big data stacks like Hadoop and Spark.
- lazy
  - No data is read at the time that these commands are issued
*/
val trainingData = session.read.textFile(trainingDataPath)
val trainingParsed = trainingData.map { line =>
  val parts = line.split(',')
  // A LabeledPoint is a machine learning specific type designed to encode an instance
  // consisting of a feature vector and a concept.
  LabeldPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}.cache() // The cache method tells Spark you intend to reuse this data, so keep it in memory if possible.

val testingData = session.read.textFile(testingDataPath)
val testingParsed = testingData.map {
  val parts = line.split(',')
  LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}.cache()

/*
Training a model

Using linear regression
At this point, you’ve merely learned a model—you have no idea whether that model is good enough for the
critical problem of predicting future canine singing sensations.
*/
val numIterations = 100
val model = LinearRegressionWithSGD.train(trainingParsed.rdd, numIterations)

/*
Testing a model

To understand how useful this model is, you should now evaluate it on data that it hasn’t seen - the testing set.
*/
val predictionsAndLabels = testingParsed.map {
  case LabeledPoint(label, features) => val prediction = model.predict(features)
  (prediction, label)
}

/*
Model metrics
*/
val metrics = new MulticlassMetrics(predictionsAndLabels.rdd)
val precision = metrics.precision
val recall = metrics.recall
println(s"Precision: $precision Recall: $recall")

/*
Saving a  model
*/
model.save(session.SparkContext, currentOutputPath)