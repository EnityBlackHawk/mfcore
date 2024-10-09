package org.utfpr.mf.exceptions;

public class InvalidOutputData extends RuntimeException{

    public InvalidOutputData(String step, String was) {
        super("Invalid output data for step: " + step + ". Was: " + was);
    }

}
