package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

public final class ExcludePropertyRule extends Rule{
    private String targetProperty;

    public ExcludePropertyRule(String ruleString, String predicateString) throws RuleException {
        super(ruleString, new RulePredicate(predicateString));

        if(ruleString.contains("where")){
            throw new RuleException("The where property cannot be excluded.");
        }

        targetProperty = ruleString.trim().replace("!", "");
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // check to ensure the rule can be applied
        if(!super.rulePredicate.canApply(processingStep)){
            return;
        }


        if(!processingStep.isExplicitInclusionSpecified()){
            processingStep.excludeProperty(targetProperty);
        }
    }

}
