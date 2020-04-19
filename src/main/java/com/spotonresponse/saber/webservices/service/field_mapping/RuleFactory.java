package com.spotonresponse.saber.webservices.service.field_mapping;

import com.spotonresponse.saber.webservices.service.field_mapping.rules.*;

public class RuleFactory {
    public static Rule createRule(String ruleString) throws RuleException{
        if(ruleString.contains("~=")){
            return new NewPropertyWithStaticValueRule(ruleString);
        }

        if(ruleString.startsWith("!")){
            return new ExcludePropertyRule(ruleString);
        }

        if(ruleString.contains("~")){
            return new CompoundTransformPropertyNameRule(ruleString);
        }

        if(ruleString.contains("=")){
            return new SimpleTransformPropertyNameRule(ruleString);
        }

        return new IncludePropertyRule(ruleString);
    }
}
