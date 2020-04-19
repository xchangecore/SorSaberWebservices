package com.spotonresponse.saber.webservices.service.field_mapping;

import com.spotonresponse.saber.webservices.service.field_mapping.rules.Rule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RuleChain {
    private static final Logger logger = Logger.getLogger(RuleChain.class.getName());
    private List<Rule> rules = new ArrayList<>();

    public RuleChain(String ruleChainInput){
        ruleChainInput = ruleChainInput
                .replace("(", "")
                .replace(")", "");

        String[] ruleStrings = ruleChainInput.split(",");
        for(String ruleString : ruleStrings){
            try {
                Rule rule = RuleFactory.createRule(ruleString);
                rules.add(rule);
            } catch (Exception e){
                logger.severe(e.getMessage());
            }
        }
    }

    public JSONObject applyRules(JSONObject inputObject){
        // create a processing step
        ProcessingStep processingStep = new ProcessingStep(inputObject);

        // apply the rules, with each rule taking in the processing step the previous one worked on.
        rules.forEach(rule -> rule.apply(processingStep));

        // get the processed object once done
        return processingStep.getProcessedObject();
    }
}
