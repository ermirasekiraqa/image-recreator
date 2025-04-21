package org.ImageRecreator;

public class Polygon {
    private final int[] genes; // The first three elements represent the RGB colors
    private long fitness;
    private final int numOfPoints;

    public Polygon(int[] genes) {
        this.genes = genes;
        numOfPoints = (genes.length - 3) / 2;
    }

    public int[] getGenes() {
        return genes;
    }

    public int getGene(int i) {
        return genes[i];
    }

    public void setGene(int i, int gene) {
        genes[i] = gene;
    }

    public long getFitness() {
        return fitness;
    }

    public void setFitness(long fitness) {
        this.fitness = fitness;
    }

    public int getNumOfPoints() {
        return numOfPoints;
    }

    public int[] getXCoordinates() {
        int[] xCoordinates = new int[numOfPoints];
        int index = 0;
        for (int i = 3; i < genes.length; i+=2) {
            xCoordinates[index] = genes[i];
            index++;
        }

        return xCoordinates;
    }

    public int[] getYCoordinates() {
        int[] yCoordinates = new int[numOfPoints];
        int index = 0;
        for (int i = 4; i < genes.length; i+=2) {
            yCoordinates[index] = genes[i];
            index++;
        }

        return yCoordinates;
    }
}