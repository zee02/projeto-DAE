package pt.ipleiria.estg.dei.ei.dae.academics.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Collaborator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsible;

import javax.crypto.spec.SecretKeySpec;
import java.security.Principal;

/**
 * Optional authentication filter that sets SecurityContext when valid token is present.
 * Runs BEFORE @Authenticated filter but doesn't throw exceptions.
 * Allows endpoints to check authentication optionally via securityContext.getUserPrincipal()
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 1)  // Run before AuthenticationFilter
public class OptionalAuthenticationFilter implements ContainerRequestFilter {
    @EJB
    private UserBean userBean;
    @Context
    private UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Only set security context if not already set (by @Authenticated filter)
        if (requestContext.getSecurityContext().getUserPrincipal() != null) {
            return;
        }

        var header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            // No token - leave security context empty (unauthenticated)
            return;
        }

        try {
            // Get token from the HTTP Authorization header
            String token = header.substring("Bearer".length()).trim();
            var user = userBean.find(getUserId(token));

            if (user != null) {
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return user::getIdAsString;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        if (role.equals("Administrador") && user instanceof Administrator) {
                            return true;
                        }
                        if (role.equals("Responsavel") && user instanceof Responsible) {
                            return true;
                        }
                        if (role.equals("Colaborador") && user instanceof Collaborator) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean isSecure() {
                        return uriInfo.getAbsolutePath().toString().startsWith("https");
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                });
            }
        } catch (Exception e) {
            // Invalid token - silently ignore and leave user unauthenticated
            // The @Authenticated filter will catch this if the endpoint requires auth
        }
    }

    private String getUserId(String token) {
        var key = new SecretKeySpec(TokenIssuer.SECRET_KEY, TokenIssuer.ALGORITHM);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
