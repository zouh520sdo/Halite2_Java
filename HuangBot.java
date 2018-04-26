import java.util.ArrayList;

import halitecontroller.Halite2Controller;
import hlt.Constants;
import hlt.DockMove;
import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Navigation;
import hlt.Networking;
import hlt.Planet;
import hlt.Ship;
import hlt.ThrustMove;

public class HuangBot {

	public static void main(String[] args) {
		
		String playerName = "Huang";
		String meNN = null;
		String enemiesNN = null;
		String planetsNN = null;
		String commanderNN = null;
		if (args.length > 0) {
			playerName = args[0];
	        meNN = playerName + "_meNN.txt";
	        enemiesNN = playerName + "_enemiesNN.txt";
	        planetsNN = playerName + "_planetsNN.txt";
	        commanderNN = playerName + "_commanderNN.txt";
		}
		
		final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize(playerName);
        final Halite2Controller controller = new Halite2Controller(gameMap, meNN, enemiesNN, planetsNN, commanderNN);
        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            /*
            for (final Planet planet: gameMap.getAllPlanets().values()) {
            	Log.log("Planet " + planet);
            }
            */
            
            moveList.addAll(controller.getMoveList());   
            Networking.sendMoves(moveList);
        }
	}

}
