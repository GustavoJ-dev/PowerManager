package com.powermanager.powermanager.exception.fatura;

import com.powermanager.powermanager.exception.PowerManagerException;

public class FaturaAlreadyPaidException extends PowerManagerException {

    public FaturaAlreadyPaidException(String message) {
        super(message);
    }
}
