// BusinessException.java
package com.amarnath.shopkart.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}