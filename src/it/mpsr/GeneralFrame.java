package it.mpsr;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GeneralFrame extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private JPanel parameterPanel;
	
	//Buttons
	private JButton startGN;
	private JButton startMVA;
	
	private JButton gnAvgRespo;
	private JButton gnThroughput;
	private JButton gnUtilization;
	private JButton gnPopulation;
	private JButton gnSaturation;
	private JButton gnMarginal;
	private JButton gnGlobAvgRespo;
	private JButton gnGlobThroughput;
	private JButton gnGlobCycle;
	
	private JButton mvaAvgRespo;
	private JButton mvaThroughput;
	private JButton mvaUtilization;
	private JButton mvaPopulation;
	private JButton mvaSaturation;
	private JButton mvaGlobAvgRespo;
	private JButton mvaGlobThroughput;
	private JButton mvaGlobCycle;
	
	//Labels
	private JLabel gordon_label;
	private JLabel mva_label;
	private JLabel client_label;
    private JLabel constantG_label;
    private JLabel constantG_text;
    private JLabel minResp1_label;
    private JLabel minResp1_text;
	
	private GridBagLayout gBagLayout;
	
	private String [] terminalsOpt;
	private String [] simulationOpt;
	
	private MeanValueAnalysis mva;
	private GordonNewell gn;
	
	public GeneralFrame(MeanValueAnalysis mva, GordonNewell gn){
		
		this.mva = mva;
		this.gn = gn;

		this.setTitle("MPSR Project");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon arrow = new ImageIcon("res/arrow.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		this.setResizable(false);
		
		setSize(1000, 600);
		
		Container contentPane = this.getContentPane();
		GridBagLayout main = new GridBagLayout();
		contentPane.setLayout(main);
		GridBagConstraints main_c = new GridBagConstraints();
		
		//Labels
		gordon_label= new JLabel("Gordon-Newell:");
		mva_label = new JLabel("Mean Value Analysis:");
		client_label = new JLabel("Client:");
		constantG_label = new JLabel("Constant G:");
		constantG_text = new JLabel("");
		minResp1_label = new JLabel("Minimum Response Time C1:");
	    minResp1_text = new JLabel("");
		
		
		//Buttons
		startGN= new JButton("GN", arrow);
		startGN.setSize(200, 10);
		startGN.addActionListener(this);
		
		startMVA= new JButton("MVA", arrow);
		startMVA.setEnabled(false);
		startMVA.setSize(200, 10);
		startMVA.addActionListener(this);
	
		gnAvgRespo = new JButton("AvgResponseTime");
		gnThroughput = new JButton("Throughput");
		gnUtilization = new JButton("Utilization");
		gnPopulation = new JButton("Population");
		gnSaturation = new JButton("Saturation");
		gnMarginal = new JButton("Marginal");
		gnGlobAvgRespo = new JButton("GlobalAvgResponseTime");
		gnGlobThroughput = new JButton("GlobalThroughput");
		gnGlobCycle = new JButton("GlobalCycleTime");
		
		gnAvgRespo.setEnabled(false);
		gnThroughput.setEnabled(false);
		gnUtilization.setEnabled(false);
		gnPopulation.setEnabled(false);
		gnSaturation.setEnabled(false);
		gnMarginal.setEnabled(false);
		gnGlobAvgRespo.setEnabled(false);
		gnGlobThroughput.setEnabled(false);
		gnGlobCycle.setEnabled(false);
		
		gnAvgRespo.addActionListener(this);
		gnThroughput.addActionListener(this);
		gnUtilization.addActionListener(this);
		gnPopulation.addActionListener(this);
		gnSaturation.addActionListener(this);
		gnMarginal.addActionListener(this);
		gnGlobAvgRespo.addActionListener(this);
		gnGlobThroughput.addActionListener(this);
		gnGlobCycle.addActionListener(this);
		
		mvaAvgRespo = new JButton("AvgResponseTime");
		mvaThroughput = new JButton("Throughput");
		mvaUtilization = new JButton("Utilization");
		mvaPopulation = new JButton("Population");
		mvaSaturation = new JButton("Saturation");
		mvaGlobAvgRespo = new JButton("GlobalAvgResponseTime");
		mvaGlobThroughput = new JButton("GlobalThroughput");
		mvaGlobCycle = new JButton("GlobalCycleTime");
		
		mvaAvgRespo.addActionListener(this);
		mvaThroughput.addActionListener(this);
		mvaUtilization.addActionListener(this);
		mvaPopulation.addActionListener(this);
		mvaSaturation.addActionListener(this);
		mvaGlobAvgRespo.addActionListener(this);
		mvaGlobThroughput.addActionListener(this);
		mvaGlobCycle.addActionListener(this);
		
		mvaAvgRespo.setEnabled(false);
		mvaThroughput.setEnabled(false);
		mvaUtilization.setEnabled(false);
		mvaPopulation.setEnabled(false);
		mvaSaturation.setEnabled(false);
		mvaGlobAvgRespo.setEnabled(false);
		mvaGlobThroughput.setEnabled(false);
		mvaGlobCycle.setEnabled(false);
		
		setMyConstraints(main_c,0,0,GridBagConstraints.CENTER);
		contentPane.add(getFieldPanel(),main_c);
		  
		setMyConstraints(main_c,0,1,GridBagConstraints.CENTER);
		contentPane.add(getButtonPanel(),main_c);
		
		this.pack();
		this.setVisible(true);
	}
	
	public JPanel getFieldPanel() {
		
		JPanel p = new JPanel(new GridBagLayout());
	    p.setBorder(BorderFactory.createTitledBorder("Product Form"));
	    GridBagConstraints c = new GridBagConstraints();
	    
	    setMyConstraints(c,0,0,GridBagConstraints.EAST);
	    p.add(gordon_label,c);
	    
	    setMyConstraints(c,1,0,GridBagConstraints.WEST);
	    p.add(startGN,c);
	    
	    setMyConstraints(c,0,2,GridBagConstraints.EAST);
	    p.add(mva_label,c);
	    
	    setMyConstraints(c,1,2,GridBagConstraints.WEST);
	    p.add(startMVA,c);
	    
	    setMyConstraints(c,0,4,GridBagConstraints.EAST);
	    p.add(constantG_label,c);
	    
	    setMyConstraints(c,1,4,GridBagConstraints.WEST);
	    p.add(constantG_text,c);
	    
	    setMyConstraints(c,0,6,GridBagConstraints.EAST);
	    p.add(minResp1_label,c);
	    
	    setMyConstraints(c,1,6,GridBagConstraints.WEST);
	    p.add(minResp1_text,c);
	    
	    return p;
	}
	
	public JPanel getButtonPanel() {
		
		JPanel main = new JPanel(new GridBagLayout());
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("Result GN"));
		GridBagConstraints c = new GridBagConstraints();
		
	    setMyConstraints(c,0,0,GridBagConstraints.EAST);
		p.add(gnSaturation, c);
		setMyConstraints(c,0,1,GridBagConstraints.EAST);
		p.add(gnMarginal, c);
		setMyConstraints(c,0,2,GridBagConstraints.EAST);
		p.add(gnAvgRespo, c);
		setMyConstraints(c,0,3,GridBagConstraints.EAST);
		p.add(gnGlobAvgRespo, c);
		setMyConstraints(c,1,0,GridBagConstraints.WEST);
		p.add(gnThroughput, c);
		setMyConstraints(c,1,1,GridBagConstraints.WEST);
		p.add(gnPopulation, c);
		setMyConstraints(c,1,2,GridBagConstraints.WEST);
		p.add(gnGlobCycle, c);
		setMyConstraints(c,1,3,GridBagConstraints.WEST);
		p.add(gnGlobThroughput, c);
		setMyConstraints(c,1,4,GridBagConstraints.WEST);
		p.add(gnUtilization, c);
		
		JPanel q = new JPanel(new GridBagLayout());
		q.setBorder(BorderFactory.createTitledBorder("Result MVA"));
		
		setMyConstraints(c,0,0,GridBagConstraints.EAST);
		q.add(mvaAvgRespo,c);
		setMyConstraints(c,0,1,GridBagConstraints.EAST);
		q.add(mvaPopulation,c);
		setMyConstraints(c,0,2,GridBagConstraints.EAST);
		q.add(mvaThroughput,c);
		setMyConstraints(c,0,3,GridBagConstraints.EAST);
		q.add(mvaUtilization,c);
		setMyConstraints(c,1,0,GridBagConstraints.WEST);
		q.add(mvaGlobAvgRespo,c);
		setMyConstraints(c,1,1,GridBagConstraints.WEST);
		q.add(mvaGlobCycle,c);
		setMyConstraints(c,1,2,GridBagConstraints.WEST);
		q.add(mvaGlobThroughput,c);
		
		setMyConstraints(c,0,0,GridBagConstraints.EAST);
		main.add(p, c);
		setMyConstraints(c,0,1,GridBagConstraints.WEST);
		main.add(q, c);
		
		return main;
	}
	
	public void setMyConstraints(GridBagConstraints c, int gridx, int gridy, int anchor) {
		c.gridx = gridx;
		c.gridy = gridy;
	    c.anchor = anchor;
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getSource() == startGN){
			System.out.println("INIZIO - GN");
			
			this.gn.start();
			
			this.startMVA.setEnabled(true);
			this.constantG_text.setText(gn.getNormalizedConstantG().toString().substring(0, 14));
			
			this.gn.printAllOnStd();
			this.gn.printAllOnFile();
			
			System.out.println("FINE - GN");
			
			gnAvgRespo.setEnabled(true);
			gnThroughput.setEnabled(true);
			gnUtilization.setEnabled(true);
			gnPopulation.setEnabled(true);
			gnSaturation.setEnabled(true);
			gnMarginal.setEnabled(true);
			gnGlobAvgRespo.setEnabled(true);
			gnGlobThroughput.setEnabled(true);
			gnGlobCycle.setEnabled(true);
			
		}
		if (e.getSource() == startMVA){
			System.out.println("INIZIO - MVA");
			
			this.mva.start();
			this.minResp1_text.setText(mva.getAverageGlobalResponseTime().get(mva.getCentersSchema().get(0)).toString().substring(0, 14));
			
			this.mva.printAllOnStd();
			this.mva.printAllOnFile();
			
			System.out.println("FINE - MVA");
			
			mvaAvgRespo.setEnabled(true);
			mvaThroughput.setEnabled(true);
			mvaUtilization.setEnabled(true);
			mvaPopulation.setEnabled(true);
			mvaGlobAvgRespo.setEnabled(true);
			mvaGlobThroughput.setEnabled(true);
			mvaGlobCycle.setEnabled(true);
		}
		if (e.getSource() == gnAvgRespo){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgResponseTime", gn.getAverageResponseTime(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnGlobAvgRespo){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgGlobalResponseTime", gn.getAverageGlobalResponseTime(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnGlobCycle){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgGlobalCycleTime", gn.getAverageGlobalCycleTime(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnGlobThroughput){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("GlobalThroughput", gn.getGlobalThroughput(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnMarginal){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("Marginal probabilities", gn.getMarginalProbabilities(), gn.getCentersSchema(), 1);
	            }
	        });
				
		}
		if (e.getSource() == gnPopulation){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgPopulation", gn.getAveragePopulation(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnSaturation){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("Saturation probabilities", gn.getSaturationProbabilites());
	            }
	        });
				
		}
		if (e.getSource() == gnThroughput){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgTrhoughput", gn.getThroughput(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == gnUtilization){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("Utilization probabilities", gn.getUtilizationCoefficient(), gn.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == mvaAvgRespo){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgResponseTime", mva.getAverageResponseTime(), mva.getCentersSchema(), mva.getMinGlobalResponseTimeCoupleIndex());
	            }
	        });
				
		}
		if (e.getSource() == mvaGlobAvgRespo){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("GlobalAvgResponseTime", mva.getAverageGlobalResponseTime(), mva.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == mvaGlobCycle){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("GlobalAvgCycleTime", mva.getAverageGlobalCycleTime(), mva.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == mvaGlobThroughput){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("GlobalThroughput", mva.getGlobalThroughput(), mva.getCentersSchema());
	            }
	        });
				
		}
		if (e.getSource() == mvaPopulation){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgPopulation", mva.getAveragePopulation(), mva.getCentersSchema(), mva.getMinGlobalResponseTimeCoupleIndex());
	            }
	        });
				
		}
		if (e.getSource() == mvaThroughput){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgThroughput", mva.getThroughput(), mva.getCentersSchema(), mva.getMinGlobalResponseTimeCoupleIndex());
	            }
	        });
				
		}
		if (e.getSource() == mvaUtilization){
			//Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                TableIndex.createAndShowGUI("AvgUtilization", mva.getUtilization(), mva.getCentersSchema());
	            }
	        });
				
		}
		
	}

}