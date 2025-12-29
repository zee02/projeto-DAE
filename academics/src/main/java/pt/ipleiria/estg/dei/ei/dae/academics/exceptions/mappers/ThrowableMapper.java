package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger =
            Logger.getLogger(ThrowableMapper.class.getCanonicalName());

    @Override
    public Response toResponse(Throwable e) {
        String errorMsg = e.getMessage();
        logger.severe("UNEXPECTED ERROR: " + errorMsg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorMsg != null ? errorMsg : "An unexpected error occurred.")
                .build();
    }
}
