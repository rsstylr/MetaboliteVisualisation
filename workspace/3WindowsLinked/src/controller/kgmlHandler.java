package controller;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLayeredPane;

import model.Model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import view.View;

public class kgmlHandler extends DefaultHandler {
	
	boolean entry, graphics, relation, reaction, substrate, product;
	double xScaleFactor, yScaleFactor;
	int currentSub, currentPro;
	int subs[], pros[];
	String currentKegg, sub, pro;
	model.Model theModel;
	view.View theView;
	
	// change to send a Dimension object
	public kgmlHandler(int panelWidth, int panelHeight, double imgWidth, double imgHeight,
			Model theModel, View theView) {
		super();
		this.xScaleFactor = (double)panelWidth/(double)imgWidth;
		this.yScaleFactor = (double)panelHeight/(double)imgHeight;
		this.theModel = theModel;
		this.theView = theView;
		entry = false;
		graphics = false;
		relation = false;
		reaction = false;
		substrate = false;
		product = false;
	}

	public model.Model getModel() {
		return theModel;
	}

	public void setModel(model.Model theModel) {
		this.theModel = theModel;
	}

	public view.View getView() {
		return theView;
	}

	public void setView(view.View theView) {
		this.theView = theView;
	}
	
	// deal with reactions with at least 2 substrates or 2 products
//	public static void multiReaction(int[] subs, int[] pros){
//		int[] entries = new int[subs.length + pros.length];
//		int[] entryGroups = new int[entries.length];
//		int[] entryGroupSizes = new int[entries.length];
//		int maxGroupIndex;
//		boolean found = false;
//		
//		for (int i = 0; i < subs.length; i++)
//			entries[i] = subs[i];
//		
//		for (int i = 0; i < pros.length; i++){
//			if (i > 0){
//				for (int j = 0; j < i; j++){
//					if (entries[i] == entries[j]){
//						System.out.println("FOUND");
//						found = true;
//					}
//				}
//			}
//			if (!found)
//				entries[i + subs.length] = pros[i];
//			found = false;
//		}
//		
//		for (int i = 0; i < entries.length; i++){
//			entryGroups[i] = findGroup(entries[i]);
//			if (entryGroups[i] == -1)
//				entryGroupSizes[i] = 0;
//			else
//				entryGroupSizes[i] = relatedGroups.get(entryGroups[i]).size();
//		}
//		
//		maxGroupIndex = 0;
//		for (int i = 1; i < entries.length; i++){
//			if (entryGroupSizes[i] > entryGroupSizes[maxGroupIndex]){
//				maxGroupIndex = i;
//			}
//		}
//		
//		if (entryGroupSizes[maxGroupIndex] == 0){
//			ArrayList<Integer> newGroup = new ArrayList<Integer>();
//			for (int i = 0; i < entries.length; i++)
//				newGroup.add(entries[i]);
//			relatedGroups.add(newGroup);
//		} else {
//			ArrayList<Integer> newGroup = new ArrayList<Integer>(
//					relatedGroups.get(entryGroups[maxGroupIndex]));
//			for (int i = 0; i < entries.length; i++){
//				if (entries[i] != entries[maxGroupIndex]){
//					newGroup.add(entries[i]);
//					if (entryGroups[i] != -1)
//						relatedGroups.get(entryGroups[i]).clear();
//				}
//			}
//			relatedGroups.set(entryGroups[maxGroupIndex], newGroup);
//		}
//	}
//	
//	public static void bothOneReaction(int sub, int pro){
//		
//		boolean found = false;
//		int subGroup = findGroup(sub);
//		int proGroup = findGroup(pro);
//		
//		if (subGroup == -1 && proGroup == -1){
//			relatedGroups.add(new ArrayList<Integer>());
//			System.out.println("NEW GROUP. GROUP: " + relatedGroups.size());
//			relatedGroups.get(relatedGroups.size() - 1).add(sub);
//			relatedGroups.get(relatedGroups.size() - 1).add(pro);
//		} else if (subGroup == -1){
////			System.out.println("SUBGROUP == -1");
//			ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
//			for (int i = 0; i < pG.size(); i++){
//					if (sub == pG.get(i)){
////						System.out.println("FOUND");
//						found = true;
//					}
//				}
//				if (!found)
//					relatedGroups.get(proGroup).add(sub);
//				found = false;
//		} else if (proGroup == -1){
////			System.out.println("PROGROUP == -1");
//			ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
//			for (int i = 0; i < sG.size(); i++){
//					if (pro == sG.get(i)){
////						System.out.println("FOUND");
//						found = true;
//					}
//				}
//				if (!found)
//					relatedGroups.get(subGroup).add(pro);
//				found = false;
//		} else if (subGroup != proGroup){
////			System.out.println("IN HERE");
//			ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
//			ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
//			found = false;
//			if (sG.size() >= pG.size()){
//				for (int i = 0; i < pG.size(); i++){
//					if (i > 0){
//						for (int j = 0; j < i; j++){
//							if (pG.get(i) == sG.get(j)){
//								System.out.println("FOUND1");
//								found = true;
//							}
//						}
//					}
//					if (!found)
//						sG.add(pG.get(i));
//					found = false;
//				}
//				relatedGroups.set(subGroup, sG);
//				relatedGroups.get(proGroup).clear();
////				System.out.println("CLEARING GROUP " + proGroup + ". ADDING TO GROUP " + subGroup);
//			} else {
//				for (int i = 0; i < sG.size(); i++){
//					if (i > 0){
//						for (int j = 0; j < i; j++){
//							if (sG.get(i) == pG.get(j)){
//								System.out.println("FOUND2");
//								found = true;
//							}
//						}
//					}
//					if (!found)
//						pG.add(sG.get(i));
//					found = false;
//				}
//				relatedGroups.set(proGroup, pG);
//				relatedGroups.get(subGroup).clear();
////				System.out.println("CLEARING GROUP " + subGroup + ". ADDING TO GROUP " + proGroup);
//			}
//		}
//	}

