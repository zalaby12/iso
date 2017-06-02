package opt.test;

import dist.*;
import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.feedfwd.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying abalone as having either fewer 
 * or more than 15 rings. 
 *
 * @author Hannah Lau
 * @version 1.0
 */
/*
public class AbaloneTest {
    private static Instance[] instances = initializeInstances();

    private static final int NUMATTRIBUTES = 11;
    private static final int NUMLLINESINFILE = 3000;
    private static final int NUMCLASSES = 10;
    private static final int TRAIINGITER = 0;;
    private static final int NODESINHIDDENLAYER = 21;

    
    private static int inputLayer = NUMATTRIBUTES, hiddenLayer = NODESINHIDDENLAYER, outputLayer = NUMCLASSES, trainingIterations = TRAIINGITER; 
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(instances);
    private static Instance[] instancesTesting;
    private static Instance[] instancesTraining;

    private static FeedForwardNetwork networks[] = new FeedForwardNetwork[3];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[3];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[3];
    private static String[] oaNames = {"RHC", "SA", "GA"};
    private static DecimalFormat df = new DecimalFormat("0.000");

    
    public static void main(String[] args) {
        trainingIterations = Integer.parseInt(args[0]); 
        //testing and training dataset
        randomOrder();
        filterData(.90); //% of data for training
        
        for(int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                new int[] {inputLayer, hiddenLayer, outputLayer});
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }
        
        oa[0] = new RandomizedHillClimbing(nnop[0]);              //CHANGE THESE PARAMETERS
        oa[1] = new SimulatedAnnealing(1E11, .95, nnop[1]);
        oa[2] = new StandardGeneticAlgorithm(200, 100, 10, nnop[2]);

        for(int i = 0; i < oa.length; i++) {
            String results = "";
            String confusionMatrix = "";
            double ssee = 0;
                double mse, rmse;
            int[] distPredictions = new int[NUMCLASSES];
            int[] distActual = new int[NUMCLASSES];
            
            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
            train(oa[i], networks[i], oaNames[i]); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10,9);

            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            //double predicted, actual;
            start = System.nanoTime();
            for(int j = 0; j < instancesTesting.length; j++) {
                networks[i].setInputValues(instancesTesting[j].getData());
                networks[i].run();
                
                //determine the calculated class
                double [] predicted = toDoubleArray(networks[i].getOutputValues().toString().split(","));
                double[] output = toDoubleArray(instancesTesting[j].getLabel().toString().split(","));
                int predictedClass = indexOfMaxValue(predicted);
                int actualClass = indexOfMaxValue(output);
                
                //keep track of distribution of answers
                distPredictions[predictedClass]++;
                distActual[actualClass]++;                

                double trash = predictedClass == actualClass ? correct++ : incorrect++;
                if(predictedClass != actualClass) {
                  ssee++;
                }


            }
            end = System.nanoTime();
            testingTime = end - start;
            testingTime /= Math.pow(10,9);
            
            mse = ssee / instancesTesting.length;
            rmse = Math.pow(mse, .5);

            results =  "\nNumber of attributes is: " + NUMATTRIBUTES + ": \nTraining iterations: " + trainingIterations + 
                        "\nNodes in hidden layer:" + NODESINHIDDENLAYER + "\nNumber of classes:" + NUMCLASSES +
                        "\nResults for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct/(correct+incorrect)*100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n RMSE: " + rmse;
           
           for(int k = 0; k < distPredictions.length; k++) {
               confusionMatrix += "\nResults for " + oaNames[i] + 
                       "\nNumber predicted as " + (k) + " is: " + distPredictions[k] + 
                       " and actual is: " + distActual[k];
           }
            
           System.out.println(results);
           System.out.println(confusionMatrix);
                   
        }


    }
    
    private static int indexOfMaxValue(double [] arr) {
        int maxI= 0;
        double max = arr[0];
        
        for(int j = 1; j < arr.length; j++) {
            if(arr[j] > max) {
                max = arr[j];
                maxI = j;
            }
        }
        return maxI;
    }

    private static void randomOrder() {
        for (int i = instances.length-1; i > 0; i--) {
            int j = Distribution.random.nextInt(i + 1);
            Instance temp = instances[i];
            instances[i] = instances[j];
            instances[j] = temp;
        }
    }
    
    private static void filterData(double pctTrain) {
        int totalInstances = instances.length;
        int trainInstances = (int) (totalInstances * pctTrain);
        int testInstances  = totalInstances - trainInstances;
        instancesTraining = new Instance[trainInstances];
        instancesTesting  = new Instance[testInstances];
        for (int ii = 0; ii < trainInstances; ii++) {
            instancesTraining[ii] = instances[ii];
        }
        for (int ii = trainInstances; ii < totalInstances; ii++) {
            instancesTesting[ii - trainInstances] = instances[ii];
        }
    }
    
    
    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName) {
        System.out.println("\nError results for " + oaName + "\n---------------------------");

        for(int i = 0; i < trainingIterations; i++) {
            oa.train();

            double error = 0;
            for(int j = 0; j < instancesTraining.length; j++) {
                network.setInputValues(instancesTraining[j].getData());
                network.run();
                
                double [] predicted = toDoubleArray(network.getOutputValues().toString().split(","));
                double[] output = toDoubleArray(instancesTraining[j].getLabel().toString().split(","));
                double dotP = dotProduct(predicted, output);
                                               
                error += (.05 / dotP);
            }
        }
    }

    private static double[] toDoubleArray(String[] sArray) {
        double[] d = new double[sArray.length];
        int i = 0;
        for (String s: sArray){
            d[i] = Double.parseDouble(s);
            i++;
        }
        return d;
    }
    
    
    private static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
          sum += a[i] * b[i];
        }
        return sum;
      }
    
    
    //Reading in our TXT data. each row of data is saved as an element of instances array
    private static Instance[] initializeInstances() {

        double[][][] attributes = new double[NUMLLINESINFILE][][];

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("src/opt/test/3000_train.txt")));  
            //IS TRAINING & testing happening on same dataset?

            for(int i = 0; i < attributes.length; i++) {        
                Scanner scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[NUMATTRIBUTES]; 
                attributes[i][1] = new double[1];

                for(int j = 0; j < NUMATTRIBUTES; j++)  
                    attributes[i][0][j] = Double.parseDouble(scan.next());

                attributes[i][1][0] = Double.parseDouble(scan.next());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Instance[] instances = new Instance[attributes.length];

        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            
            //new stuff
            int c = (int) attributes[i][1][0];
            double[] classes = new double[NUMCLASSES];
            classes[c] = 1.0;
            instances[i].setLabel(new Instance(classes));
        }
        return instances;
    }
}
*/
