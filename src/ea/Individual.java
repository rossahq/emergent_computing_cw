package ea;

import teamPursuit.*;

import java.util.ArrayList;

public class Individual {

	
	public boolean[] transitionStrategy = new boolean[22] ;
	public int[] pacingStrategy = new int[23];
	
	SimulationResult result = null;	
	
	public Individual() {		
		
	}

	// this code evolves the transition strategy
	// an individual is initialised with a random strategy that will evolve
	// the pacing strategy is initialised with random values between the minimum and max power for cyclists
	
	public void initialise(){
		for(int i = 0; i < transitionStrategy.length; i++){
			transitionStrategy[i] = Parameters.rnd.nextBoolean();
			//transitionStrategy[i] = Parameters.DEFAULT_WOMENS_TRANSITION_STRATEGY[i];
		}
		
		for(int i = 0; i < pacingStrategy.length; i++){
			int randomNum = Parameters.rnd.nextInt((700 - 200) + 1) + 200;
			pacingStrategy[i] = randomNum;
			//pacingStrategy[i] = Parameters.DEFAULT_WOMENS_PACING_STRATEGY[i];
		}
		
	}
	
	// this is just there in case you want to check the default strategies
	public void initialise_default(){
		for(int i = 0; i < transitionStrategy.length; i++){
			transitionStrategy[i] = Parameters.DEFAULT_WOMENS_TRANSITION_STRATEGY[i];
		}
		
		for(int i = 0; i < pacingStrategy.length; i++){
			pacingStrategy[i] = Parameters.DEFAULT_WOMENS_PACING_STRATEGY[i];
		}
		
	}
	
	
	public void evaluate(TeamPursuit teamPursuit){		
		try {
			result = teamPursuit.simulate(transitionStrategy, pacingStrategy);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	// this is a very basic fitness function
	// if the race is not completed, the chromosome gets a normalised fitness value
	// otherwise, the fitness is equal to the time taken

	public double getFitness(){
		double fitness = 1000;
		if (result == null) {
			return fitness;
		}
		if (result.getProportionCompleted() < 0.999) {
			return normalisedFitness(result.getProportionCompleted());
		}
		else{
            fitness = result.getFinishTime();
		}
		return fitness;

	}

	public double normalisedFitness(double x) {
		return ((x - 0.999)
				/ (0.999 - 0.001))
				* (400 - 500) + 400;
	}

	public ArrayList getCombinedChromosome(Individual i) {
		//merge transition and pacing strategies to create single chromosome
		ArrayList<String> chromosome = new ArrayList<>();

		for (int j = 0; j < i.pacingStrategy.length; j++) {
			String gene = String.valueOf(i.pacingStrategy[j]);
			chromosome.add(gene);
		}

		for (int j = 0; j < i.transitionStrategy.length; j++) {
			String gene = String.valueOf(i.transitionStrategy[j]);
			chromosome.add(gene);
		}

		return chromosome;
	}
	
	
	public Individual copy(){
		Individual individual = new Individual();
		for(int i = 0; i < transitionStrategy.length; i++){
			individual.transitionStrategy[i] = transitionStrategy[i];
		}		
		for(int i = 0; i < pacingStrategy.length; i++){
			individual.pacingStrategy[i] = pacingStrategy[i];
		}		
		individual.evaluate(SimultaneousEA.teamPursuit);
		return individual;
	}
	
	@Override
	public String toString() {
		String str = "";
		if(result != null){
			str += getFitness();
		}
		return str;
	}

	public void print() {
		for(int i : pacingStrategy){
			System.out.print(i + ",");			
		}
		System.out.println();
		for(boolean b : transitionStrategy){
			if(b){
				System.out.print("true,");
			}else{
				System.out.print("false,");
			}
		}
		System.out.println("\r\n" + this);
	}
}
