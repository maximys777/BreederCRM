package com.maximys777.pugs.exception.exceptions.user;

public class UserEmailAlreadyExistsException extends RuntimeException {
  public UserEmailAlreadyExistsException(String message) {
    super(message);
  }
}
