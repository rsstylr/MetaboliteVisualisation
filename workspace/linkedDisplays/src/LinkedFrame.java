import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
//import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.math.plot.Plot2DPanel;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import peakml.BackgroundIon;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
//import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

public class LinkedFrame extends JFrame{
	
	private static IPeakSet<IPeak> peakset;
	private static ArrayList<Link[]> linkingData;
	private static int[] currentRows;
	private static ArrayList<Integer> currentPathEntries;
	private static ArrayList<Color> currentPathEntryColours;
	
	private static javax.swing.JTabbedPane mainTabbedPane;
	private javax.swing.JTabbedPane peakTabbedPane;
	private static Plot2DPanel chromPlot;
    private static Plot2DPanel specPlot;
    private static Plot2DPanel relatedPlot;
    private javax.swing.JButton updateButton;
    private javax.swing.JScrollPane tableScroll;
    private static MyTableModel myTM;
    private static javax.swing.ListSelectionModel tableSelectionModel;
    private static javax.swing.JTable infoTable;
    private static javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu fileMenu;
    
    private static ImagePanel linkPanel;
    private static BufferedImage pathImg;
	private static ArrayList<JButton> regButtons;
	private static ArrayList<properIdent> buttonDetails;
	private static ArrayList<ArrayList<Integer>> relatedGroups;
	private static int prevGroup;
	private static int prevButton;
	private static Color prevColours[];
	private static properIdent[] fileInput;
	private static TransparentLabel relatedLabel;
	private static ArrayList<Integer> pathLink;
	private static int pathSelected;
	private static Color pathLinkColour;
	
    public LinkedFrame() {
        initComponents();
    }
    
