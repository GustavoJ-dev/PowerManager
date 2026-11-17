package com.powermanager.powermanager.exception.usuario;

import com.powermanager.powermanager.exception.PowerManagerException;

public class UsuarioNotFoundException extends PowerManagerException {

    public UsuarioNotFoundException(String message) {
        super(message);
    }
}
