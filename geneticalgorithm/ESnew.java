package geneticalgorithm;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import halitecontroller.Halite2Controller;
import halitecontroller.Halite2ControllerHelper;
import neuralnetwork.NeuralNetwork;


public class ESnew {

	// manage an array of NN
	// evaluate parent
	//*use numbers divisible by 3
	// choose 1/3 elite
	// 1/3 for mutation & 1/3 for recombination
	public String outputFile = "best";
	private Halite2ControllerHelper[] population;
	private double[] fitness;
	private int elite;
	private Random r;
	public float ageCost = 1.5f;
	
	protected double mutationRate = 0.02;
	protected double preElitesFitness;
	
	public ESnew(int populationSize) {
		preElitesFitness = 0;
		// generate array of random NN
		this.population = new Halite2ControllerHelper[populationSize];
		for(int i = 0; i < population.length; i++) {
			population[i] = new Halite2ControllerHelper("Huang" + i, false);
		}
		this.fitness = new double[populationSize];
		this.elite = populationSize/4;
		//cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		r = new Random(System.currentTimeMillis());
		System.out.println("start");
		sortPopulationByFitness();
	}
	
	public ESnew(int populationSize, String filename) {
		preElitesFitness = 0;
		// generate array of random NN 
		this.population = new Halite2ControllerHelper[populationSize];
		this.fitness = new double[populationSize];
		this.elite = populationSize/4;
		
		population[0] = new Halite2ControllerHelper(filename, true);
		
		for(int i = 1; i < population.length; i++) {
			population[i] = new Halite2ControllerHelper("Huang" + i, false);
		}
		
		r = new Random(System.currentTimeMillis());
		System.out.println("start");
		sortPopulationByFitness();
	}
	
	public void nextGeneration() {
		// parents should be sorted
		// keep the best 1/3
		// mutate them as the second 1/3
		// recombine as the third 1/3
		// evaluate second and third 
		// sort
		/*
		for (int i=0; i<elite; i++) {
			population[i]
			fitness[i] -= ageCost;
		}
		*/
		for(int i = 0; i < elite; i++) {
			mutate(population[elite+i]);
		}
		
		AddNewGuys();
		OrderRecombineWithNew();
		
		//this.cmdLineOptions.setLevelRandSeed(r.nextInt());
		
		// set true in the main function
		//cmdLineOptions.setLevelRandSeed(r.nextInt());
		for(int i = elite; i < population.length; i++) {
			evaluate(i);
		}
		sortPopulationByFitness();
	}
	
	// mutate the agent
	private void mutate(Halite2ControllerHelper agent) {
		List<NeuralNetwork> allNN = agent.allNN;
		for (int j=0; j < allNN.size(); j++) {
			double[] weights = allNN.get(j).getWeights().clone();
			double[] mutationValue = new double[weights.length];
			//for(double d : mutationValue) {
			//	d = r.nextGaussian()*0.2;
			//}
			for(int i = 0; i < weights.length; i++) {
				mutationValue[i] = r.nextGaussian() * r.nextDouble() * mutationRate;
			}
			for(int i = 0; i < weights.length; i++) {
				weights[i] += mutationValue[i];
			}
			agent.allNN.get(j).loadWeights(weights);
		}
		agent.saveAllNN();
	}
	
	private void OrderRecombineWithNew() {
		List<Integer> indices = new ArrayList<Integer>(elite);
		for (int i=0; i<elite;i++) {
			indices.add(i);
		}
		int currentI = 0;
		while (!indices.isEmpty()) {
			int p1 = indices.remove((r.nextInt(indices.size())));
			for (int i=0; i<population[elite*2+currentI].allNN.size(); i++) {
				population[elite*2+currentI].allNN.get(i).loadWeights(RandomCrossover(population[p1].allNN.get(i).weights, population[elite* 3 + p1].allNN.get(i).weights));
			}
			population[elite*2+currentI].saveAllNN();
			currentI++;
		}
	}
	
	
	
	private void AddNewGuys() {
		for (int i=0; i< elite; i++) {
			population[elite*3+i].randomAllNN();
		}
	}
	
	// take in two NN and randomly cross over
	private double[] RandomCrossover(double[] w1, double[] w2) {
		double[] weights1 = w1.clone();
		double[] weights2 = w2.clone();
		for(int i = 0; i < weights1.length; i++) {
			int randint = r.nextInt(2);
			if (randint == 0)
				weights1[i] = weights2[i];
		}
		return weights1;
	}
	
	private void evaluate(int which) {
		// run one map? many map? difficulty?
		// collect score and progress
		population[which].runGame();
		fitness[which] = population[which].getFitness();
	}
	
	
	// higher >> lower
	private void sortPopulationByFitness()
    {
        for (int i = 0; i < population.length; i++)
        {
            for (int j = i + 1; j < population.length; j++)
            {
                if (fitness[i] < fitness[j])
                {
                    swap(i, j);
                }
            }
        }
    }
	
	// swap both fitness and population
	private void swap(int i, int j)
    {
        double cache = fitness[i];
        fitness[i] = fitness[j];
        fitness[j] = cache;
        Halite2ControllerHelper gcache = population[i];
        population[i] = population[j];
        population[j] = gcache;
    }
	
	public double getBestFitnesses() {

		population[0].meNN.writeWeightsToFile(outputFile + "_meNN.txt");
		population[0].enemiesNN.writeWeightsToFile(outputFile + "_enemiesNN.txt");
		population[0].planetsNN.writeWeightsToFile(outputFile + "_planetsNN.txt");
		population[0].commanderNN.writeWeightsToFile(outputFile + "_commanderNN.txt");
	
		return fitness[0];
	}
	
	public double getNthFitness(int n) {
		population[n].meNN.writeWeightsToFile(outputFile + "_meNN.txt");
		population[n].enemiesNN.writeWeightsToFile(outputFile + "_enemiesNN.txt");
		population[n].planetsNN.writeWeightsToFile(outputFile + "_planetsNN.txt");
		population[n].commanderNN.writeWeightsToFile(outputFile + "_commanderNN.txt");
		return fitness[n];
	}
}