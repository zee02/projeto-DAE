package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {

        // ir buscar apenas a mensagem útil
        String message = ex.getConstraintViolations()
                .iterator()
                .next()
                .getMessage();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", "Bad Request",
                        "message", message
                ))
                .build();
    }
}
