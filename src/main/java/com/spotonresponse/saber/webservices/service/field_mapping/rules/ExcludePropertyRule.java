package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

public final class ExcludePropertyRule extends Rule{
    private String targetProperty;

    public ExcludePropertyRule(String ruleString) throws RuleException {
        super(ruleString);

        if(ruleString.contains("where")){
            throw new RuleException("The where property cannot be excluded.");
        }

        targetProperty = ruleString.trim().replace("!", "");
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        if(!processingStep.isExplicitInclusionSpecified()){
            processingStep.excludeProperty(targetProperty);
        }
    }

}
