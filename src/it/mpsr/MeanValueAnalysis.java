package it.mpsr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MeanValueAnalysis 
{	
	private ArrayList<Center> centersSchema; //insieme di tutti i centri del sistema
	private ArrayList<Center> queueCenters; //insieme dei centri con coda
	private HashMap<Integer, BigDecimal> saturationProbabilites; //probabilità di saturazione dei centri con coda per ogni valore di S
	private BigDecimal threshold; //soglia superiore probabilità di saturazione
	private BigDecimal probabilityOfRoutingFromBeToClient2; //probabilità di routing da back end a Client 2
	private int numJobInSystem; //numero di job nel sistema
	
	private ArrayList<Couple> couplesOfS; //insieme di tutte le coppie (S1, S2) avendo escluso le probabilità di saturazione oltre la soglia threshold
	
	private ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> averagePopulation; //per ogni coppia (S1, S2), per ogni centro, per ogni N contiene la popolazione media
	private ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> averageResponseTime; //per ogni coppia (S1, S2), per ogni centro, per ogni N contiene il tempo di risposta medio
	private ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> throughput; //per ogni coppia (S1, S2), per ogni centro, per ogni N contiene il throughput
	
	private HashMap<Center, BigDecimal> averageGlobalResponseTime; //tempo globale medio di risposta visto da ogni centor
	private HashMap<Center, BigDecimal> averageGlobalCycleTime; //tempo globale medio di ciclo visto da ogni centor
	private HashMap<Center, BigDecimal> globalThroughput; //throughput globale visto da ogni centro
	private HashMap<Couple, ArrayList<BigDecimal>> yHashMap; //insieme delle y per ogni coppia (S1, S2)
	private HashMap<Couple, BigDecimal[][]> yRatioHashMap; //insieme delle y ratio per ogni coppia (S1, S2)
	private HashMap<Center, BigDecimal> utilization; //utilizzazione di ogni centro
	private HashMap<Couple, BigDecimal> globalResponseTimeForCenter1ForAllCouple; //tempo globale medio di risposta per ogni coppia (S1, S2)
	private ArrayList<Integer> allS; //tutte le S delle probabilità di saturazione maggiori della soglia
	
	public final static int SCALE = 20;
	
	public final static String folder = "MVA";
	
	private int minGlobalResponseTimeCoupleIndex; //indice coppia (S1, S2) che minimizza il tempo di risposta GLOBALE del centro 1
	//private int minLocalResponseTimeCoupleIndex; //indice coppia (S1, S2) che minimizza il tempo di risposta LOCALE del centro 1
	
	/**
	 * Algoritmo Mean Value Analysis
	 * @param centersSchema: struttura centri (1: Client 1, 2: Front End, 3: Back End, 4: Client 2, 5: Client 1 Reject, 6; Client 2 Reject)
	 * @param threshold: soglia massima probabilità di saturazione
	 * @param probabilityOfRoutingFromBeToClient2: probabilità di routing da Back End (Centro 3) a Client 2 (Centro 4)
	 * @param numJobInSystem: numero di job nel sistema
	 */
	public MeanValueAnalysis(ArrayList<Center> centersSchema, BigDecimal threshold, BigDecimal probabilityOfRoutingFromBeToClient2, int numJobInSystem)
	{
		//inizializza centersSchema, queueCenters
		this.centersSchema = new ArrayList<Center>();
		this.queueCenters = new ArrayList<Center>();
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			this.centersSchema.add(curCenter);
			
			if(curCenter.isQueueCenter())
				this.queueCenters.add(curCenter);
		}
		
		this.threshold = threshold;
		
		averagePopulation = new ArrayList<ArrayList<HashMap<Integer, BigDecimal>>>();
		averageResponseTime = new ArrayList<ArrayList<HashMap<Integer, BigDecimal>>>();
		throughput = new ArrayList<ArrayList<HashMap<Integer, BigDecimal>>>();
		
		averageGlobalResponseTime = new HashMap<Center, BigDecimal>();
		averageGlobalCycleTime = new HashMap<Center, BigDecimal>();
		globalThroughput = new HashMap<Center, BigDecimal>();
		
		saturationProbabilites = new HashMap<Integer, BigDecimal>();
		couplesOfS = new ArrayList<Couple>();
		this.probabilityOfRoutingFromBeToClient2 = probabilityOfRoutingFromBeToClient2;
		this.numJobInSystem = numJobInSystem;	
		
		yHashMap = new HashMap<Couple, ArrayList<BigDecimal>>();
		yRatioHashMap = new HashMap<Couple, BigDecimal[][]>();
		utilization = new HashMap<Center, BigDecimal>();
		
		minGlobalResponseTimeCoupleIndex = -1;
		//minLocalResponseTimeCoupleIndex = -1;
		
		globalResponseTimeForCenter1ForAllCouple = new HashMap<Couple, BigDecimal>();
		allS = new ArrayList<Integer>();
	}
	
	/**
	 * Avvia l'algoritmo e calcola gli indici locali e globali
	 */
	public void start()
	{
		loadSaturationProbabilitiesUnderThresholdFromFile();
		generateCoupleOfS();
		mva();
		findIndexOfMinimumGlobalResponseTimeOfCenter1();
		//findIndexOfMinimumLocaleResponseTimeOfCenter1();
		calcuateAverageGlobalResponseTime();
		calculateAverageGlobaleCycleTime();
		calculateGlobalThroughput();
		calculateUtilization();
	}
	
	/**
	 * Carica le probabilità di saturazione e ne preleva un sottoinsieme il cui valore è minore della soglia "threshold"
	 */
	private void loadSaturationProbabilitiesUnderThresholdFromFile()
	{
		try 
		{
			BufferedReader saturationProbabilitiesReader = new BufferedReader(new FileReader("Gordon/saturationProbabilities.txt"));
			
			String line = saturationProbabilitiesReader.readLine(); //prima linea vuota
			while(line != null)
			{
				line = saturationProbabilitiesReader.readLine();
				
				if(line != null)
				{
					int index = line.indexOf(":");					
					String probability = line.substring(index + 1).trim();
					BigDecimal bgProbability = new BigDecimal(probability);
					
					if(bgProbability.compareTo(threshold) <= 0)
					{
						int indexOfOpenBracket = line.indexOf("(");
						int indexOfCloseBracket = line.indexOf(")");					
						String saturationNumber = line.substring(indexOfOpenBracket + 1, indexOfCloseBracket);
						Integer S = new Integer(saturationNumber);				
						allS.add(S);
						
						saturationProbabilites.put(S, bgProbability);					
						//System.out.println(S + ": " + saturationProbabilites.get(S));
					}
				}	
			}
			
			System.out.println("Saturation Probabilities Size: " + saturationProbabilites.size());			
			saturationProbabilitiesReader.close();
		} catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	/**
	 * Genera tutte le coppie (S1, S2) dalle probabilità di saturazione precedentemente caricate
	 */
	private void generateCoupleOfS()
	{		
		for(int i = 0; i < allS.size(); i++)
		{
			for(int j = 0; j < allS.size(); j++)
			{
				Couple newCouple = new Couple(allS.get(i), allS.get(j));
				couplesOfS.add(newCouple);
								
				averagePopulation.add(new ArrayList<HashMap<Integer, BigDecimal>>());
				averageResponseTime.add(new ArrayList<HashMap<Integer, BigDecimal>>());
				throughput.add(new ArrayList<HashMap<Integer, BigDecimal>>());
			}
		}
		
		for(int i = 0; i < couplesOfS.size(); i++)
		{
			for(int centerIndex = 0; centerIndex < centersSchema.size(); centerIndex++)
			{
				averagePopulation.get(i).add(new HashMap<Integer, BigDecimal>());
				averageResponseTime.get(i).add(new HashMap<Integer, BigDecimal>());
				throughput.get(i).add(new HashMap<Integer, BigDecimal>());
			}
		}
		
		System.out.println("Couple Of S size: " + couplesOfS.size());
	}
	
	/**
	 * Esecuzione dell'algoritmo Mean Value Analysis	
	 */
	private void mva()  
	{
		for(int i = 0; i < couplesOfS.size(); i++)
		{
			Couple curCouple = couplesOfS.get(i);
			Integer S1 = curCouple.getS1();
			Integer S2 = curCouple.getS2();
			
			ArrayList<BigDecimal> yList = linearSystem(saturationProbabilites.get(S1), saturationProbabilites.get(S2), probabilityOfRoutingFromBeToClient2);
			yHashMap.put(curCouple, yList);
			
			BigDecimal[][] yRatio = calculateYRatio(yList);
			yRatioHashMap.put(curCouple, yRatio);
			
			System.out.println(i);
			
			//if(i == 101)
			//	System.out.println(yList);
			
			for(int centerIndex = 0; centerIndex < centersSchema.size(); centerIndex++)
			{
				for(int n = 1; n <= numJobInSystem; n++)
				{
					avgPopulation(i, centerIndex, n, yList, yRatio); //meglio chiamare come prima funzione questa, in modo da calcolarne anche il valore per N = numJobInSystem (altrimenti saltato)
				}
			}
		}				
	}
	
	/**
	 * Calcola i valori di ogni y data la coppia (S1, S2) e la probabilità di routing da Back End a Client 2
	 */
	private ArrayList<BigDecimal> linearSystem(BigDecimal s1, BigDecimal s2, BigDecimal probabilityOfRoutingFromBeToClient2)
	{
		BigDecimal p = probabilityOfRoutingFromBeToClient2;
		
		BigDecimal y1 = new BigDecimal(1);
		BigDecimal y2 = new BigDecimal(1);
		BigDecimal y3 = new BigDecimal(1);
		BigDecimal y4 = new BigDecimal(1);
		BigDecimal y5 = new BigDecimal(1);
		BigDecimal y6 = new BigDecimal(1);
		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		
		y2 = y1.multiply((new BigDecimal(1).subtract(s1)).divide(new BigDecimal(1).subtract(p), SCALE, BigDecimal.ROUND_HALF_DOWN)); // ((1-s1)/(1-p))*y1
		y3 = y2;
		y4 = p.multiply(new BigDecimal(1).subtract(s1)).multiply(y1).divide(new BigDecimal(1).subtract(p), SCALE, BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(1).subtract(s2), SCALE, BigDecimal.ROUND_HALF_DOWN); // (p*(1-s1)*y1)/((1-s1)*(1-p))
		y5 = s1.multiply(y1); // s1*y1
		y6 = s2.multiply(p).multiply(new BigDecimal(1).subtract(s1)).multiply(y1).divide(new BigDecimal(1).subtract(p), SCALE, BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(1).subtract(s2), SCALE, BigDecimal.ROUND_HALF_DOWN); // (s2*p*(1-s1)*y1)/((1-s2)(1-p))

		result.add(y1);
		result.add(y2);
		result.add(y3);
		result.add(y4);
		result.add(y5);
		result.add(y6);
		
		return result;
		
		/*BigDecimal p = probabilityOfRoutingFromBeToClient2;
		
		BigDecimal y1 = new BigDecimal(1);
		BigDecimal y2 = new BigDecimal(1);
		BigDecimal y3 = new BigDecimal(1);
		BigDecimal y4 = new BigDecimal(1);

		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		
		y2 = y1.divide(new BigDecimal(1).subtract(p), SCALE, BigDecimal.ROUND_HALF_DOWN);
		y3 = y2;
		y4 = p.multiply(y3);

		result.add(y1);
		result.add(y2);
		result.add(y3);
		result.add(y4);
		
		return result;*/
	}
	
	/**
	 * Calcola i rapporti tra visite (yi/yj)
	 */
	private BigDecimal[][] calculateYRatio(ArrayList<BigDecimal> list){
			
			BigDecimal[][] yRatio = new BigDecimal[list.size()][list.size()];
			
			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < list.size(); j++) {	
					BigDecimal yi = list.get(i);
					BigDecimal yj = list.get(j);
					BigDecimal result = throughputRatio(yi, yj);
					yRatio[i][j] = new BigDecimal(0.0);
					yRatio[i][j] = result;
				}
			}
			
			return yRatio;
	}
	
	/**
	 * Calcola la popolazione media data una coppia (S1, S2), il centro, il numero di utenti, i valori delle y, i rapporti tra visite (yi/yj)
	 */
	private BigDecimal avgPopulation(int coupleSS, int indexOfYOfCurCenter, int N, ArrayList<BigDecimal> yList, BigDecimal[][] yRatio)
	{
		BigDecimal inMemAvgPopulation = averagePopulation.get(coupleSS).get(indexOfYOfCurCenter).get(N);		
		if(inMemAvgPopulation != null)
			return inMemAvgPopulation;
		else
		{
			BigDecimal first = throughput(coupleSS, indexOfYOfCurCenter, N, yList, yRatio);
			BigDecimal second = new BigDecimal(1.0); //throughputRatio(yList.get(indexOfYOfCurCenter), yList.get(indexOfYOfCurCenter));
			BigDecimal third = avgResponseTime(coupleSS, indexOfYOfCurCenter, N, yList, yRatio);
			
			BigDecimal result = first.multiply(second).multiply(third);
			averagePopulation.get(coupleSS).get(indexOfYOfCurCenter).put(N, result);			
			return result;
		}
	}
	
	/**
	 * Calcola il throughput data una coppia (S1, S2), il centro, il numero di utenti, i valori delle y, i rapporti tra visite (yi/yj)
	 */
	private BigDecimal throughput(int coupleSS, int indexOfYOfCurCenter, int N, ArrayList<BigDecimal> yList, BigDecimal[][] yRatio)
	{
		BigDecimal inMemThroughput = throughput.get(coupleSS).get(indexOfYOfCurCenter).get(N);
		if(inMemThroughput != null)
			return inMemThroughput;
		else
		{		
			BigDecimal temp = new BigDecimal(N);
			BigDecimal sum = new BigDecimal(0.0);
			BigDecimal den;
			
			for(int i=0; i<yList.size(); ++i){
				den = avgResponseTime(coupleSS, i, N, yList, yRatio);
				sum = sum.add(den.multiply(yRatio[i][indexOfYOfCurCenter]));
			}
			
			BigDecimal result = temp.divide(sum, SCALE, BigDecimal.ROUND_HALF_DOWN);
			throughput.get(coupleSS).get(indexOfYOfCurCenter).put(N, result);
			return result;
		}
	}
	
	/**
	 * Calcola tempo medio di risposta data una coppia (S1, S2), il centro, il numero di utenti, i valori delle y, i rapporti tra visite (yi/yj)
	 */
	private BigDecimal avgResponseTime(int coupleSS, int indexOfYOfCurCenter, int N, ArrayList<BigDecimal> yList, BigDecimal[][] yRatio)
	{
		BigDecimal inMemAvgResponseTime = averageResponseTime.get(coupleSS).get(indexOfYOfCurCenter).get(N);
		if(inMemAvgResponseTime != null)
			return inMemAvgResponseTime;
		else{
			if(N==1){
				BigDecimal base = new BigDecimal(new Double(centersSchema.get(indexOfYOfCurCenter).getServiceTime()).toString());
				averageResponseTime.get(coupleSS).get(indexOfYOfCurCenter).put(N, base);
				return base;
			}
			
			BigDecimal first = new BigDecimal(centersSchema.get(indexOfYOfCurCenter).getServiceTime()); //tempo di servizio del centro
			BigDecimal second = new BigDecimal(1.0);
			
			BigDecimal result = new BigDecimal(0.0);
			if(centersSchema.get(indexOfYOfCurCenter).isQueueCenter()) // è un centro con coda
				result = first.multiply(second.add(avgPopulation(coupleSS, indexOfYOfCurCenter, N-1, yList, yRatio)));
			else //è un centro IS
				result = first;
			
			averageResponseTime.get(coupleSS).get(indexOfYOfCurCenter).put(N, result);
			return result;
		}
	}
	
	/**
	 * Calcola il rapporto tra visite (rapporto tra throughput, fissato un centro di riferimento)
	 */
	private BigDecimal throughputRatio(BigDecimal lambda_i, BigDecimal lambda_M)
	{
		return lambda_i.divide(lambda_M, SCALE, BigDecimal.ROUND_HALF_DOWN);
	}
	
	/**
	 * 	Calcola l'indice della coppia (S1, S2) con minor tempo di risposta LOCALE del centro 1
	 */	
	/*private void findIndexOfMinimumLocaleResponseTimeOfCenter1()
	{
		BigDecimal min = new BigDecimal(0.0);
		BigDecimal cur;
		int min_i = 0;
		
		for (int i = 0; i < couplesOfS.size(); i++) {
			
			cur = averageResponseTime.get(i).get(0).get(numJobInSystem);
			
			if(i == 0)
				min = cur;
			else{
				if(min.compareTo(cur) > 0){
					min = cur;
					min_i = i;
				}
			}
		}
		
		minLocalResponseTimeCoupleIndex = min_i;
	}*/
	
	/**
	 * Calcola l'indice della coppia (S1, S2) con minor tempo di risposta GLOBALE rispetto al centro 1
	 */
	private void findIndexOfMinimumGlobalResponseTimeOfCenter1()
	{
		BigDecimal min = new BigDecimal(0.0);
		BigDecimal cur;
		int min_i = 0;
		
		for (int i = 0; i < couplesOfS.size(); i++) {
			
			//calcolo tempo medio globale di risposta del centro 1 per la coppia i-esima (S1, S2)
			cur = calculateAverageGlobalResponseTimeForCenter(i, 0);
			globalResponseTimeForCenter1ForAllCouple.put(couplesOfS.get(i), cur);
			
			if(i == 0)
				min = cur;
			else{
				if(min.compareTo(cur) > 0){
					min = cur;
					min_i = i;
				}
			}
		}
		
		minGlobalResponseTimeCoupleIndex = min_i;
	}
	
	/**
	 * Calcola tempo medio globale di risposta visto da un centro per una coppia S1 e S2
	 */
	private BigDecimal calculateAverageGlobalResponseTimeForCenter(int coupleSS, int center)
	{
		BigDecimal responseTimeM = new BigDecimal(0.0);
		BigDecimal temp;
		
		
		for(int j=0; j <centersSchema.size(); ++j){
			if(center == j)
				continue;
			//temp = throughputRatio(throughput.get(coupleSS).get(j).get(numJobInSystem), throughput.get(coupleSS).get(center).get(numJobInSystem));
			temp = throughputRatio(yHashMap.get(couplesOfS.get(coupleSS)).get(j), yHashMap.get(couplesOfS.get(coupleSS)).get(center));
			temp = temp.multiply(averageResponseTime.get(coupleSS).get(j).get(numJobInSystem));
			responseTimeM = responseTimeM.add(temp);
		}
		
		return responseTimeM;
	}
	
	/**
	 * Calcola tempo medio globale di risposta visto da un centro
	 */
	private void calcuateAverageGlobalResponseTime()
	{
		BigDecimal res;
		for (int i = 0; i < centersSchema.size(); i++) {
			res = calculateAverageGlobalResponseTimeForCenter(minGlobalResponseTimeCoupleIndex, i);
			averageGlobalResponseTime.put(centersSchema.get(i), res);
		}
	}
	
	/**
	 * Calcola tempo globale medio di ciclo visto da ogni centro
	 */
	private void calculateAverageGlobaleCycleTime()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal globalResponseTimeOfCurCenter = averageGlobalResponseTime.get(curCenter);
			BigDecimal localResponseTimeOfCurCenter = averageResponseTime.get(minGlobalResponseTimeCoupleIndex).get(i).get(numJobInSystem);
			BigDecimal globalCyclicTimeOfCurCenter = globalResponseTimeOfCurCenter.add(localResponseTimeOfCurCenter);
			averageGlobalCycleTime.put(curCenter, globalCyclicTimeOfCurCenter);
		}
	}
	
	/**
	 * Calcola throughput globale visto da ogni centro
	 */
	private void calculateGlobalThroughput() 
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal globalCyclicTime = averageGlobalCycleTime.get(curCenter);
			BigDecimal numJob = new BigDecimal(numJobInSystem);
			BigDecimal globalThroughputOfCurCenter = numJob.divide(globalCyclicTime, SCALE, BigDecimal.ROUND_HALF_DOWN);			
			globalThroughput.put(curCenter, globalThroughputOfCurCenter);
		}
	}
	
	/**
	 * Calcola utilizzazione di ogni centro
	 */
	private void calculateUtilization()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{ 
			Center curCenter = centersSchema.get(i);
			
			BigDecimal utilizationCurCenter = new BigDecimal(0.0);
			BigDecimal throughputCurCenter = throughput.get(minGlobalResponseTimeCoupleIndex).get(i).get(numJobInSystem);;
			BigDecimal serviceTimeCurCenter = new BigDecimal(new Double(curCenter.getServiceTime()).toString());
			
			if(curCenter.isQueueCenter())			
				utilizationCurCenter = throughputCurCenter.multiply(serviceTimeCurCenter); //ro = lambda*Ts			
			else //centro IS			
				utilizationCurCenter = throughputCurCenter.multiply(serviceTimeCurCenter).divide(new BigDecimal(numJobInSystem), SCALE, BigDecimal.ROUND_HALF_DOWN); //ro = lambda*Ts/N
				
			utilization.put(curCenter, utilizationCurCenter);
		}
	}
	
	public HashMap<Center, BigDecimal> getAverageGlobalResponseTime() {
		return averageGlobalResponseTime;
	}

	public HashMap<Integer, BigDecimal> getSaturationProbabilites() {
		return saturationProbabilites;
	}

	public ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> getAveragePopulation() {
		return averagePopulation;
	}

	public ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> getAverageResponseTime() {
		return averageResponseTime;
	}

	public ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> getThroughput() {
		return throughput;
	}

	public HashMap<Center, BigDecimal> getAverageGlobalCycleTime() {
		return averageGlobalCycleTime;
	}

	public HashMap<Center, BigDecimal> getGlobalThroughput() {
		return globalThroughput;
	}

	public HashMap<Center, BigDecimal> getUtilization() {
		return utilization;
	}

	public ArrayList<Center> getCentersSchema() {
		return centersSchema;
	}
	
	public int getMinGlobalResponseTimeCoupleIndex() {
		return minGlobalResponseTimeCoupleIndex;
	}

	public HashMap<Couple, BigDecimal> getGlobalResponseTimeForCenter1ForAllCouple() {
		return globalResponseTimeForCenter1ForAllCouple;
	}

	/**********************************************************************************************************************************************/
	/* Funzioni di stampa */
	/**********************************************************************************************************************************************/	
	
	public void printAllOnStd()
	{
		try
		{
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
			printSaturationProbabilities(log);
			printAveragePopulation(log);
			printThroughput(log);
			printAvgResponseTime(log);
			printAvgGlobalResponseTime(log);
			printGlobalThroughput(log);
			printAverageGlobalCycleTime(log);
			printyRatio(log);
			printYList(log);
			printUtilization(log);
			printGlobalResponseTimeForCenter1ForAllCouple(log);
			
			log.flush();
		}
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAllOnFile()
	{
		printSaturationProbabilitiesOnFile();
		printAveragePopulationOnFile();
		printThroughputOnFile();
		printAvgResponseTimeOnFile();		
		printAvgGlobalResponseTimeOnFile();
		printGlobalThroughputOnFile();
		printAverageGlobalCycleTimeOnFile();
		printyRatioOnFile();
		printYListOnFile();
		printUtilizationOnFile();
		printGlobalResponseTimeForCenter1ForAllCoupleOnFile();
	}
	
	
	public void printSaturationProbabilitiesOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/saturationProbabilities.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printSaturationProbabilities(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAveragePopulationOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/averagePopulation.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printAveragePopulation(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printThroughputOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/throughput.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printThroughput(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAvgResponseTimeOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/avgResponseTime.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printAvgResponseTime(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAvgGlobalResponseTimeOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/avgGlobalResponseTime.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printAvgGlobalResponseTime(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printGlobalThroughputOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/globalThroughput.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printGlobalThroughput(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAverageGlobalCycleTimeOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/averageGlobalCycleTime.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printAverageGlobalCycleTime(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	public void printyRatioOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/yRatio.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printyRatio(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	public void printYListOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/yList.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printYList(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	public void printUtilizationOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/utilization.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printUtilization(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	public void printGlobalResponseTimeForCenter1ForAllCoupleOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/globalResponseTimeForAllCouple.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printGlobalResponseTimeForCenter1ForAllCouple(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }
	}
	
	private void makeDir()
	{
		File dir = new File(folder);
		dir.mkdir();
	}

	
	private void printSaturationProbabilities(BufferedWriter log) throws IOException
	{
		log.append("********** Saturation Probabilities **********\n");
		
		Integer S1 = couplesOfS.get(minGlobalResponseTimeCoupleIndex).getS1();
		Integer S2 = couplesOfS.get(minGlobalResponseTimeCoupleIndex).getS2();
		log.append("Coppia scelta: (" + S1 + ", " + S2 + ")\n");
		
		Iterator<Integer> iterator = saturationProbabilites.keySet().iterator();
		
		while(iterator.hasNext())
		{
			Integer S = iterator.next();
			
			BigDecimal curProbability = saturationProbabilites.get(S);
			log.append("Saturation Probability For S(" + S + "): " + curProbability + "\n");
		}
	}
	
	private void printAveragePopulation(BufferedWriter log) throws IOException
	{
		log.append("********** Average Population **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			indexOfCurCenter = i+1;
			BigDecimal avgPopulationForCurCenter = averagePopulation.get(minGlobalResponseTimeCoupleIndex).get(i).get(numJobInSystem);
			log.append("Average Population for Center " + indexOfCurCenter + ": " + avgPopulationForCurCenter + "\n");
		}		
	}
	
	private void printThroughput(BufferedWriter log) throws IOException
	{
		log.append("********** Throughput **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			indexOfCurCenter = i+1;
			BigDecimal throughputForCurCenter = throughput.get(minGlobalResponseTimeCoupleIndex).get(i).get(numJobInSystem);
			log.append("Throughput for Center " + indexOfCurCenter + ": " + throughputForCurCenter + "\n");
		}	
	}
	
	private void printAvgResponseTime(BufferedWriter log) throws IOException
	{
		log.append("********** Average Response Time **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			indexOfCurCenter = i+1;
			BigDecimal avgResponseTimeForCurCenter = averageResponseTime.get(minGlobalResponseTimeCoupleIndex).get(i).get(numJobInSystem);
			log.append("Average Response Time for Center " + indexOfCurCenter + ": " + avgResponseTimeForCurCenter + "\n");
		}	
	}	
	
	private void printAvgGlobalResponseTime(BufferedWriter log) throws IOException
	{
		log.append("********** Average Global Response Time **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal avgGlobalResponseTimeForCurCenter = averageGlobalResponseTime.get(curCenter);
			log.append("Average Global Response Time for Center " + indexOfCurCenter + ": " + avgGlobalResponseTimeForCurCenter + "\n");
		}	
	}	
	
	private void printGlobalThroughput(BufferedWriter log) throws IOException
	{
		log.append("********** Global Throughput **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal globalThroughputForCurCenter = globalThroughput.get(curCenter);
			log.append("Global Throughput for Center " + indexOfCurCenter + ": " + globalThroughputForCurCenter + "\n");
		}	
	}	
	
	private void printAverageGlobalCycleTime(BufferedWriter log) throws IOException
	{
		log.append("********** Average Global CycleTime **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal averageGlobaleCycleTimeForCurCenter = averageGlobalCycleTime.get(curCenter);
			log.append("Average Global CycleTime for Center " + indexOfCurCenter + ": " + averageGlobaleCycleTimeForCurCenter + "\n");
		}	
	}
	
	private void printyRatio(BufferedWriter log) throws IOException
	{
		log.append("********** yRatio **********\n");
		
		BigDecimal[][] yRatio = yRatioHashMap.get(couplesOfS.get(minGlobalResponseTimeCoupleIndex));
		
		 for (int i = 0; i < centersSchema.size(); i++) 
		 {
				for (int j = 0; j < centersSchema.size(); j++) 
				{					
					log.append("yRatio[" + i + "][" + j + "]: " + yRatio[i][j] + "\n");
				}
				log.append("\n");
		}
	}
	
	private void printYList(BufferedWriter log) throws IOException
	{
		log.append("********** yList **********\n");
		
		ArrayList<BigDecimal> yList = yHashMap.get(couplesOfS.get(minGlobalResponseTimeCoupleIndex));
		
		 for (int i = 0; i < yList.size(); i++) 
		 {
			 log.append("yList[" + i + "]: " + yList.get(i) + "\n");
		 }
	}
	
	private void printUtilization(BufferedWriter log) throws IOException
	{
		log.append("********** Utilization **********\n");
		int indexOfCurCenter = 0;
		
		for (int i = 0; i < centersSchema.size(); i++) 
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal utilizationCurCenter = utilization.get(curCenter);
			log.append("Utilization for Center " + indexOfCurCenter + ": " + utilizationCurCenter + "\n");
		}
	}
	
	private void printGlobalResponseTimeForCenter1ForAllCouple(BufferedWriter log) throws IOException
	{
		log.append("********** Global Response Time For Center 1 For All Couple**********\n");
		
		for(int i = 0; i < couplesOfS.size(); i++)
		{
			Couple curCouple = couplesOfS.get(i);
			BigDecimal globalResponseTime = globalResponseTimeForCenter1ForAllCouple.get(curCouple);
			String number = globalResponseTime.toString().substring(0, 15);
			log.append(curCouple.getS1() + "," + curCouple.getS2() + "," + number + ";\n");			
		}
	}	
}
