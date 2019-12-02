package ea;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class TwoPtCrossoverTournamentsEA implements Runnable {

    // create a new team with the deflault settings
    public static TeamPursuit teamPursuit = new WomensTeamPursuit();

    private ArrayList<Individual> population = new ArrayList<Individual>();
    private int iteration = 0;
    private int runs = 0;

    public TwoPtCrossoverTournamentsEA() {

    }


    public static void main(String[] args) {
        TwoPtCrossoverTournamentsEA ea = new TwoPtCrossoverTournamentsEA();

        ea.run();
    }

    public void run() {
        runs = 0;
        while(runs < Parameters.maxRuns) {
            initialisePopulation();
            System.out.println("finished init pop");
            runs++;
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
            try {
                writeResulstToFile(best.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Individual worst = getWorst(population);
            worst.print();
        }
    }

    private void printStats() {
        System.out.println("" + iteration + "\t" + getBest(population) + "\t" + getWorst(population));
    }

    private void writeResulstToFile(String result) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\ROSSA\\IdeaProjects\\emergent_computing_cw\\src\\results\\results", true));
        writer.append(' ');
        writer.append(result);
        writer.append('\n');

        writer.close();
    }

	/*//replace worst
	private void replace(Individual child) {
		Individual worst = getWorst(population);
		if(child.getFitness() < worst.getFitness()){
			int idx = population.indexOf(worst);
			population.set(idx, child);
		}
	}*/

    //tournament replacement
    private void replace(Individual child) {
        ArrayList<Individual> candidates = new ArrayList<>();
        for (int i = 0; i < Parameters.tournamentSize; i++) {
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }
        Individual loser = getWorst(candidates);

        //replace loser with the child
        int idx = population.indexOf(loser);
        population.set(idx, child);
    }

    private Individual mutate(Individual child) {

        //mutate the transition strategy by flipping boolean value
        for (int i = 0; i < child.transitionStrategy.length; i++) {
            if (Parameters.rnd.nextDouble() < Parameters.mutationProbability) {
                int index = Parameters.rnd.nextInt(child.transitionStrategy.length);
                child.transitionStrategy[index] = !child.transitionStrategy[index];
            }
        }

        //mutate the pacing strategy by changing the int value
        for (int i = 0; i < child.pacingStrategy.length; ++i) {
            if (Parameters.rnd.nextDouble() < Parameters.mutationProbability) {
                int mutateAmount = (int) (child.pacingStrategy[i] * Parameters.PacingMutationRate);
                if (Parameters.rnd.nextBoolean()) {
                    child.pacingStrategy[i] += mutateAmount;
                    if (child.pacingStrategy[i] > 1200) {
                        child.pacingStrategy[i] -= mutateAmount;
                    }
                } else {
                    child.pacingStrategy[i] -= mutateAmount;
                    if (child.pacingStrategy[i] < 200) {
                        child.pacingStrategy[i] += mutateAmount;
                    }
                }
            }
        }

        return child;
    }

    //one point crossover
    private Individual crossover(Individual parent1, Individual parent2) {

        Individual child = new Individual();
        int crossoverPoint = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        //one-point crossover
        for(int i = 0; i < crossoverPoint; i++){
            child.pacingStrategy[i] = parent1.pacingStrategy[i];
        }
        for(int i = crossoverPoint; i < parent2.pacingStrategy.length; i++){
            child.pacingStrategy[i] = parent2.pacingStrategy[i];
        }
        //get new random number for transition crossover
        crossoverPoint = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        //one-point crossover
        for(int i = 0; i < crossoverPoint; i++){
            child.transitionStrategy[i] = parent1.transitionStrategy[i];
        }
        for(int i = crossoverPoint; i < parent2.transitionStrategy.length; i++){
            child.transitionStrategy[i] = parent2.transitionStrategy[i];
        }
        return child;
    }

    /*//2 point crossover
    private Individual crossover(Individual parent1, Individual parent2) {

        Individual child = new Individual();
        int crossoverPoint1 = Parameters.rnd.nextInt(parent1.pacingStrategy.length);
        int crossoverPoint2 = Parameters.rnd.nextInt(parent1.pacingStrategy.length);

        //2-point crossover
        for(int i = 0; i < crossoverPoint1; i++){
            child.pacingStrategy[i] = parent1.pacingStrategy[i];
        }
        for(int i = crossoverPoint1; i < crossoverPoint2; i++){
            child.pacingStrategy[i] = parent2.pacingStrategy[i];
        }
        for(int i = crossoverPoint2; i < parent1.pacingStrategy.length; i++) {
            child.pacingStrategy[i] = parent1.pacingStrategy[i];
        }
        //get new random number for transition crossover
        crossoverPoint1 = Parameters.rnd.nextInt(parent1.transitionStrategy.length);
        crossoverPoint2 = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        //one-point crossover
        for(int i = 0; i < crossoverPoint1; i++){
            child.transitionStrategy[i] = parent1.transitionStrategy[i];
        }
        for(int i = crossoverPoint1; i < crossoverPoint2; i++){
            child.transitionStrategy[i] = parent2.transitionStrategy[i];
        }
        for(int i = crossoverPoint2; i < parent1.transitionStrategy.length; i++) {
            child.transitionStrategy[i] = parent1.transitionStrategy[i];
        }
        return child;
    }*/
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
        if (population.size() > 0) {
            population.clear();
        }

        while(population.size() < Parameters.popSize){
            Individual individual = new Individual();
            individual.initialise();
            individual.evaluate(teamPursuit);
            population.add(individual);

        }
    }
}
