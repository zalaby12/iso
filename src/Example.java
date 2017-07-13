import java.io.*;
import java.util.ArrayList;
import java.util.List;


import jsat.datatransform.FastICA;
import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.linear.*;
import shared.Instance;


public class Example {

    private static double[] snareInput, overheadInput, snareOutput, overheadOutput;
    private static int bufferSize, index, outputIndex, nonZeroCount, samplesLeft;
    private static long startTime;
    private static ArrayList<Double> snareSamples, overheadSamples;

    private static boolean jsat = true;


    public static void main(String[] args) {
        //TODO -> add checking for sample size and make sure they're the same... no need to attempt to guess what someone else recorded and align it right
        try {
            init();
            while(samplesStillNeedProcessing()) {
                samplesLeft = assignSamplesLeft();
                nonZeroCount = 0;
                insertSamples();
                boolean majorityNonZeroSamples = (nonZeroCount >= (samplesLeft / 2));                      //this is for whitening to know what's up... assume nonzero samples at the beginning
                if(majorityNonZeroSamples) {
                    if(jsat) {
                        runJsatIca();
                    } else {
                        runAbbyIca();
                    }
                } else {
                    outputZeros();
                }
            }
            end();
        } catch (Exception e) {
            System.out.println("there was an exception");
            e.printStackTrace();
        }
    }


    private static void init() throws Exception{
        //trying another, hopefully easier way, of reading/writing wav files for right now.

        System.out.println("\n********************");
        System.out.println("reading in wav files...");


        snareInput = StdAudio.read("SNARE_mixed.wav");
        overheadInput = StdAudio.read("Overhead_mixed.wav");

        snareOutput = new double[snareInput.length];
        overheadOutput = new double[overheadInput.length];

        //this is just some really nice information to see... we don't have to keep this, but it might be good to use in the future
        WavFile snareWavFile = WavFile.openWavFile(new File("SNARE_mixed.wav"));
        WavFile overheadWavFile = WavFile.openWavFile(new File("Overhead_mixed.wav"));
        System.out.println("");
        overheadWavFile.display();
        System.out.println("");
        snareWavFile.display();
        snareWavFile.close();
        overheadWavFile.close();

        bufferSize = 100;
        index = 0;
        outputIndex = 0;

        startTime = System.currentTimeMillis();

    }

    private static void end() {

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
    }

    private static boolean samplesStillNeedProcessing() {
        return index < snareInput.length;
    }

    private static int assignSamplesLeft() {
        if(snareInput.length - index < 100) {           //first check if there are enough to fill the whole virtual buffer
            return snareInput.length - index;           //and if not, just use the amount of samples left
        } else {
            return bufferSize;                          //otherwise just use a full virtual buffer
        }
    }

    private static void insertSamples() {
        snareSamples = new ArrayList<>();
        overheadSamples = new ArrayList<>();
        nonZeroCount = 0;
        for(int i = 0; i < samplesLeft; i++) {          //fill the virtual buffers
            if((snareInput[index] != 0) || (overheadInput[index] != 0)) { //short circuit if we already know to save us some time...
                nonZeroCount++;
            }
            snareSamples.add(snareInput[index]);
            overheadSamples.add(overheadInput[index++]);  //don't forget to increment index!
        }
    }

    private static void runJsatIca() { //maybe this is backward... maybe i'm understanding instances and actual data backwards. maybe switching them fixes things??
        SimpleDataSet source;
        List<DataPoint> dataPoints = new ArrayList<>();
        Vec snareVector = new DenseVector(snareSamples);
        Vec ohVector = new DenseVector(overheadSamples);
        dataPoints.add(new DataPoint(snareVector));
        dataPoints.add(new DataPoint(ohVector));
        source = new SimpleDataSet(dataPoints);
        //this is a copy for later comparison.
        SimpleDataSet sourceCopy = new SimpleDataSet(source.getBackingListCopy());
        System.out.println("\nsource length: " + source.getSampleSize());
        System.out.println("\ndatapoint length: " + source.getDataPoint(0).numNumericalValues());

        //DO THE DAMN THING
        //currently, whitening the data produces a problem... might have something to do with it being 0's???
        //
        //do we just need to simply place them all back if they're all zeros...????
        FastICA ica = new FastICA(source, 2, FastICA.DefaultNegEntropyFunc.KURTOSIS, false);
        source.applyTransform(ica);

        int[] ranks = Find.theMatchingWavFile(sourceCopy, source);
        //so...samples were non-negative... place them back


        System.out.println("\nsource length: " + source.getSampleSize());
        System.out.println("\ndatapoint length: " + source.getDataPoint(0).numNumericalValues());
        for(int i = 0; i < samplesLeft; i++) {
            snareOutput[outputIndex] = source.getDataPoint(ranks[0]).getValue(i);
            overheadOutput[outputIndex] = source.getDataPoint(ranks[1]).getValue(i);
            outputIndex++;
        }
    }

    private static void runAbbyIca() {
        Instance[] instances = new Instance[snareSamples.size()];

    }

    private static void outputZeros() {
        //all samples were zeros... just write whatever to whatever.
        //i think this will end up being redundant... should we pass all zero's to ICA...??
        for(int i = 0; i < samplesLeft; i++) {
            snareOutput[outputIndex] = 0;
            overheadOutput[outputIndex] = 0;
            outputIndex++;
        }
    }

    private static void normalizeOutput() {
        //find loudest in original tracks corresponding to output tracks
        //find multiplier for loudest on output tracks
        //multiply output by that to get it back to normal
    }

}
