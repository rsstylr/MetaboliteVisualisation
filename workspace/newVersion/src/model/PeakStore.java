package model;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Vector;

import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

public class PeakStore {
	
	static IPeakSet<IPeak> peakset;
	static ArrayList<Link[]> linkingData;
	
	@SuppressWarnings("unchecked")
	public IPeakSet<IPeak> parseInputFile(File file){
		try {
			System.out.println(file.getAbsolutePath());
        	ParseResult result = PeakMLParser.parse(new FileInputStream(file), true);
//			Header header = result.header;
			peakset = (IPeakSet<IPeak>) result.measurement;
			linkingData = new ArrayList<Link[]>();
			getProbabilityAttributes();
			return peakset;
		}
		catch (Exception e)
		{
			System.out.println("File operation failed.");
		}
		return null;
	}
	
	public static IPeakSet<IPeak> getPeakset() {
		return peakset;
	}



	public static void setPeakset(IPeakSet<IPeak> peakset) {
		PeakStore.peakset = peakset;
	}



	public ArrayList<Link[]> getLinkingData() {
		return linkingData;
	}



	public static void setLinkingData(ArrayList<Link[]> linkingData) {
		PeakStore.linkingData = linkingData;
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

	public Vector<IPeak> getSelectedPeaks(int[] selected) {
		Vector<IPeak> selectedPeaks = new Vector<IPeak>();
		if (selected.length == 0){
			return null;
		} else {
			for (int i = 0; i < selected.length; i++){
				selectedPeaks.add(peakset.get(selected[i]));
			}
			return selectedPeaks;
		}
	}
    
//    @SuppressWarnings("unchecked")
//	public static void recursive(IPeak peak, char type){
//		
//		Class<? extends IPeak> cls = peak.getClass();
//		if (cls.equals(IPeakSet.class))
//		{
//			for (IPeak p : (IPeakSet<IPeak>) peak){
//				recursive(p, type);
//			}
//		}
//		else if (cls.equals(MassChromatogram.class)){
//			MassChromatogram mc = (MassChromatogram) peak;
//			if (type == 'c'){
//				drawPlot(mc.getPeaks(), peak, type);
//			Vector<IPeak> peaksInRT = peakset.getPeaksInRetentionTimeRange((mc.getRetentionTime())-1, (mc.getRetentionTime())+1);
//			drawPlot(peaksInRT, peak, 's');
//			} else if (type == 'r')
//				drawPlot(mc.getPeaks(), peak, type);
//		}
//		else if (cls.equals(BackgroundIon.class))
//			;
//	}
}