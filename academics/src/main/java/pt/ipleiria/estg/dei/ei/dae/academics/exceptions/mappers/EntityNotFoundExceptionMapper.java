package pt.ipleiria.estg.dei.ei.dae.academics.exceptions.mappers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
    private static final Logger logger =
            Logger.getLogger(EntityNotFoundExceptionMapper.class.getCanonicalName());

    @Override
    public Response toResponse(EntityNotFoundException e) {
        String errorMsg = e.getMessage();
        logger.warning("ERROR: " + errorMsg);
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorMsg != null ? errorMsg : "Recurso não encontrado")
                .build();
    }
}

