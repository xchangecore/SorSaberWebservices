package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Class to encapsulate a mapping rule.
 */
public abstract class Rule {
    private String ruleString;

    /**
     * @param ruleString The rules string for the rule e.g a=b.
     */
    public Rule(String ruleString){
        this.ruleString = ruleString;
    }

    /**
     * @param processingStep A processing step on which further processing needs to be done by the current rule.
     */
    public abstract void apply(ProcessingStep processingStep);

    @Override
    public String toString() {
        return "Rule{" +
                "ruleString='" + ruleString + '\'' +
                '}';
    }
}
