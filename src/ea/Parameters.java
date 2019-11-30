package ea;

import java.util.Random;

public class Parameters {

	public static Random rnd = new Random(System.currentTimeMillis());
	
	/**
	 * used as a seed
	 * 
	 */	
	static final boolean [] DEFAULT_WOMENS_TRANSITION_STRATEGY = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
	public static final int [] DEFAULT_WOMENS_PACING_STRATEGY = {300, 300, 300, 300, 300, 300, 300, 350, 350, 300, 300, 350, 350, 350, 350, 300, 300, 350, 350, 350, 350, 300, 300};
	
	
	public static int popSize = 150;
	public static int tournamentSize = 40;
	
	public static double mutationProbability = 0.1;
	public static double PacingMutationRate = 0.05;
	public static double crossoverProbability = 1.0;
	public static double pacingCrossoverProbability = 0.5; //for uniform crossover


	public static int maxIterations = 1000;


	public static int maxRuns = 10;
}
