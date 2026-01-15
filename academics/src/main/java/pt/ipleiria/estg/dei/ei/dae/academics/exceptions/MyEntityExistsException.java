package pt.ipleiria.estg.dei.ei.dae.academics.exceptions;

public class MyEntityExistsException extends RuntimeException {

    public MyEntityExistsException(String argument) {
        super(argument);
    }
}
