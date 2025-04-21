package org.ImageRecreator;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.IntStream;

public class GeneticAlgorithm {
    private static int tournamentSize; // This may change depending on the population size
//    private static final int sigma = 128; // Represents the Standard Deviation for Gaussian Mutation
    private static final float mutationRate = 0.1f;
    private final int maxWidth;
    private final int maxHeight;
    private final int popSize;
    private final byte numOfPointsPerPolygon;
    private final int
            generations;
    private final FitnessCalculator calculator;
    private final Random random = new Random();

    public GeneticAlgorithm(int popSize, byte numOfPointsPerPolygon, int generations, BufferedImage targetImg) {
        this.popSize = popSize;
        this.numOfPointsPerPolygon = numOfPointsPerPolygon;
        this.generations = generations;
        this.maxWidth = targetImg.getWidth();
        this.maxHeight = targetImg.getHeight();
        calculator = new FitnessCalculator(targetImg);
        tournamentSize = (popSize / 2);
    }

    public BufferedImage runGeneticAlgorithm() {
        // Initialize population
        PolygonPopulation generation = initializePopulation();
        Polygon fittest = generation.getFittestIndividual();
        for (int i = 0; i < generations; i++) {
            if (i > 0)  {
                generation = evolvePopulation(generation);
                fittest = generation.getFittestIndividual();
            }
            calculator.paintImage(fittest, true);
            System.out.println("Generation: " + i + ", Fittest: " + fittest.getFitness());
        }

        return calculator.getPaintedImg();
    }

    private PolygonPopulation initializePopulation() {
        PolygonPopulation population = new PolygonPopulation(popSize);
        for(int i = 0; i < popSize; i++) {
            // Randomly generate each gene
            int[] genes = generateGenes();
            population.setIndividual(i, new Polygon(genes));
            long fitness = calculator.calculateFitnessSIMD(population.getIndividual(i));
            population.getIndividual(i).setFitness(fitness);
        }

        return population;
    }

    private PolygonPopulation evolvePopulation(PolygonPopulation initialPopulation) {
        PolygonPopulation evolvedPopulation = new PolygonPopulation(popSize);
        IntStream.range(0, popSize).parallel().forEach(i -> {
            // Keep the fittest individual of each generation (ELITISM)
            if (i == 0) {
                Polygon fittest = initialPopulation.getFittestIndividual();
                Polygon mutatedFittest = mutation(fittest, 0.5f);
                mutatedFittest.setFitness(calculator.calculateFitnessSIMD(mutatedFittest));
                if (mutatedFittest.getFitness() > fittest.getFitness())
                    evolvedPopulation.setIndividual(i, fittest);
                else
                    evolvedPopulation.setIndividual(i, mutatedFittest);
            }
            else {
                // Select parents through Tournament Selection
                Polygon[] parents = tournamentSelection(initialPopulation);
                // Create offspring using Uniform Crossover and mutate genes of offspring
                Polygon offspring = mutation(crossover(parents[0], parents[1]), mutationRate);
                evolvedPopulation.setIndividual(i, offspring);
                long fitness = calculator.calculateFitnessSIMD(offspring);
                evolvedPopulation.getIndividual(i).setFitness(fitness);
            }
        });

        return evolvedPopulation;
    }

    private int[] generateGenes() {
        int[] genes = new int[3 + 2 * numOfPointsPerPolygon];
        genes[0] = random.nextInt(256); // RED
        genes[1] = random.nextInt(256); // GREEN
        genes[2] = random.nextInt(256); // BLUE
        for (int j = 3; j < genes.length; j+=2) {
            // Generate the coordinates for the Polygon points
            genes[j] = random.nextInt(maxWidth); // x coordinate of a point
            genes[j + 1] = random.nextInt(maxHeight); // y coordinate of a point
        }

        return genes;
    }

    private Polygon[] tournamentSelection(PolygonPopulation population) {
        PolygonPopulation tournamentPopulation = new PolygonPopulation(tournamentSize);
        for (int i = 0; i < tournamentSize; i++) {
            int index = random.nextInt(popSize);
            tournamentPopulation.setIndividual(i, population.getIndividual(index));
        }
        return tournamentPopulation.getTwoFittestIndividuals();
    }

    private Polygon crossover (Polygon firstParent, Polygon secondParent) {
        int [] genes = new int[3 + 2 * numOfPointsPerPolygon];
        for (int i = 0; i < genes.length; i++) {
            if (random.nextBoolean()) genes[i] = firstParent.getGene(i);
            else genes[i] = secondParent.getGene(i);
        }

        return new Polygon(genes);
    }

//    private Polygon mutation(Polygon individual, float mutationRate, short sigma) {
//        short[] genes = individual.getGenes();
//        for (int i = 0; i < genes.length; i++) {
//            if (random.nextFloat() < mutationRate) {
//                short currentGene = genes[i];
//                short mutatedGene = (short) (currentGene + random.nextGaussian() * sigma);
//                if (i < 3)
//                    mutatedGene = (short) clamp(mutatedGene, 0, 255);
//                else if ( i % 2 == 1)
//                    mutatedGene = (short) clamp(mutatedGene, 0, maxWidth);
//                else
//                    mutatedGene = (short) clamp(mutatedGene, 0, maxHeight);
//
//                individual.setGene(i, mutatedGene);
//            }
//        }
//
//        return individual;
//    }

    private Polygon mutation(Polygon individual, float mutationRate) {
        int[] genes = individual.getGenes();
        for (int i = 0; i < genes.length; i++) {
            if (random.nextFloat() < mutationRate) {
                int mutatedGene;
                if (i < 3)
                    mutatedGene = random.nextInt(256);
                else if ( i % 2 == 1)
                    mutatedGene = random.nextInt(maxWidth);
                else
                    mutatedGene = random.nextInt(maxHeight);

                individual.setGene(i, mutatedGene);
            }
        }

        return individual;
    }
}
