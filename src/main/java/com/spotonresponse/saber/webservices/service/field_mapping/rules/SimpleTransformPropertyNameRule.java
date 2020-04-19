package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Rule to transform a property name from to another name.
 * E.g 'a = b' will convert any property with the name 'a' to 'b'.
 */
public final class SimpleTransformPropertyNameRule extends Rule{
    private String oldPropertyName;
    private String newPropertyName;

    public SimpleTransformPropertyNameRule(String ruleString) {
        super(ruleString);
        String[] rulesStringSplit = ruleString.split("=");
        oldPropertyName = rulesStringSplit[0].trim();
        newPropertyName = rulesStringSplit[1].trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        processingStep.includeProperty(oldPropertyName, newPropertyName);
    }
}
