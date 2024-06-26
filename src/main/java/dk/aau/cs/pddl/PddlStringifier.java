package dk.aau.cs.pddl;

import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.pddl.expression.*;

import java.util.ArrayList;
import java.util.HashMap;

public class PddlStringifier {
    private Model model;

    public PddlStringifier(Model model) {
        this.model = model;
    }

    public String getString() {
        return buildDomain().toString();
    }


    public StringBuilder buildDomain() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("(define (domain %s)\n", model.getName()));
        sb.append(this.buildExtensions());
        sb.append(this.buildTypes());
        sb.append(this.buildConstants());
        sb.append(this.buildPredicateDefinitions());
        sb.append(this.buildFunctions());
        sb.append(this.buildActions());
        sb.append(")");
        sb.append("\n");
        return sb;
    }

    public HashMap<String, StringBuilder> buildTasks() {

        StringBuilder commonStartSb = new StringBuilder();
        commonStartSb.append(String.format("(define (problem %s_problem)\n", model.getName()));
        commonStartSb.append(String.format("\t(:domain %s)\n", model.getName()));
        commonStartSb.append(this.buildObjects());
        commonStartSb.append(this.buildInitialState());
        commonStartSb.append("\t(:goal \n");

        String commonStart = commonStartSb.toString();

        HashMap<String, StringBuilder> taskStrings = new HashMap<>();
        for(var entry: this.model.getQueries().entrySet()) {
            String taskName = entry.getKey();
            QueryParser parser = entry.getValue();

            var sb = new StringBuilder(commonStart);
            sb.append("\t\t").append(parser.expression.toString());
            sb.append("\n\t)\n");
            sb.append(")");

            taskStrings.put(taskName, sb);
        }

        return taskStrings;
    }

    public StringBuilder buildExtensions() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:requirements");
        for(Extension extension : model.getExtensions()) {
            sb.append(String.format(" :%s", extension.getName()));
        }
        sb.append(")\n");

        return sb;
    }

    public StringBuilder buildTypes() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:types\n");
        for(UserType type : model.getTypes().values()) {
            sb.append(String.format("\t\t%s\n", type.getName()));
        }
        sb.append("\t)\n");

        return sb;
    }

    public StringBuilder buildPredicateDefinitions() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:predicates\n");
        sb.append("\t\t(isPredecessor ?x ?xPred)\n");
        sb.append("\t\t(isSuccessor ?x ?xSucc)\n");
        sb.append("\t)\n");

        return sb;
    }

    public StringBuilder buildInitialPredicates() {
        var sb = new StringBuilder();
        for(var pred: this.model.getPredicates()) {
            sb.append("\n\t\t").append(pred);
        }
        return sb;
    }
    public StringBuilder buildFunctions() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:functions\n");
        for(FunctionSignature function : model.getFunctions().values()) {
            sb.append(String.format("\t\t(%s%s)\n",
                function.getName(),
                this.buildParameters(function.getParameters()).toString()
            ));
        }
        sb.append("\t)\n");

        return sb;
    }

    public StringBuilder buildParameters(Iterable<Parameter> parameters) {
        StringBuilder sb = new StringBuilder();

        for(Parameter param : parameters) {
            sb.append(String.format(" ?%s - %s",
                param.getName(),
                param.getType().getName()
            ));
        }

        return sb;
    }

    public StringBuilder buildActions() {
        StringBuilder sb = new StringBuilder();

        var parsedActionSchema = model.getActionSchemas();

        for (ActionSchema action : parsedActionSchema.values()) {
            sb.append(buildAction(action));
        }

        return sb;
    }

    public StringBuilder buildAction(ActionSchema actionSchema) {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:action ").append(actionSchema.getName()).append("\n");

        sb.append("\t\t:parameters (").append(buildParameters(actionSchema.getParameters().values())).append(")\n");

        if(actionSchema.hasPrecondition())
            sb.append("\t\t:precondition ").append(actionSchema.getPrecondition().toString()).append("\n");

        sb.append("\t\t:effect ").append(actionSchema.getEffects().toString()).append("\n");

        sb.append("\t)\n");

        return sb;
    }


    public StringBuilder buildInitialState() {
        return buildInitialState(this.model.getState());
    }
    public StringBuilder buildInitialState(PlaceWeights placeWeights) {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:init ");

        sb.append(this.buildInitialPredicates());


        for(var placeWeightEntry : placeWeights.entrySet()) {
            TimedPlace place = placeWeightEntry.getKey();
            for(var weightEntry : placeWeightEntry.getValue().entrySet()) {
                ArrayList<IExpression_Value> colors = weightEntry.getKey();
                int weight = weightEntry.getValue();

                var funcExp = new Expression_FunctionValue(place.name(), colors);
                var assignment = new Expression_FunctionAssign(funcExp, new Expression_IntegerLiteral(weight));

                sb.append("\n\t\t").append(assignment);
            }
        }

        sb.append("\n\t)\n");

        return sb;
    }

    public StringBuilder buildConstants() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t(:constants");
        for(var e: this.model.getTypes().entrySet()) {
            UserType type = e.getValue();
            sb.append("\n\t\t");
            type.getObjectNames().forEach(o -> {

                String objName = o;
                if(objName == "dot")
                    objName = "dot_obj";

                sb.append(objName).append(" ");
            });
            sb.append("- ").append(type.getName());
        }
        sb.append("\n\t)\n");

        return sb;
    }

    public StringBuilder buildObjects() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t(:objects");
        sb.append("\n\t)\n");

        return sb;
    }

}
