package pt.ipleiria.estg.dei.ei.dae.academics.ws;


import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.AuthResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.academics.security.TokenIssuer;

@Path("auth")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AuthService {
    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    public Response authenticate(@Valid AuthDTO auth) {

        User user = userBean.canLogin(auth.getEmail(), auth.getPassword());

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED) .entity("Invalid credentials").build();
        }

        String token = TokenIssuer.issue(String.valueOf(user.getId()));
        AuthResponseDTO response = new AuthResponseDTO("Autenticação bem-sucedida", token, user);

        return Response.ok(response).build();
    }


    /*@PATCH
    @Path("/set-password")
    @RolesAllowed({"Administrator", "Student", "Teacher"})
    @Authenticated
    public Response updatePassword(@Valid AuthDTO auth) {
        try {
            var principal = securityContext.getUserPrincipal();

            // Exemplo: o novo password vem do campo "password" no AuthDTO
            userBean.updatePassword(principal.getName(), auth.getPassword());

            return Response.ok().entity("Password updated successfully").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating password: " + e.getMessage())
                    .build();
        }
    }*/

    @GET
    @Authenticated
    @Path("/user")
    public Response getAuthenticatedUser() {
        String id = securityContext.getUserPrincipal().getName();
        var user = userBean.find(id);
        
        // Initialize lazy collection before converting to DTO
        if (user != null && user.getSubscribedTags() != null) {
            org.hibernate.Hibernate.initialize(user.getSubscribedTags());
        }
        
        return Response.ok(UserDTO.from(user)).build();
    }

    //EP30 - Logout / Terminar sessão do utilizador
    @POST
    @Authenticated
    @Path("/logout")
    public Response logout() {
        // Com JWT stateless, o token não pode ser invalidado no servidor.
        // O frontend deve apagar o token do lado do cliente.
        // Para invalidação real, seria necessário implementar uma blacklist de tokens.
        return Response.ok(java.util.Map.of("message", "Logout efetuado com sucesso")).build();
    }
}
