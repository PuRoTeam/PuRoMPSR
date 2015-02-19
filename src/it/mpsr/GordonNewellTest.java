package it.mpsr;

import java.util.ArrayList;

public class GordonNewellTest 
{
	private GordonNewell gn;
	
	public GordonNewellTest(){
		
		int numJobsInSystem = 35; 
		
		ArrayList<Double> variables_x = new ArrayList<Double>();
		
		variables_x.add(new Double(1)); //xC1
		variables_x.add(new Double(0.892857142)); //xFE
		variables_x.add(new Double(0.238095238)); //xBE    
		variables_x.add(new Double(19.833333325)); //xC2
		
		//(1: Client 1, 2: Front End, 3: Back End, 4: Client 2)
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center("Client 1", 0, 7.0, false)); //Client 1: IS
		centersSchema.add(new Center("FE Server", 0, 0.3, true));  //FE: QUEUE
		centersSchema.add(new Center("BE Server", 0, 0.08, true));  //BE: QUEUE
		centersSchema.add(new Center("Client 2", 0, 7.0, false)); //Client 2: IS
		
		/*variables_x.add(new Double(5)); //xC1
		variables_x.add(new Double(5)); //xFE
		variables_x.add(new Double(2.5)); //xBE
		
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center(0, 0.05, true));
		centersSchema.add(new Center(0, 0.05, true)); 
		centersSchema.add(new Center(0, 0.05, true));*/
		
		GordonNewell gn = new GordonNewell(centersSchema, variables_x, numJobsInSystem);
		
		this.gn = gn;
	}
	public GordonNewell getGn() {
		return gn;
	}
	
	public static void main(String[] args) 
	{
		int numJobsInSystem = 31; 
		
		ArrayList<Double> variables_x = new ArrayList<Double>();
		
		variables_x.add(new Double(1)); //xC1
		variables_x.add(new Double(0.892857142)); //xFE
		variables_x.add(new Double(0.238095238)); //xBE    
		variables_x.add(new Double(19.833333325)); //xC2
		
		//(1: Client 1, 2: Front End, 3: Back End, 4: Client 2)
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center("Client 1", 0, 7.0, false)); //Client 1: IS
		centersSchema.add(new Center("FE Server", 0, 0.3, true));  //FE: QUEUE
		centersSchema.add(new Center("BE Server", 0, 0.08, true));  //BE: QUEUE
		centersSchema.add(new Center("Client 2", 0, 7.0, false)); //Client 2: IS
		
		/*variables_x.add(new Double(5)); //xC1
		variables_x.add(new Double(5)); //xFE
		variables_x.add(new Double(2.5)); //xBE
		
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center(0, 0.05, true));
		centersSchema.add(new Center(0, 0.05, true)); 
		centersSchema.add(new Center(0, 0.05, true));*/
		
		GordonNewell gordonNewell = new GordonNewell(centersSchema, variables_x, numJobsInSystem);
		gordonNewell.start();
				
		gordonNewell.printAllOnStd();
		gordonNewell.printAllOnFile();
	}
	
}
