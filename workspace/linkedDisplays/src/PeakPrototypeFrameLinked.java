

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
//import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.math.plot.Plot2DPanel;

import peakml.BackgroundIon;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
//import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

public class PeakPrototypeFrameLinked extends javax.swing.JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static IPeakSet<IPeak> peakset;
	private static ArrayList<Link> linkingData;
//	private static int[] oldSelected;
	
	private javax.swing.JTabbedPane tabbedPane;
	private static Plot2DPanel chromPlot;
    private static Plot2DPanel specPlot;
    private static Plot2DPanel relatedPlot;
    private javax.swing.JButton updateButton;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.ListSelectionModel tableSelectionModel;
    private javax.swing.JTable infoTable;
    private static javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu fileMenu;

    public PeakPrototypeFrameLinked() {
        initComponents();
    }

//    @SuppressWarnings("unchecked")
    private void initComponents() {

    	tabbedPane = new javax.swing.JTabbedPane();
    	chromPlot = new Plot2DPanel();
    	specPlot = new Plot2DPanel();
    	relatedPlot = new Plot2DPanel();
    	updateButton = new javax.swing.JButton();
        infoTable = new javax.swing.JTable();
        tableScroll = new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
    	fileMenu = new javax.swing.JMenu("File");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        infoTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Mass",
                    "Intensity",
                    "Retention Time",
                    "relation.ship",
                    "Relation ID"}
        ));
        tableSelectionModel = infoTable.getSelectionModel();
        tableSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        infoTable.setSelectionModel(tableSelectionModel);
        
        tableScroll.setViewportView(infoTable);

        updateButton.setText("Update Plots");

        fileMenu.setText("File");
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tableScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tabbedPane)
                    .add(layout.createSequentialGroup()
                        .add(updateButton)
                        .add(0, 544, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(updateButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tabbedPane)
                .addContainerGap())
        );
        
        chromPlot.setAxisLabels("RT", "Intensity");
		chromPlot.addLegend("EAST");
		chromPlot.removePlotToolBar();
		tabbedPane.addTab("Chromatogram",chromPlot);
		
		specPlot.setAxisLabels("m/z", "Intensity");
		specPlot.addLegend("EAST");
		specPlot.removePlotToolBar();
		tabbedPane.addTab("Mass Spectrum", specPlot);
		
		relatedPlot.setAxisLabels("RT", "Intensity");
		relatedPlot.addLegend("EAST");
		relatedPlot.removePlotToolBar();
		tabbedPane.addTab("Related Peaks", relatedPlot);
		
		updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
        		int[] selected = infoTable.getSelectedRows();
                updateActionPerformed(selected);
            }
		});
		
		menuBar.add(fileMenu);
		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	chromPlot.removeAllPlots();
        		specPlot.removeAllPlots();
        		relatedPlot.removeAllPlots();
            	boolean success = openMenuItemActionPerformed(evt);
            	if (success){
	            	String[] columnNames = {"Mass",
	                        "Intensity",
	                        "Retention Time",
	                        "relation.ship",
	                        "Relation ID"};
	            	infoTable.setModel(new javax.swing.table.DefaultTableModel(updateTable(), columnNames));
            	}
            }
		});
		fileMenu.add(openMenuItem);
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	System.exit(0);
            }
		});
		fileMenu.add(exitMenuItem);
		
