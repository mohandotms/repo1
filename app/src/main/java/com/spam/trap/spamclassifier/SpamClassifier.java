package com.spam.trap.spamclassifier;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;

import weka.core.*;
import weka.core.converters.ArffSaver;

import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

public class SpamClassifier {

    private FilteredClassifier classifier;
    private Instances trainData;
    private Instances testData;
    private ArrayList<Attribute> fvWekaAttributes;
    Context contextInstance;
    private static final String TAG = "SpamClassifier";
    public SpamClassifier(Context context){
        contextInstance=context;
        classifier = new FilteredClassifier();
        // Declare text attribute
        Attribute attribute_text = new Attribute("text",(List<String>) null);

        // Declare the label attribute along with its values
        ArrayList<String> classAttributeValues = new ArrayList<String>();
        classAttributeValues.add("spam");
        classAttributeValues.add("ham");
        Attribute classAttribute = new Attribute("label", classAttributeValues);

        // Declare the feature vector
        fvWekaAttributes = new ArrayList<Attribute>();
        fvWekaAttributes.add(classAttribute);
        fvWekaAttributes.add(attribute_text);

    }
    public Instances load (String filename)  throws IOException
    {
        BufferedReader br=null;
        Instances dataSet=null;
        try{
            br = new BufferedReader( new InputStreamReader(contextInstance.getAssets().open(filename)));
            dataSet = new Instances(br);
            br.close();
            dataSet.setClassIndex(0);
        }
//        catch(Exception e){
//
//        }
//
//        // http://geekswithblogs.net/razan/archive/2011/11/08/creating-a-simple-sparse-arff-file.aspx
//        // http://weka.wikispaces.com/Programmatic+Use
//
//        // // Declare text attribute
//        //  Attribute attribute_text = new Attribute("text",(List<String>) null);
//
//        //  // Declare the label attribute along with its values
//        //  ArrayList<String> classAttributeValues = new ArrayList<String>();
//        //  classAttributeValues.add("spam");
//        //  classAttributeValues.add("ham");
//        //  Attribute classAttribute = new Attribute("label", classAttributeValues);
//
//        //  // Declare the feature vector
//        //  ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
//        //  fvWekaAttributes.add(classAttribute);
//        //  fvWekaAttributes.add(attribute_text);
//
//					 /*
//					    Create an empty training set
//						name the relation “Rel”.
//						set intial capacity of 10*
//					*/
//        Instances dataset = new Instances("Rel", fvWekaAttributes, 10);
//
//        // Set class index
//        dataset.setClassIndex(0);
//        BufferedReader br=null;
//        // read text file, parse data and add to instance
//        try{
//            br = new BufferedReader( new InputStreamReader(contextInstance.getAssets().open(filename),"UTF-8"));
//            int count=500;
//            for(String line; (line = br.readLine()) != null; ) {
//                try{
//                    int idx=line.charAt(3)==','?3:4;
//                    line=line.substring(0,idx)+" "+line.substring(idx+1);
//
////                    System.out.println(line);
//                    // split at first occurance of n no. of words
//                    String parts[] = line.split("\\s+",2);
//                    // basic validation
//                    if (!parts[0].isEmpty() && !parts[1].isEmpty()){
//
//                        DenseInstance row = new DenseInstance(2);
//                        row.setValue(fvWekaAttributes.get(0), parts[0]);
//                        row.setValue(fvWekaAttributes.get(1), parts[1]);
//
//                        // add row to instances
//                        dataset.add(row);
//                    }
//                    //
//                }
//                catch (ArrayIndexOutOfBoundsException e){
//                    System.out.println("invalid row");
//                }
//                count--;
//                if(count==0) break;
//
//            }
//
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
        catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG,e.getMessage());
            Toast.makeText(contextInstance, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(TAG,e.getMessage());
                    Toast.makeText(contextInstance, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        return dataSet;

    }

    public void prepare() throws Exception{
        trainData = load("trainData.arff");
        testData = load("testData.arff");

    }

    public void transform(){

        // create the filter and set the attribute to be transformed from text into a feature vector (the last one)
        StringToWordVector filter = new StringToWordVector();
        filter.setAttributeIndices("last"); // sets which attribute to be worked on(text attribute)

        classifier.setFilter(filter);
        classifier.setClassifier(new NaiveBayes());
    }
    public void fit() throws Exception{
        classifier.buildClassifier(trainData);
    }

    public String classify(String text) throws Exception  {

        Instances newDataset = new Instances("testdata", fvWekaAttributes, 1);
        newDataset.setClassIndex(0);

        DenseInstance newinstance = new DenseInstance(2);
        newinstance.setDataset(newDataset);

        newinstance.setValue(fvWekaAttributes.get(1), text);

        double pred = classifier.classifyInstance(newinstance);

        System.out.println("===== Classified instance =====");
        System.out.println("Class predicted: " + trainData.classAttribute().value((int) pred));
        return trainData.classAttribute().value((int) pred);

        // try {
        // 	DenseInstance instance = new DenseInstance(2);
        // 	instance.setValue(new Attribute("text",(List<String>) null), text);
        // 	double pred = classifier.classifyInstance(instance);
        // 	System.out.println("===== Classified instance =====");
        // 	System.out.println("Class predicted: " + trainData.classAttribute().value((int) pred));
        // 	return trainData.classAttribute().value((int) pred);
        // }
        // catch (Exception e) {
        // 	System.out.println("Problem found when classifying the text");
        // }
        // return "";
    }

    public String evaluate() throws Exception{
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(classifier, testData);
//        System.out.println(eval.toSummaryString());
        return eval.toSummaryString();
    }


    public void saveArff(Instances dataset,String filename)   throws IOException{
        try
        {
            // initialize
            ArffSaver arffSaverInstance = new ArffSaver();
            arffSaverInstance.setInstances(dataset);
            arffSaverInstance.setFile(new File(filename));
            arffSaverInstance.writeBatch();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws Exception{
//
//        SpamClassifier wt = new SpamClassifier();
//
//        wt.classify("goldviking (29/M) is inviting you to be his friend. Reply YES-762 or NO-762 See him: www.SMS.ac/u/goldviking STOP? Send STOP FRND to 62468");
//        wt.classify("Congratulation! You've won a $1.000 Walmart gift card. Go to http://bit.ly/12345");
//        wt.classify("Your IRS tax refund is pending acceptance . must accept within 24 hours http://bit.ly/sdfsdf.");
//        wt.classify("Amazon is sending you a refund of $32.64. Please reply with your bank account an =d routing number to receive your refund");
//        wt.classify("Wells Fargo Bank: Your account is temporarily locked . Please log in at http://goo.gl/2a234 to secure your account.");
//        wt.classify("Hello , your FEDEX package with tracking code DZ-8342-FY34 is waiting for you to set delivery preferences: c4lrs.info/Gm08s43vz1");
//        wt.classify("Apple notification. your Apple iCloud ID expires today. Log i to prevent deletion http://apple.id/user-auth/online");
//        wt.classify("((Coinbase)) Amount received 2.2221 Bitcoin BTC($18,421 USD) Please confirm transaction http://bit.do/Coinbase432194-53242");
//        wt.classify("URGENT! Need bail money immediately Western Union Wire $9,500 http://goo.gl/ndf4g5");
//        // Instances trainData = wt.load("data/train.txt");
//        // System.out.println(trainData);
////              1,1,0,1,1,1,0,0,0
//
//        // // create the filter and set the attribute to be transformed from text into a feature vector (the last one)
//        // StringToWordVector filter = new StringToWordVector();
//        // filter.setAttributeIndices("last");
//
//        // FilteredClassifier classifier = new FilteredClassifier();
//        // classifier.setFilter(filter);
//        // classifier.setClassifier(new NaiveBayes());
//
//        // classifier.buildClassifier(trainData);
//
//        // System.out.println(WekaTransformer.classify("I said its okay. Sorry"));
//
//					/*
//					  Now that we create and trained a classifier, let’s test it.
//					  To do so, we need an evaluation module (weka.classifiers.Evaluation) to which we feed our testing set
//					*/
//
//        // Instances testData = wt.transform("data/test.txt");
//        // evaluation set
//        // Evaluation eval = new Evaluation(testData);
//        // eval.evaluateModel(classifier, testData);
//        // System.out.println(eval.toSummaryString());
//
//
//
//
//
//
//    }
}