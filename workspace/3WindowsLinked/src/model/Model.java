package model;

import java.io.File;
import java.util.ArrayList;

import peakml.IPeak;
import peakml.IPeakSet;

public class Model {
		
	static IdentificationStore idStore;
	private static PeakStore peakStore;
	static ArrayList<Link[]> linkingData;
	boolean pathLoaded, idsLoaded;
	
	int[] peakRows, idRows;
	
	public Model(){
		idStore = new IdentificationStore();
		peakStore = new PeakStore();
		pathLoaded = false;
	}
	
	public properIdent[] loadIdentifications(File file){
		return idStore.parseInputFile(file);
	}
	
	public IdentificationStore getIdStore() {
		return idStore;
	}

	public static void setIdStore(IdentificationStore idStore) {
		Model.idStore = idStore;
	}

	public IPeakSet<IPeak> loadPeaks(File file){
		return getPeakStore().parseInputFile(file);
	}

	public static void setPeakStore(PeakStore peakStore) {
		Model.peakStore = peakStore;
	}

	public PeakStore getPeakStore() {
		return peakStore;
	}

	public boolean isPathLoaded() {
		return pathLoaded;
	}

	public void setPathLoaded(boolean pathLoaded) {
		this.pathLoaded = pathLoaded;
	}

	public boolean isIdsLoaded() {
		return idsLoaded;
	}

	public void setIdsLoaded(boolean idsLoaded) {
		this.idsLoaded = idsLoaded;
	}
	
	
}