//		oldSelected = null;
		
        pack();
    }
    
    @SuppressWarnings("unchecked")
	private static boolean openMenuItemActionPerformed(java.awt.event.ActionEvent evt){
		
		final JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showOpenDialog(menuBar);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);
            try {
            	ParseResult result = PeakMLParser.parse(new FileInputStream(file), true);
//            	Header header = result.header;
    			peakset = (IPeakSet<IPeak>) result.measurement;
    			linkingData = new ArrayList<Link>();
    			getProbabilityAttributes();
    			return true;
    		}
    		catch (Exception e)
    		{
    			System.out.println("File operation failed.");
    			return false;
    		}
		}
		else{
			System.out.println("File operation cancelled.");
			return false;
		}
    }
    
	public static void addLinkData(String anno, int peakIndex){
		System.out.println("ADDING NEW LINK DATA: " + anno);
		if (anno.contains(";"))
			anno = anno.substring(0, anno.indexOf(';'));
		Link newLink = null;
		String[] splitLine = anno.split(",", 0);
		if (splitLine.length == 3){
			newLink = new Link(splitLine[0], "default", null, Double.parseDouble(splitLine[2]), peakIndex);
		} else if (splitLine.length == 4) {
			newLink = new Link(splitLine[0], splitLine[1], splitLine[2], Double.parseDouble(splitLine[3]), peakIndex);
		}
		linkingData.add(newLink);
		System.out.println(linkingData.size());
	}
	
	public static void getProbabilityAttributes(){
		
		String annotation;
		linkingData.clear();
		int peakCounter = 0;
		for (IPeak peak : peakset){
			if (peak.getAnnotation("probabilityIdentification") != null){
				annotation = peak.getAnnotation("probabilityIdentification").getValue();
				addLinkData(annotation, peakCounter);
			} else
				System.out.println("no anno");
			peakCounter++;
		}
	}
    
    private static Object[][] updateTable(){
    	
		IPeak current;

		Object[][] tableData = new Object[peakset.size()][5];
		for(int i = 0; i < peakset.size(); i++){
			current = peakset.get(i);
			tableData[i][0] = current.getMass();
			tableData[i][1] = current.getIntensity();
			tableData[i][2] = current.getRetentionTime();
			try{
				tableData[i][3] = current.getAnnotation("relation.ship").getValue();
			}
			catch(Exception e){
				tableData[i][3] = "n/a";
			}
			try{
				tableData[i][4] = current.getAnnotation("relation.id").getValue();
			}
			catch(Exception e){
				tableData[i][4] = "n/a";
			}
		}
		return tableData;
	}
        
    private static void updateActionPerformed(int[] sel){
    	
    	chromPlot.removeAllPlots();
		specPlot.removeAllPlots();
		relatedPlot.removeAllPlots();
		
		String anno;
		int count;
		boolean found;
    	for (int i : sel){
			IPeak currentPeak = peakset.get(i);
			count = 0;
			found = false;
			// change cut off condition to include when peakIndex > i
			while (!found && count < linkingData.size()){
				if (linkingData.get(count).getPeakIndex() == i){
					System.out.println(linkingData.get(count).toString());
					found = true;
				}
				count++;
			}
			if (!found)
				System.out.println("No linking data found for peak: " + i);
			try{
    			anno = (currentPeak.getAnnotation("relation.id")).getValue();
    			if (!anno.equals("-1")){
    				for (IPeak p : peakset){
    					if (p.getAnnotation("relation.id").getValue().equals(anno)){
    						recursive(p, 'r');
    					}
    				}
    			}
    			recursive(currentPeak, 'c');
			} catch (NullPointerException npe){
				recursive(currentPeak, 'c');
			}
    	}
    }
    
	@SuppressWarnings("unchecked")
	public static void recursive(IPeak peak, char type){
		
		Class<? extends IPeak> cls = peak.getClass();
		if (cls.equals(IPeakSet.class))
		{
			for (IPeak p : (IPeakSet<IPeak>) peak){
				recursive(p, type);
			}
		}
		else if (cls.equals(MassChromatogram.class)){
			MassChromatogram mc = (MassChromatogram) peak;
			if (type == 'c'){
				drawPlot(mc.getPeaks(), peak, type);
			Vector<IPeak> peaksInRT = peakset.getPeaksInRetentionTimeRange((mc.getRetentionTime())-1, (mc.getRetentionTime())+1);
			drawPlot(peaksInRT, peak, 's');
			} else if (type == 'r')
				drawPlot(mc.getPeaks(), peak, type);
		}
		else if (cls.equals(BackgroundIon.class))
			;
	}
	
	public static void drawPlot(Vector<IPeak> selectedPeaks, IPeak original, char type){
		
		int count = 0;
		double[] xPoints = new double[selectedPeaks.size()];
		double[] yPoints = new double[selectedPeaks.size()];
		for (IPeak currentPeak : selectedPeaks){
			if (type == 'c' || type == 'r')
				xPoints[count] = currentPeak.getRetentionTime();
			else if (type == 's')
				xPoints[count] = currentPeak.getMass();
			yPoints[count] = currentPeak.getIntensity();
			count++;
		}
		Double mass = original.getMass();
		if (type == 'c')
			chromPlot.addLinePlot(mass.toString(), xPoints, yPoints);
		else if (type == 's')
			specPlot.addBarPlot(mass.toString(), xPoints, yPoints);
		else if (type == 'r')
			relatedPlot.addLinePlot(mass.toString(), xPoints, yPoints);
	}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PeakPrototypeFrameLinked().setVisible(true);
            }
        });
    }
    
    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
        	if (!e.getValueIsAdjusting() == true){
//	            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	 
//	            int[] newSelected;
//	            if (lsm.isSelectionEmpty()) {
//	            	chromPlot.removeAllPlots();
//	        		specPlot.removeAllPlots();
//	        		relatedPlot.removeAllPlots();
//	            	oldSelected = null;
//	            } else {
//	                int minIndex = lsm.getMinSelectionIndex();
//	                int maxIndex = lsm.getMaxSelectionIndex();
//	                int count = 0;
//	                newSelected = new int[(maxIndex - minIndex) + 1];
//	                for (int i = minIndex; i <= maxIndex; i++) {
//	                    if (lsm.isSelectedIndex(i)) {
//	                        newSelected[count] = i;
//	                        count++;
//	                    }
//	                }
////                	updateActionPerformed(newSelected);	// temp solution
//	                if (oldSelected == null){
//	                	oldSelected = newSelected;
//	                	updateActionPerformed(newSelected);
//	                } else {
//	                	boolean found = false;
//	                	for (int i : oldSelected){
//	                		for (int j : newSelected){
//	                			if (i == j){
//	                				found = true;
//	                				break;
//	                			}
//	                		}
//	                		if (!found){
//	                			System.out.println("delete plot for index " + i);
//	                			// removePlot(index of i in plot list)
//	                		}
//	                	}
//	                	found = false;
//	                	for (int i : newSelected){
//	                		for (int j : oldSelected){
//	                			if (i == j){
//	                				found = true;
//	                				break;
//	                			}
//	                		}
//	                		if (!found){
//	                			System.out.println("add plot for index " + i);
//	                			recursive(peakset.get(i), 'c');
//	                		}
//	                	}
//	                }
//	                oldSelected = newSelected;
	            }
        	}
        }
    }
//}
