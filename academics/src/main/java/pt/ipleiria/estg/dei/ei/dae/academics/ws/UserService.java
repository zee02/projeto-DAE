package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {

    @EJB
    private UserBean userBean;

    @GET
    @RolesAllowed("ADMINISTRADOR")
    public Response getAllUsers() {
        List<User> users = userBean.findAll();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMINISTRADOR")
    public Response getUserById(@PathParam("id") long id) {
        User user = userBean.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Utilizador não encontrado"))
                    .build();
        }

        return Response.ok(user).build();
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}