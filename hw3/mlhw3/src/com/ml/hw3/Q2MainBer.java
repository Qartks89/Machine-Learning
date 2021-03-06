package com.ml.hw3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Q2MainBer {

	public static void main(String[] args) throws Exception {
		DataSet allData = DataInputer.getData("/Users/kartikeyashukla/Desktop/Masters/Machine Learning/HW3/spambase.data");
		FeatureStats fStats =  DataInputer.getFeatureStatsFromFile("/Users/kartikeyashukla/Desktop/Masters/Machine Learning/HW3/spambase-stats.data");
		allData.setFeatureStat(fStats); 
		errorWithKFold(allData, 10);
//		getROCData(allData);
		System.out.println();
	}

	
	private static void getROCData(DataSet dataSet) {

		int dataPerFold = dataSet.getDataSize()/ 10;
		
		Collections.shuffle(dataSet.getData());
		
		for(int fold=0; fold< 10 ; fold++) {
			
			DataSet trainingData = new DataSet(dataSet.getFeatureSize());
			DataSet testData = new DataSet(dataSet.getFeatureSize());
			
			trainingData.setFeatureStat(dataSet.getFeatureStat());
			testData.setFeatureStat(dataSet.getFeatureStat());
			
			NaiveBayesBernoulli nbb = new NaiveBayesBernoulli();
			List<Double> threshList = null;
			
			for(int x = 0; x < dataSet.getDataSize(); x++) {
				Data d = dataSet.getData().get(x);
				if(x >= fold * dataPerFold && x < (fold+1)*dataPerFold) {
					testData.addData(d);
					d.setDataSet(testData);
				} else {
					trainingData.addData(d);
					d.setDataSet(trainingData);
				}
			}
			
			NBBStats nbStat = nbb.train(trainingData);
			
			ErrorStat errTest = nbb.test(nbStat, testData, 0);
//			System.out.println(errTest.toString());
			threshList =  new ArrayList<Double>(nbb.rocData);
			Collections.sort(threshList);
			
			for(Double thres: threshList){
				ErrorStat errTest2 = nbb.test(nbStat, testData, thres);
				System.out.println(errTest2.getFPRate() + "\t" + errTest2.getTPRate());
			}

			if(fold == 0)
				break;
			
		}
		
		
	}


private static void errorWithKFold(DataSet dataSet, int k) {
		
		double avgTestError = 0;
		double avgTrainError = 0;
		ErrorStat avgError = new ErrorStat();
		int dataPerFold = dataSet.getDataSize()/ k;
		
		
		for(int fold=0; fold< k ; fold++) {
			
			Collections.shuffle(dataSet.getData());
			double error = 0;
			
			DataSet trainingData = new DataSet(dataSet.getFeatureSize());
			DataSet testData = new DataSet(dataSet.getFeatureSize());
			
			trainingData.setFeatureStat(dataSet.getFeatureStat());
			testData.setFeatureStat(dataSet.getFeatureStat());
			
			NaiveBayesBernoulli nbb = new NaiveBayesBernoulli();
			
			for(int x = 0; x < dataSet.getDataSize(); x++) {
				Data d = dataSet.getData().get(x);
				if(x >= fold * dataPerFold && x < (fold+1)*dataPerFold) {
					testData.addData(d);
					d.setDataSet(testData);
				} else {
					trainingData.addData(d);
					d.setDataSet(trainingData);
				}
			}
			
			NBBStats nbStat = nbb.train(trainingData);
			ErrorStat err = nbb.test(nbStat, trainingData, 0.2);
			avgTrainError += err.getAccuracy();
			System.out.println("Training Error For Fold " + (fold+1) + " -> " + err.getAccuracy());
			
			ErrorStat errTest = nbb.test(nbStat, testData, 0.2);
			avgTestError += errTest.getAccuracy();
			System.out.println("Testing Error For Fold " + (fold+1) + " -> " + errTest.getAccuracy());
			System.out.println("Prob Error Rate -> " + errTest.toString());
			System.out.println();
			avgError.incrementErrorRates(errTest.errR);
		}
		
		for(int i = 0; i< 4; i++){
			avgError.errR[i] /= k;
		}
		
		System.out.println();
		System.out.println("Average Training Error -> " + avgTrainError/k);
		System.out.println("Average Testing Error -> " + avgTestError/k);
		System.out.println("Average Error Rate" + avgError.toString());
		
	}
	
}
