package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;
import pt.ipleiria.estg.dei.ei.dae.academics.security.TokenIssuer;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthService {

    @EJB
    private UserBean userBean;

    private Hasher hasher = new Hasher();

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Username e password são obrigatórios"))
                    .build();
        }

        User user = userBean.find(request.getUsername());

        if (user == null || !hasher.verify(request.getPassword(), user.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Username ou password inválidos"))
                    .build();
        }

        String token = TokenIssuer.issue(user);

        return Response.ok(new TokenResponse(token)).build();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class TokenResponse {
        private String token;

        public TokenResponse(String token) { this.token = token; }
        public String getToken() { return token; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}