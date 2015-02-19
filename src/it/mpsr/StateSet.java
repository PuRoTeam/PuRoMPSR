package it.mpsr;

import java.util.ArrayList;

public class StateSet 
{
	private int numJobsInSystem; //numero di job nell'impianto
	private int numCenter; //numero di centri con coda nell'impianto
	private ArrayList<State> statesSpace; //spazio degli stati
	private ArrayList<Center> centersSchema; //schema dei centri (necessario per creare i singoli stati)	

	/**
	 * Spazio degli stati, ossia insieme di tutti gli stati in cui è possibile si trovi un sistema
	 * @param centersSchema: Schema dei centri indicante quali hanno code e quali sono Infinite Server
	 * @param numJobsInSystem: Numero totale di job nel sistema
	 */
	public StateSet(ArrayList<Center> centersSchema, int numJobsInSystem)
	{
		this.numJobsInSystem = numJobsInSystem;
		this.numCenter = centersSchema.size();
		statesSpace = new ArrayList<State>();

		this.centersSchema = new ArrayList<Center>();		
		for(int i = 0; i < centersSchema.size(); i++)
			this.centersSchema.add(centersSchema.get(i));

		//calculateFeasibleState();
	}	

	/**
	 * Calcola tutti gli stati in cui è possibile si trovi il sistema
	 */	
	public void calculateFeasibleState() 
	{
		Combination combination = new Combination(numJobsInSystem, numCenter);
		ArrayList<ArrayList<Integer>> allCombinations = combination.getCombinations(); //genera tutte le combinazioni possibili
		
		for(int i = 0; i < allCombinations.size(); i++) //per ogni combinazione aggiunge il relativo stato
		{
			ArrayList<Integer> numberString = allCombinations.get(i);

			State newState = new State();

			for(int j = 0; j < numCenter; j++) //pari alla size di numberString e centersSchema
			{
				Center curSchemaCenter = centersSchema.get(j);
				int curNit = numberString.get(j);

				Center newStateCenter = new Center(curSchemaCenter.getName(), curNit, curSchemaCenter.getServiceTime(), curSchemaCenter.isQueueCenter());
				newState.getCentersList().add(newStateCenter);
			}
			statesSpace.add(newState);
		}
	}

	public ArrayList<Center> getCentersSchema()
	{
		return centersSchema;
	}
	
	public int getNumJobsInSystem()
	{
		return numJobsInSystem;
	}
	
	public int getNumCenter()
	{
		return numCenter;
	}
	
	public ArrayList<State> getStatesSpace()
	{
		return statesSpace;
	}
	
	public void printAllState()
	{
		for(int i = 0; i < statesSpace.size(); i++)
		{
			ArrayList<Center> centersList = statesSpace.get(i).getCentersList();
			
			for(int j = 0; j < centersList.size(); j++)
				System.out.println(centersList.get(j).getJobInQueue());
			System.out.println("***");
		}
	}
	
	/*
	public static void main(String[] args) 
	{
		int numJobsInSystem = 5;
		int numCenter = 4;
		
		ArrayList<Center> centersSchema = new ArrayList<Center>();
		for(int i = 0; i < numCenter; i++)
			centersSchema.add(new Center(0, 1.0, true));
		
		StateSet stateSet = new StateSet(centersSchema, numJobsInSystem);
		stateSet.printAllState();
		
		System.out.println("State Space Size: " + stateSet.getStatesSpace().size());
	}
	*/
	
}
