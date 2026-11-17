package com.powermanager.powermanager.exception.cliente;

import com.powermanager.powermanager.exception.PowerManagerException;

public class ClienteNotFoundException extends PowerManagerException {

    public ClienteNotFoundException(String message) {
        super(message);
    }
}
