import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

public class Ident {

	public String formula;
	public String name;
	public String keggID;
	public double[] probabilities;
	
	public Ident(String formula, String name, double[] probabilities) {
		this.formula = formula;
		this.name = name;
		this.keggID = cacheLookUp();
		this.probabilities = probabilities;
	}
	
	public Ident(String formula, String name, String kegg, double[] probabilities) {
		this.formula = formula;
		this.name = name;
		this.keggID = kegg;
		this.probabilities = probabilities;
	}
	
	public Ident(Ident ident){
		this.formula = ident.formula;
		this.name = ident.name;
		this.keggID = ident.keggID;
		this.probabilities = ident.probabilities;
	}
	
	public String getKeggID() {
		return keggID;
	}

	public void setKeggID(String keggID) {
		this.keggID = keggID;
	}

	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double[] getProbabilities() {
		return probabilities;
	}
	public void setProbabilities(double[] probabilities) {
		this.probabilities = probabilities;
	}

	@Override
	public String toString() {
		return "Ident [formula=" + formula + ", name=" + name
				+ ", KEGG ID= " + keggID + ", probabilities=" + Arrays.toString(probabilities) + "]";
	}
	
	public static void writeToCache(String text) {
        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
//                            "/users/level3/1002858t/SummerProject2013/keggCache.csv"), true));
        	BufferedWriter bw = new BufferedWriter(new FileWriter(new File("keggCache.csv"), true));
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
        	System.out.println("file writing error.");
        }
	}
	
	public String cacheLookUp(){
		File cacheFile = new File("/users/level3/1002858t/SummerProject2013/keggCache.csv");
//		File cacheFile = new File("keggCache.csv");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(cacheFile));
		} catch (FileNotFoundException e) {
			System.out.println("Problem creating buffered reader in cacheLookup");
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			
			String sCurrentLine;
			String[] splitLine;
			Boolean found = false;
			
			while ((sCurrentLine = br.readLine()) != null && found==false) {
				splitLine = sCurrentLine.split(",", 0);
				if (splitLine[0].equalsIgnoreCase(formula)){
					for (int i = 1; i < splitLine.length - 1; i++){
						if (splitLine[i].equalsIgnoreCase(name)){
							found = true;
							System.out.println("found in cache file");
							return splitLine[splitLine.length - 1];
						}
					}
				}
			}
 
			String keggId = keggLookUp();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keggID;
	}
	
	public String keggLookUp(){
		System.out.println("in kegg lookup");
		try {
			URL url;
			if (formula.contains("(")){
				url = new URL("http://rest.kegg.jp/find/compound/" + formula.substring(0, formula.indexOf('(')) + "/formula");
			}
			else {
				url = new URL("http://rest.kegg.jp/find/compound/" + formula + "/formula");
			}
			URL innerUrl;
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			HttpURLConnection innerConn;
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String inputLine;
	        Scanner resultsScanner;
	        Scanner infoScanner;
	        String target = name.replace('_', ',');
	        while ((inputLine = in.readLine()) != null) {
	        	try {
	        		resultsScanner = new Scanner(inputLine);
	        		String keggID = resultsScanner.next();
	        		System.out.println(keggID);
//	        		return keggID;	// temp fix for overly long sequence
	        		innerUrl = new URL("http://rest.kegg.jp/get/" + keggID);
	    			innerConn = (HttpURLConnection) innerUrl.openConnection();
	    			infoScanner = new Scanner(innerConn.getInputStream());
	    			String currentToken;
	    			boolean inNames = false;
	    	        while ((currentToken = infoScanner.nextLine()) != null){
	    	        	if (!inNames){
	    	        		if (currentToken.substring(0,4).equalsIgnoreCase("NAME")){
	    	        			inNames = true;
	    	        			currentToken = currentToken.substring(5);
	    	        			currentToken = currentToken.trim();
	    	        		}
	    	        	}
	    	        	if (inNames){
	    	        		if (currentToken.charAt(currentToken.length() - 1) == ';'){
	    	        			currentToken = currentToken.substring(0, currentToken.length() - 1);
//	    	        			System.out.println("comparing: " + currentToken + " to; " + target);
	    	        			if (currentToken.equalsIgnoreCase(target)){
	    	        				String addToCache = formula + "," + name + "," + keggID;
	    	        				writeToCache(addToCache);
	    	        				System.out.println("found from kegg");
	    	        				return keggID;
	    	        			}
	    	        		} else {
	    	        			if (currentToken.equalsIgnoreCase(target)){
	    	        				String addToCache = formula + "," + name + "," + keggID;
	    	        				writeToCache(addToCache);
	    	        				System.out.println("found from kegg");
	    	        				return keggID;
	    	        			}
	    	        			break;
	    	        		}
	    	        	}
	    	        }
	        	} catch (Exception e){
	        		
	        	}
	        }
	        in.close();
	        return "n/a";
		} catch(Exception e){
			System.out.println("exception");
	        return "n/a";

		}
	}
}