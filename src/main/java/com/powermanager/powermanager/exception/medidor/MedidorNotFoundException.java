package com.powermanager.powermanager.exception.medidor;

import com.powermanager.powermanager.exception.PowerManagerException;

public class MedidorNotFoundException extends PowerManagerException {

    public MedidorNotFoundException(String message) {
        super(message);
    }
}
