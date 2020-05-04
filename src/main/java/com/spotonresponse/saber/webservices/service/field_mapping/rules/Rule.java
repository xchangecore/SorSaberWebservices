package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Class to encapsulate a mapping rule.
 */
public abstract class Rule {
    private final String ruleString;
    protected final RulePredicate rulePredicate;

    /**
     * @param ruleString The rules string for the rule e.g a=b.
     * @param predicate The predicate that determines whether a rule can be executed or not.
     */
    public Rule(String ruleString, RulePredicate predicate){
        this.ruleString = ruleString;
        rulePredicate = predicate;
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
