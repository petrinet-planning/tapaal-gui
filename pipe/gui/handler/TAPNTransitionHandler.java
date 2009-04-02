package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.Transition;
import pipe.gui.action.ShowHideInfoAction;

public class TAPNTransitionHandler extends TransitionHandler {

	public TAPNTransitionHandler(Container contentpane, Transition obj) {
		super(contentpane, obj);
		// TODO Auto-generated constructor stub
	}
	/** 
	    * Creates the popup menu that the user will see when they right click on a 
	    * component 
	    */
	   public JPopupMenu getPopup(MouseEvent e) {
	      int index = 0;
	      JPopupMenu popup = super.getPopup(e);
	      
	      JMenuItem menuItem = new JMenuItem("Edit Transition");      
	      menuItem.addActionListener(new ActionListener(){
	         public void actionPerformed(ActionEvent e) {
	            ((Transition)myObject).showEditor();
	         }
	      });       
	      popup.insert(menuItem, index++);             
	      
	      menuItem = new JMenuItem(new ShowHideInfoAction((Transition)myObject));
	      if (((Transition)myObject).getAttributesVisible() == true){
	         menuItem.setText("Hide Attributes");
	      } else {
	         menuItem.setText("Show Attributes");
	      }
	      popup.insert(menuItem, index++);      
	      popup.insert(new JPopupMenu.Separator(), index);

	      return popup;
	   }

}
