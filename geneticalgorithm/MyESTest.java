package geneticalgorithm;


public class MyESTest {

	final static int generations = 1000;
    final static int populationSize = 120;
    
	public static void main(String[] args) {
		
		ESnew es = new ESnew(populationSize);
        System.out.println("Evolving ");
        for (int gen = 0; gen < generations; gen++)
        {
            es.nextGeneration();
            double bestResult = es.getBestFitnesses();
            System.out.println("Generation " + gen + " best " + bestResult);
            double secondResult = es.getNthFitness(1);
            System.out.println("Generation " + gen + " 2nd best " + secondResult);
        }
        es.nextGeneration();
        double bestResult = es.getBestFitnesses();
        System.out.println("Generation " + generations + " best " + bestResult);
        double secondResult = es.getNthFitness(1);
        System.out.println("Generation " + generations + " 2nd best " + secondResult);
	}
}