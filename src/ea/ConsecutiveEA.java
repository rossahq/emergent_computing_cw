package ea;

import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

import java.util.ArrayList;

public class ConsecutiveEA {
    // create a new team with the deflault settings
    public static TeamPursuit teamPursuit = new WomensTeamPursuit();

    private ArrayList<Individual> population = new ArrayList<Individual>();
    private int iteration = 0;

    public SimultaneousEA() {

    }


    public static void main(String[] args) {
        SimultaneousEA ea = new SimultaneousEA();
        ea.run();
    }

    public void run() {
        initialisePopulation();
        System.out.println("finished init pop");
        iteration = 0;
        while(iteration < Parameters.maxIterations){
            iteration++;
            Individual parent1 = tournamentSelection();
            Individual parent2 = tournamentSelection();
            Individual child = crossover(parent1, parent2);
            child = mutateTransition(child);
            child = mutatePacing(child);
            child.evaluate(teamPursuit);
            replace(child);
            printStats();
        }
        Individual best = getBest(population);
        best.print();
    }

    private void printStats() {
        System.out.println("" + iteration + "\t" + getBest(population) + "\t" + getWorst(population));
    }

/*	//replace worst
	private void replace(Individual child) {
		Individual worst = getWorst(population);
		if(child.getFitness() < worst.getFitness()){
			int idx = population.indexOf(worst);
			population.set(idx, child);
		}
	}*/

    //tournament replacement
    private void replace(Individual child) {
        ArrayList<Individual> candidates = new ArrayList<Individual>();
        for(int i = 0; i < Parameters.tournamentSize; i++){
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }
        Individual loser = getWorst(candidates);

        //replace loser with the child
        if (loser.getFitness() < child.getFitness()) {
            int idx = population.indexOf(loser);
            population.set(idx, child);
        }
    }


    private Individual mutateTransition(Individual child) {
        if(Parameters.rnd.nextDouble() > Parameters.mutationProbability){
            return child;
        }

        // choose how many elements to alter
        int mutationRate = 1 + Parameters.rnd.nextInt(Parameters.mutationRateMax);

        //mutate the transition strategy by flipping boolean value
        for(int i = 0; i < mutationRate; i++){
            int index = Parameters.rnd.nextInt(child.transitionStrategy.length);
            child.transitionStrategy[index] = !child.transitionStrategy[index];
        }

        return child;
    }

    private Individual mutatePacing(Individual child) {
        if (Parameters.rnd.nextDouble() > Parameters.mutationProbability) {
            return child;
        }

        // choose how many elements to alter
        int mutationRate = 1 + Parameters.rnd.nextInt(Parameters.mutationRateMax);

        //mutate the pacing strategy by changing the int value
        int x = 0;
        while (mutationRate < x) {
            for(int i = 0; i < child.pacingStrategy.length; ++i) {
                if (Parameters.rnd.nextBoolean()) {
                    child.pacingStrategy[i] += (int) (child.pacingStrategy[i] * Parameters.PacingMutationRate);
                } else {
                    child.pacingStrategy[i] -= (int) (child.pacingStrategy[i] * -Parameters.PacingMutationRate);
                }
                i++;
            }
        }

        return child;
    }

    private Individual crossover(Individual parent1, Individual parent2) {
        if(Parameters.rnd.nextDouble() > Parameters.crossoverProbability){
            return parent1;
        }
        Individual child = new Individual();

        int crossoverPoint = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        // just copy the pacing strategy from p1 - not evolving in this version
        for(int i = 0; i < crossoverPoint; i++){
            child.pacingStrategy[i] = parent1.pacingStrategy[i];
        }
        for(int i = crossoverPoint; i < parent2.pacingStrategy.length; i++){
            child.pacingStrategy[i] = parent2.pacingStrategy[i];
        }


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
        return getBest(candidates).copy();
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

    private void initialisePopulation() {
        while(population.size() < Parameters.popSize){
            Individual individual = new Individual();
            individual.initialise();
            individual.evaluate(teamPursuit);
            population.add(individual);

        }
    }
}
