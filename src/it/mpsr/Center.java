package it.mpsr;

public class Center 
{
	private int jobInQueue;      
	private double serviceTime; 
	private double serviceRate;
	private boolean isQueueCenter;
	private String name;

	public String getName() {
		return name;
	}

	/**
	 * Rappresentazione situazione di un centro in un dato istante di tempo (il parametro variabile è il numero di job in coda, jobInQueue)
	 * @param jobInQueue: numero di job nel centro
	 * @param serviceTime: tempo di servizio del centro
	 * @param isQueueCenter: se il centro ha una coda (valore true) o se è INFINITE SERVER (valore false)
	 */
	public Center(String name, int jobInQueue, double serviceTime, boolean isQueueCenter)
	{
		this.name = name;
		this.jobInQueue = jobInQueue;
		this.serviceTime = serviceTime;
		this.serviceRate = (1.0)/serviceTime;
		this.isQueueCenter = isQueueCenter;
	}

	public int getJobInQueue() 
	{
		return jobInQueue;
	}

	public void setJobInQueue(int jobInQueue) 
	{
		this.jobInQueue = jobInQueue;
	}

	public double getServiceTime() 
	{
		return serviceTime;
	}

	public double getServiceRate()
	{
		return serviceRate;
	}
	
	public void setServiceTime(double serviceTime) 
	{
		this.serviceTime = serviceTime;
	}

	public boolean isQueueCenter() 
	{
		return isQueueCenter;
	}

	public void setQueueCenter(boolean isQueueCenter) 
	{
		this.isQueueCenter = isQueueCenter;
	}

}
