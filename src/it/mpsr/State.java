package it.mpsr;

import java.math.BigDecimal;
import java.util.ArrayList;

public class State 
{
	private ArrayList<Center> centersList; //rappresentazione della situazione dei centri in un determinato istante di tempo	
	private BigDecimal probability; //probabilità di stato
	
	/**
	 * Stato rappresentante la situazione del sistema (in un dato istante) in termini di numero di job per ogni centro del sistema
	 */
	public State()
	{
		this.centersList = new ArrayList<Center>();
		this.probability = new BigDecimal(0.0); //viene valorizzato nell'algoritmo di GordonNewell
	}		

	public ArrayList<Center> getCentersList() 
	{
		return centersList;
	}
	
	public BigDecimal getProbability()
	{
		return probability;
	}
	
	public void setProbability(BigDecimal probability)
	{
		this.probability = new BigDecimal(probability.toString());
	}
}
