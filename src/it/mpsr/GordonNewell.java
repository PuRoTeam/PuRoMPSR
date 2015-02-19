package it.mpsr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GordonNewell 
{
	private ArrayList<Double> variables_x; //vettore delle x
	private ArrayList<Center> centersSchema; //tipologia di centri
	private ArrayList<Center> queueCenters; //job con coda 
	
	private StateSet stateSet; //insieme di tutti gli stati in cui si può trovare il sistema
	private BigDecimal normalizedConstantG; //costante G
	
	private HashMap<Integer, BigDecimal> saturationProbabilites; //probabilità di saturazione dei centri con coda per ogni valore di S
	private HashMap<Center,HashMap<Integer, BigDecimal>> marginalProbabilities; //probabilità che in un centro i ci siano j job
	private HashMap<Center, BigDecimal> averagePopulation; //popolazione media nei vari centri 
	private HashMap<Center, BigDecimal> utilizationCoefficient; //coefficiente di utilizzazione dei centri
	private HashMap<Center, BigDecimal> throughput; //throughput dei centri
	private HashMap<Center, BigDecimal> averageResponseTime; //tempo medio di risposta dei centri
	
	private HashMap<Center, BigDecimal> averageGlobalResponseTime; //tempo globale medio di risposta visto da ogni centor
	private HashMap<Center, BigDecimal> averageGlobalCycleTime; //tempo globale medio di ciclo visto da ogni centor
	private HashMap<Center, BigDecimal> globalThroughput; //throughput globale visto da ogni centro
	
	public final static int SCALE = 20;
	public final static String folder = "Gordon";
	
	private int numJobsInSystem; //numero totale di job nel sistema
	private int numCenter; //numero totale di centri	
	
	/**
	 * Algoritmo di Gordon Newell
	 * @param centersSchema: schematizzazione impianto, specifica quali centri hanno coda e quali sono IS (1: Client 1, 2: Front End, 3: Back End, 4: Client 2)
	 * @param variables_x: array di variabili xi (lambda=x*mu)
	 * @param numJobsInSystem: numero totale di job nel sistema
	 */
	public GordonNewell(ArrayList<Center> centersSchema, ArrayList<Double> variables_x, int numJobsInSystem)
	{		
		//inizializza variables_x
		this.variables_x = new ArrayList<Double>();
		for(int i = 0; i < variables_x.size(); i++)
			this.variables_x.add(variables_x.get(i));
		
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
		
		this.stateSet = new StateSet(centersSchema, numJobsInSystem);
		
		this.marginalProbabilities = new HashMap<Center,HashMap<Integer, BigDecimal>>();
		this.saturationProbabilites = new HashMap<Integer, BigDecimal>();
		this.averagePopulation = new HashMap<Center, BigDecimal>();
		this.utilizationCoefficient = new HashMap<Center, BigDecimal>();
		this.throughput = new HashMap<Center, BigDecimal>();
		this.averageResponseTime = new HashMap<Center, BigDecimal>();
		this.averageGlobalResponseTime = new HashMap<Center, BigDecimal>();
		this.averageGlobalCycleTime = new HashMap<Center, BigDecimal>();
		this.globalThroughput = new HashMap<Center, BigDecimal>();
		
		this.numJobsInSystem = numJobsInSystem;
		this.numCenter = variables_x.size();
		this.normalizedConstantG = new BigDecimal(0.0);		
	}
	
	/**
	 * Avvia l'algoritmo e calcola gli indici locali e globali
	 */
	public void start()
	{
		//CONTROlLARE PER IS
		stateSet.calculateFeasibleState();
		calculateNormalizedConstantG(); //X
		calculateStatesProbabilities(); //X
		calculateMarginalProbabilities(); //X
		calculateSaturationProbabilityOfQueueCenters(); //X
		calculateAveragePopulationForAllCenter(); //X
		calculateUtilizationCoefficientForAllCenter(); //X
		calculateThroughputForAllCenter(); //X
		calculateAvgResponseTime(); //X
		calculateAverageGlobalResponseTime();		
		calculateAverageGlobaleCycleTime();
		calculateGlobalThroughput();
		
		System.out.println("FINE - GN");
	}
	
	/**
	 * Calcola la costante di normalizzazione G
	 */	
	private void calculateNormalizedConstantG()
	{
		ArrayList<State> statesSpace = stateSet.getStatesSpace();
		
		for(int i = 0; i < statesSpace.size(); i++) //scorro tutti gli stati
		{
			ArrayList<Center> curStateCenters = statesSpace.get(i).getCentersList();
						
			BigDecimal bdProduct = new BigDecimal(1.0);
			
			for(int j = 0; j < numCenter; j++)
			{				
				BigDecimal bdBase = new BigDecimal(variables_x.get(j).doubleValue()); //variabile xj
				int bdExp = curStateCenters.get(j).getJobInQueue(); //numero di job nel centro j nello stato i
				
				BigDecimal bgConstantBeta = new BigDecimal(1.0);
				if(!curStateCenters.get(j).isQueueCenter()) //non è un centro con coda, ma un IS, quindi la costante beta è pari al fattoriale di bdExp
				{
					bgConstantBeta = new BigDecimal(factorial(new Integer(bdExp)));
				}
				
				BigDecimal bdPow = bdBase.pow(bdExp); //xj^nj
						
				BigDecimal div = bdPow.divide(bgConstantBeta, SCALE, BigDecimal.ROUND_HALF_DOWN); //(xj^nj) / (betaj)
				
				bdProduct = div.multiply(bdProduct); //moltiplico tutti gli (xj^nj) / (betaj) per ogni j
			}
			
			normalizedConstantG = normalizedConstantG.add(bdProduct); //G = sommatoria di produttoria termini ((xj^nj) / (betaj))
		}
	}
	
	/**
	 * Fattoriale di N	 
	 */
	private BigInteger factorial(Integer N){
		
		BigInteger factorial = new BigInteger("1");
		
		for(int i = 1; i <= N; i++)
			factorial = factorial.multiply(new BigInteger(Integer.toString(i)));		
		
		return factorial;		
	}
	
	/**
	 * Calcolo delle probabilità di stato
	 */
	private void calculateStatesProbabilities()
	{
		ArrayList<State> statesSpace = stateSet.getStatesSpace();
		
		for(int i = 0; i < statesSpace.size(); i++) //scorro tutti gli stati
		{
			State curState = statesSpace.get(i);
			ArrayList<Center> curStateCenters = curState.getCentersList();
						
			BigDecimal bdProduct = new BigDecimal(1.0);
			
			for(int j = 0; j < numCenter; j++) //stessa size di variables e curStateCenters
			{
				BigDecimal bdBase = new BigDecimal(variables_x.get(j).doubleValue()); //variabile xj
				int bdExp = curStateCenters.get(j).getJobInQueue(); //numero di job nel centro j nello stato i
				
				BigDecimal bgConstantBeta = new BigDecimal(1.0);
				if(!curStateCenters.get(j).isQueueCenter()) //non è un centro con coda, ma un IS
				{
					bgConstantBeta = new BigDecimal(factorial(new Integer(bdExp)));
				}
								
				BigDecimal bdPow = bdBase.pow(bdExp); //xj^nj
				
				BigDecimal div = bdPow.divide(bgConstantBeta, SCALE, BigDecimal.ROUND_HALF_DOWN); //(xj^nj) / (betaj)
				
				bdProduct = div.multiply(bdProduct); //moltiplico tutti gli (xj^nj) / (betaj) per ogni j
			}
			
			BigDecimal probability = bdProduct.divide(normalizedConstantG, SCALE, BigDecimal.ROUND_HALF_DOWN);
			
			curState.setProbability(probability);
		}
	}
	
	/**
	 * Calcola le probabilità marginali per ogni centro, ossia la probabilità che in un centro ci sia un centro numero di utenti
	 */
	private void calculateMarginalProbabilities()
	{
		ArrayList<State> statesSpace = stateSet.getStatesSpace();
		BigDecimal marginalProbability;
		HashMap<Integer, BigDecimal> temp = null;
		
		for(int i=0; i<centersSchema.size(); ++i){
			
			temp = new HashMap<Integer, BigDecimal>();
			
			for(int S = 0; S <= numJobsInSystem; S++){ //S utenti nel centro i - S = [0, N]
				
				marginalProbability = new BigDecimal(0.0);
				
				for(int j = 0; j < statesSpace.size(); j++){ //cerco tutti gli stati con S utenti nel centro i
				
					State curState = statesSpace.get(j); //prendo lo stato j-esimo
					ArrayList<Center> curStateCenters = curState.getCentersList(); //prendo la lista dei centri dello stato
					Center curCenter = curStateCenters.get(i); //prendo il centro in posizione i-esima
					
					//Se il centro in posizione i-esima ha esattamente S job, prendo la probabilità di stato 
					if(S == curCenter.getJobInQueue())
						marginalProbability = marginalProbability.add(curState.getProbability());

				}
				
				temp.put(new Integer(S), marginalProbability);			
			}
			marginalProbabilities.put(centersSchema.get(i), temp);
		}
	}
	
	/**
	 * Calcolo della probabilità di saturazione dei centri con coda, dove il limite è dato da S, per S = [0, N] con N = numJobsInSystem
	 */
	private void calculateSaturationProbabilityOfQueueCenters()
	{		
		for(int S = 0; S <= numJobsInSystem; S++) //S = probabilità di saturazione - S = [0, N]
		{
			BigDecimal saturationProbabilityOfQueueCentersForS = new BigDecimal(0.0);			
			
			ArrayList<State> statesSpace = stateSet.getStatesSpace();
						
			for(int i = 0; i < statesSpace.size(); i++) //più semplice calcolarlo dagli stati che dalle probabilità marginali
			{
				State curState = statesSpace.get(i);
				
				ArrayList<Center> centerList = curState.getCentersList();
				
				BigInteger numJobInQueueCenterInState = new BigInteger("0");
				
				for(int j = 0; j < centerList.size(); j++) //conto il numero totale di job presenti nei centri con coda nello stato attuale
				{
					Center curCenter = centerList.get(j);
					
					if(curCenter.isQueueCenter())
						numJobInQueueCenterInState = numJobInQueueCenterInState.add(new BigInteger(Integer.toString(curCenter.getJobInQueue())));
				}
				//Se il numero totale di job nei centri con coda dello stato corrente è maggiore o uguale a S, aggiungo la probabilità di stato alla probabilità di saturazione
				if(numJobInQueueCenterInState.compareTo(new BigInteger(Integer.toString(S))) >= 0)
					saturationProbabilityOfQueueCentersForS = saturationProbabilityOfQueueCentersForS.add(curState.getProbability());
			}
			
			saturationProbabilites.put(S, saturationProbabilityOfQueueCentersForS);
		}
	}
	
	/** 
	 * Calcola la popolazione media per ogni centro
	 */
	private void calculateAveragePopulationForAllCenter()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal avgPopulationCurCenter = centerAvgPopulation(curCenter);
			averagePopulation.put(curCenter, avgPopulationCurCenter);
		}
	}	
	
	/**
	 * Calcolo popolazione media nel centro i-esimo
	 * @param c
	 */
	private BigDecimal centerAvgPopulation(Center c){
		
		BigDecimal avg = new BigDecimal(0.0);
		BigDecimal temp;
		
		for(int i=1; i<=numJobsInSystem; ++i){ //i = [1, N]
			temp = new BigDecimal(i).multiply(marginalProbabilities.get(c).get(i));
			avg = avg.add(temp);
		}
		
		return avg;
	}
	
	private void calculateThroughputForAllCenter()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal throughputOfCurCenter = throughputCenter(curCenter);
			throughput.put(curCenter, throughputOfCurCenter);
		}
	}
	
	/**
	 * Calcolo che throughput del centro i-esimo singolo (6.53')
	 * @param c
	 */
	public BigDecimal throughputCenter(Center c)
	{
		BigDecimal serviceRate = new BigDecimal(c.getServiceRate());
		BigDecimal temp = new BigDecimal(0.0);
				
		if(c.isQueueCenter()) //centro con coda
		{
			temp = serviceRate.multiply(utilizationCoefficient.get(c));
		}
		else //centro IS
		{
			temp = serviceRate.multiply(utilizationCoefficient.get(c)).multiply(new BigDecimal(numJobsInSystem));
		}
		
		return temp;
	}
	
	/**
	 * Calcola il coefficiente di utilizzazione di ogni centro
	 */
	public void calculateUtilizationCoefficientForAllCenter()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			BigDecimal utilizationOfCurCenter = utilizationCoefficientCenter(curCenter);
			
			utilizationCoefficient.put(curCenter, utilizationOfCurCenter);
		}
	}
	
	/**
	 * Calcolo del coefficiente di utilizzazione del centro i-esimo singolo (6.52'')
	 * @param c
	 */
	public BigDecimal utilizationCoefficientCenter(Center c)
	{
		BigDecimal temp = new BigDecimal(0.0);
		BigDecimal tempMul = new BigDecimal(0.0);
		
		if(c.isQueueCenter()){	
			for(int i=1; i<=numJobsInSystem; ++i){ //i = [1, N]
				temp = temp.add(marginalProbabilities.get(c).get(i));
			}
		}
		else //centri IS
		{
			for(int i=1; i<=numJobsInSystem; ++i){ //i = [1, N]
				tempMul = marginalProbabilities.get(c).get(i).multiply(new BigDecimal(i));
				temp = temp.add(tempMul);
			}
			
			temp = temp.divide(new BigDecimal(numJobsInSystem), SCALE, BigDecimal.ROUND_HALF_DOWN);
		}

		return temp;
	}
		
	/**
	 * Calcola tempo medio di risposta per ogni centro
	 */
	public void calculateAvgResponseTime()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			BigDecimal avgTime = responseAvgTime(curCenter);
			
			averageResponseTime.put(curCenter, avgTime);
		}
	}
	
	/**
	 *  Restituisce tempo medio di risposta di un Centro	 
	 */	
	public BigDecimal responseAvgTime(Center c)
	{		
		return averagePopulation.get(c).divide(throughput.get(c), SCALE, BigDecimal.ROUND_HALF_DOWN);
	}
	
	/**
	 * Calcola tempo medio globale di risposta visto da un centro
	 */
	public void calculateAverageGlobalResponseTime()
	{
		BigDecimal responseTimeM;
		BigDecimal temp;
		
		for(int i=0; i<centersSchema.size(); ++i){
			
			responseTimeM = new BigDecimal(0.0);
			
			for(int j=0; j <centersSchema.size(); ++j){
				
				if(j==i)
					continue;
				
				temp = throughputRatio(throughput.get(centersSchema.get(j)), throughput.get(centersSchema.get(i)));				
				temp = temp.multiply(averageResponseTime.get(centersSchema.get(j)));
				responseTimeM = responseTimeM.add(temp);
			}
			
			averageGlobalResponseTime.put(centersSchema.get(i), responseTimeM);
		}
	}
	
	/**
	 * Calcola il rapporto tra visite (rapporto tra throughput, fissato un centro di riferimento)
	 */
	public BigDecimal throughputRatio(BigDecimal lambda_i, BigDecimal lambda_M)
	{
		return lambda_i.divide(lambda_M, SCALE, BigDecimal.ROUND_HALF_DOWN);
	}
	
	/**
	 * Calcola tempo globale medio di ciclo visto da ogni centro
	 */
	public void calculateAverageGlobaleCycleTime()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal globalResponseTimeOfCurCenter = averageGlobalResponseTime.get(curCenter);
			BigDecimal localResponseTimeOfCurCenter = averageResponseTime.get(curCenter);
			BigDecimal globalCyclicTimeOfCurCenter = globalResponseTimeOfCurCenter.add(localResponseTimeOfCurCenter);
			averageGlobalCycleTime.put(curCenter, globalCyclicTimeOfCurCenter);
		}
	}
	
	/**
	 * Calcola throughput globale visto da ogni centro
	 */
	public void calculateGlobalThroughput()
	{
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			
			BigDecimal globalCyclicTime = averageGlobalCycleTime.get(curCenter);
			BigDecimal numJob = new BigDecimal(numJobsInSystem);
			BigDecimal globalThroughputOfCurCenter = numJob.divide(globalCyclicTime, SCALE, BigDecimal.ROUND_HALF_DOWN);			
			globalThroughput.put(curCenter, globalThroughputOfCurCenter);
		}
	}
	
	public StateSet getStateSet()
	{
		return stateSet;
	}
	
	public BigDecimal getNormalizedConstantG()
	{
		return normalizedConstantG;
	}
	
	public HashMap<Integer, BigDecimal> getSaturationHashMap() 
	{
		return saturationProbabilites;
	}
	
	public int getNumJobsInSystem()
	{
		return numJobsInSystem;
	}
	
	public int getNumCenter()
	{
		return numCenter;
	}
	
	public ArrayList<Center> getCentersSchema()
	{
		return centersSchema;
	}	
	
	public ArrayList<Center> getQueueCenters()
	{
		return queueCenters;
	}	
	
	public HashMap<Center,HashMap<Integer, BigDecimal>> getMarginalProbabilities()
	{
		return marginalProbabilities;
	}
	
	public HashMap<Integer, BigDecimal> getSaturationProbabilites() {
		return saturationProbabilites;
	}

	public HashMap<Center, BigDecimal> getAveragePopulation() {
		return averagePopulation;
	}

	public HashMap<Center, BigDecimal> getUtilizationCoefficient() {
		return utilizationCoefficient;
	}

	public HashMap<Center, BigDecimal> getThroughput() {
		return throughput;
	}

	public HashMap<Center, BigDecimal> getAverageResponseTime() {
		return averageResponseTime;
	}

	public HashMap<Center, BigDecimal> getAverageGlobalResponseTime() {
		return averageGlobalResponseTime;
	}

	public HashMap<Center, BigDecimal> getAverageGlobalCycleTime() {
		return averageGlobalCycleTime;
	}

	public HashMap<Center, BigDecimal> getGlobalThroughput() {
		return globalThroughput;
	}

	/**********************************************************************************************************************************************/
	/* Funzioni di stampa */
	/**********************************************************************************************************************************************/	
	
	public void printAllOnStd()
	{
		try
		{
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
			printStatesProbabilities(log);
			printMarginalProbabilities(log);
			printSaturationProbabilities(log);
			printAveragePopulation(log);
			printUtilizationCoefficient(log);
			printThroughput(log);
			printAvgResponseTime(log);
			printAvgGlobalResponseTime(log);
			printGlobalThroughput(log);
			printAverageGlobalCycleTime(log);
			
			log.flush();
			
		}
		catch (IOException e) 
		{ e.printStackTrace(); }	
	}
	
	public void printAllOnFile()
	{
		printStatesProbabilitiesOnFile();
		printMarginalProbabilitiesOnFile();
		printSaturationProbabilitiesOnFile();
		printAveragePopulationOnFile();
		printUtilizationCoefficientOnFile();
		printThroughputOnFile();
		printAvgResponseTimeOnFile();		
		printAvgGlobalResponseTimeOnFile();
		printGlobalThroughputOnFile();
		printAverageGlobalCycleTimeOnFile();
	}
	
	public void printStatesProbabilitiesOnFile()
	{
		makeDir();
		try 
		{		
			FileOutputStream file = new FileOutputStream(folder + "/statesProbabilities.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printStatesProbabilities(log);
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }		
	}
	
	public void printMarginalProbabilitiesOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/marginalProbabilities.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printMarginalProbabilities(log);			
			log.close();
		} 
		catch (FileNotFoundException e) 
		{ e.printStackTrace(); } 
		catch (IOException e) 
		{ e.printStackTrace(); }		
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
	
	public void printUtilizationCoefficientOnFile()
	{
		makeDir();
		try 
		{			
			FileOutputStream file = new FileOutputStream(folder + "/utilizationCoefficient.txt");
			BufferedWriter log = new BufferedWriter(new OutputStreamWriter(file));
			printUtilizationCoefficient(log);
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
	
	private void makeDir()
	{
		File dir = new File(folder);
		dir.mkdir();
	}

	private void printStatesProbabilities(BufferedWriter log) throws IOException
	{		
		log.append("********** State Probabilities **********\n");
		log.append("Normalized Constant G: " + normalizedConstantG + "\n");
		
		ArrayList<State> statesSpace = stateSet.getStatesSpace();
		
		BigDecimal probabilitiesSum = new BigDecimal(0.0);
		
		for(int i = 0; i < statesSpace.size(); i++)
		{
			State curState = statesSpace.get(i);
			ArrayList<Center> curStateCenters = curState.getCentersList();
			
			log.append("(");
			for(int j = 0; j < numCenter - 1; j++)
			{
				int numJobInCurQueue = curStateCenters.get(j).getJobInQueue();
				log.append(numJobInCurQueue + ", ");
			}
			log.append("" + curStateCenters.get(numCenter - 1).getJobInQueue());
			log.append("): " + curState.getProbability() + "\n");
			
			probabilitiesSum = probabilitiesSum.add(curState.getProbability());
		}
		log.append("Probabilities Sum: " + probabilitiesSum + "\n");
	}
	
	private void printMarginalProbabilities(BufferedWriter log) throws IOException
	{
		log.append("********** Marginal Probabilities **********\n");

		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);

			HashMap<Integer, BigDecimal> marginalProbabilitiesOfCurCenter = marginalProbabilities.get(curCenter);
			
			for(int curNumJob = 0; curNumJob <= numJobsInSystem; curNumJob++)
			{
				BigDecimal probability = marginalProbabilitiesOfCurCenter.get(curNumJob);
				log.append("Probability of " + curNumJob + " job in " + curCenter.getName() + ": " + probability + "\n");
				//log.append(curNumJob + "," + curCenter.getName() + "," + probability + "\n");
			}	
		}
	}
	
	private void printSaturationProbabilities(BufferedWriter log) throws IOException
	{
		log.append("********** Saturation Probabilities **********\n");
		
		for(int S = 0; S <= numJobsInSystem; S++)
		{
			BigDecimal curProbability = saturationProbabilites.get(S);
			log.append("Saturation Probability For S(" + S + "): " + curProbability + "\n");
			//log.append(S + "," + curProbability + "\n");
		}
	}
	
	private void printAveragePopulation(BufferedWriter log) throws IOException
	{
		log.append("********** Average Population **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal avgPopulationForCurCenter = averagePopulation.get(curCenter);
			log.append("Average Population for Center " + indexOfCurCenter + ": " + avgPopulationForCurCenter + "\n");
		}		
	}
	
	private void printUtilizationCoefficient(BufferedWriter log) throws IOException
	{
		log.append("********** Utilization Coefficient **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal utilizationForCurCenter = utilizationCoefficient.get(curCenter);
			log.append("Utilization Coefficient for Center " + indexOfCurCenter + ": " + utilizationForCurCenter + "\n");
		}	
	}
	
	private void printThroughput(BufferedWriter log) throws IOException
	{
		log.append("********** Throughput **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal throughputForCurCenter = throughput.get(curCenter);
			log.append("Throughput for Center " + indexOfCurCenter + ": " + throughputForCurCenter + "\n");
		}	
	}
	
	private void printAvgResponseTime(BufferedWriter log) throws IOException
	{
		log.append("********** Average Response Time **********\n");
		int indexOfCurCenter = 0;
		
		for(int i = 0; i < centersSchema.size(); i++)
		{
			Center curCenter = centersSchema.get(i);
			indexOfCurCenter = i+1;
			
			BigDecimal avgResponseTimeForCurCenter = averageResponseTime.get(curCenter);
			log.append("Average Global Response Time for Center " + indexOfCurCenter + ": " + avgResponseTimeForCurCenter + "\n");
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
}
