package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule to transform a set of property names to one property name.
 * E.g 'a ~ b ~ c = d' will convert any property with the names 'a' or 'b' or 'c' to 'd'.
 */
public final class CompoundTransformPropertyNameRule extends Rule{
    private Set<String> oldPropertyNamesList;
    private String newPropertyName;

    public CompoundTransformPropertyNameRule(String ruleString, String predicateString) {
        super(ruleString, new RulePredicate(predicateString));

        String[] rulesStringSplit = ruleString.split("=");
        String[] oldPropertyNamesSplit = rulesStringSplit[0].split("~");
        oldPropertyNamesList =
                Arrays.stream(oldPropertyNamesSplit)
                .map(String::trim)
                .collect(Collectors.toSet());
        newPropertyName = rulesStringSplit[1];
    }

    @Override
    public void apply(ProcessingStep processingStep) {
        // check to ensure the rule can be applied
        if(!super.rulePredicate.canApply(processingStep)){
            return;
        }

        String firstMatchingOldKey = null;
        for (Iterator<String> it = processingStep.getInputObjectKeys(); it.hasNext(); ) {
            String key = it.next();
            if(oldPropertyNamesList.contains(key)){
                firstMatchingOldKey = key;
                break;
            }
        }

        if(firstMatchingOldKey == null){
            return;
        }

        processingStep.includeProperty(firstMatchingOldKey, newPropertyName);
    }
}
