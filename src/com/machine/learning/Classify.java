package com.machine.learning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/*
 * Classify - This class does the machine learning work in the background
 * to match a User with a Company. The machine learning frame work named Weka
 * is used.
 */
public class Classify {

	/*
	 * Main - The backbone of the machine learning, this main method does an API
	 * call to create a filter.
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
		if (args.length < 2) {
			System.out.println("Need User String");
		} else {
			PrintWriter writer3 = new PrintWriter("the-file-name.txt", "UTF-8");
			writer3.println(args[0] + " hey " +args[1]);
			writer3.close();
			MongoClientURI uri = new MongoClientURI(
					"mongodb+srv://classifier:QFVpg1Gmn6phT4tH@toptalentcluster-m67po.mongodb.net/test?retryWrites=true"); // user:classifier
																															// password:
																															// QFVpg1Gmn6phT4tH
			MongoClient mongoClient = new MongoClient(uri);
			MongoDatabase database = mongoClient.getDatabase("TTdb");
			// Retrieving a collection
			MongoCollection<Document> TrainingData = database.getCollection("TrainingData");
			System.out.println("Collection TrainingData selected successfully");

			PrintWriter out = new PrintWriter(new FileWriter("Users.arff"));
			out.println(
					"@RELATION Users\n@ATTRIBUTE Answer	string\n@ATTRIBUTE Companies {Amazon,Raytheon,Cisco,two_sigma,Lockheed_Martin,BAE_Systems,Goldman_Sachs,Capital_One,Google,Facebook,Uber,Snapchat}\n\n@DATA");
			out.println("\"" + args[0] + "\", ?");
			out.close();

			PrintWriter out1 = new PrintWriter(new FileWriter("Companies.arff"));
			out1.println(
					"@RELATION Companies\n@ATTRIBUTE Answer	string\n@ATTRIBUTE Companies {Amazon,Raytheon,Cisco,two_sigma,Lockheed_Martin,BAE_Systems,Goldman_Sachs,Capital_One,Google,Facebook,Uber,Snapchat}\n\n@DATA");
			//
			FindIterable<Document> fi = TrainingData.find();
			MongoCursor<Document> cursor = fi.iterator();
			try {
				while (cursor.hasNext()) {
					String obj = cursor.next().toString().replaceAll("'", "");
					out1.println("\"" + obj.substring(47, obj.indexOf(", company=")) + "\", "
							+ obj.substring(obj.indexOf(", company=") + 10, obj.length() - 2));
				}
			} finally {
				cursor.close();
			}

			//
			out1.close();
			// Create Filter and Classifier
			StringToWordVector filter = new StringToWordVector();
			Classifier cls = new weka.classifiers.bayes.NaiveBayesMultinomial();

			// Create training and Test instances
			Instances train = new Instances(new BufferedReader(new FileReader("Companies.arff")));
			int lastIndex = train.numAttributes() - 1;
			train.setClassIndex(lastIndex);
			filter.setInputFormat(train);
			train = Filter.useFilter(train, filter);
			Instances test = new Instances(new BufferedReader(new FileReader("Users.arff")));
			test.setClassIndex(lastIndex);
			Instances test2 = Filter.useFilter(test, filter);

			// Build Classifier
			cls.buildClassifier(train);

			// Print prediction & percentage
			MongoCollection<Document> Users = database.getCollection("Users");
			for (int i = 0; i < test2.numInstances(); i++) {
				double index = cls.classifyInstance(test2.instance(i));
				String className = test.classAttribute().value((int) index);
				Bson filter1 = new Document("username", args[1]);
				Bson newValue = new Document("company", className);
				Bson updateOperationDocument = new Document("$set", newValue);
				Users.updateOne(filter1, updateOperationDocument);
				int sz = test.instance(i).toString().length();
				System.out.print(test.instance(i).toString().substring(1, sz - 3) + "....." + className);
				// push classname to users mongo
			}
			mongoClient.close();
		}
	}

	/*
	 * stopWords - This class is meant to prep the training data by removing the
	 * stop words in order to improve accuracy in NLP.
	 * 
	 * @param String: The paragraph that we are cleaning
	 * 
	 * @return String: The paragraph without the stopwords
	 */
	@SuppressWarnings("resource")
	static String stopWords(String sentence) {
		int count = 0;
		ArrayList<String> wordsList = new ArrayList<String>();
		String cLine;
		String[] stopwords = new String[100];
		try {
			FileReader fr = new FileReader("stopwords.txt"); // list of stopwords
			BufferedReader br = new BufferedReader(fr);
			while ((cLine = br.readLine()) != null) {
				stopwords[count] = cLine;
				count++;
			}
			String s = sentence;
			StringBuilder builder = new StringBuilder(s);
			String[] words = builder.toString().split("\\s");
			for (String word : words) {
				wordsList.add(word);
			}
			for (int i = 0; i < wordsList.size(); i++) {
				for (int j = 0; j < count; j++) {
					if (stopwords[j].contains(wordsList.get(i).toLowerCase())) {
						wordsList.set(i, "");
						break;
					}
				}
			}
			String sent = "";
			for (String str : wordsList) {
				if (!str.equals(""))
					sent = sent + str + " ";
			}
			return sent;
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return null;
	}

}
