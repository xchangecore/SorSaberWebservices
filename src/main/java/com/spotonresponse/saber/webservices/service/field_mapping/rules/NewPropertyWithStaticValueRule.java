package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Rule to create a new property name with a static value.
 * E.g c ~= 12 will create a new property with name 'c' and value '12'.
 */
public final class NewPropertyWithStaticValueRule extends Rule{
    private final String newPropertyName;
    private final String staticValue;

    public NewPropertyWithStaticValueRule(String ruleString, String predicateString) {
        super(ruleString, new RulePredicate(predicateString));
        String[] ruleStringParts = ruleString.split("~=");
        newPropertyName = ruleStringParts[0].trim();
        staticValue = ruleStringParts[1].trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // check to ensure the rule can be applied
        if(!super.rulePredicate.canApply(processingStep)){
            return;
        }

        processingStep.createStaticProperty(newPropertyName, staticValue);
        processingStep.includeProperty(newPropertyName);
    }
}
