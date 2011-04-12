package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;


public class RenameSharedPlaceVisitor extends VisitorBase  {

	private final String oldName;
	private final String newName;

	public RenameSharedPlaceVisitor(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	
	@Override
	public void visit(TCTLAtomicPropositionNode node, Object context) {
		if(node.getTemplate().equals("") && node.getPlace().equals(oldName)){
			node.setPlace(newName);
		}
	}
}
