package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.UserDeactivatedException;

import java.util.logging.Logger;

@Provider
public class UserDeactivatedExceptionMapper implements ExceptionMapper<UserDeactivatedException> {

    private static final Logger logger = Logger.getLogger(UserDeactivatedException.class.getCanonicalName());

    @Override
    public Response toResponse(UserDeactivatedException e) {
        String errorMsg = e.getMessage();
        logger.warning("ERROR: " + errorMsg);

        return Response.status(Response.Status.FORBIDDEN)
                .entity(java.util.Map.of("error", "USER_DEACTIVATED", "message", errorMsg))
                .build();
    }
}
