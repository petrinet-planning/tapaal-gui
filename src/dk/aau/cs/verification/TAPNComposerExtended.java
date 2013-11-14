package dk.aau.cs.verification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PNMLWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import dk.aau.cs.Messenger;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

public class TAPNComposerExtended implements ITAPNComposer {
	private static final String PLACE_FORMAT = "P%1$d";
	private static final String TRANSITION_FORMAT = "T%1$d";

	private Messenger messenger;
	private boolean hasShownMessage = false;
	
	private int nextPlaceIndex;
	private int nextTransitionIndex;

	private HashSet<String> processedSharedObjects;
	private HashMap<TimedArcPetriNet, DataLayer> guiModels;

	public TAPNComposerExtended(Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels){
		this.messenger = messenger;
		
		HashMap<TimedArcPetriNet, DataLayer> newGuiModels = new HashMap<TimedArcPetriNet, DataLayer>();
		for(Entry<TimedArcPetriNet, DataLayer> entry : guiModels.entrySet()) {
			newGuiModels.put(entry.getKey(), entry.getValue().copy(entry.getKey()));
		}
		
		this.guiModels = newGuiModels;
	}
	
	public Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model) {
		nextPlaceIndex = -1;
		nextTransitionIndex = -1;
		processedSharedObjects = new HashSet<String>();
		TimedArcPetriNet tapn = new TimedArcPetriNet("ComposedModel");
		DataLayer guiModel = new DataLayer();
		NameMapping mapping = new NameMapping();
		hasShownMessage = false;

		createSharedPlaces(model, tapn, mapping);
		createPlaces(model, tapn, mapping);
		createTransitions(model, tapn, mapping);
		createInputArcs(model, tapn, mapping);
		createOutputArcs(model, tapn, mapping);
		createTransportArcs(model, tapn, mapping);
		createInhibitorArcs(model, tapn, mapping);
		
		// Combine DataLayers to be used in XMLWriter
		int i = 0;
		for(Entry<TimedArcPetriNet, DataLayer> entry : this.guiModels.entrySet()) {
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for(PetriNetObject netObject : entry.getValue().getPetriNetObjects()) {
				if (netObject instanceof PlaceTransitionObject) {
					PlaceTransitionObject netObjectPlace = (PlaceTransitionObject) netObject;
					// Modify netObjectPlace to arrange components
					netObjectPlace.setPositionX(netObjectPlace.getPositionX() + (offset.value1() * 200));
					netObjectPlace.setPositionY(netObjectPlace.getPositionY() + (offset.value2() * 200));
					
					guiModel.addPetriNetObject(netObjectPlace);
				} else if (netObject instanceof Arc) {
					Arc netObjectArc = (Arc) netObject;
					// Modify netObjectArc to arrange components
					netObjectArc.updateArcPosition();
					
					guiModel.addPetriNetObject(netObjectArc);
				}
				
			}
			i++;
		}
		
		ArrayList<Template> templates = new ArrayList<Template>(1);
		templates.add(new Template(tapn, guiModel, new Zoomer()));
		
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
		network.add(tapn);
		
		PNMLWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<TAPNQuery>(0), new ArrayList<Constant>(0));

		try {
			String outputPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "/output_2.xml";
			tapnWriter.savePNML(new File(outputPath));
		} catch (NullPointerException | DOMException
				| IOException
				| ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}

		dumpToConsole(tapn, mapping);

		return new Tuple<TimedArcPetriNet, NameMapping>(tapn, mapping);
	}
	
	private Tuple<Integer, Integer> calculateComponentPosition(int netNumber) {
		Integer x = netNumber % 2;
		Integer y = (int) Math.floor(netNumber / 2d);
		
		return new Tuple<Integer, Integer>(x, y);		
	}
	
	private PlaceTransitionObject getRightmostObject(DataLayer guiModel) {
		PlaceTransitionObject returnObject = null;
		
		for (PlaceTransitionObject currentObject : guiModel.getPlaces()) {
			if (returnObject == null || returnObject.getPositionX() < currentObject.getPositionX()) {
				returnObject = currentObject;
			}
		}
		
		for (PlaceTransitionObject currentObject : guiModel.getTransitions()) {
			if (returnObject == null || returnObject.getPositionX() < currentObject.getPositionX()) {
				returnObject = currentObject;
			}
		}
		
		return returnObject;
	}

	@SuppressWarnings("unused")
	private void dumpToConsole(TimedArcPetriNet tapn, NameMapping mapping) {
		System.out.println("Composed Model:");
		System.out.println("PLACES:");
		for(TimedPlace place : tapn.places()){
			System.out.print('\t');
			System.out.print(place.name());
			System.out.print(", invariant: ");
			System.out.print(place.invariant().toString());
			System.out.print(" (Original: ");
			System.out.print(mapping.map(place.name()));
			System.out.println(')');
		}

		System.out.println();
		System.out.println("TRANSITIONS:");
		for(TimedTransition transition : tapn.transitions()){
			System.out.print('\t');
			System.out.print(transition.name());
			System.out.print(" (Original: ");
			System.out.print(mapping.map(transition.name()).toString());
			System.out.println(')');
		}

		System.out.println();
		System.out.println("INPUT ARCS:");
		for(TimedInputArc arc : tapn.inputArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("OUTPUT ARCS:");
		for(TimedOutputArc arc : tapn.outputArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.println(arc.destination().name());
		}

		System.out.println();
		System.out.println("TRANSPORT ARCS:");
		for(TransportArc arc : tapn.transportArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Transition: ");
			System.out.print(arc.transition().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("INHIBITOR ARCS:");
		for(TimedInhibitorArc arc : tapn.inhibitorArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("MARKING:");
		for(TimedPlace place : tapn.places()){
			for(TimedToken token : place.tokens()){
				System.out.print(token.toString());
				System.out.print(", ");
			}
		}
		System.out.println();
		System.out.println();
	}
	
	private void createSharedPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for(SharedPlace place : model.sharedPlaces()){
			String uniquePlaceName = getUniquePlaceName();
			
			LocalTimedPlace constructedPlace = new LocalTimedPlace(uniquePlaceName, place.invariant());
			constructedModel.add(constructedPlace);
			mapping.addMappingForShared(place.name(), uniquePlaceName);

			if(model.isSharedPlaceUsedInTemplates(place)){
				for (TimedToken token : place.tokens()) {
					constructedPlace.addToken(new TimedToken(constructedPlace, token.age()));
				}
			}
		}
	}

	private void createPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			
			for (TimedPlace timedPlace : tapn.places()) {			
				if(!timedPlace.isShared()){
					String uniquePlaceName = getUniquePlaceName();
					
					Place oldPlace = currentGuiModel.getPlaceByName(timedPlace.name());
					oldPlace.setName(uniquePlaceName);

					LocalTimedPlace place = new LocalTimedPlace(uniquePlaceName, timedPlace.invariant());
					constructedModel.add(place);
					mapping.addMapping(tapn.name(), timedPlace.name(), uniquePlaceName);

					for (TimedToken token : timedPlace.tokens()) {
						place.addToken(new TimedToken(place, token.age()));
					}
				}
			}
		}
	}

	private String getUniquePlaceName() {
		nextPlaceIndex++;
		return String.format(PLACE_FORMAT, nextPlaceIndex);
	}

	private void createTransitions(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			
			for (TimedTransition timedTransition : tapn.transitions()) {
				if(!processedSharedObjects.contains(timedTransition.name())){
					
					// CAUTION: This if statement removes orphan transitions.
					//   This changes answers for e.g. DEADLOCK queries if
					//   support for such queries are added later.
					// ONLY THE IF SENTENCE SHOULD BE REMOVED. REST OF CODE SHOULD BE LEFT INTACT
					if(!timedTransition.isOrphan()){
						String uniqueTransitionName = getUniqueTransitionName();
						
						Transition oldTransition = currentGuiModel.getTransitionByName(timedTransition.name());
						oldTransition.setName(uniqueTransitionName);											
	
						constructedModel.add(new TimedTransition(uniqueTransitionName, timedTransition.isUrgent()));
						if(timedTransition.isShared()){
							String name = timedTransition.sharedTransition().name();
							processedSharedObjects.add(name);
							mapping.addMappingForShared(name, uniqueTransitionName);
						}else{
							mapping.addMapping(tapn.name(), timedTransition.name(), uniqueTransitionName);
						}
					}else{
						if(!hasShownMessage){
							messenger.displayInfoMessage("There are orphan transitions (no incoming and no outgoing arcs) in the model."
									+ System.getProperty("line.separator") + "They will be removed before the verification.");
							hasShownMessage = true;
						}
					}
				}
			}
		}
	}

	private String getUniqueTransitionName() {
		nextTransitionIndex++;
		return String.format(TRANSITION_FORMAT, nextTransitionIndex);
	}

	private void createInputArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			
			for (TimedInputArc arc : tapn.inputArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));
				
				template = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedInputArc(source, target, arc.interval(), arc.getWeight()));
			}
		}
	}

	private void createOutputArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedOutputArc arc : tapn.outputArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedTransition source = constructedModel.getTransitionByName(mapping.map(template, arc.source().name()));

				template = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedOutputArc(source, target, arc.getWeight()));
			}
		}
	}

	private void createTransportArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TransportArc arc : tapn.transportArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));
				
				template = arc.transition().isShared() ? "" : tapn.name();
				TimedTransition transition = constructedModel.getTransitionByName(mapping.map(template, arc.transition().name()));
				
				template = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TransportArc(source, transition,target, arc.interval(), arc.getWeight()));
			}
		}
	}

	private void createInhibitorArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedInhibitorArc arc : tapn.inhibitorArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));

				template = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedInhibitorArc(source, target, arc.interval(), arc.getWeight()));
			}
		}
	}
}
