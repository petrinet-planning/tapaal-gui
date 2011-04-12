package dk.aau.cs.verification;

import java.util.ArrayList;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.UntimedTAPNNetworkTrace;
import dk.aau.cs.util.Tuple;

public class TAPNTraceDecomposer {
	private final TimedArcPetriNetTrace trace;
	private final NameMapping mapping;
	private final TimedArcPetriNetNetwork tapnNetwork;

	public TAPNTraceDecomposer(TimedArcPetriNetTrace trace, TimedArcPetriNetNetwork tapnNetwork, NameMapping mapping) {
		this.trace = trace;
		this.tapnNetwork = tapnNetwork;
		this.mapping = mapping;
	}

	public TAPNNetworkTrace decompose() {
		if (!trace.isTimedTrace()){
			return decomposeUntimedTrace();
		} else {
			return decomposeTimedTrace();
		}
	}

	private TAPNNetworkTrace decomposeUntimedTrace() {
		UntimedTAPNNetworkTrace decomposedTrace = new UntimedTAPNNetworkTrace();

		for (TimedArcPetriNetStep action : trace) {
			decomposedTrace.add((TAPNNetworkTimedTransitionStep)decomposeAction(action));
		}

		return decomposedTrace;
	}

	private TAPNNetworkTrace decomposeTimedTrace() {
		TimedTAPNNetworkTrace decomposedTrace = new TimedTAPNNetworkTrace();

		for (TimedArcPetriNetStep action : trace) {
			decomposedTrace.add(decomposeAction(action));
		}

		return decomposedTrace;
	}

	private TAPNNetworkTraceStep decomposeAction(TimedArcPetriNetStep action) {
		TAPNNetworkTraceStep decomposedAction = null;
		if (action instanceof TimedTransitionStep) {
			TimedTransitionStep transitionFiring = (TimedTransitionStep) action;
			decomposedAction = decomposeTransitionFiring(transitionFiring);
		} else if (action instanceof TimeDelayStep) {
			decomposedAction = new TAPNNetworkTimeDelayStep(((TimeDelayStep) action).delay());
		}
		return decomposedAction;
	}

	private TAPNNetworkTraceStep decomposeTransitionFiring(TimedTransitionStep transitionFiring) {
		Tuple<String, String> originalName = mapping.map(transitionFiring.transition().name());
		TimedTransition transition = tapnNetwork.getTAPNByName(originalName.value1()).getTransitionByName(originalName.value2());

		ArrayList<TimedToken> convertedTokens = null;
		if(trace.isTimedTrace()){
			convertedTokens = new ArrayList<TimedToken>(transitionFiring.consumedTokens().size());
			for (TimedToken token : transitionFiring.consumedTokens()) {
				Tuple<String, String> remappedName = mapping.map(token.place().name());
				TimedPlace place = tapnNetwork.getTAPNByName(remappedName.value1()).getPlaceByName(remappedName.value2());
				convertedTokens.add(new TimedToken(place, token.age()));
			}
		}

		return new TAPNNetworkTimedTransitionStep(transition, convertedTokens);
	}
}
