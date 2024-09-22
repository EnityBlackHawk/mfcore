package org.utfpr.mf.exceptions;

//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//@ResponseStatus(HttpStatus.NOT_FOUND)
public class IdNotFoundException extends RuntimeException{
    public IdNotFoundException(Integer id) {
        super("Could not find id " + id);
    }
}
