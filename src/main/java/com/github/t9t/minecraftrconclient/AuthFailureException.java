package com.github.t9t.minecraftrconclient;

public class AuthFailureException extends RconClientException {
    public AuthFailureException() {
        super("Authentication failure");
    }
}
