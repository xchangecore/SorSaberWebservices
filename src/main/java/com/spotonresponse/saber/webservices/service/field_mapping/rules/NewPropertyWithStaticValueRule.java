package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

/**
 * Rule to create a new property name with a static value.
 * E.g c ~= 12 will create a new property with name 'c' and value '12'.
 */
public final class NewPropertyWithStaticValueRule extends Rule{
    private final String newPropertyName;
    private final String staticValue;

    public NewPropertyWithStaticValueRule(String ruleString) {
        super(ruleString);
        String[] ruleStringParts = ruleString.split("~=");
        newPropertyName = ruleStringParts[0].trim();
        staticValue = ruleStringParts[1].trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        processingStep.createStaticProperty(newPropertyName, staticValue);
    }
}
