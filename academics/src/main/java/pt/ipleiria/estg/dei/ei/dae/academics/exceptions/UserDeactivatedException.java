package pt.ipleiria.estg.dei.ei.dae.academics.exceptions;

public class UserDeactivatedException extends Exception {
    public UserDeactivatedException() {
        super("Your account has been deactivated. Please contact an administrator.");
    }

    public UserDeactivatedException(String message) {
        super(message);
    }
}
