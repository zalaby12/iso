package opt.test;

import java.util.Arrays;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.*;
import opt.ga.CrossoverFunction;
import opt.ga.DiscreteChangeOneMutation;
import opt.ga.SingleCrossOver;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.StandardGeneticAlgorithm;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;

/**
 * Copied from ContinuousPeaksTest
 * @version 1.0
 */
public class FourPeaksTestSimmulatedAnnealing {
    /** The n value */
    private static final int N = 200;
    /** The t value */
    private static final int T = N / 3;
    
    public static void main(String[] args) {

        int iterations = Integer.parseInt(args[0]);
        double startingTemp = Double.parseDouble(args[1]);
        double coolingExponent = Double.parseDouble(args[2]) / 100;

        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new FourPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
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
