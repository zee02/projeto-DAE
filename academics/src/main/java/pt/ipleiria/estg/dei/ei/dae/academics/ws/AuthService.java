package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.LoginResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.security.TokenIssuer;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthService {
    @EJB
    private UserBean userBean;

    @POST
    @Path("/login")
    public Response login(@Valid AuthDTO auth) {
        if (userBean.canLogin(auth.getEmail(), auth.getPassword())) {
            // 1. Recuperar o User (objeto)
            User user = userBean.findOrFail(auth.getEmail());

            // 2. Gerar token usando o User (TokenIssuer espera um User)
            String token = TokenIssuer.issue(user);

            // 3. Resposta
            return Response.ok(new LoginResponseDTO(token, UserDTO.from(user))).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciais inválidas").build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        // Implement logout logic if needed (e.g., invalidate token)
        return Response.ok().build();
    }
}
