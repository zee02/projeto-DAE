package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityConflictException;

import java.util.logging.Logger;

@Provider
public class MyEntityConflictExceptionMapper implements ExceptionMapper<MyEntityConflictException> {
    private static final Logger logger =
            Logger.getLogger(MyEntityConflictException.class.getCanonicalName());

    @Override
    public Response toResponse(MyEntityConflictException e) {
        String errorMsg = e.getMessage();
        logger.warning("ERROR: " + errorMsg);
        return Response.status(Response.Status.CONFLICT)
                .entity(errorMsg)
                .build();
    }
}
