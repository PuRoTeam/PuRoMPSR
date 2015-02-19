package it.mpsr;
/*
 * SimpleTableDemo.java requires no other files.
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class TableIndex extends JPanel {
   
	/**
	 * Costruttore che tratta un hashmap di Center e BigDecimal
	 * @param hash
	 * @param centersSchema
	 */
    public TableIndex(HashMap<Center, BigDecimal> hash, ArrayList<Center> centersSchema) {
        
    	super(new GridLayout(1,0));
    	this.setSize(700, 400);
 
        String[] columnNames = {"Center", "Value"};
 
        Object[][] data = new Object[centersSchema.size()][columnNames.length];
        
        for (int i = 0; i < centersSchema.size(); i++) {
			data[i][0] = centersSchema.get(i).getName();
			data[i][1] = hash.get(centersSchema.get(i));
		}
        
        final JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    /**
     * Costruttore che tratta un hashmap di Integer e BigDecimal
     * @param hash
     */
    public TableIndex(HashMap<Integer, BigDecimal> hash) {
        
    	super(new GridLayout(1,0));
    	this.setSize(700, 400);
 
        String[] columnNames = {"N", "Value"};
 
        Object[][] data = new Object[36][columnNames.length];
        
        for (int i = 0; i <= 35; i++) {
			data[i][0] = i;
			data[i][1] = hash.get(i);
		}
        
        final JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    /**
     * Costruttore che tratta un hashmap di Center e un hashmap di Integer e BigDecimal
     * @param hash
     * @param centersSchema
     * @param j
     */
    public TableIndex(HashMap<Center, HashMap<Integer, BigDecimal>> hash, ArrayList<Center> centersSchema, int j) {
        
    	super(new GridLayout(1,0));
    	this.setSize(700, 400);
 
        String[] columnNames = {"Center","N","Value"};
 
        Object[][] data = new Object[144][columnNames.length];
        
        for (int i = 0; i < centersSchema.size() ; i++) {
        	for (int k=0; k <= 35; k++) {
        		data[(36*i)+(k)][0] = centersSchema.get(i).getName();
    			data[(36*i)+(k)][1] = k;
    			data[(36*i)+(k)][2] = hash.get(centersSchema.get(i)).get(k);
			}
		}
        
        final JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    /**
     * Costruttore che accetta un ArrayList di ArrayList di HashMap
     * @param list
     * @param centersSchema
     * @param minIndex
     */
    public TableIndex(ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> list, ArrayList<Center> centersSchema, int minIndex) {
        
    	super(new GridLayout(1,0));
    	this.setSize(700, 400);
 
        String[] columnNames = {"Couple","Value"};
 
        Object[][] data = new Object[centersSchema.size()][columnNames.length];
        for (int i = 0; i < centersSchema.size() ; i++) {
        	data[i][0] = centersSchema.get(i).getName();
        	data[i][1] = list.get(minIndex).get(i).get(35);
        }
        
        final JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI(String s, HashMap<Center, BigDecimal> hash, ArrayList<Center> centersSchema) {
        //Create and set up the window.
        JFrame frame = new JFrame(s);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Create and set up the content pane.
        TableIndex newContentPane = new TableIndex(hash, centersSchema);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void createAndShowGUI(String s, HashMap<Integer, BigDecimal> hash) {
        //Create and set up the window.
        JFrame frame = new JFrame(s);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Create and set up the content pane.
        TableIndex newContentPane = new TableIndex(hash);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void createAndShowGUI(String s, HashMap<Center, HashMap<Integer, BigDecimal>> hash, ArrayList<Center> centersSchema, int j) {
        //Create and set up the window.
        JFrame frame = new JFrame(s);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Create and set up the content pane.
        TableIndex newContentPane = new TableIndex(hash, centersSchema, j);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void createAndShowGUI(String s, ArrayList<ArrayList<HashMap<Integer, BigDecimal>>> list, ArrayList<Center> centersSchema, int minIndex) {
        //Create and set up the window.
        JFrame frame = new JFrame(s);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Create and set up the content pane.
        TableIndex newContentPane = new TableIndex(list, centersSchema, minIndex);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
}