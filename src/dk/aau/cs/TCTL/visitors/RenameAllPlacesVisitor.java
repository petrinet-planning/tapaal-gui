package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.verification.NameMapping;

public class RenameAllPlacesVisitor implements ITCTLVisitor {

	private final NameMapping mapping;

	public RenameAllPlacesVisitor(NameMapping mapping) {
		this.mapping = mapping;
	}

	public void visit(TCTLAFNode afNode, Object context) {
		afNode.getProperty().accept(this, context);
	}

	public void visit(TCTLAGNode agNode, Object context) {
		agNode.getProperty().accept(this, context);
	}

	public void visit(TCTLEFNode efNode, Object context) {
		efNode.getProperty().accept(this, context);
	}

	public void visit(TCTLEGNode egNode, Object context) {
		egNode.getProperty().accept(this, context);
	}

	public void visit(TCTLNotNode notNode, Object context) {
		notNode.getProperty().accept(this, context);
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context) {
	}

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context) {
	}

	public void visit(TCTLAndListNode andListNode, Object context) {
		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	public void visit(TCTLOrListNode orListNode, Object context) {
		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		String mappedName = mapping.map(atomicPropositionNode.getTemplate(),
				atomicPropositionNode.getPlace());
		if (mappedName != null) {
			atomicPropositionNode.setPlace(mappedName);
			atomicPropositionNode.setTemplate("");
		}
	}
}