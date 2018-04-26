package halitecontroller;

import java.util.ArrayList;
import java.util.Arrays;

import hlt.Constants;
import hlt.DockMove;
import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Move.MoveType;
import hlt.Navigation;
import hlt.Planet;
import hlt.Player;
import hlt.Ship;
import hlt.Ship.DockingStatus;
import hlt.UndockMove;
import neuralnetwork.Layer;
import neuralnetwork.NeuralNetwork;

/* Input for commander
 * 
 * 
 * 
 * 
 */
public class Halite2Controller {
	
	public int me;
	public ArrayList<Integer> enemies;
	public ArrayList<Integer> planets;
	public GameMap gameMap;
	
	protected NeuralNetwork meNN;
	protected NeuralNetwork enemiesNN;
	protected NeuralNetwork planetsNN;
	protected NeuralNetwork commanderNN;
	
	public Halite2Controller(GameMap map) {
		gameMap = map;
		
		meNN = new NeuralNetwork(8, 8);
		meNN.randomWeights();
		enemiesNN = new NeuralNetwork(8, 8);
		enemiesNN.randomWeights();
		planetsNN = new NeuralNetwork(9, 9);
		planetsNN.randomWeights();
		commanderNN = new NeuralNetwork(33, 7);
		commanderNN.randomWeights();
	}
	
	public Halite2Controller(GameMap map, String meNNWeights, String enemiesNNWeights, String planetsNNWeights, String commanderNNWeights) {
		gameMap = map;
		
		meNN = new NeuralNetwork(8, 8);
		if (meNNWeights == null) {
			meNN.randomWeights();
		}
		else {
			meNN.loadWeights(meNNWeights);
		}
		
		enemiesNN = new NeuralNetwork(8, 8);
		if (enemiesNNWeights == null) {
			enemiesNN.randomWeights();
		}
		else {
			enemiesNN.loadWeights(enemiesNNWeights);
		}
		
		planetsNN = new NeuralNetwork(9, 9);
		if (planetsNNWeights == null) {
			planetsNN.randomWeights();
		}
		else {
			planetsNN.loadWeights(planetsNNWeights);
		}
		
		commanderNN = new NeuralNetwork(33, 7);
		if (commanderNNWeights == null) {
			commanderNN.randomWeights();
		}
		else {
			commanderNN.loadWeights(commanderNNWeights);
		}
	}
	
	public ArrayList<Move> getMoveList() {
		ArrayList<Move> moveList = new ArrayList<>();
		double[] info = initializeGameInfo();
		
		for (Ship ship: gameMap.getMyPlayer().getShips().values()) {
			double[] shipInfo = getShipInfo(ship);
			double[] input = new double[33];
			for (int i=0; i<33; i++) {
				if (i<8) {
					input[i] = shipInfo[i];
				}
				else {
					input[i] = info[i-8];
				}
			}
			double[] output = commanderNN.calc(input, true);
			Move move = getMove(output, ship);
			if (move != null) {
				moveList.add(move);
			}
			info = updateGameInfo(ship, info);
		}
		return moveList;
	}
	
