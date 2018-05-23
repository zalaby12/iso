package opt.test;

import java.util.Arrays;
import java.util.Random;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

import opt.SwapNeighbor;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.*;
import opt.ga.CrossoverFunction;
import opt.ga.SwapMutation;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.StandardGeneticAlgorithm;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;

/**
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class TravelingSalesmanTestSimmulatedAnnealing {
    /** The n value */
    private static final int N = 50;
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) {
        
        int iterations = Integer.parseInt(args[0]);
        double startingTemp = Double.parseDouble(args[1]);
        double coolingExponent = Double.parseDouble(args[2]) / 100;
        
        Random random = new Random();
        // create the random points
        double[][] points = new double[N][2];
        for (int i = 0; i < points.length; i++) {
            points[i][0] = random.nextDouble();
            points[i][1] = random.nextDouble();   
        }
        // for rhc, sa, and ga we use a permutation based encoding
        TravelingSalesmanEvaluationFunction ef = new TravelingSalesmanRouteEvaluationFunction(points);
        Distribution odd = new DiscretePermutationDistribution(N);
        NeighborFunction nf = new SwapNeighbor();
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        
        long starttime = System.currentTimeMillis();
        SimulatedAnnealing sa = new SimulatedAnnealing(startingTemp, coolingExponent, hcp);
        FixedIterationTrainer fit = new FixedIterationTrainer(sa, iterations);
        fit.train();
        System.out.println("SA with " + iterations + " iterations, a starting temp of " + startingTemp + ", and a cooling exponent of " + coolingExponent + "\nvalue: " + ef.value(sa.getOptimal()));
        System.out.println("Time: "+ (System.currentTimeMillis() - starttime));
        System.out.println("===================="); 
        
    }
}
