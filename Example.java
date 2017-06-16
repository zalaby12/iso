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

    public static void main(String[] args) {

        try {

            //trying another, hopefully easier way, of reading/writing wav files for right now. 
      
            System.out.println("\n********************");
            System.out.println("reading in wav files...");

            double[] snareInput = StdAudio.read("SNARE_mixed.wav");
            double[] overheadInput = StdAudio.read("Overhead_mixed.wav");

            double[] snareOutput = new double[snareInput.length];
            double[] overheadOutput = new double[overheadInput.length];

    
            //this is just some really nice information to see... we don't have to keep this, but it might be good to use in the future
            WavFile snareWavFile = WavFile.openWavFile(new File("SNARE_mixed.wav"));
            WavFile overheadWavFile = WavFile.openWavFile(new File("Overhead_mixed.wav"));
            System.out.println("");
            overheadWavFile.display();
            System.out.println("");
            snareWavFile.display();
            snareWavFile.close();
            overheadWavFile.close();

            int bufferSize = 100;
            int bufferLimit = 0;
            int index = 0;

            long startTime = System.currentTimeMillis();

            while(index < snareInput.length) {                //while there are still samples left to read

        
                ArrayList<Double> snareSamples = new ArrayList<>();
                ArrayList<Double> overheadSamples = new ArrayList<>();

                int samplesLeft;
                if(snareInput.length - index < 100) {           //first check if there are enough to fill the whole virtual buffer
                    samplesLeft = snareInput.length - index;      //and if not, just use the amount of samples left
                } else {
                    samplesLeft = bufferSize;                     //otherwise just use a full virtual buffer 
                }

                int nonZeroCount = 0;
                for(int i = 0; i < samplesLeft; i++) {          //fill the virtual buffers
                    if((snareInput[index] != 0) && (overheadInput[index] != 0)) { //short circuit if we already know to save us some time...
                        nonZeroCount++;
                    }
                    snareSamples.add(snareInput[index]); 
                    overheadSamples.add(overheadInput[index++]);  //don't forget to increment index!
                }

    
                boolean majorityNonZeroSamples = (nonZeroCount >= (samplesLeft / 2));                      //this is for whitening to know what's up... assume nonzero samples at the beginning
                int outputIndex = 0;
            
                if(majorityNonZeroSamples) {
                    SimpleDataSet source;
                    List<DataPoint> dataPoints = new ArrayList<DataPoint>();
                    Vec snareVector = new DenseVector(snareSamples);
                    Vec ohVector = new DenseVector(overheadSamples);
                    dataPoints.add(new DataPoint(snareVector));
                    dataPoints.add(new DataPoint(ohVector));
                    source = new SimpleDataSet(dataPoints);
                    //this is a copy for later comparison.
                    SimpleDataSet sourceCopy = new SimpleDataSet(source.getBackingList());

                    //DO THE DAMN THING
                    //currently, whitening the data produces a problem... might have something to do with it being 0's???
                    //
                    //do we just need to simply place them all back if they're all zeros...????
                    FastICA ica = new FastICA(source, 2, FastICA.DefaultNegEntropyFunc.KURTOSIS, false);
                    source.applyTransform(ica);
        
                    int[] ranks = Find.theMatchingWavFile(sourceCopy, source);
                    //so...samples were non-negative... place them back
                    

                    for(int i = 0; i < samplesLeft; i++) {
                        snareOutput[outputIndex] = source.getDataPoint(ranks[0]).getValue(outputIndex);
                        overheadOutput[outputIndex] = source.getDataPoint(ranks[1]).getValue(outputIndex++);
                    }

                } else {
                    //all samples were zeros... just write whatever to whatever.
                    //i think this will end up being redundant... should we pass all zero's to ICA...??
                    for(int i = 0; i < samplesLeft; i++) {
                        snareOutput[outputIndex] = 0;
                        overheadOutput[outputIndex++] = 0;
                    }
                }
            }

    
            System.out.println("********************");
            System.out.println("Writing to files...");

            StdAudio.save("SNARE_FINAL.wav", snareOutput);
            System.out.println("Wrote SNARE_FINAL.wav");

            StdAudio.save("OH_FINAL.wav", overheadOutput);
            System.out.println("Wrote OH_FINAL.wav");


    
            System.out.println("Done.");
            System.out.println("********************");

            long finishTime = (System.currentTimeMillis() - startTime);
            double elapsedICA = finishTime / 1000.0; 
            System.out.println("Running ICA took " + elapsedICA + " seconds");
    

        } catch (Exception e) {

            System.out.println("there was an exception");
            System.out.println(e);

        }



    }  


}