	public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException {
 
		if (qName.equalsIgnoreCase("entry")) {
			if (attributes.getValue("type").equalsIgnoreCase("compound")){
				view.RoundButton newButton = new view.RoundButton("");
				newButton.setName(attributes.getValue("id"));
				theView.getPathFrame().initButton(attributes, newButton);
				theView.getPathFrame().getPathButtons().add(newButton);
				theView.getPathFrame().getPathPanel().add(
						theView.getPathFrame().getPathButtons().get(
								theView.getPathFrame().getPathButtons().size() - 1), JLayeredPane.PALETTE_LAYER);
			}
			currentKegg = attributes.getValue("name");
			entry = true;
		}
		
		if (qName.equalsIgnoreCase("graphics")) {
			
			if (attributes.getValue("type").equalsIgnoreCase("circle")){
				JButton theButton = theView.getPathFrame().getPathButtons().get(
						theView.getPathFrame().getPathButtons().size() - 1);
				int keggNo = 0, index;
				double prob;
				if (currentKegg.contains(":"))
		    		keggNo = Integer.parseInt(currentKegg.substring(currentKegg.indexOf(':') + 2));
		    	if ((index = theModel.getIdStore().findIdent(theModel.getIdStore().getIdentifications(), keggNo, 0, theModel.getIdStore().getIdentifications().length - 1)) != -1){
		    		prob = theModel.getIdStore().getIdentifications()[index].getProbabilities()[0];
		    		theView.getPathFrame().buttonGraphics(attributes, theButton, xScaleFactor, yScaleFactor, prob);
		    	}
			graphics = true;
			}
		}
		
//		if (qName.equalsIgnoreCase("relation")) {
//			
//			String entry1 = attributes.getValue("entry1");
//			String entry2 = attributes.getValue("entry2");
//			
//			int e1 = findInButtons(entry1);
//			int e2 = findInButtons(entry2);
//			
//			if (e1 != -1 && e2 != -1){
////				System.out.println("IN HERE");
//				int subGroup = findGroup(e1);
//				int proGroup = findGroup(e2);
//				
//				if (subGroup == -1 && proGroup == -1){
//					relatedGroups.add(new ArrayList<Integer>());
//					relatedGroups.get(relatedGroups.size() - 1).add(e1);
//					relatedGroups.get(relatedGroups.size() - 1).add(e2);
//				} else if (subGroup == -1){
//					relatedGroups.get(proGroup).add(e1);
//				} else if (proGroup == -1){
//					relatedGroups.get(subGroup).add(e2);
//				} else {
//					ArrayList<Integer> sG = new ArrayList<Integer>(relatedGroups.get(subGroup));
//					ArrayList<Integer> pG = new ArrayList<Integer>(relatedGroups.get(proGroup));
//					if (sG.size() >= pG.size()){
//						for (int current : pG){
//							sG.add(current);
//						}
//						relatedGroups.set(subGroup, sG);
//						relatedGroups.get(proGroup).clear();
//					} else {
//						for (int current : sG){
//							pG.add(current);
//						}
//						relatedGroups.set(proGroup, pG);
//						relatedGroups.get(subGroup).clear();
//					}
//				}
//			}
//			
//			relation = true;
//		}
		
//		if (qName.equalsIgnoreCase("reaction")) {
//										
////			System.out.println("REACTION ID: " + attributes.getValue("id"));
//			currentSub = -1;
//			currentPro = -1;
//			subs = null;
//			pros = null;
//			substrate = false;
//			product = false;
//			
//			reaction = true;
//		}
//		
//		if (qName.equalsIgnoreCase("substrate")) {
//			sub = attributes.getValue("name").substring(4);
//			currentSub = findInButtons(sub);
////			System.out.println(currentSub);
//			if (currentSub == -1){
//			} else if (buttonDetails.get(currentSub) != null){
//				if (buttonDetails.get(currentSub).getProbabilities()[0] >= 0.50){
//					if(subs != null){
////						System.out.println("SUBS >= 1");
////						System.out.println(subs.length);
//						int[] temp = new int[subs.length + 1];
//						for (int i = 0; i < subs.length; i++){
//							temp[i] = subs[i];
//						}
//						temp[temp.length - 1] = currentSub;
////						System.out.println(subs.length);
//						subs = temp;
////						subs[subs.length] = currentSub;
//					} else {
////						System.out.print("CREATING SUBS");
//						subs = new int[1];
//						subs[0] = currentSub;
//					}
//				}
//			}
//			substrate = true;
//		}
//		
//		if (qName.equalsIgnoreCase("product")) {
//			
//			if (subs != null){
////				System.out.println("SUB != NULL");
//				if (subs.length >= 1){
//					pro = attributes.getValue("name").substring(4);
//					currentPro = findInButtons(pro);
//					if (currentPro == -1){
//					} else if (buttonDetails.get(currentPro) != null){
//						if (buttonDetails.get(currentPro).getProbabilities()[0] >= 0.50){
//							if (pros != null){
//								int[] temp = new int[pros.length];
//								for (int i = 0; i < pros.length; i++){
//									temp[i] = pros[i];
//								}
//								pros = temp;
//							} else {
//								pros = new int[1];
//								pros[0] = currentPro;
//							}
//						}
//					}
//				}
//			}
//			product = true;
//		}
	}
 
	public void endElement(String uri, String localName,
		String qName) throws SAXException {
		
		
//		if (qName.equalsIgnoreCase("reaction")){
//			if (subs != null && pros != null){
//				if (subs.length > 1 || pros.length > 1){
//					multiReaction(subs, pros);
//				} else if (subs.length == 1 && pros.length == 1){
//					bothOneReaction(subs[0], pros[0]);
//				}
//			}
//		}
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
	}
};
