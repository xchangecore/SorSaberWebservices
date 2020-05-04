package com.spotonresponse.saber.webservices.service.field_mapping;

import com.spotonresponse.saber.webservices.service.field_mapping.rules.*;

public class RuleFactory {
    public static Rule createRule(String ruleString) throws RuleException{
        String predicateString = "";
        String coreRuleString = ruleString;

        if(ruleString.contains("where")){
            // extract the core rule string
            coreRuleString = ruleString.substring(0, ruleString.indexOf("where"));

            // extract the predicate clause
            predicateString = ruleString.substring(ruleString.indexOf("where"));
        }

        if(coreRuleString.contains("~=")){
            return new NewPropertyWithStaticValueRule(coreRuleString, predicateString);
        }

        if(coreRuleString.startsWith("!")){
            return new ExcludePropertyRule(coreRuleString, predicateString);
        }

        if(coreRuleString.contains("~")){
            return new CompoundTransformPropertyNameRule(coreRuleString, predicateString);
        }

        if(coreRuleString.contains("=")){
            return new SimpleTransformPropertyNameRule(coreRuleString, predicateString);
        }

        return new IncludePropertyRule(coreRuleString, predicateString);
    }
}
