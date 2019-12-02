package ea;

/*
Implementation:

Tournament size: 10
Pop: 150
Tournament Selection & Tournament Replacement
Uniform & one-point crossover
Mutation probability: 0.1
Mutate rate: 0.05
Iterations: 1000

 */


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class AdaptiveEA implements Runnable {

    // create a new team with the deflault settings
    public static TeamPursuit teamPursuit = new WomensTeamPursuit();

    private ArrayList<AdaptiveIndividual> population = new ArrayList<AdaptiveIndividual>();
    private int iteration = 0;
    private double pacingTau;

    public AdaptiveEA() {

    }


    public static void main(String[]  args) {
        AdaptiveEA ea = new AdaptiveEA();

        ea.run();
    }

    public void run() {
        int runs = 0;
        while(runs < Parameters.maxRuns) {
            initialisePopulation();
            System.out.println("finished init pop");
            runs++;
            iteration = 0;
            // set initial values for tau parameter
            pacingTau = 1.0/Math.sqrt(population.get(1).adptvPacingStrategy.length);

            while (iteration < Parameters.maxIterations) {
                iteration++;
                AdaptiveIndividual parent1 = tournamentSelection();
                AdaptiveIndividual parent2 = tournamentSelection();

                AdaptiveIndividual child = crossover(parent1, parent2);

                child = mutate(child);

                child.evaluate(teamPursuit);
                replace(child);
                printStats();
            }
            AdaptiveIndividual best = getBest(population);
            best.print();
            try {
                writeResulstToFile(best.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            AdaptiveIndividual worst = getWorst(population);
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

    //tournament replacement
    private void replace(AdaptiveIndividual child) {
        ArrayList<AdaptiveIndividual> candidates = new ArrayList<>();
        for (int i = 0; i < Parameters.tournamentSize; i++) {
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }
        AdaptiveIndividual loser = getWorst(candidates);

        //replace loser with the child
        int idx = population.indexOf(loser);
        population.set(idx, child);
    }

    private AdaptiveIndividual mutate(AdaptiveIndividual child) {
        double pacingSigma = child.adptvPacingStrategy.sigma;
        double transSigma = child.adptvTransitionStrategy.sigma;
        // modify sigma value stored in the chromosome   s = s.e^(t.N(0,1))
        child.adptvPacingStrategy.sigma = pacingSigma*Math.exp(pacingTau*Parameters.rnd.nextGaussian());

        //mutate the transition strategy by flipping boolean value
        for (int i = 0; i < child.adptvTransitionStrategy.length; i++) {
            if (Parameters.rnd.nextDouble() < Parameters.mutationProbability) {
                int index = Parameters.rnd.nextInt(child.adptvTransitionStrategy.length);
                child.adptvTransitionStrategy.transitionStrategy[index] = !child.adptvTransitionStrategy.transitionStrategy[index];
            }
        }

        //mutate the pacing strategy by changing the int value
        for (int i = 0; i < child.adptvPacingStrategy.length; ++i) {
            if (Parameters.rnd.nextDouble() < Parameters.mutationProbability) {
                int mutateAmount = (int) (pacingSigma * Parameters.rnd.nextGaussian());
                if (Parameters.rnd.nextBoolean()) {
                    child.adptvPacingStrategy.pacingStrategy[i] += mutateAmount;
                    if (child.adptvPacingStrategy.pacingStrategy[i] > 1200) {
                        child.adptvPacingStrategy.pacingStrategy[i] -= mutateAmount;
                    }
                } else {
                    child.adptvPacingStrategy.pacingStrategy[i] -= mutateAmount;
                    if (child.adptvPacingStrategy.pacingStrategy[i] < 200) {
                        child.adptvPacingStrategy.pacingStrategy[i] += mutateAmount;
                    }
                }
            }
        }
        //lastly mutate the sigma value
        double mutateAmount = pacingSigma * Parameters.rnd.nextGaussian();
        child.adptvPacingStrategy.sigma = child.adptvPacingStrategy.sigma + mutateAmount;

        return child;
    }

    private AdaptiveIndividual crossover(AdaptiveIndividual parent1, AdaptiveIndividual parent2) {

        AdaptiveIndividual child = new AdaptiveIndividual();

        //uniform crossover
        for (int i = 0; i < child.adptvPacingStrategy.pacingStrategy.length; i++) {
            if (Parameters.rnd.nextFloat() > Parameters.pacingCrossoverProbability) {
                child.adptvPacingStrategy.pacingStrategy[i] += parent1.adptvPacingStrategy.pacingStrategy[i];
            } else {
                child.adptvPacingStrategy.pacingStrategy[i] += parent2.adptvPacingStrategy.pacingStrategy[i];
            }
        }

        int crossoverPoint = Parameters.rnd.nextInt(parent1.adptvTransitionStrategy.transitionStrategy.length);

        //one-point crossover
        for(int i = 0; i < crossoverPoint; i++){
            child.adptvTransitionStrategy.transitionStrategy[i] = parent1.adptvTransitionStrategy.transitionStrategy[i];
        }
        for(int i = crossoverPoint; i < parent2.adptvTransitionStrategy.transitionStrategy.length; i++){
            child.adptvTransitionStrategy.transitionStrategy[i] = parent2.adptvTransitionStrategy.transitionStrategy[i];
        }

        if (Parameters.rnd.nextFloat() > Parameters.pacingCrossoverProbability) {
            child.adptvPacingStrategy.sigma = parent1.adptvPacingStrategy.sigma;
        } else {
            child.adptvPacingStrategy.sigma = parent2.adptvPacingStrategy.sigma;
        }

        return child;
    }

    /**
     * Returns a COPY of the individual selected using tournament selection
     * @return
     */
    private AdaptiveIndividual tournamentSelection() {
        ArrayList<AdaptiveIndividual> candidates = new ArrayList<AdaptiveIndividual>();
        for(int i = 0; i < Parameters.tournamentSize; i++){
            candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
        }
        return getBest(candidates).copy();
    }


    private AdaptiveIndividual getBest(ArrayList<AdaptiveIndividual> aPopulation) {
        double bestFitness = Double.MAX_VALUE;
        AdaptiveIndividual best = null;
        for(AdaptiveIndividual individual : aPopulation){
            if(individual.getFitness() < bestFitness || best == null){
                best = individual;
                bestFitness = best.getFitness();
            }
        }
        return best;
    }

    private AdaptiveIndividual getWorst(ArrayList<AdaptiveIndividual> aPopulation) {
        double worstFitness = 0;
        AdaptiveIndividual worst = null;
        for(AdaptiveIndividual individual : population){
            if(individual.getFitness() > worstFitness || worst == null){
                worst = individual;
                worstFitness = worst.getFitness();
            }
        }
        return worst;
    }

    private void printPopulation() {
        for(AdaptiveIndividual individual : population){
            System.out.println(individual);
        }
    }

    private void initialisePopulation() {
        if (population.size() > 0) {
            population.clear();
        }

        while(population.size() < Parameters.popSize){
            AdaptiveIndividual individual = new AdaptiveIndividual();
            individual.initialise();
            individual.evaluate(teamPursuit);
            population.add(individual);

        }
    }
}
