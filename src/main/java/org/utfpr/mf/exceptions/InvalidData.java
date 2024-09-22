package org.utfpr.mf.exceptions;

//import org.mf.langchain.enums.ProcessStepName;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//@ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
public class InvalidData extends RuntimeException{

    public InvalidData(String step, String was) {
        super("Invalid input data for step: " + step + ". Was: " + was);
    }

}
