package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Rule to transform a property name from to another name.
 * E.g 'a = b' will convert any property with the name 'a' to 'b'.
 */
public final class SimpleTransformPropertyNameRule extends Rule{
    private String oldPropertyName;
    private String newPropertyName;

    public SimpleTransformPropertyNameRule(String ruleString, String predicateString) {
        super(ruleString, new RulePredicate(predicateString));
        String[] rulesStringSplit = ruleString.split("=");
        oldPropertyName = rulesStringSplit[0].trim();
        newPropertyName = rulesStringSplit[1].trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // check to ensure the rule can be applied
        if(!super.rulePredicate.canApply(processingStep)){
            return;
        }

        processingStep.includeProperty(oldPropertyName, newPropertyName);
    }
}
