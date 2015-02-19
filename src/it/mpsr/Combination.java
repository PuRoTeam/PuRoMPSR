package it.mpsr;

import java.util.ArrayList;

public class Combination
{
	private ArrayList<ArrayList<Integer>> combinations; //array di tutte le combinazioni
	private int maxValue; //massimo valore possibile per uno slot
	private int numSlotPerCombination; //combinazioni composte da "numSlotPerCombination" numeri 
	
	/**
	 * Insieme di combinazioni possibili date stringhe di "numSlotPerCombination" numeri, dove ogni numero può assumere al massimo valore "maxValue"
	 */
	public Combination(int maxValue, int numSlotPerCombination)
	{
		combinations = new ArrayList<ArrayList<Integer>>(); 
		this.maxValue = maxValue;
		this.numSlotPerCombination = numSlotPerCombination;
		
		generateCombinations();
	}
	
	/**
	 * Generazione di tutte le combinazioni possibili
	 */
	private void generateCombinations()
	{
		for(int value = maxValue; value >= 0; value--)
		{
			ArrayList<Integer> combination = addEmptyCombination();
			recursiveGeneration(combination, maxValue, 0, value, 0);
		}
	}
	
	private void recursiveGeneration(ArrayList<Integer> combination, int valueToDistribute, int position, int valueToPutInPosition, int sumOfPreviouslyValues)
	{	
		combination.set(position, valueToPutInPosition);		
		
		int newPosition = position + 1;
		int newValueToDistribute = valueToDistribute - valueToPutInPosition;
		sumOfPreviouslyValues += valueToPutInPosition;
		
		if(sumOfPreviouslyValues != maxValue && newPosition >= numSlotPerCombination)
		{
			combinations.remove(combination);
			return;
		}
		
		if(newPosition >= numSlotPerCombination)
			return;
			
		if(newValueToDistribute <= 0)
		{
			fillOtherPositionWithZeroes(combination, newPosition);
			return;
		}
		
		for(int newValue = newValueToDistribute; newValue >= 0; newValue--)
		{
			ArrayList<Integer> curCombination = combination;
			ArrayList<Integer> previousCombination = combination;
			if(newValue < newValueToDistribute) //non è la prima iterazione
			{
				curCombination = addEmptyCombination();
				fillCombinationWithPreviousValues(curCombination, previousCombination);
			}					
			
			recursiveGeneration(curCombination, newValueToDistribute, newPosition, newValue, sumOfPreviouslyValues);
		}
	}
	
	private void fillOtherPositionWithZeroes(ArrayList<Integer> combination, int startPosition)
	{
		for(int i = startPosition; i < combination.size(); i++)
			combination.set(i, 0);
	}
	
	private ArrayList<Integer> addEmptyCombination()
	{
		ArrayList<Integer> newCombination = new ArrayList<Integer>();
		for(int i = 0; i < numSlotPerCombination; i++)
			newCombination.add(0);
		
		combinations.add(newCombination);
		return newCombination;
	}
	
	private void fillCombinationWithPreviousValues(ArrayList<Integer> newCombination, ArrayList<Integer> previousCombination)
	{
		for(int i = 0; i < numSlotPerCombination; i++)
			newCombination.set(i, previousCombination.get(i));
	}
	
	public void printCombinations()
	{
		for(int i = 0; i < combinations.size(); i++)
		{
			ArrayList<Integer> curCombination = combinations.get(i);			
			System.out.print(curCombination);
		}
	}
	
	public ArrayList<ArrayList<Integer>> getCombinations()
	{
		return combinations;
	}
	
	/*
	public static void main(String argc[])
	{
		int maxValue = 4; 
		int numSlotPerCombination = 4;
		Combination combination= new Combination(maxValue, numSlotPerCombination);
		
		combination.printCombinations();
	}*/
}
