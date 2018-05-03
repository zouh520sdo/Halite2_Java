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
	public int turn;
	public List<NeuralNetwork> allNN;
	
	protected Runtime rt;
	protected String meNNFile;
	protected String enemiesNNFile;
	protected String planetsNNFile;
	protected String commanderNNFile;
	protected List<Double> fitItems;

	protected double fitness;
	
	public Halite2ControllerHelper (String name, boolean loadFromFile) {
		turn = 0;
		fitness = 0;
		playerName = name;
		rt = Runtime.getRuntime();
		allNN = new ArrayList<NeuralNetwork>();
		fitItems = new ArrayList<Double>();
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
			Process pr = rt.exec("halite -d \"240 160\" \"java HuangBot " + playerName + "\" \"py mellendo/Halite2/MyBot.py\"");
			pr.waitFor();
			String filename = playerName + ".txt";
			try {
				String line = null;
				FileReader reader = new FileReader(filename);
				BufferedReader bufferedReader = new BufferedReader(reader);
				fitItems.clear();
				while ((line = bufferedReader.readLine()) != null) {
					String[] numbers = line.split(" ");
					for (int i=0; i<numbers.length; i++) {
						fitItems.add(Double.parseDouble(numbers[i]));
						// rank, shipNum, damage, lastframe
					}
				}
				bufferedReader.close();
				updateFitness(fitItems);
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
		double temp = 0;
		temp += (10000.0 / (result.get(0) * result.get(0)));
		temp += (10 * result.get(1));
		temp += (result.get(2) / 10.0);
		//fitness += 10.0*(300-result.get(3));
		fitness = (fitness * turn + temp) / (double)(turn + 1);
		turn++;
	}
	
	public String fitItemsToString() {
		String output = "";
		output += "10000.0 / (" + fitItems.get(0) + " * " + fitItems.get(0) + ") = " + (10000.0 / (fitItems.get(0) * fitItems.get(0))) + "\n";
		output += "10 * " + fitItems.get(1) + " = " + (10*fitItems.get(1)) + "\n";
		output += fitItems.get(2) + " / 10 = " + (fitItems.get(2) / 10.0) + "\n";
		return output;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public void saveAllNN() {
		fitness = 0;
		turn = 0;
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
