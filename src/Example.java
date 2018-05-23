import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import jsat.datatransform.FastICA;
import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import shared.Instance;

public class Example {

    private static double[] inputA, inputB, outputA, outputB;
    private static ArrayList<Double> samplesForA, samplesForB;

    private static final double[] FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES = StdAudio.note(400, 5, .5);
    private static final double[] SEVEN_HUNDRED_HERTZ_SIN_WAV_SAMPLES = StdAudio.note(700, 5, .5);

    private static int bufferSize, index, outputIndex, nonZeroCount, samplesLeft;
    private static long startTime;

    private static boolean jsat = true;
    private static boolean sin  = true;


    public static void main(String[] args) {
        //TODO -> add checking for sample size and make sure they're the same... no need to attempt to guess what someone else recorded and align it right
        try {
            init();
            while(samplesStillNeedProcessing()) {
                samplesLeft = assignSamplesLeft();
                nonZeroCount = 0;
                insertSamples();
                boolean majorityNonZeroSamples = (nonZeroCount >= (samplesLeft / 20));                      //this is for whitening to know what's up... assume nonzero samples at the beginning
                //at some point we need to count how many actually are going to need this... maybe some trickery by track?? i'm not sure on this one..
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
            normalizeOutput();
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

        if (sin) {
            inputA = new double[FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES.length];
            inputB = new double[SEVEN_HUNDRED_HERTZ_SIN_WAV_SAMPLES.length];

            for (int i = 0; i < FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES.length; i++) {
                inputA[i] = (FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES[i] * .7) + (SEVEN_HUNDRED_HERTZ_SIN_WAV_SAMPLES[i] * .3);
                inputB[i] = (FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES[i] * .3) + (SEVEN_HUNDRED_HERTZ_SIN_WAV_SAMPLES[i] * .7);
            }
        } else {
            inputA = StdAudio.read("SNARE_mixed.wav");
            inputB = StdAudio.read("Overhead_mixed.wav");

            //this is just some really nice information to see... we don't have to keep this, but it might be good to use in the future
            WavFile snareWavFile = WavFile.openWavFile(new File("SNARE_mixed.wav"));
            WavFile overheadWavFile = WavFile.openWavFile(new File("Overhead_mixed.wav"));
            System.out.println("");
            overheadWavFile.display();
            System.out.println("");
            snareWavFile.display();
            snareWavFile.close();
            overheadWavFile.close();
        }

        // TODO THIS IS REALLY LOUD AND IT HURTS 
        //StdAudio.play(inputA);
        //StdAudio.play(inputB);

        outputA = new double[inputA.length];
        outputB = new double[inputB.length];

        bufferSize = 100;
        index = 0;
        outputIndex = 0;

        startTime = System.currentTimeMillis();

    }

    private static void end() {

        System.out.println("********************");
        System.out.println("Writing to files...");

        if(sin) {
            if(Arrays.equals(outputA, FOUR_HUNDRED_HERTZ_SIN_WAV_SAMPLES)) {
                System.out.println("400 Hz successful");
            } else {
                System.out.println("400 Hz unsuccessful");
            }

            if(Arrays.equals(outputB, SEVEN_HUNDRED_HERTZ_SIN_WAV_SAMPLES)) {
                System.out.println("700 Hz successful");
            } else {
                System.out.println("700 Hz unsuccessful");
            }
        } else {
            StdAudio.save("SNARE_FINAL.wav", outputA);
            System.out.println("Wrote SNARE_FINAL.wav");

            StdAudio.save("OH_FINAL.wav", outputB);
            System.out.println("Wrote OH_FINAL.wav");
        }
        System.out.println("Done.");
        System.out.println("********************");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("OUTPUT_A.txt"))) {
            StringBuilder builder = new StringBuilder();
            int counter = 0;
            for (double sample : outputA) {
                if (counter++ < 100) {
                    builder.append(sample + ", ");
                }
            }
            counter = 0;
            builder.append("\n\n\n");
            for (double sample : inputA) {
                if (counter++ < 100) {
                    builder.append(sample + ", ");
                }
            }
            writer.write(builder.toString());
        } catch (Exception e) {
            System.out.println("BROKE IT");
        }

        long finishTime = (System.currentTimeMillis() - startTime);
        double elapsedICA = finishTime / 1000.0;
        System.out.println("Running ICA took " + elapsedICA + " seconds");
    }

    private static boolean samplesStillNeedProcessing() {
        return index < inputA.length;
    }

    private static int assignSamplesLeft() {
        if(inputA.length - index < 100) {           //first check if there are enough to fill the whole virtual buffer
            return inputA.length - index;           //and if not, just use the amount of samples left
        } else {
            return bufferSize;                          //otherwise just use a full virtual buffer
        }
    }

