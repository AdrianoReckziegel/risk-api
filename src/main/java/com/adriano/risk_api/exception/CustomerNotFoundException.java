package com.adriano.risk_api.exception;

public class CustomerNotFoundException extends RuntimeException{

    public CustomerNotFoundException(Long id) {
        super("Customer not found: " + id);
    }

    public CustomerNotFoundException(String externalId) {
        super("Customer not found: " + externalId);
    }
}
