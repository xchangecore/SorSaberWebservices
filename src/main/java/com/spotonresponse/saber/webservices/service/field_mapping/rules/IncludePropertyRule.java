package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

public final class IncludePropertyRule extends Rule{
    private String targetProperty;

    public IncludePropertyRule(String ruleString) {
        super(ruleString);
        targetProperty = ruleString.trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // The user hath explicitly specified that a property should be included.
        processingStep.setExplicitInclusionSpecified(true);

        // specify the property to be included.
        processingStep.includeProperty(targetProperty);
    }
}