	public Move getMove(double[] input, Ship ship) {
		double move = Double.NEGATIVE_INFINITY;
		int moveI = -1;
		for (int i=0; i<4; i++) {
			if ((i==1 && ship.getDockingStatus() == Ship.DockingStatus.Docked) || i!=1) {
				if (input[i] > move) {
					move = input[i];
					moveI = i;
				}
			}
		}
		
		Log.log("ShipID " + ship.getId() + " choose " + moveI);
		
		if (moveI == 0) { /*Noop*/
			return new Move(MoveType.Noop, ship);
		}
		else if (moveI == 1) {
			return new UndockMove(ship);
		}
		else if (moveI == 2) {
			ArrayList<Ship> ships = new ArrayList<Ship>();
			for (Player player: gameMap.getAllPlayers()) {
				if (player.getId() == gameMap.getMyPlayerId()) {
					continue;
				}
				for (Ship s: player.getShips().values()){
					ships.add(s);
				}
			}
			int shipI = (int)Math.floor(input[4]*ships.size());
			Log.log("Target Ship " + ships.get(shipI));
			return Navigation.navigateShipTowardsTarget(gameMap, ship, ships.get(shipI), Constants.MAX_SPEED, 
					true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
		}
		else if (moveI == 3) {
			ArrayList<Planet> planets = new ArrayList<Planet>();
			Log.log("planets " + planets);
			for (Planet planet: gameMap.getAllPlanets().values()) {
				if ((!planet.isOwned() || planet.getOwner() == gameMap.getMyPlayerId()) && !planet.isFull()) {
					if (ship.canDock(planet)) {
						return new DockMove(ship, planet);
					}
					planets.add(planet);
                }
			}
			return Navigation.navigateShipToDock(gameMap, ship, planets.get((int) Math.floor(input[5]*planets.size())), Constants.MAX_SPEED);
		}
		else {
			return new Move(MoveType.Noop, ship);
		}
	}
	
	public void saveWeights(String meNNFile, String enemiesNNFile, String planetsNNFile, String commanderNNFile) {
		meNN.writeWeightsToFile(meNNFile);
		enemiesNN.writeWeightsToFile(enemiesNNFile);
		planetsNN.writeWeightsToFile(planetsNNFile);
		commanderNN.writeWeightsToFile(commanderNNFile);
	}
	
	protected double[] getGameInfo() {
		double[] info = new double[25];
		
		double[] meInfo = new double[8];
		Arrays.fill(meInfo, 0);
		double[] enemiesInfo = new double[8];
		Arrays.fill(enemiesInfo, 0);
		double[] planetsInfo = new double[9];
		Arrays.fill(planetsInfo, 0);
		
		for (Ship ship: gameMap.getMyPlayer().getShips().values()) {
			double[] output = getShipInfo(ship);
			//output = sigmoidAll(output);
			for (int i=0; i<meInfo.length; i++) {
				meInfo[i] += output[i];
			}
			meInfo = meNN.calc(meInfo, false);
		}
		
		for (Player player: gameMap.getAllPlayers()) {
			if (player.getId() == gameMap.getMyPlayerId()) {
				continue;
			}
			for (Ship ship: player.getShips().values()) {
				double[] output = getShipInfo(ship);
				//output = sigmoidAll(output);
				for (int i=0; i<enemiesInfo.length; i++) {
					enemiesInfo[i] += output[i];
				}
				enemiesInfo = enemiesNN.calc(enemiesInfo, false);
			}
		}
		
		for (Planet planet: gameMap.getAllPlanets().values()) {
			double[] output = getPlanetInfo(planet);
			//output = sigmoidAll(output);
			for (int i=0; i<planetsInfo.length; i++) {
				planetsInfo[i] += output[i];
			}
			planetsInfo = planetsNN.calc(planetsInfo, false);
		}
		
		for (int i=0; i<info.length; i++) {
			if (i<8) {
				info[i] = meInfo[i];
			}
			else if (i<16) {
				info[i] = enemiesInfo[i-8];
			}
			else {
				info[i] = planetsInfo[i-16];
			}
		}
		
		return info;
	}
	
	protected double[] initializeGameInfo() {
		double[] info = new double[25];
		
		double[] meInfo = new double[8];
		Arrays.fill(meInfo, 0);
		double[] enemiesInfo = new double[8];
		Arrays.fill(enemiesInfo, 0);
		double[] planetsInfo = new double[9];
		Arrays.fill(planetsInfo, 0);
		
		for (Player player: gameMap.getAllPlayers()) {
			if (player.getId() == gameMap.getMyPlayerId()) {
				continue;
			}
			for (Ship ship: player.getShips().values()) {
				double[] output = getShipInfo(ship);
				//output = sigmoidAll(output);
				for (int i=0; i<enemiesInfo.length; i++) {
					enemiesInfo[i] += output[i];
				}
				enemiesInfo = enemiesNN.calc(enemiesInfo, false);
			}
		}
		
		for (Planet planet: gameMap.getAllPlanets().values()) {
			double[] output = getPlanetInfo(planet);
			//output = sigmoidAll(output);
			for (int i=0; i<planetsInfo.length; i++) {
				planetsInfo[i] += output[i];
			}
			planetsInfo = planetsNN.calc(planetsInfo, false);
		}
		
		for (int i=0; i<info.length; i++) {
			if (i<8) {
				info[i] = meInfo[i];
			}
			else if (i<16) {
				info[i] = enemiesInfo[i-8];
			}
			else {
				info[i] = planetsInfo[i-16];
			}
		}
		
		return info;
	}
	
	protected double[] updateGameInfo(Ship ship, double[] gameInfo) {
	
		double[] meInfo = Arrays.copyOf(gameInfo, 8);
		double[] info = Arrays.copyOf(gameInfo, gameInfo.length);
		
		double[] output = getShipInfo(ship);
		//output = sigmoidAll(output);
		for (int i=0; i<meInfo.length; i++) {
			meInfo[i] += output[i];
		}
		meInfo = meNN.calc(meInfo, false);
		
		for (int i=0; i<meInfo.length; i++) {
			info[i] = meInfo[i];
		}
		
		return info;
	}
	
	protected double[] sigmoidAll(double[] input) {
		double[] output = new double[input.length];
		
		for (int i=0; i<output.length; i++) {
			output[i] = Layer.sigmoid(input[i]);
		}
		return output;
	}
	
	protected double[] getShipInfo(Ship ship) {
		double[] output = new double[8];
		output[0] = ship.getXPos();
		output[1] = ship.getYPos();
		output[2] = ship.getHealth();
		output[3] = ship.getRadius();
		output[4] = DockingStatusToDouble(ship.getDockingStatus());
		output[5] = ship.getDockedPlanet();
		output[6] = ship.getDockingProgress();
		output[7] = ship.getWeaponCooldown();
		return output;
	}
	
	protected double[] getPlanetInfo(Planet planet) {
		double[] output = new double[9];
		output[0] = planet.getXPos();
		output[1] = planet.getYPos();
		output[2] = planet.getOwner();
		output[3] = planet.getHealth();
		output[4] = planet.getRadius();
		output[5] = planet.getRemainingProduction();
		output[6] = planet.getCurrentProduction();
		output[7] = planet.getDockingSpots();
		output[8] = planet.getDockedShips().size();
		return output;
	}
	
	protected double DockingStatusToDouble(DockingStatus s) {
		switch(s) {
		case Undocked:
			return 0;
		case Docking:
			return 1;
		case Docked:
			return 2;
		case Undocking:
			return 3;
		default:
			return -1;
		}
	}
}
