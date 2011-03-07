package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TimedOutputArc extends TAPNElement {
	private TimedTransition source;
	private TimedPlaceInterface destination;

	public TimedOutputArc(TimedTransition source, TimedPlaceInterface destination) {
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");

		this.source = source;
		this.destination = destination;
	}

	public TimedTransition source() {
		return source;
	}

	public TimedPlaceInterface destination() {
		return destination;
	}

	@Override
	public void delete() {
		model().remove(this);
	}

	public TimedOutputArc copy(TimedArcPetriNet tapn) {
		return new TimedOutputArc(tapn.getTransitionByName(source.name()), tapn.getPlaceByName(destination.name()));
	}

	public void setDestination(TimedPlaceInterface place) {
		Require.that(place != null, "place cannot be null");
		this.destination = place;		
	}
}
