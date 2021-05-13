package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.verification.NameMapping;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.Zoomer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

public class VerifyTACPNExporter extends  VerifyTAPNExporter {
    protected void outputModel(TimedArcPetriNet model, File modelFile, NameMapping mapping, DataLayer guiModel) throws FileNotFoundException {
        ArrayList<Template> templates = new ArrayList<Template>(1);
        ArrayList<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>(1);
        templates.add(new Template(model, guiModel, new Zoomer()));

        TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(model.parentNetwork(), templates, queries, model.parentNetwork().constants());
        try {
            writerTACPN.savePNML(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
