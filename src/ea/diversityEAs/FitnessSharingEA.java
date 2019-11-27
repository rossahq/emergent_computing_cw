package ea.diversityEAs;

/***
 * This is an example of an SimultaneousEA used to solve the problem
 *  A chromosome consists of two arrays - the pacing strategy and the transition strategy
 * This algorithm is only provided as an example of how to use the code and is very simple - it ONLY evolves the transition strategy and simply sticks with the default
 * pacing strategy
 * The default settings in the parameters file make the SimultaneousEA work like a hillclimber:
 * 	the population size is set to 1, and there is no crossover, just mutation
 * The pacing strategy array is never altered in this version- mutation and crossover are only
 * applied to the transition strategy array
 * It uses a simple (and not very helpful) fitness function - if a strategy results in an
 * incomplete race, the fitness is set to 1000, regardless of how much of the race is completed
 * If the race is completed, the fitness is equal to the time taken
 * The idea is to minimise the fitness value
 */


import java.util.ArrayList;

import ea.Individual;
import ea.Parameters;
import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class FitnessSharingEA implements Runnable {

    // create a new team
    public static TeamPursuit teamPursuit = new WomensTeamPursuit();

    int hd_mating_threshold = 10;  // use this to set a mating threshold in the genotypic space
    double ec_mating_threshold = 0.2; // use this to set a mating thresholdin the phenotypic space


    private ArrayList<Individual> population = new ArrayList<Individual>();
    private int iteration = 0;

    public FitnessSharingEA() {

    }


    public static void main(String[] args) {
        FitnessSharingEA ea = new FitnessSharingEA();
        ea.run();
    }

    public void run() {
        initialisePopulation();

        System.out.println("finished init pop");
        iteration = 0;
        while (iteration < Parameters.maxIterations) {
            iteration++;
            Individual parent1 = tournamentSelection();
            Individual parent2 = tournamentSelection();
            Individual child = crossover(parent1, parent2);
            child = mutate(child);
            child.evaluate(teamPursuit);
            replace(child);
            printStats();
        }
        Individual best = getBest(population);
        best.print();
        Individual worst = getWorst(population);
        worst.print();
    }

    private void printStats() {
        System.out.println("" + iteration + "\t" + getBest(population) + "\t" + getWorst(population));
    }

    //tournament replacement
    private void replace(Individual child) {
        ArrayList<Individual> candidates = new ArrayList<Individual>();
        for (int i = 0; i < 20; i++) {
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }
        Individual loser = getWorst(candidates);

        //replace loser with the child
        int idx = population.indexOf(loser);
        population.set(idx, child);
    }

    private Individual mutate(Individual child) {
        if (Parameters.rnd.nextDouble() > Parameters.mutationProbability) {
            return child;
        }

        // choose how many elements to alter
        int mutationRate = 1 + Parameters.rnd.nextInt(Parameters.mutationRateMax);

        //mutate the transition strategy by flipping boolean value
        for (int i = 0; i < mutationRate; i++) {
            int index = Parameters.rnd.nextInt(child.transitionStrategy.length);
            child.transitionStrategy[index] = !child.transitionStrategy[index];
        }

        //mutate the pacing strategy by changing the int value
        int x = 0;
        while (mutationRate > x) {
            for (int i = 0; i < child.pacingStrategy.length; ++i) {
                if (Parameters.rnd.nextBoolean()) {
                    child.pacingStrategy[i] += (int) (child.pacingStrategy[i] * Parameters.PacingMutationRate);
                } else {
                    child.pacingStrategy[i] -= (int) (child.pacingStrategy[i] * -Parameters.PacingMutationRate);
                }
            }
            x++;
        }
        return child;
    }

    private Individual crossover(Individual parent1, Individual parent2) {
//		if(Parameters.rnd.nextDouble() > Parameters.crossoverProbability){
//			return parent1;
//		}

        Individual child = new Individual();

        int crossoverPoint = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        //uniform crossover
        for (int i = 0; i < child.pacingStrategy.length; i++) {
            if (Parameters.rnd.nextFloat() > Parameters.pacingCrossoverProbability) {
                child.pacingStrategy[i] += parent1.pacingStrategy[i];
            } else {
                child.pacingStrategy[i] += parent2.pacingStrategy[i];
            }
        }

        // one-point crossover
        for(int i = 0; i < crossoverPoint; i++){
            child.transitionStrategy[i] = parent1.transitionStrategy[i];
        }
        for(int i = crossoverPoint; i < parent2.transitionStrategy.length; i++){
            child.transitionStrategy[i] = parent2.transitionStrategy[i];
        }
        return child;
    }

    /**
     * Returns a COPY of the individual selected using tournament selection
     * @return
     */
    private Individual tournamentSelection() {
        ArrayList<Individual> candidates = new ArrayList<Individual>();
        for(int i = 0; i < Parameters.tournamentSize; i++){
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }

        Individual bestParent = candidates.get(0);

        for (int i=0;i<Parameters.tournamentSize;i++){
            double fitness = calculateSharedFitness(candidates.get(i));
            if (fitness > calculateSharedFitness(bestParent)){
                bestParent = candidates.get(i);
            }
        }

        return bestParent.copy();
    }


    private Individual getBest(ArrayList<Individual> aPopulation) {
        double bestFitness = Double.MAX_VALUE;
        Individual best = null;
        for(Individual individual : aPopulation){
            if(individual.getFitness() < bestFitness || best == null){
                best = individual;
                bestFitness = best.getFitness();
            }
        }
        return best;
    }

    private Individual getWorst(ArrayList<Individual> aPopulation) {
        double worstFitness = 0;
        Individual worst = null;
        for(Individual individual : population){
            if(individual.getFitness() > worstFitness || worst == null){
                worst = individual;
                worstFitness = worst.getFitness();
            }
        }
        return worst;
    }

    private void printPopulation() {
        for(Individual individual : population){
            System.out.println(individual);
        }
    }

    public double calculateSharedFitness(Individual i) {

        double fitness = i.getFitness();
        int denominator = 1;

        int index = population.indexOf(i);

        for (int j = 0; j < population.size(); j++) {
            double dist = hammingDistance(index, j);
            if (dist < hd_mating_threshold)
                denominator += (1 - (dist / hd_mating_threshold));
        }

        double sharedFitness =  fitness/denominator;
        return sharedFitness;
    }

    private void initialisePopulation() {
        while(population.size() < Parameters.popSize){
            Individual individual = new Individual();
            individual.initialise();
            individual.evaluate(teamPursuit);
            population.add(individual);

        }
    }

//    public double convertToRealValue(int[] someSolution){
//        // map to real  number
//        double min = 0.0;
//        double max = 1.0;
//
//
//        String s = convertToString(someSolution);
//        int newVal = Integer.parseInt(s, 2);
//
//        double val = min + (double)newVal*(max-min)/(Math.pow(2.0, (double)length)-1);
//
//        return val;
//    }

    public int hammingDistance(int p1, int p2){

        //merge transition and pacing strategies to create single chromosome

        Individual indivd1 = population.get(p1);
        Individual indivd2 = population.get(p2);

        int hd = 0;

        for (int i=0;i < indivd1.transitionStrategy.length; i++)
            if (indivd1.transitionStrategy[i] != indivd2.transitionStrategy[i])
                hd++;

        return hd;
    }

//    public double euclideanDistance(Individual p1, Individual p2){
//
//        // get real value equivalents of bitstrings
//        double r1 = convertToRealValue(population[p1]);
//        double r2 = convertToRealValue(population[p2]);
//
//        double distance = Math.sqrt(Math.pow(r1-r2, 2.0));
//        return distance;
//    }

    public String convertToString(int[] solution){

        String s ="";
        for(int i=0;i<Parameters.popSize;i++){
            int j = solution[i];
            s= s+ Integer.toString(j);
        }

        return s;
    }

}
