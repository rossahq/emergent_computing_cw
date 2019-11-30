package ea.diversityEAs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ea.Individual;
import ea.Parameters;
import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class EDFitnessSharingEA implements Runnable {

    // create a new team
    public static TeamPursuit teamPursuit = new WomensTeamPursuit();

    double ec_mating_threshold = 0.2; // sets a mating threshold in the phenotypic space


    private ArrayList<Individual> population = new ArrayList<Individual>();
    private int iteration = 0;
    private int runs = 0;

    public EDFitnessSharingEA() {
    //NOT WORKING
    }


    public static void main(String[] args) {
        EDFitnessSharingEA ea = new EDFitnessSharingEA();
        ea.run();
    }

    public void run() {
        initialisePopulation();

        System.out.println("finished init pop");
        iteration = 0;
        runs = 0;
        while(runs < Parameters.maxRuns) {
            runs++;
            iteration = 0;
            while (iteration < Parameters.maxIterations) {
                iteration++;
                Individual parent1 = tournamentSelection();
                Individual parent2 = tournamentSelection();

                //stop parents mating if the difference between shared fitness is less than the threshold
                double size = 0.0;
                int maxTries = 0;
                double SFparent1 = calculateSharedFitness(parent1);
                while (ec_mating_threshold >= size) {
                    double SFparent2 = calculateSharedFitness(parent2);
                    double x = SFparent1 - SFparent2;
                    double y = SFparent2 - SFparent1;
                    size = Math.abs(x * y);

                    if (maxTries < 10) {
                        parent2 = tournamentSelection();
                    } else {
                        parent2 = population.get(Parameters.rnd.nextInt(population.size()));
                    }
                    maxTries += 1;
                }
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

    private void writeResulstToFile(String result) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\ROSSA\\IdeaProjects\\emergent_computing_cw\\src\\results\\results", true));
        writer.append(' ');
        writer.append(result);

        writer.close();
    }

    private void printStats() {
        System.out.println("" + iteration + "\t" + getBest(population) + "\t" + getWorst(population));
    }

    //tournament replacement
    private void replace(Individual child) {
        ArrayList<Individual> candidates = new ArrayList<Individual>();
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

    private Individual crossover(Individual parent1, Individual parent2) {
        Individual child = new Individual();

        //uniform crossover
        for (int i = 0; i < child.pacingStrategy.length; i++) {
            if (Parameters.rnd.nextFloat() > Parameters.pacingCrossoverProbability) {
                child.pacingStrategy[i] += parent1.pacingStrategy[i];
            } else {
                child.pacingStrategy[i] += parent2.pacingStrategy[i];
            }
        }

        int crossoverPoint = Parameters.rnd.nextInt(parent1.transitionStrategy.length);

        //one-point crossover
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
        double denominator = 1.0;

        for (int j = 0; j < population.size(); j++) {
            double dist = euclideanDistance(i, j);
            if (dist < ec_mating_threshold) {
                denominator += (1 - (dist / ec_mating_threshold));
            }
        }

        return fitness/denominator;
    }

    private void initialisePopulation() {
        while(population.size() < Parameters.popSize){
            Individual individual = new Individual();
            individual.initialise();
            individual.evaluate(teamPursuit);
            population.add(individual);

        }
    }

    public double convertToRealValue(int[] someSolution){
        // map to real  number
        double min = 0.0;
        double max = 1.0;

        int length = someSolution.length;
        int newVal = 0;
        for (int i = 0; i < length; i++) {
            newVal += someSolution[i];
        }


        double val = min + (double)newVal*(max-min)/(Math.pow(2.0, (double)length)-1);

        System.out.println("SHARED FITNESS "  + val);

        return val;
    }

    public double euclideanDistance(Individual p1, int i){

        Individual parent = p1;
        Individual indivd = population.get(i);

        // get real value equivalents of bitstrings
        double r1 = convertToRealValue(parent.pacingStrategy);
        double r2 = convertToRealValue(parent.pacingStrategy);


        double distance = Math.sqrt(Math.pow(r1-r2, 2.0));
        return distance;
    }
}
