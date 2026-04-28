package com.example.taskmanager.exception;

public class InvalidFieldFormatException extends RuntimeException{

    public InvalidFieldFormatException(String message) {
        super(message);
    }
}
