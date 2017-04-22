package com.github.t9t.minecraftrconclient;

/**
 * Exception thrown by {@link RconClientException} when the specified password is incorrect.
 */
public class AuthFailureException extends RconClientException {
    public AuthFailureException() {
        super("Authentication failure");
    }
}
