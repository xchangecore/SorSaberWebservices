package com.spotonresponse.saber.webservices.service.field_mapping.rules;

import com.spotonresponse.saber.webservices.service.field_mapping.ProcessingStep;
import org.json.JSONObject;

import java.util.stream.Stream;

/**
 * Rule predicate that determines whether a rule can be applied or not.
 */
public class RulePredicate {
    private final String predicateString;

    public RulePredicate(String predicateString) {
        // replace the first 'where' as it was used to indicate a clause in the
        // rule.
        this.predicateString = predicateString
                .replaceFirst("where", "");
    }

    public boolean canApply(ProcessingStep processingStep){
        if(predicateString.isEmpty()){
            return true;
        }

        JSONObject inputObject = processingStep.getInputObject();

        if(predicateString.contains("~") && predicateString.contains("=")){
            // the predicate looks either like:-
            // a = b ~ c ~ d OR a ~ b ~ c = d. We need to distinguish.
            String leftOperand = predicateString.split("=")[0].trim();
            String rightOperand = predicateString.split("=")[1].trim();

            String[] leftOperandComponents = leftOperand.split("~");
            String[] rightOperandComponents = rightOperand.split("~");

            if(leftOperandComponents.length == 1 && rightOperandComponents.length > 1){
                // we are dealing with a rule like:- a = b ~ c ~ d, with the
                // left operand as the key and the right as the values.
                if(!inputObject.has(leftOperand)){
                    return false;
                }

                String value = inputObject.get(leftOperand).toString().trim();
                return Stream.of(rightOperandComponents)
                        .map(String::trim)
                        .anyMatch(value::equals);
            }

            if(rightOperandComponents.length == 1 && leftOperandComponents.length > 1){
                // we are dealing with a rule like:- b ~ c ~ d = a, with the
                if(Stream.of(leftOperandComponents)
                        .map(String::trim)
                        .noneMatch(inputObject::has)){
                    return false;
                }

                return Stream.of(leftOperandComponents)
                        .map(String::trim)
                        .anyMatch(key -> {

                    if(!inputObject.has(key)){
                        return false;
                    }

                    String value = inputObject.get(key).toString().trim();
                    return value.equals(rightOperand);
                });
            }

        }

        if(predicateString.contains("!=")){
            // we are dealing with a clause like 'a != b'
            String[] split = predicateString.split("!=");
            if(split.length != 2){
                return false;
            }

            String leftOperand = predicateString.split("!=")[0].trim();
            String rightOperand = predicateString.split("!=")[1].trim();

            // the input object does not have the key, so
            // the output can never have the forbidden value. In example, if the input
            // property does not have a property 'a', there is no way the output
            // can have a property 'a' with the forbidden value 'b'.
            if(!inputObject.has(leftOperand)){
                return true;
            }

            String value = inputObject.get(leftOperand).toString().trim();
            return !value.equals(rightOperand);
        }

        if(predicateString.contains("=")){
            String[] split = predicateString.split("=");
            if(split.length != 2){
                return false;
            }

            String leftOperand = split[0].trim();
            String rightOperand = split[1].trim();

            if(!inputObject.has(leftOperand)){
                return false;
            }

            String value = inputObject.get(leftOperand).toString();
            return value.equals(rightOperand);
        }

        return inputObject.has(predicateString.trim());
    }


}
