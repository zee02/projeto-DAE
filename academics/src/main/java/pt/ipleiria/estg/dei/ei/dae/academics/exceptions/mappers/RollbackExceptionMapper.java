package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.transaction.RollbackException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class RollbackExceptionMapper
        implements ExceptionMapper<RollbackException> {

    @Override
    public Response toResponse(RollbackException ex) {

        Throwable cause = ex.getCause();

        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {

                String message = cve.getConstraintViolations()
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
            cause = cause.getCause();
        }

        // fallback genérico
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "error", "Internal Server Error",
                        "message", "Transaction failed"
                ))
                .build();
    }
}
