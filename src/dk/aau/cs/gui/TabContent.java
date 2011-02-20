package dk.aau.cs.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.AnimationController;
import pipe.gui.AnimationHistoryComponent;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftConstantsPane;
import pipe.gui.widgets.LeftQueryPane;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;
	private static final double RATIO = 0.2;

	private TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	private HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private JScrollPane drawingSurfaceScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;

	// Normal mode
	private JPanel editorLeftPane;
	private LeftQueryPane queries;
	private LeftConstantsPane constantsPanel;
	private TemplateExplorer templateExplorer;

	// / Animation
	private AnimationHistoryComponent animBox;
	private AnimationController animControlerBox;
	private JScrollPane animationHistoryScrollPane;
	private JScrollPane animationControllerScrollPane;
	private AnimationHistoryComponent abstractAnimationPane = null;

	private JPanel animatorLeftPane;
	private JSplitPane animationHistorySplitter;


	public TabContent() {

		for (TimedArcPetriNet net : tapnNetwork.templates()) {
			guiModels.put(net, new DataLayer());
		}

		drawingSurface = new DrawingSurfaceImpl(new DataLayer(), this);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		drawingSurfaceScroller.setWheelScrollingEnabled(true);

		createEditorLeftPane();

		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(editorLeftPane);
		this.setRightComponent(drawingSurfaceScroller);

		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);
	}

	public void createEditorLeftPane() {
		editorLeftPane = new JPanel(new GridBagLayout());
		editorLeftPane.setPreferredSize(new Dimension(230, 100)); // height is ignored because the component is stretched
		editorLeftPane.setMinimumSize(new Dimension(210, 100));
		boolean enableAddButton = getModel() == null ? true : !getModel().netType().equals(NetType.UNTIMED);
		
		constantsPanel = new LeftConstantsPane(enableAddButton, this);
		queries = new LeftQueryPane(new ArrayList<TAPNQuery>(), this);
		templateExplorer = new TemplateExplorer(this);
		SharedPlacesAndTransitionsPanel sharedPTPanel = new SharedPlacesAndTransitionsPanel(tapnNetwork, drawingSurface.getUndoManager());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.25;
		editorLeftPane.add(templateExplorer, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.25;
		editorLeftPane.add(queries, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.25;
		editorLeftPane.add(constantsPanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.25;
		editorLeftPane.add(sharedPTPanel, gbc);
	}

	public void updateConstantsList() {
		constantsPanel.showConstants();
	}

	public DataLayer getModel() {
		return drawingSurface.getGuiModel();
	}

	public void setDrawingSurface(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}

	public File getFile() {
		return appFile;
	}

	public void setFile(File file) {
		appFile = file;
	}

	/** Creates a new animationHistory text area, and returns a reference to it */
	private void createAnimationHistory() {
		animBox = new AnimationHistoryComponent();

		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory
						.createTitledBorder("Simulation History"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}

	public void switchToAnimationComponents() {
		if(animBox == null) createAnimationHistory();
		if(animControlerBox == null) createAnimationController();
		
		animatorLeftPane = new JPanel(new GridBagLayout());
		animatorLeftPane.setPreferredSize(animControlerBox.getPreferredSize()); // height is ignored because the component is stretched
		animatorLeftPane.setMinimumSize(animControlerBox.getMinimumSize());
		templateExplorer.hideButtons();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = RATIO;
		animatorLeftPane.add(templateExplorer, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		animatorLeftPane.add(animControlerBox, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1 - RATIO;
		animatorLeftPane.add(animationHistoryScrollPane, gbc);
		this.setLeftComponent(animatorLeftPane);

		drawingSurface.repaintAll();
	}

	public void switchToEditorComponents() {
		templateExplorer.showButtons();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.34;
		editorLeftPane.add(templateExplorer, gbc);
		this.setLeftComponent(editorLeftPane);

		drawingSurface.repaintAll();
	}

	public AnimationHistoryComponent getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationController getAnimationController() {
		return animControlerBox;
	}

	public void addAbstractAnimationPane() {
		animatorLeftPane.remove(animationHistoryScrollPane);
		abstractAnimationPane = new AnimationHistoryComponent();

		JScrollPane untimedAnimationHistoryScrollPane = new JScrollPane(abstractAnimationPane);
		untimedAnimationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createTitledBorder("Untimed Trace"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		animationHistorySplitter = new JSplitPaneFix(JSplitPane.HORIZONTAL_SPLIT, animationHistoryScrollPane, untimedAnimationHistoryScrollPane);

		animationHistorySplitter.setContinuousLayout(true);
		animationHistorySplitter.setOneTouchExpandable(true);
		animationHistorySplitter.setBorder(null); // avoid multiple borders
		animationHistorySplitter.setDividerSize(8);
		animationHistorySplitter.setDividerLocation(0.5);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1 - RATIO;
		animatorLeftPane.add(animationHistorySplitter, gbc);
	}

	public void removeAbstractAnimationPane() {
		animatorLeftPane.remove(animationHistorySplitter);
		abstractAnimationPane = null;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1 - RATIO;
		animatorLeftPane.add(animationHistoryScrollPane);
	}

	private void createAnimationController() {
		animControlerBox = new AnimationController();

		animationControllerScrollPane = new JScrollPane(animControlerBox);
		animationControllerScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		animControlerBox.requestFocus(true);
	}

	public AnimationHistoryComponent getAnimationHistory() {
		return animBox;
	}

	public JScrollPane drawingSurfaceScrollPane() {
		return drawingSurfaceScroller;
	}

	public TimedArcPetriNetNetwork network() {
		return tapnNetwork;
	}

	public DrawingSurfaceImpl drawingSurface() {
		return drawingSurface;
	}

	public Iterable<Template> templates() {
		ArrayList<Template> list = new ArrayList<Template>();
		for (TimedArcPetriNet net : tapnNetwork.templates()) {
			list.add(new Template(net, guiModels.get(net)));
		}
		return list;
	}

	public void addTemplate(Template template) {
		tapnNetwork.add(template.model());
		guiModels.put(template.model(), template.guiModel());
		templateExplorer.updateTemplateList();
	}
	
	public void removeTemplate(Template template) {
		tapnNetwork.remove(template.model());
		guiModels.remove(template.model());
		templateExplorer.updateTemplateList();
	}

	public Template activeTemplate() {
		return templateExplorer.selectedModel();
	}

	public void setActiveTemplate(Template template) {
		drawingSurface.setModel(template.guiModel(), template.model());
	}

	public Iterable<TAPNQuery> queries() {
		return queries.getQueries();
	}

	public void setQueries(Iterable<TAPNQuery> queries) {
		this.queries.setQueries(queries);

	}

	public void removeQuery(TAPNQuery queryToRemove) {
		queries.removeQuery(queryToRemove);

	}

	public void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
		constantsPanel.showConstants();
	}

	public void setupNameGeneratorsFromTemplates(Iterable<Template> templates) {
		drawingSurface.setupNameGeneratorsFromTemplates(templates);

	}

	
}
