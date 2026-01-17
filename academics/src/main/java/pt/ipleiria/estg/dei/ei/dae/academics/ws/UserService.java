package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CollaboratorDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UpdateUserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.RoleDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.ForgotPasswordDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.ChangePasswordDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.*;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.*;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;
import jakarta.persistence.EntityNotFoundException;
import jakarta.mail.MessagingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {
    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CollaboratorBean collaboratorBean;

    @EJB
    private ResponsibleBean responsibleBean;

    @EJB
    private AdministratorBean administratorBean;

    @EJB
    private EmailBean emailBean;

    @Context
    private SecurityContext securityContext;

    // EP31 - Consultar todos (Apenas Admin)
    @GET
    @Authenticated
    @RolesAllowed({"Administrador"})
    public List<UserDTO> getAllUsers() {
        return UserDTO.from(userBean.findAll());
    }

    // EP22 - Criar utilizador (Apenas Admin)
    @POST
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response createUser(@Valid UserDTO dto) {
        try {
            User newUser;

            switch (dto.getRole()) {
                case Administrador:
                    newUser = administratorBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                case Responsavel:
                    newUser = responsibleBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                case Colaborador:
                    newUser = collaboratorBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Role inválido").build();
            }

            return Response.status(Response.Status.CREATED).entity(UserDTO.from(newUser)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }
    // EP27 - Apagar utilizador (Hard delete)

    @DELETE
    @Path("/{user_id}")
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response deleteUser(@PathParam("user_id") long userId) {
        try {
            userBean.delete(userId);
            return Response.noContent().build(); // 204
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // EP25 - Desativar utilizador (Soft delete)
    @PUT
    @Path("/{user_id}/activate")
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response activateUser(@PathParam("user_id") long userId) {
        try {
            userBean.activate(userId);
            return Response.ok("{\"message\": \"Utilizador ativado com sucesso\", \"status\": \"active\"}").build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build(); // [cite: 836]
        }
    }

    // EP26 - Desativar utilizador (Soft delete)
    @PUT
    @Path("/{user_id}/deactivate")
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response deactivateUser(@PathParam("user_id") long userId) {
        try {
            userBean.deactivate(userId);
            return Response.ok("{\"message\": \"Utilizador desativado com sucesso\", \"status\": \"inactive\"}").build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build(); // [cite: 836]
        }
    }



    //EP07
    @GET
    @Path("/me/posts")
    @Authenticated
    public Response getMyPublications(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("is_visible") Boolean isVisible, @QueryParam("tag") Long tagId) {

        String user_id = securityContext.getUserPrincipal().getName();

        List<PublicationDTO> publications = publicationBean.findMyPublications(user_id, page, limit, isVisible, tagId);


        return Response.ok(publications).build();
    }

    //EP13 - Editar dados pessoais
    @PATCH
    @Path("/me")
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    public Response updatePersonalData(@Valid UpdateUserDTO dto) {
        String user_id = securityContext.getUserPrincipal().getName();

        User updatedUser = userBean.updatePersonalData(user_id, dto.getName(), dto.getEmail());
        return Response.ok(UserDTO.from(updatedUser)).build();
    }

    //EP27 - Alterar o papel (role) de um utilizador
    @PUT
    @Path("/{user_id}/role")
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response updateUserRole(@PathParam("user_id") long userId, @Valid RoleDTO dto) {
        try {
            User updatedUser = userBean.updateRole(userId, dto.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Papel do utilizador atualizado com sucesso");
            response.put("user_id", updatedUser.getId());
            response.put("new_role", dto.getRole());

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    //EP23 - Editar dados de um utilizador (Admin)
    @PUT
    @Path("/{user_id}")
    @Authenticated
    @RolesAllowed({"Administrador"})
    public Response updateUser(@PathParam("user_id") long userId, @Valid UserDTO dto) {
        try {
            User updatedUser = userBean.updateUser(userId, dto.getName(), dto.getEmail(), dto.getRole().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Dados atualizados com sucesso");
            response.put("user_id", updatedUser.getId());
            response.put("name", updatedUser.getName());
            response.put("email", updatedUser.getEmail());
            response.put("role", dto.getRole().toString());

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", e.getMessage())).build();
        } catch (MyEntityExistsException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    //EP15 - Recuperar palavra-passe através do email
    @POST
    @Path("/password/forgot")
    public Response forgotPassword(@Valid ForgotPasswordDTO dto) {
        userBean.resetPassword(dto.getEmail());

        try {
            // Tentar enviar email (só se o utilizador existir, mas não revelamos isso)
            if (userBean.findByEmail(dto.getEmail()) != null) {
                emailBean.send(
                        dto.getEmail(),
                        "Recuperação de Palavra-passe",
                        "A sua palavra-passe foi redefinida para: 123\n\nPor favor, altere a sua palavra-passe após iniciar sessão."
                );
            }
        } catch (MessagingException e) {
            // Log do erro mas não falhar o pedido
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }

        return Response.ok(Map.of("message", "Se existir uma conta associada a este email, enviámos instruções para redefinir a palavra-passe.")).build();
    }

    // EP14 - Alterar palavra-passe
    @PATCH
    @Path("/me/password")
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(@Valid ChangePasswordDTO dto) {
        try {
            String userId = securityContext.getUserPrincipal().getName();

            userBean.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Palavra-passe alterada com sucesso");
            response.put("updated_at", new Date());

            return Response.ok(response).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // EP12 - Consultar o próprio histórico de atividade
    @GET
    @Path("/me/activity")
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyActivity(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        
        try {
            String odUserId = securityContext.getUserPrincipal().getName();
            long userId = Long.parseLong(odUserId);
            
            Map<String, Object> result = userBean.getUserActivity(userId, page, limit);
            return Response.ok(result).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP28 - Consultar histórico de atividade de qualquer utilizador
    @GET
    @Path("/{user_id}/activity")
    @Authenticated
    @RolesAllowed({"Administrador"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserActivity(
            @PathParam("user_id") long userId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        
        try {
            Map<String, Object> result = userBean.getUserActivity(userId, page, limit);
            return Response.ok(result).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}
