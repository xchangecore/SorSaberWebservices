package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

public final class IncludePropertyRule extends Rule{
    private String targetProperty;

    public IncludePropertyRule(String ruleString, String predicateString) {
        super(ruleString, new RulePredicate(predicateString));
        targetProperty = ruleString.trim();
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // check to ensure the rule can be applied
        if(!super.rulePredicate.canApply(processingStep)){
            return;
        }

        // The user hath explicitly specified that a property should be included.
        processingStep.setExplicitInclusionSpecified(true);

        // specify the property to be included.
        processingStep.includeProperty(targetProperty);
    }
}
