package com.spotonresponse.saber.webservices.service.field_mapping;

import com.spotonresponse.saber.webservices.service.field_mapping.rules.Rule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

public class RuleChain {
    private static final Logger logger = Logger.getLogger(RuleChain.class.getName());
    private List<Rule> rules = new ArrayList<>();

    public RuleChain(String ruleChainInput){
        ruleChainInput = ruleChainInput
                .replace("(", "")
                .replace(")", "");

        List<String> ruleStrings = parseRules(ruleChainInput);
        for(String ruleString : ruleStrings){
            try {
                Rule rule = RuleFactory.createRule(ruleString);
                rules.add(rule);
            } catch (Exception e){
                logger.severe(e.getMessage());
            }
        }
    }

    private List<String> parseRules(String rulesString){
        List<String> rules = new ArrayList<>();

        Function<StringBuilder, String> builderToString = stringBuilder -> {
            String stringRpr = stringBuilder.toString();
            return stringRpr.replace("\"", "").trim();
        };

        StringBuilder ruleBuilder = new StringBuilder();
        for (char c : rulesString.toCharArray()) {
            if(c == ','){
                long quotesNum = ruleBuilder.toString()
                        .chars()
                        .filter(it -> it == '\"')
                        .count();

                if(quotesNum % 2 == 0){
                    rules.add(builderToString.apply(ruleBuilder));
                    ruleBuilder = new StringBuilder();
                    continue;
                }
            }

            ruleBuilder.append(c);
        }

        if(!ruleBuilder.toString().isEmpty()){
            rules.add(builderToString.apply(ruleBuilder));
        }

        return rules;
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
