package com.powermanager.powermanager.exception.fatura;

import com.powermanager.powermanager.exception.PowerManagerException;

public class FaturaAlreadyExistsException extends PowerManagerException {

    public FaturaAlreadyExistsException(String message) {
        super(message);
    }
}
