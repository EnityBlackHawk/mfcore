package org.utfpr.mf.exceptions;

//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DBConnectionException  extends RuntimeException{
    public DBConnectionException(String message) {
        super("Unable to connect to the database. Please check your connection settings: " + message);
    }
}