    private void initComponents() {

    	mainTabbedPane = new javax.swing.JTabbedPane();
    	peakTabbedPane = new javax.swing.JTabbedPane();
    	chromPlot = new Plot2DPanel();
    	specPlot = new Plot2DPanel();
    	relatedPlot = new Plot2DPanel();
    	updateButton = new javax.swing.JButton();
        tableScroll = new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
    	fileMenu = new javax.swing.JMenu("File");
    	
    	currentRows = new int[0];
    	currentPathEntries = new ArrayList<Integer>();
    	currentPathEntryColours = new ArrayList<Color>();
    	
    	pathLink = new ArrayList<Integer>();
    	pathSelected = -1;
    	
    	setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        myTM = new MyTableModel(new Object [][] {},
            new String [] {"Mass",
                    "Intensity",
                    "Retention Time",
                    "Link"});
//                    "relation.ship",
//                    "Relation ID"});
        infoTable = new JTable(myTM){
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);

				if (pathLink.contains(row)){
					c.setBackground(Color.CYAN);
				} else if (isRowSelected(row)){
					c.setBackground(Color.YELLOW);
				} else {
					for (int i : currentRows){
						if (row == i){
							c.setBackground(Color.GREEN);
							return c;
						}
					}
					c.setBackground(Color.WHITE);
				}
				return c;
    		}
        };
        
        tableSelectionModel = infoTable.getSelectionModel();
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
                    .add(mainTabbedPane)
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
                .add(mainTabbedPane)
                .addContainerGap())
        );
        
        chromPlot.setAxisLabels("RT", "Intensity");
		chromPlot.addLegend("EAST");
		chromPlot.removePlotToolBar();
		peakTabbedPane.addTab("Chromatogram",chromPlot);
		
		specPlot.setAxisLabels("m/z", "Intensity");
		specPlot.addLegend("EAST");
		specPlot.removePlotToolBar();
		peakTabbedPane.addTab("Mass Spectrum", specPlot);
		
		relatedPlot.setAxisLabels("RT", "Intensity");
		relatedPlot.addLegend("EAST");
		relatedPlot.removePlotToolBar();
		peakTabbedPane.addTab("Related Peaks", relatedPlot);
		
		mainTabbedPane.addTab("Peaks", peakTabbedPane);
		
		updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
        		int[] selected = infoTable.getSelectedRows();
                updateActionPerformed(selected);
            }
		});
		
		menuBar.add(fileMenu);
		JMenuItem openPeakMLItem = new JMenuItem("Open PeakML File");
		openPeakMLItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	chromPlot.removeAllPlots();
        		specPlot.removeAllPlots();
        		relatedPlot.removeAllPlots();
            	boolean success = openPeakMLItemActionPerformed(evt);
            	if (success){
	            	String[] columnNames = {"Mass",
	                        "Intensity",
	                        "Retention Time",
	                        "Link"};
//	                        "relation.ship",
//	                        "Relation ID"};
	            	infoTable.setModel(new javax.swing.table.DefaultTableModel(updateTable(), columnNames));
            	}
            }
		});
		fileMenu.add(openPeakMLItem);
		JMenuItem openIdentificationItem = new JMenuItem("Open Identification File");
		openIdentificationItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	boolean success = openIdentificationItemActionPerformed(evt);
            	if (success){
//        			String pathID = "ko01100";
            		String pathID = "tbr01100";
            		try {
						getImg(pathID);
						getLinks(pathID);
						addPopupMenus();
//						System.out.println(relatedGroups.size());
					} catch (IOException e) {
						System.out.println("Image loading failed.");
					}
//        			frame.validate();
//        			frame.repaint();
//        		    frame.setVisible(true);
            	}
            }
		});
		fileMenu.add(openIdentificationItem);
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	System.exit(0);
            }
		});
		fileMenu.add(exitMenuItem);
		
		regButtons = new ArrayList<JButton>();
	    buttonDetails = new ArrayList<properIdent>();
	    relatedGroups = new ArrayList<ArrayList<Integer>>();
	    
	    prevGroup = -1;
	    prevButton = -1;
	    
	    relatedLabel = new TransparentLabel("");
	    relatedLabel.setBackground(Color.BLUE);
		
		linkPanel = new ImagePanel();
	    linkPanel.setLayout(null);
	    linkPanel.setOpaque(false);
	    
		linkPanel.add(relatedLabel, JLayeredPane.DEFAULT_LAYER);
	    mainTabbedPane.addTab("Pathway", linkPanel);
		
        pack();
    }
    
    public static void addPopupMenus(){
    	MetaboliteMenu mm;
    	for (int i = 0; i < regButtons.size(); i++){
    		if (buttonDetails.get(i) != null){
    			final RoundButton rb = (RoundButton) regButtons.get(i);
    			mm = new MetaboliteMenu(buttonDetails.get(i).getKegg());
    			ActionListener al = new java.awt.event.ActionListener() {
    	            public void actionPerformed(java.awt.event.ActionEvent evt) {
                	updateRelated(rb.getName());
                }};
    			mm.setRelatedAction(al);
    			regButtons.get(i).setComponentPopupMenu(mm);
    		}
    	}
    }
    
    // pull identifications from input file
	public static properIdent[] parseInputFile(File file){
		
		properIdent[] returnVals = null;
		try {
			returnVals = new properIdent[countLines(file.getAbsolutePath()) - 1];
		} catch (IOException e1) {
			System.out.println("Problem when counting lines in csv");
			e1.printStackTrace();
		}
		BufferedReader br = null;
		String[] splitLine;
		
		try {
			
			String sCurrentLine;
			int i = 0;
			int j;
			br = new BufferedReader(new FileReader(file));
			sCurrentLine = br.readLine();
			while ((sCurrentLine = br.readLine()) != null) {
				j = 0;
				splitLine = sCurrentLine.split(",", 0);
				double[] probs = new double[splitLine.length - 2];
				while(j < splitLine.length - 2){
					probs[j] = new Double(splitLine[j + 2]);
					j++;
				}
				returnVals[i] = new properIdent(splitLine[1], probs);
				System.out.println("dealt with " + (i+1) + " entries in csv");
				i++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sortIdents(returnVals, 0, returnVals.length - 1);
		
		return returnVals;
	}
	
	public static int partition(properIdent[] array, int left, int right, int pivotIndex){
		properIdent temp = array[pivotIndex];
		int pivotValue = Integer.parseInt(array[pivotIndex].getKegg().substring(1));
		array[pivotIndex] = array[right];
		array[right] = temp;
		int storeIndex = left;
		for (int i = left; i < right; i++){
			if (Integer.parseInt(array[i].getKegg().substring(1)) < pivotValue){
				temp = array[i];
				array[i] = array[storeIndex];
				array[storeIndex] = temp;
				storeIndex++;
			}
		}
		temp = array[storeIndex];
		array[storeIndex] = array[right];
		array[right] = temp;
		return storeIndex;
	}
	
	public static void sortIdents(properIdent[] array, int left, int right){
		if (left < right) {
			int pivotIndex = left + (right-left)/2;
			System.out.println(pivotIndex);
			int pivotNewIndex = partition(array, left, right, pivotIndex);
			sortIdents(array, left, pivotNewIndex - 1);
			sortIdents(array, pivotNewIndex + 1, right);
		}
	}
    
	// get pathway image from KEGG and display on image panel
	public static void getImg(String pID) throws IOException{
		
		try{
			URL pathImgUrl = new URL("http://rest.kegg.jp/get/" + pID + "/image");
			pathImg = ImageIO.read(pathImgUrl);
			ImagePanel.setImage(pathImg, mainTabbedPane.getWidth(), mainTabbedPane.getHeight());
		} catch (MalformedURLException e) {
			System.out.println("URL is malformed: http://rest.kegg.jp/get/" + pID + "/image");
			e.printStackTrace();
		}
	}
	
    public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
    
    private static boolean openIdentificationItemActionPerformed(java.awt.event.ActionEvent evt){
		
		final JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showOpenDialog(menuBar);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
            	fileInput = parseInputFile(file);
            	return true;
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
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
		String[] annos;
		String[] splitLine;
		Link[] newLinks;
		if (anno.contains(";")){
			 annos = anno.split(";", 0);
			 newLinks = new Link[annos.length];
			 for (int i = 0; i < annos.length; i++){
				 splitLine = annos[i].split(",", 0);
				 if (splitLine.length == 3){
						newLinks[i] = new Link(splitLine[0], "default", null, Double.parseDouble(splitLine[2]), peakIndex);
					} else if (splitLine.length == 4) {
						newLinks[i] = new Link(splitLine[0], splitLine[1], splitLine[2], Double.parseDouble(splitLine[3]), peakIndex);
					}
			 }
		} else {
			newLinks = new Link[1];
			splitLine = anno.split(",", 0);
			if (splitLine.length == 3){
				newLinks[0] = new Link(splitLine[0], "default", null, Double.parseDouble(splitLine[2]), peakIndex);
			} else if (splitLine.length == 4) {
				newLinks[0] = new Link(splitLine[0], splitLine[1], splitLine[2], Double.parseDouble(splitLine[3]), peakIndex);
			}
		}
		linkingData.add(newLinks);
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
    
    private static boolean openPeakMLItemActionPerformed(java.awt.event.ActionEvent evt){
		
		final JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showOpenDialog(menuBar);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);
            try {
            	ParseResult result = PeakMLParser.parse(new FileInputStream(file), true);
//            	Header header = result.header;
    			peakset = (IPeakSet<IPeak>) result.measurement;
    			linkingData = new ArrayList<Link[]>();
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
    
    private static Object[][] updateTable(){
    	
		IPeak current;

//		Object[][] tableData = new Object[peakset.size()][5];
		Object[][] tableData = new Object[peakset.size()][4];
		for(int i = 0; i < peakset.size(); i++){
			current = peakset.get(i);
			tableData[i][0] = current.getMass();
			tableData[i][1] = current.getIntensity();
			tableData[i][2] = current.getRetentionTime();
			try{
				tableData[i][3] = current.getAnnotation("probabilityIdentification").getValue();
			}
			catch(Exception e){
				tableData[i][3] = "n/a";
			}
//			try{
//				tableData[i][3] = current.getAnnotation("relation.ship").getValue();
//			}
//			catch(Exception e){
//				tableData[i][3] = "n/a";
//			}
//			try{
//				tableData[i][4] = current.getAnnotation("relation.id").getValue();
//			}
//			catch(Exception e){
//				tableData[i][4] = "n/a";
//			}
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
		
		currentRows = new int[sel.length];
		for (int i = 0; i < sel.length; i++){
			currentRows[i] = sel[i];
		}
		
		if (!currentPathEntries.isEmpty())
			for (int i = 0; i < currentPathEntries.size(); i++)
				regButtons.get(currentPathEntries.get(i)).setBackground(currentPathEntryColours.get(i));
		
		currentPathEntries.clear();
		currentPathEntryColours.clear();
		
		for (int i = 0; i < sel.length; i++){
			IPeak currentPeak = peakset.get(sel[i]);
			count = 0;
			found = false;
			///////////////////////////////////////////////////////////////////////////////////////			
			while (count < linkingData.size()){
				for (Link link : linkingData.get(count)){
					if (link.getPeakIndex() == sel[i]){
						for (JButton button : regButtons){
							if (button.getName().equalsIgnoreCase(link.getKeggID())){
								currentPathEntries.add(regButtons.indexOf(button));
								currentPathEntryColours.add(button.getBackground());
								button.setBackground(Color.YELLOW);
								System.out.println("Link found for: " + link.getKeggID());
							}
						}
						found = true;
					}
				}
				count++;
			}
			///////////////////////////////////////////////////////////////////////////////////////
//			// change cut off condition to include when peakIndex > i
//			while (!found && count < linkingData.size() && linkingData.get(count).getPeakIndex() <= sel[i]){
//				if (linkingData.get(count).getPeakIndex() == sel[i]){
//					for (JButton button : regButtons){
//						if (button.getName().equalsIgnoreCase(linkingData.get(count).getKeggID())){
//							currentPathEntries.add(regButtons.indexOf(button));
//							currentPathEntryColours.add(button.getBackground());
//							button.setBackground(Color.YELLOW);
//							System.out.println("Link found for: " + linkingData.get(count).getKeggID());
//						}
//					}
////					System.out.println(linkingData.get(count).toString());
//					found = true;
//				}
//				count++;
//			}
			if (!found){
				System.out.println("No linking data found for peak: " + sel[i]);
			}
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
    
    // initialize a button with the given attributes
	public static void initButton(Attributes att, final JButton button){
		button.setBorderPainted(false);
		button.setMargin(new Insets(0,0,0,0));
		button.setFont(new Font("Dialog", 0, 10));
		button.addActionListener(new ActionListener(){
	        @Override
	        public void actionPerformed(ActionEvent evt) {
	        	try {
	        		boolean found = false;
	        		int count = 0;
	        		//////////////////////////////////////////////////////////////////////////////////////////
	        		if (!pathLink.isEmpty()){
						regButtons.get(pathSelected).setBackground(pathLinkColour);
					}
	        		pathLink.clear();
	        		while(count < linkingData.size()){
	        			for (Link link : linkingData.get(count)){
	        				if (link.getKeggID().equals(button.getName())){
		    					for (JButton button : regButtons){
		    						if (button.getName().equalsIgnoreCase(link.getKeggID())){
		    							pathLinkColour = button.getBackground();
		    							pathSelected = findInButtons(button.getName());
		    							button.setBackground(Color.CYAN);
		    							pathLink.add(link.getPeakIndex());
		    						}
		    					}
		    					found = true;
		    				}
	        			}
	        			count++;
	        		}
	        		//////////////////////////////////////////////////////////////////////////////////////////
//	        		while (!found && count < linkingData.size()){
//	        			if (linkingData.get(count).getKeggID().equals(button.getName())){
//	    					for (JButton button : regButtons){
//	    						if (button.getName().equalsIgnoreCase(linkingData.get(count).getKeggID())){
//	    							if (pathLink != -1){
//	    								regButtons.get(pathSelected).setBackground(pathLinkColour);
//	    							}
//	    							pathLinkColour = button.getBackground();
//	    							pathSelected = findInButtons(button.getName());
//	    							button.setBackground(Color.CYAN);
//	    							pathLink = linkingData.get(count).getPeakIndex();
//	    						}
//	    					}
////	    					System.out.println(linkingData.get(count).toString());
//	    					found = true;
//	    				}
//	    				count++;
//	    			}
	    			if (!found){
	    				System.out.println("No linking data found for button: " + button.getName());
	    			}
				} catch (Exception e) {	// create actual handling
					System.out.println("Error when updating display of related nodes");
					e.printStackTrace();
				};
	        }
	    });
	}
	
	// set up visual details of a button
	public static void buttonGraphics(Attributes att, JButton button, String kegg, double xScale, double yScale){
		
		button.setText("");
    	button.setBorderPainted(true);
    	button.setName(att.getValue("name"));
    	double w = new Integer(att.getValue("width"));
    	double h = new Integer(att.getValue("height"));
    	double prob;
    	int index;
    	int keggNo = -1;
    	
    	if (kegg.contains(":"))
    		keggNo = Integer.parseInt(kegg.substring(kegg.indexOf(':') + 2));
    	if ((index = findIdent(fileInput, keggNo, 0, fileInput.length - 1)) != -1){
    		prob = fileInput[index].getProbabilities()[0];
    		if (prob == 0)
    			button.setVisible(false);
//    			button.setBackground(Color.BLACK);
    		else 
    			button.setBackground(new Color((int)(255*(1-prob)), (int)(255*prob), 0));
    		w = w*(1+prob);
			h = h*(1+prob);
    		properIdent newIdent = new properIdent(fileInput[index]);
    		buttonDetails.add(newIdent);
   		} else {
   			button.setBackground(Color.WHITE);
    		buttonDetails.add(null);
   		}
    	
    	BigDecimal x = new BigDecimal((new Integer(att.getValue("x")) - w/2)*xScale).setScale(0, RoundingMode.HALF_UP);
    	BigDecimal y = new BigDecimal((new Integer(att.getValue("y")) - h/2)*yScale).setScale(0, RoundingMode.HALF_UP);
    	BigDecimal adjustedWidth = new BigDecimal((w)*xScale).setScale(0, RoundingMode.HALF_EVEN);
    	BigDecimal adjustedHeight = new BigDecimal((h)*yScale).setScale(0, RoundingMode.HALF_EVEN);
    	
    	button.setBounds(x.intValueExact(), y.intValueExact(), adjustedWidth.intValueExact(), adjustedHeight.intValueExact());
	
	}
	
	public static void updateRelated(String buttonID){
		
		relatedLabel.setVisible(false);
		int x1, y1, x2, y2;
		JButton currentButton;
		
//		System.out.println(prevGroup);
		if (prevGroup != -1){
			for (int i = 0; i < relatedGroups.get(prevGroup).size(); i++){
				System.out.println(regButtons.get(relatedGroups.get(prevGroup).get(i)).getName());
				System.out.println(prevColours[i]);
				regButtons.get(relatedGroups.get(prevGroup).get(i)).setBackground(prevColours[i]);
			}
		} else if (prevButton != -1){
			regButtons.get(prevButton).setBackground(prevColours[0]);
		}
		
		System.out.println("///////////////////////////");
		int index = findInButtons(buttonID);
		int group = findGroup(index);
		if (group == -1){
			prevButton = index;
			prevColours = new Color[1];
			currentButton = regButtons.get(index);
			prevColours[0] = currentButton.getBackground();
			relatedLabel.setBounds(currentButton.getX()-3, currentButton.getY()-3,
					currentButton.getWidth()+6, currentButton.getHeight()+6);
			relatedLabel.setVisible(true);
			regButtons.get(index).setBackground(Color.ORANGE);
			prevGroup = -1;
		} else {
			prevGroup = group;
			prevColours = new Color[relatedGroups.get(group).size()];
			currentButton = regButtons.get(relatedGroups.get(group).get(0));
//			System.out.println(group);
			prevColours[0] = currentButton.getBackground();
			regButtons.get(relatedGroups.get(group).get(0)).setBackground(Color.ORANGE);
//			System.out.println(currentButton.getName());
			x1 = currentButton.getX();
			y1 = currentButton.getY();
			x2 = x1 + currentButton.getWidth();
			y2 = y1 + currentButton.getHeight();
			
			for (int i = 1; i < relatedGroups.get(group).size(); i++){
				currentButton = regButtons.get(relatedGroups.get(group).get(i));
				System.out.println(currentButton.getName());
				System.out.println(currentButton.getBackground());
				prevColours[i] = currentButton.getBackground();
				regButtons.get(relatedGroups.get(group).get(i)).setBackground(Color.ORANGE);
				if (x1 > currentButton.getX())
					x1 = currentButton.getX();
				if (y1 > currentButton.getY())
					y1 = currentButton.getY();
				if (currentButton.getX() + currentButton.getWidth() > x2)
					x2 = currentButton.getX() + currentButton.getWidth();
				if (currentButton.getY() + currentButton.getHeight() > y2)
					y2 = currentButton.getY() + currentButton.getHeight();
			}
			
			relatedLabel.setBounds(x1 - 3, y1 - 3, x2 - x1 + 6, y2 - y1 + 6);
			relatedLabel.setVisible(true);
			prevButton = -1;
		}
		
		System.out.println("///////////////////////////");
	}

	public static int findIdent(properIdent[] array, int key, int min, int max){
		if (max < min)
			return -1;
		else {
			int mid = min + (max-min)/2;
			if (Integer.parseInt(array[mid].getKegg().substring(1)) > key)
				return (findIdent(array, key, min, mid -1));
			else if (Integer.parseInt(array[mid].getKegg().substring(1)) < key)
				return (findIdent(array, key, mid + 1, max));
			else
				return mid;
		}
	}

	// find index of given button name in regButtons
	public static int findInButtons(String target){
		for (int i = 0; i < regButtons.size(); i++){
			if (target.equals(regButtons.get(i).getName())){
				return i;
			}
		}
		return -1;
	}
	
	// find group a given button (index) is in
	public static int findGroup(int regIndex){
		for (ArrayList<Integer> group : relatedGroups){
			for (Integer member : group){
				if (member == regIndex){
					return relatedGroups.indexOf(group);
				}
			}
		}
		return -1;
	}
    
 // deal with reactions with at least 2 substrates or 2 products
	public static void multiReaction(int[] subs, int[] pros){
		int[] entries = new int[subs.length + pros.length];
		int[] entryGroups = new int[entries.length];
		int[] entryGroupSizes = new int[entries.length];
		int maxGroupIndex;
		boolean found = false;
		
		for (int i = 0; i < subs.length; i++)
			entries[i] = subs[i];
		
		for (int i = 0; i < pros.length; i++){
			if (i > 0){
				for (int j = 0; j < i; j++){
					if (entries[i] == entries[j]){
						System.out.println("FOUND");
						found = true;
					}
				}
			}
			if (!found)
				entries[i + subs.length] = pros[i];
			found = false;
		}
		
		for (int i = 0; i < entries.length; i++){
			entryGroups[i] = findGroup(entries[i]);
			if (entryGroups[i] == -1)
				entryGroupSizes[i] = 0;
			else
				entryGroupSizes[i] = relatedGroups.get(entryGroups[i]).size();
		}
		
		maxGroupIndex = 0;
		for (int i = 1; i < entries.length; i++){
			if (entryGroupSizes[i] > entryGroupSizes[maxGroupIndex]){
				maxGroupIndex = i;
			}
		}
		
		if (entryGroupSizes[maxGroupIndex] == 0){
			ArrayList<Integer> newGroup = new ArrayList<Integer>();
			for (int i = 0; i < entries.length; i++)
				newGroup.add(entries[i]);
			relatedGroups.add(newGroup);
		} else {
			ArrayList<Integer> newGroup = new ArrayList<Integer>(
					relatedGroups.get(entryGroups[maxGroupIndex]));
			for (int i = 0; i < entries.length; i++){
				if (entries[i] != entries[maxGroupIndex]){
					newGroup.add(entries[i]);
					if (entryGroups[i] != -1)
						relatedGroups.get(entryGroups[i]).clear();
				}
			}
			relatedGroups.set(entryGroups[maxGroupIndex], newGroup);
		}
	}
	
	public static void bothOneReaction(int sub, int pro){
		
		boolean found = false;
		int subGroup = findGroup(sub);
		int proGroup = findGroup(pro);
		
		if (subGroup == -1 && proGroup == -1){
			relatedGroups.add(new ArrayList<Integer>());
			System.out.println("NEW GROUP. GROUP: " + relatedGroups.size());
			relatedGroups.get(relatedGroups.size() - 1).add(sub);
			relatedGroups.get(relatedGroups.size() - 1).add(pro);
		} else if (subGroup == -1){
//			System.out.println("SUBGROUP == -1");
			ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
			for (int i = 0; i < pG.size(); i++){
					if (sub == pG.get(i)){
//						System.out.println("FOUND");
						found = true;
					}
				}
				if (!found)
					relatedGroups.get(proGroup).add(sub);
				found = false;
		} else if (proGroup == -1){
//			System.out.println("PROGROUP == -1");
			ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
			for (int i = 0; i < sG.size(); i++){
					if (pro == sG.get(i)){
//						System.out.println("FOUND");
						found = true;
					}
				}
				if (!found)
					relatedGroups.get(subGroup).add(pro);
				found = false;
		} else if (subGroup != proGroup){
//			System.out.println("IN HERE");
			ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
			ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
			found = false;
			if (sG.size() >= pG.size()){
				for (int i = 0; i < pG.size(); i++){
					if (i > 0){
						for (int j = 0; j < i; j++){
							if (pG.get(i) == sG.get(j)){
								System.out.println("FOUND1");
								found = true;
							}
						}
					}
					if (!found)
						sG.add(pG.get(i));
					found = false;
				}
				relatedGroups.set(subGroup, sG);
				relatedGroups.get(proGroup).clear();
//				System.out.println("CLEARING GROUP " + proGroup + ". ADDING TO GROUP " + subGroup);
			} else {
				for (int i = 0; i < sG.size(); i++){
					if (i > 0){
						for (int j = 0; j < i; j++){
							if (sG.get(i) == pG.get(j)){
								System.out.println("FOUND2");
								found = true;
							}
						}
					}
					if (!found)
						pG.add(sG.get(i));
					found = false;
				}
				relatedGroups.set(proGroup, pG);
				relatedGroups.get(subGroup).clear();
//				System.out.println("CLEARING GROUP " + subGroup + ". ADDING TO GROUP " + proGroup);
			}
		}
	}
    
 // add buttons with links onto the panel
	public static void getLinks(String pID){
		
		try {
			 
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			
			DefaultHandler handler = new DefaultHandler() {
				
				boolean entry = false;
				boolean graphics = false;
				boolean relation = false;
				boolean reaction = false;
				boolean substrate = false;
				boolean product = false;
				double xScaleFactor = (double)mainTabbedPane.getWidth()/(double)pathImg.getWidth();
				double yScaleFactor = (double)mainTabbedPane.getHeight()/(double)pathImg.getHeight();
				String currentKegg;
				int currentSub;
				int currentPro;
				int subs[];
				int pros[];
				String sub;
				String pro;
			 
				public void startElement(String uri, String localName,String qName, 
			                Attributes attributes) throws SAXException {
			 
					if (qName.equalsIgnoreCase("entry")) {
						if (attributes.getValue("type").equalsIgnoreCase("compound")){
							JButton newButton = new RoundButton("");
							newButton.setName(attributes.getValue("id"));
							initButton(attributes, newButton);
							regButtons.add(newButton);
							linkPanel.add(regButtons.get(regButtons.size() - 1), JLayeredPane.PALETTE_LAYER);
						}
						currentKegg = attributes.getValue("name");
						entry = true;
					}
					
					if (qName.equalsIgnoreCase("graphics")) {
						
						if (attributes.getValue("type").equalsIgnoreCase("circle")){
							JButton theButton = regButtons.get(regButtons.size() - 1);
							buttonGraphics(attributes, theButton, currentKegg, xScaleFactor, yScaleFactor);
						}
						graphics = true;
					}
					
					if (qName.equalsIgnoreCase("relation")) {
						
						String entry1 = attributes.getValue("entry1");
						String entry2 = attributes.getValue("entry2");
						
						int e1 = findInButtons(entry1);
						int e2 = findInButtons(entry2);
						
						if (e1 != -1 && e2 != -1){
//							System.out.println("IN HERE");
							int subGroup = findGroup(e1);
							int proGroup = findGroup(e2);
							
							if (subGroup == -1 && proGroup == -1){
								relatedGroups.add(new ArrayList<Integer>());
								relatedGroups.get(relatedGroups.size() - 1).add(e1);
								relatedGroups.get(relatedGroups.size() - 1).add(e2);
							} else if (subGroup == -1){
								relatedGroups.get(proGroup).add(e1);
							} else if (proGroup == -1){
								relatedGroups.get(subGroup).add(e2);
							} else {
								ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
								ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
								if (sG.size() >= pG.size()){
									for (int current : pG){
										sG.add(current);
									}
									relatedGroups.set(subGroup, sG);
									relatedGroups.get(proGroup).clear();
								} else {
									for (int current : sG){
										pG.add(current);
									}
									relatedGroups.set(proGroup, pG);
									relatedGroups.get(subGroup).clear();
								}
							}
						}
						
						relation = true;
					}
					
					if (qName.equalsIgnoreCase("reaction")) {
													
//						System.out.println("REACTION ID: " + attributes.getValue("id"));
						currentSub = -1;
						currentPro = -1;
						subs = null;
						pros = null;
						substrate = false;
						product = false;
						
						reaction = true;
					}
					
					if (qName.equalsIgnoreCase("substrate")) {
						sub = attributes.getValue("name").substring(4);
						currentSub = findInButtons(sub);
//						System.out.println(currentSub);
						if (currentSub == -1){
						} else if (buttonDetails.get(currentSub) != null){
							if (buttonDetails.get(currentSub).getProbabilities()[0] >= 0.50){
								if(subs != null){
//									System.out.println("SUBS >= 1");
//									System.out.println(subs.length);
									int[] temp = new int[subs.length + 1];
									for (int i = 0; i < subs.length; i++){
										temp[i] = subs[i];
									}
									temp[temp.length - 1] = currentSub;
//									System.out.println(subs.length);
									subs = temp;
//									subs[subs.length] = currentSub;
								} else {
//									System.out.print("CREATING SUBS");
									subs = new int[1];
									subs[0] = currentSub;
								}
							}
						}
						substrate = true;
					}
					
					if (qName.equalsIgnoreCase("product")) {
						
						if (subs != null){
//							System.out.println("SUB != NULL");
							if (subs.length >= 1){
								pro = attributes.getValue("name").substring(4);
								currentPro = findInButtons(pro);
								if (currentPro == -1){
								} else if (buttonDetails.get(currentPro) != null){
									if (buttonDetails.get(currentPro).getProbabilities()[0] >= 0.50){
										if (pros != null){
											int[] temp = new int[pros.length];
											for (int i = 0; i < pros.length; i++){
												temp[i] = pros[i];
											}
											pros = temp;
										} else {
											pros = new int[1];
											pros[0] = currentPro;
										}
									}
								}
							}
						}
						product = true;
					}
				}
			 
				public void endElement(String uri, String localName,
					String qName) throws SAXException {
					
					
					if (qName.equalsIgnoreCase("reaction")){
						if (subs != null && pros != null){
							if (subs.length > 1 || pros.length > 1){
								multiReaction(subs, pros);
							} else if (subs.length == 1 && pros.length == 1){
								bothOneReaction(subs[0], pros[0]);
							}
						}
					}
				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
				}
			};
			
			saxParser.parse(new InputSource(new URL("http://rest.kegg.jp/get/" + pID + "/kgml").openStream()), handler);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LinkedFrame().setVisible(true);
            }
        });
    }
}
