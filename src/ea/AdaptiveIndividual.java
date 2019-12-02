package ea;

import teamPursuit.*;

public class AdaptiveIndividual {


    AdaptivePacingStrategy adptvPacingStrategy = new AdaptivePacingStrategy();
    AdaptiveTransitionStrategy adptvTransitionStrategy = new AdaptiveTransitionStrategy();
    SimulationResult result = null;

    public AdaptiveIndividual() {

    }

    // this code evolves the transition strategy
    // an individual is initialised with a random strategy that will evolve
    // the pacing strategy is initialised with random values for cyclists

    public void initialise(){
        adptvPacingStrategy.initialise();
        adptvTransitionStrategy.initialise();
    }



    public void evaluate(TeamPursuit teamPursuit){
        try {
            result = teamPursuit.simulate(adptvTransitionStrategy.transitionStrategy, adptvPacingStrategy.pacingStrategy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public AdaptiveIndividual copy(){
        AdaptiveIndividual individual = new AdaptiveIndividual();
        for(int i = 0; i < adptvTransitionStrategy.length; i++){
            individual.adptvTransitionStrategy.transitionStrategy[i] = adptvTransitionStrategy.transitionStrategy[i];
        }
        for(int i = 0; i < adptvPacingStrategy.length; i++){
            individual.adptvPacingStrategy.pacingStrategy[i] = adptvPacingStrategy.pacingStrategy[i];
        }
        individual.evaluate(MixedTournamentsEA.teamPursuit);
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
        for(int i : adptvPacingStrategy.pacingStrategy){
            System.out.print(i + ",");
        }
        System.out.println();
        for(boolean b : adptvTransitionStrategy.transitionStrategy){
            if(b){
                System.out.print("true,");
            }else{
                System.out.print("false,");
            }
        }
        System.out.println("\r\n" + this);
    }

    public class AdaptivePacingStrategy {
        int[] pacingStrategy = new int[23];
        double sigma = 1.0;
        int length = 23;

        public AdaptivePacingStrategy() {

        }

        public void initialise(){
            for(int i = 0; i < pacingStrategy.length; i++){
                int randomNum = Parameters.rnd.nextInt((700 - 200) + 1) + 200;
                pacingStrategy[i] = randomNum;
            }
        }

    }

    public class AdaptiveTransitionStrategy {
        boolean[] transitionStrategy = new boolean[22];
        double sigma;
        int length = 22;

        public AdaptiveTransitionStrategy() {

        }

        public void initialise(){
            sigma = 1.0;
            for(int i = 0; i < transitionStrategy.length; i++){
                transitionStrategy[i] = Parameters.rnd.nextBoolean();
            }
        }

    }

}


