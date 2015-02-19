package it.mpsr;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MeanValueAnalysisTest 
{
	private MeanValueAnalysis mva;
	
	public MeanValueAnalysisTest(){
		
		//(1: Client 1, 2: Front End, 3: Back End, 4: Client 2, 5: Client 1 Reject, 6; Client 2 Reject)
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center("Client 1", 0, 7.0, false)); //Client 1: IS
		centersSchema.add(new Center("FE Server", 0, 0.3, true));  //FE: QUEUE
		centersSchema.add(new Center("BE Server", 0, 0.08, true));  //BE: QUEUE
		centersSchema.add(new Center("Client 2", 0, 7.0, false)); //Client 2: IS
		centersSchema.add(new Center("Client 1 rej", 0, 0.05, false)); //Client 1 Reject: IS
		centersSchema.add(new Center("Client 2 rej", 0, 0.05, false)); //Client 2 Reject: IS
		
		BigDecimal threshold = new BigDecimal(0.25); //delta
		BigDecimal probabilityOfRoutingFromBeToClient2 = new BigDecimal(new Double(0.952).toString()); //routing da BE a Client 2
		int numJobInSystem = 35;
		
		MeanValueAnalysis mva = new MeanValueAnalysis(centersSchema, threshold, probabilityOfRoutingFromBeToClient2, numJobInSystem);
		
		this.mva = mva;
		
	}
	
	public MeanValueAnalysis getMva() {
		return mva;
	}
	
	public static void main(String args[])
	{
		//(1: Client 1, 2: Front End, 3: Back End, 4: Client 2, 5: Client 1 Reject, 6; Client 2 Reject)
		ArrayList<Center> centersSchema = new ArrayList<Center>();

		centersSchema.add(new Center("Client 1", 0, 7.0, false)); //Client 1: IS
		centersSchema.add(new Center("FE Server", 0, 0.3, true));  //FE: QUEUE
		centersSchema.add(new Center("BE Server", 0, 0.08, true));  //BE: QUEUE
		centersSchema.add(new Center("Client 2", 0, 7.0, false)); //Client 2: IS
		centersSchema.add(new Center("Client 1 rej", 0, 0.05, false)); //Client 1 Reject: IS
		centersSchema.add(new Center("Client 2 rej", 0, 0.05, false)); //Client 2 Reject: IS
		
		BigDecimal threshold = new BigDecimal(0.25); //delta
		BigDecimal probabilityOfRoutingFromBeToClient2 = new BigDecimal(new Double(0.952).toString()); //routing da BE a Client 2
		int numJobInSystem = 31;
		
		MeanValueAnalysis mva = new MeanValueAnalysis(centersSchema, threshold, probabilityOfRoutingFromBeToClient2, numJobInSystem);
		
		mva.start();
		
		mva.printAllOnStd();
		mva.printAllOnFile();
	}
	
}
