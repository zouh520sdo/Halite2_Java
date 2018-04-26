package halitecontroller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neuralnetwork.NeuralNetwork;

public class Halite2ControllerHelper {
	
	public String playerName;
	public Halite2Controller controller;
	public NeuralNetwork meNN;
	public NeuralNetwork enemiesNN;
	public NeuralNetwork planetsNN;
	public NeuralNetwork commanderNN;
	
	public List<NeuralNetwork> allNN;
	
	protected Runtime rt;
	protected String meNNFile;
	protected String enemiesNNFile;
	protected String planetsNNFile;
	protected String commanderNNFile;

	protected double fitness;
	
	public Halite2ControllerHelper (String name, boolean loadFromFile) {
		playerName = name;
		rt = Runtime.getRuntime();
		allNN = new ArrayList<NeuralNetwork>();
		// same name convention as HuangBot
		meNNFile = playerName + "_meNN.txt";
        enemiesNNFile = playerName + "_enemiesNN.txt";
        planetsNNFile = playerName + "_planetsNN.txt";
        commanderNNFile = playerName + "_commanderNN.txt";
        
        meNN = new NeuralNetwork(8, 8);
        if (loadFromFile) {
        	meNN.loadWeights(meNNFile);
        }
        else {
        	meNN.randomWeights();
        	meNN.writeWeightsToFile(meNNFile);
        }
		enemiesNN = new NeuralNetwork(8, 8);
		if (loadFromFile) {
			enemiesNN.loadWeights(enemiesNNFile);
		}
		else {
			enemiesNN.randomWeights();
			enemiesNN.writeWeightsToFile(enemiesNNFile);
		}
		planetsNN = new NeuralNetwork(9, 9);
		if (loadFromFile) {
			planetsNN.loadWeights(planetsNNFile);
		}
		else {
			planetsNN.randomWeights();
			planetsNN.writeWeightsToFile(planetsNNFile);
		}
		
		commanderNN = new NeuralNetwork(33, 7);
		if (loadFromFile) {
			commanderNN.loadWeights(commanderNNFile);
		}
		else {
			commanderNN.randomWeights();
			planetsNN.writeWeightsToFile(commanderNNFile);
		}
		allNN.add(meNN);
		allNN.add(enemiesNN);
		allNN.add(planetsNN);
		allNN.add(commanderNN);
	}
	
	public void runGame() {
		try {
			Process pr = rt.exec("halite -d \"240 160\" \"java HuangBot " + playerName + "\" \"python mellendo/Halite2/MyBot.py\"");
			pr.waitFor();
			String filename = playerName + ".txt";
			try {
				String line = null;
				FileReader reader = new FileReader(filename);
				BufferedReader bufferedReader = new BufferedReader(reader);
				List<Double> output = new ArrayList<Double>();
				while ((line = bufferedReader.readLine()) != null) {
					String[] numbers = line.split(" ");
					for (int i=0; i<numbers.length; i++) {
						output.add(Double.parseDouble(numbers[i]));
						// rank, shipNum, damage, lastframe
					}
				}
				bufferedReader.close();
				updateFitness(output);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateFitness(List<Double> result) {
		fitness = 0;
		if (result.get(0).intValue() == 1) {
			fitness += 20000;
		}
		fitness += 5000.0 / result.get(0);
		fitness += 10 * result.get(1);
		fitness += result.get(2) / 10.0;
		fitness += 10.0*(300-result.get(3));
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public void saveAllNN() {
		saveMeNN();
		saveEnemiesNN();
		savePlanetsNN();
		saveCommanderNN();
	}
	
	public void saveMeNN() {
		meNN.writeWeightsToFile(meNNFile);
	}
	
	public void saveEnemiesNN() {
		enemiesNN.writeWeightsToFile(enemiesNNFile);
	}
	
	public void savePlanetsNN() {
		planetsNN.writeWeightsToFile(planetsNNFile);
	}
	
	public void saveCommanderNN() {
		commanderNN.writeWeightsToFile(commanderNNFile);
	}
	
	public void randomAllNN() {
		meNN.randomWeights();
		enemiesNN.randomWeights();
		planetsNN.randomWeights();
		commanderNN.randomWeights();
		
		saveAllNN();
	}
}
