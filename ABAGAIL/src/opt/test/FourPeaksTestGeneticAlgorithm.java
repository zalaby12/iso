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
public class FourPeaksTestGeneticAlgorithm {
    /** The n value */
    private static final int N = 200;
    /** The t value */
    private static final int T = N / 3;
    
    public static void main(String[] args) {

        int iterations = Integer.parseInt(args[0]);
        int population = Integer.parseInt(args[1]);
        int toMate = Integer.parseInt(args[2]);
        int toMutate = Integer.parseInt(args[3]);


        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new FourPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new SingleCrossOver();
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        
        long starttime = System.currentTimeMillis();
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(population, toMate, toMutate, gap);
        FixedIterationTrainer fit = new FixedIterationTrainer(ga, iterations);
        fit.train();
        System.out.println("GA with " + iterations + " iterations, " + population + " population size, " + toMate + " matings, and " + toMutate + " mutations per iteration\nvalue: " + ef.value(ga.getOptimal()));
        System.out.println("Time: "+ (System.currentTimeMillis() - starttime));
        System.out.println("===================="); 
        
    }

}
