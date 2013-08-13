package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import model.properIdent;

import peakml.IPeak;
import peakml.IPeakSet;

@SuppressWarnings("serial")
public class LinkingFrame extends JFrame{
	
	JButton updatePlotsButton, displayLinksButton;
	JScrollPane peakScroll, idScroll;
	
	static JMenuBar menuBar;
	JMenu fileMenu;
	
	private static JTable peakTable;
	PeakTableModel peakTM;
    ListSelectionModel peakTableSelectionModel;
    String[] peakColumnNames;
    
    private static JTable idTable;
	IdentificationTableModel idTM;
    ListSelectionModel idTableSelectionModel;
    String[] idColumnNames;

    static int[] currentPeakRows;
	int[] currentIdentificationRows;
	
	ArrayList<Integer> pathLink;
	
	public LinkingFrame() {
        initComponents();
    }

	private void initComponents() {

    	updatePlotsButton = new JButton();
    	displayLinksButton = new JButton();
        peakScroll = new JScrollPane();
        idScroll = new JScrollPane();
        menuBar = new JMenuBar();
    	fileMenu = new JMenu("File");
    	
//    	setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        currentPeakRows = new int[0];
    	
    	pathLink = new ArrayList<Integer>();

    	peakColumnNames = new String [] {"No.",
    			"Mass",
                "Intensity",
                "Retention Time",
                "Link"};
        peakTM = new PeakTableModel(new Object [][] {}, peakColumnNames);
        	setPeakTable(new JTable(peakTM){
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);

				if (pathLink.contains(row)){
					c.setBackground(Color.CYAN);
				} else if (isRowSelected(row)){
					c.setBackground(Color.YELLOW);
				} else {
					for (int i : currentPeakRows){
						if (row == i){
							c.setBackground(Color.GREEN);
							return c;
						}
					}
					c.setBackground(Color.WHITE);
				}
				return c;
    		}
        });
        
        peakTableSelectionModel = getPeakTable().getSelectionModel();
        getPeakTable().setSelectionModel(peakTableSelectionModel);
        
        peakScroll.setViewportView(getPeakTable());
        
        idColumnNames = new String [] {"No.",
        		"Kegg ID",
                "Probability"};
        idTM = new IdentificationTableModel(new Object [][] {}, idColumnNames);
        	setIdTable(new JTable(idTM){
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);

				if (pathLink.contains(row)){
					c.setBackground(Color.CYAN);
				} else if (isRowSelected(row)){
					c.setBackground(Color.YELLOW);
				} else {
					for (int i : currentPeakRows){
						if (row == i){
							c.setBackground(Color.GREEN);
							return c;
						}
					}
					c.setBackground(Color.WHITE);
				}
				return c;
    		}
        });
        
        idTableSelectionModel = getIdTable().getSelectionModel();
        getIdTable().setSelectionModel(idTableSelectionModel);
        
        idScroll.setViewportView(getIdTable());

        updatePlotsButton.setText("Update Plots");
        displayLinksButton.setText("Display Links");

        fileMenu.setText("File");
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(peakScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(updatePlotsButton)
                        .add(0, 544, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(peakScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(updatePlotsButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
        );
        add(peakScroll);
        add(idScroll);
        add(updatePlotsButton);
		updatePlotsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
        		int[] selected = getPeakTable().getSelectedRows();
//                updateActionPerformed(selected);
            }
		});
		
		peakScroll.setPreferredSize(new Dimension(400, 650));
		idScroll.setPreferredSize(new Dimension(400, 650));
		
		menuBar.add(fileMenu);
		
		JMenuItem openPeakMLItem = new JMenuItem("Open PeakML File");
		fileMenu.add(openPeakMLItem);
		
		JMenuItem openIdentificationItem = new JMenuItem("Open Identification File");
		fileMenu.add(openIdentificationItem);
		
		JMenuItem loadPathItem = new JMenuItem("Load Path");
		fileMenu.add(loadPathItem);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	System.exit(0);
            }
		});
		fileMenu.add(exitMenuItem);
		
        pack();
    }
	
	public void updatePeakTable(IPeakSet<IPeak> peakset){
		
		IPeak current;
		Object[][] tableData = new Object[peakset.size()][5];
		for(int i = 0; i < peakset.size(); i++){
			current = peakset.get(i);
			tableData[i][0] = i;
			tableData[i][1] = current.getMass();
			tableData[i][2] = current.getIntensity();
			tableData[i][3] = current.getRetentionTime();
			try{
				tableData[i][4] = current.getAnnotation("probabilityIdentification").getValue();
			}
			catch(Exception e){
				tableData[i][4] = "n/a";
			}
		}
		getPeakTable().setModel(new DefaultTableModel(tableData, peakColumnNames));
	}
	
	public void updateIdTable(properIdent[] identifications){
		
		properIdent current;
		Object[][] tableData = new Object[identifications.length][3];
		for(int i = 0; i < identifications.length; i++){
			current = identifications[i];
			tableData[i][0] = i + 1;
			tableData[i][1] = current.getKegg();
			tableData[i][2] = current.getProbabilities();
//			try{
//				tableData[i][3] = current.getAnnotation("probabilityIdentification").getValue();
//			}
//			catch(Exception e){
//				tableData[i][3] = "n/a";
//			}
		}
		getPeakTable().setModel(new DefaultTableModel(tableData, idColumnNames));
	}

	public static void setPeakTable(JTable peakTable) {
		LinkingFrame.peakTable = peakTable;
	}

	public JTable getPeakTable() {
		return peakTable;
	}

	public static JTable getIdTable() {
		return idTable;
	}

	public static void setIdTable(JTable idTable) {
		LinkingFrame.idTable = idTable;
	}
	
}