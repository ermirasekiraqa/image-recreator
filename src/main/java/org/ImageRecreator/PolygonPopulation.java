package org.ImageRecreator;

public class PolygonPopulation {
    private final Polygon[] population;

    public PolygonPopulation(int popSize) {
        population = new Polygon[popSize];
    }

    public Polygon getIndividual(int i) {
        return population[i];
    }

    public void setIndividual(int i, Polygon individual) {
        population[i] = individual;
    }

    public Polygon getFittestIndividual() {
        Polygon fittestIndividual = population[0];
        for (int i = 1; i < population.length; i++) {
            if (population[i].getFitness() < fittestIndividual.getFitness()) fittestIndividual = population[i];
        }

        return fittestIndividual;
    }

    public Polygon[] getTwoFittestIndividuals() {
        Polygon fittestIndividual = population[0];
        Polygon secondFittestIndividual = population[1];
        for (int i = 2; i < population.length; i++) {
            if (population[i].getFitness() < fittestIndividual.getFitness()) {
                secondFittestIndividual = fittestIndividual;
                fittestIndividual = population[i];
            } else if (population[i].getFitness() < secondFittestIndividual.getFitness()) {
                secondFittestIndividual = population[i];
            }
        }
        Polygon[] twoFittestIndividuals = new Polygon[2];
        twoFittestIndividuals[0] = fittestIndividual;
        twoFittestIndividuals[1] = secondFittestIndividual;

        return twoFittestIndividuals;
    }
}
