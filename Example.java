import shared.Instance;
import shared.DataSet;
import shared.filt.IndependentComponentAnalysis;
import util.linalg.Matrix;
import util.linalg.RectangularMatrix;

import java.io.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.util.ArrayList;
import java.util.List;


import jsat.datatransform.FastICA;
import jsat.SimpleDataSet;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.DataPoint;
import jsat.linear.*;


public class Example {

  private static void print(DataSet set) {

    for(int sample = 0; sample < set.size(); sample++) {
      Instance temp = set.get(sample);
      System.out.println(temp);
    }

  }

  public static void main(String[] args) {

    try {

      //the display method shows useful information...we might have to redo this -_-
      WavFile snareWavFile = WavFile.openWavFile(new File("SNARE_mixed.wav"));
//      WavFile hihatWavFile = WavFile.openWavFile(new File("HiHat.wav"));
      WavFile overheadWavFile = WavFile.openWavFile(new File("Overhead_mixed.wav"));

//      hihatWavFile.display();
      System.out.println("");
      overheadWavFile.display();
      System.out.println("");
      snareWavFile.display();

      int numChannelsSnare = snareWavFile.getNumChannels();
      int numChannelsOverhead = overheadWavFile.getNumChannels();
//      int numChannelsHihat = hihatWavFile.getNumChannels();
      double[] snareBuffer = new double[100 * numChannelsSnare];
      double[] overheadBuffer = new double[100 * numChannelsOverhead];
//      double[] hihatBuffer = new double[100 * numChannelsHihat];

      //this is constant for all of them
      int framesRead;



      System.out.println("\n********************");
      System.out.println("reading in wav files...");

      int sampleRate = 44100;
      double duration = 1.0;
      long numFrames = (long) duration * sampleRate;

      WavFile snareOutput = WavFile.newWavFile(new File("snareOutput.wav"), 1, numFrames, 24, sampleRate);
      WavFile ohOutput = WavFile.newWavFile(new File("ohOutput.wav"), 1, numFrames, 24, sampleRate);

      

      double time = 0.0;
      int ONE_SECOND = 100;
      do{
        
        framesRead = snareWavFile.readFrames(snareBuffer, 100);
        framesRead = overheadWavFile.readFrames(overheadBuffer, 100);
        ArrayList<Double> snareSamples = new ArrayList<>();
        ArrayList<Double> overheadSamples = new ArrayList<>();

        for(int i = 0; i < framesRead * numChannelsSnare; i++) {
          
            snareSamples.add(snareBuffer[i]);
            overheadSamples.add(overheadBuffer[i]);

        }
            
        SimpleDataSet source;
        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
        Vec snareVector = new DenseVector(snareSamples);
        Vec ohVector = new DenseVector(overheadSamples);
        dataPoints.add(new DataPoint(snareVector));
        dataPoints.add(new DataPoint(ohVector));
        source = new SimpleDataSet(dataPoints);
        //this is a copy for later comparison.
        SimpleDataSet sourceCopy = new SimpleDataSet(source.getBackingList());


        System.out.println("\nrunning ICA...");

        long startTime = System.currentTimeMillis();

        FastICA ica = new FastICA(source, 2);

        long finishTime = (System.currentTimeMillis() - startTime);
        double elapsedICA = finishTime / 1000.0; 

        System.out.println("Done.\ntransforming with results...");
    
  
        startTime = System.currentTimeMillis();
        source.applyTransform(ica);
        finishTime = System.currentTimeMillis() - startTime;

        double elapsedTransform = finishTime / 1000.0;
    
        System.out.println("Done.");

        System.out.println("Running ICA took " + elapsedICA + " seconds");
        System.out.println("Data transformation took " + elapsedTransform + " seconds");


        int[] ranks = Find.theMatchingWavFile(sourceCopy, source);
            




        time += 1.0;


      } while(framesRead != 0 && time < duration);

      snareWavFile.close();
      overheadWavFile.close();


      System.out.println("Done.");
      System.out.println("********************");


/*
    Instance[] instances = new Instance[snareSamples.size()];
    int done = 0;
    System.out.print("\n*");
    for(int i = 0; i < instances.length; i++, done++) {
      if(instances.length / 10 < done) {
        done = 0;
        System.out.print("*");
      }

      double[] data = new double[2];
      data[0] = snareSamples.get(i);
      data[1] = overheadSamples.get(i);
      instances[i] = new Instance(data);
    }
*/

    //this is all just loading things into the source matrix



/*

    DataSet set = new DataSet(instances);
    System.out.println("\n\nBefore randomizing");
    print(set);
//    System.out.println(set);
    Matrix projection = new RectangularMatrix(new double[][]{ {.4, .4}, {.6, .6}});
    for(int i = 0; i < set.size(); i++) {
      Instance instance = set.get(i);
      instance.setData(projection.times(instance.getData()));
    }

    System.out.println("BEFORE ICA");
//    System.out.println(set);

    System.out.println("filtering...");
    IndependentComponentAnalysis filter = new IndependentComponentAnalysis(set, 2);
    filter.filter(set);

    System.out.println("AFTER ICA");
//    System.out.println(set);
    
    print(set);


*/

    } catch (Exception e) {

      System.out.println("there was an exception");
      System.out.println(e);

    }



  }


}