    private static void insertSamples() {
        samplesForA = new ArrayList<>();
        samplesForB = new ArrayList<>();
        nonZeroCount = 0;
        for(int i = 0; i < samplesLeft; i++) {          //fill the virtual buffers
            if((inputA[index] != 0) || (inputB[index] != 0)) { //short circuit if we already know to save us some time...
                nonZeroCount++;
            }
            samplesForA.add(inputA[index]);
            samplesForB.add(inputB[index++]);  //don't forget to increment index!
        }
    }

    private static void runJsatIca() { //maybe this is backward... maybe i'm understanding instances and actual data backwards. maybe switching them fixes things??
        SimpleDataSet source;
        List<DataPoint> dataPoints = new ArrayList<>();
        Vec vectorA = new DenseVector(samplesForA);
        Vec vectorB = new DenseVector(samplesForB);
        dataPoints.add(new DataPoint(vectorA));
        dataPoints.add(new DataPoint(vectorB));
        source = new SimpleDataSet(dataPoints);
        //this is a copy for later comparison.
        SimpleDataSet sourceCopy = copy(source.getBackingList());

        //DO THE DAMN THING
        //currently, whitening the data produces a problem... might have something to do with it being 0's???
        //
        //do we just need to simply place them all back if they're all zeros...????
        FastICA ica = new FastICA(source, 2, FastICA.DefaultNegEntropyFunc.KURTOSIS, false);
        source.applyTransform(ica, true); //not sure that this is actually applying the transform... i think this, in layman's terms, is "run ICA"

        SimpleDataSet projection = copy(sourceCopy.getBackingList());
        double firstMultiplier = source.getDataPoint(0).getNumericalValues().get(0);
        double secondMultiplier = source.getDataPoint(1).getNumericalValues().get(0);
        projection.getDataPoint(0).getNumericalValues().mutableMultiply(firstMultiplier);
        projection.getDataPoint(1).getNumericalValues().mutableMultiply(secondMultiplier);

        int[] ranks = Find.theMatchingWavFile(sourceCopy, projection);
        //so...samples were non-negative... place them back

        Vec ranksAt0 = projection.getDataPoint(ranks[0]).getNumericalValues();
        Vec ranksAt1 = projection.getDataPoint(ranks[1]).getNumericalValues();

        for(int i = 0; i < samplesLeft; i++) {
            outputA[outputIndex] = ranksAt0.get(i);
            outputB[outputIndex] = ranksAt1.get(i);
            outputIndex++;
        }
    }

    private static void runAbbyIca() {
        Instance[] instances = new Instance[samplesForA.size()];

    }

    private static void outputZeros() {
        //all samples were zeros... just write whatever to whatever.
        //i think this will end up being redundant... should we pass all zero's to ICA...??
        for(int i = 0; i < samplesLeft; i++) {
            outputA[outputIndex] = 0;
            outputB[outputIndex] = 0;
            outputIndex++;
        }
    }

    private static void normalizeOutput() {
        if(sin) {
            normalizeTrackUsingMaxAmplitudeOf(inputA, .5);
            normalizeTrackUsingMaxAmplitudeOf(inputB, .5);
        } else {
            normalizeTrackUsingLoudestSample(outputA);
            normalizeTrackUsingLoudestSample(outputB);
        }
    }

    private static void normalizeTrackUsingLoudestSample(double[] track) {
        normalizeTrackUsingMultiplier(track, 1 / loudestSampleIn(track));
    }

    private static void normalizeTrackUsingMaxAmplitudeOf(double[] track, double maxAmplitude) {
        normalizeTrackUsingMultiplier(track, (1 / loudestSampleIn(track) * maxAmplitude));
    }

    private static void normalizeTrackUsingMultiplier(double[] track, double multiplier) {
        for (int i = 0; i < track.length; i++) {
            track[i] = track[i] * multiplier;
        }
    }

    private static double loudestSampleIn(double[] track) { //this could be found on reading in... don't redo work
        double amplitude = 0;
        for(double sample : track) {
            if(Math.abs(sample) > amplitude) {
                amplitude = Math.abs(sample);
            }
        }
        return amplitude;
    }

    private static SimpleDataSet copy(List<DataPoint> dataPoints) {
        List<DataPoint> copy = new ArrayList<>();
        copy.addAll(dataPoints);
        return new SimpleDataSet(copy);
    }

    private static boolean errorBetweenTracksIsLessThan(double[] track1, double[] track2, double allowance) {
        return false;
    }
}
