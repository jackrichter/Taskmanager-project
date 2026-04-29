package com.example.taskmanager.exception;

public class UserNotValidException extends RuntimeException{

    public UserNotValidException() {
        super("User can't be created");
    }
}
