package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;

import java.io.File;
import java.io.StringReader;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.PublicationEdit;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.CommentBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.RatingBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("posts")
@Produces({MediaType.APPLICATION_JSON})
public class PublicationService {
    @EJB
    private PublicationBean publicationBean;

    @EJB
    private RatingBean ratingBean;

    @EJB
    private CommentBean commentBean;
    @EJB
    private TagBean tagBean;

    @Context
    private SecurityContext securityContext;

    // EP06 - Consultar Públicas
    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public List<PublicationDTO> getAllPublicPosts() {
        return PublicationDTO.from(publicationBean.getAllPublic());
    }

    // EP09 - Pesquisar publicações
    @POST
    @Path("/search")
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPublications(SearchPublicationDTO searchDTO) {
        List<Publication> publications = publicationBean.searchPublications(
                searchDTO.getTitle(),
                searchDTO.getAuthorId(),
                searchDTO.getScientificArea(),
                searchDTO.getTags(),
                searchDTO.getDateFrom(),
                searchDTO.getDateTo()
        );

        List<PublicationDTO> dtos = publications.stream()
                .map(p -> {
                    PublicationDTO dto = PublicationDTO.from(p);
                    dto.setTags(TagDTO.from(p.getTags()));
                    return dto;
                })
                .toList();

        return Response.ok(dtos).build();
    }

    //EP10 - Ordenar lista de publicações
    @POST
    @Path("/sort")
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Consumes("application/json")
    @Produces("application/json")
    public Response getSortedPublications(SortRequestDTO sortRequest) {
        // Validação dos parâmetros
        String sortBy = sortRequest.getSort_by();
        String order = sortRequest.getOrder();

        if (sortBy == null || sortBy.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "O campo sort_by não pode estar vazio"))
                    .build();
        }
        if (!sortBy.matches("^(average_rating|comments_count|ratings_count)$")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "sort_by deve ser: average_rating, comments_count ou ratings_count"))
                    .build();
        }
        if (order == null || order.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "O campo order não pode estar vazio"))
                    .build();
        }
        if (!order.matches("^(asc|desc)$")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "order deve ser: asc ou desc"))
                    .build();
        }

        List<Publication> publications = publicationBean.getAllPublicSorted(sortBy, order);

        List<PublicationDTO> dtos = publications.stream()
                .map(p -> {
                    PublicationDTO dto = PublicationDTO.from(p);
                    dto.setTags(TagDTO.from(p.getTags()));
                    dto.setComments(CommentDTO.from(p.getComments()));
                    return dto;
                })
                .toList();


        return Response.ok(dtos).build();
    }

    // EP01 - Criar Publicação (Multipart)
    @POST
    @Authenticated
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response createPost(MultipartFormDataInput input) throws IOException {
        String user_id = securityContext.getUserPrincipal().getName();
        Publication publication = publicationBean.create(input, user_id);
        return Response.status(Response.Status.CREATED).entity(PublicationDTO.from(publication)).build();
    }

    //EP02 - Corrigir resumo gerado por IA
    @PATCH
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Path("/{post_id}/summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSummary(@PathParam("post_id") long post_id, @Valid SummaryDTO summaryDTO) throws MyEntityNotFoundException {
        String user_id = securityContext.getUserPrincipal().getName();
        Publication publication = publicationBean.updateSummary(post_id, user_id, summaryDTO.getSummary());
        return Response.ok(PublicationDTO.from(publication)).build();
    }

    //EP03
    @POST
    @Authenticated
    @Path("/{post_id}/ratings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response giveRating(@PathParam("post_id") long post_id, @Valid RatingDTO ratingDTO) throws MyEntityNotFoundException {
        String user_id = securityContext.getUserPrincipal().getName();
        ratingBean.giveRating(post_id, user_id, ratingDTO.getRating());
        Publication publication = publicationBean.findWithTags(post_id);
        return Response.ok(PublicationDTO.from(publication)).build();
    }

    //EP04
    @POST
    @Authenticated
    @Path("/{post_id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(@PathParam("post_id") long post_id, @Valid CommentDTO commentDTO) throws MyEntityNotFoundException {
        String user_id = securityContext.getUserPrincipal().getName();
        Publication publication = publicationBean.findWithComments(post_id);
        Comment comment = commentBean.create(publication, user_id, commentDTO.getComment());
        CommentDTO dto = CommentDTO.from(comment);
        return Response.ok(dto).build();
    }

    //EP05 - Associar Tag
    @POST
    @Authenticated
    @Path("/{post_id}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response associateTag(@PathParam("post_id") long post_id, TagDTO tags) throws MyEntityNotFoundException {
        tagBean.associateTagToPublication(tags, post_id);
        Publication publication = publicationBean.findWithTags(post_id);
        var publicationDTO = PublicationDTO.from(publication);
        publicationDTO.setTags(TagDTO.from(publication.getTags()));
        return Response.ok(publicationDTO).build();
    }

    //EP18
    @DELETE
    @Authenticated
    @Path("/{post_id}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response disassociateTag(@PathParam("post_id") long post_id, TagDTO tags) throws MyEntityNotFoundException {
        tagBean.dissociateTagsFromPublication(tags, post_id);
        Publication publication = publicationBean.findWithTags(post_id);
        var publicationDTO = PublicationDTO.from(publication);
        publicationDTO.setTags(TagDTO.from(publication.getTags()));
        return Response.ok(publicationDTO).build();
    }

    //EP19 - Ocultar ou mostrar comentários de uma publicação
    @PUT
    @Authenticated
    @RolesAllowed({"Responsavel", "Administrador"})
    @Path("/{post_id}/comments/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCommentsVisibility(@PathParam("post_id") long post_id, @Valid VisibilityDTO dto) throws MyEntityNotFoundException {
        Date updatedAt = commentBean.updateAllVisibilityByPublication(post_id, dto.getVisible());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Visibilidade dos comentários atualizada com sucesso");
        response.put("post_id", post_id);
        response.put("visible", dto.getVisible());
        response.put("updated_at", updatedAt);

        return Response.ok(response).build();
    }

    //EP19 - Ocultar ou mostrar um comentário específico
    @PUT
    @Authenticated
    @RolesAllowed({"Responsavel", "Administrador"})
    @Path("/{post_id}/comments/{comment_id}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCommentVisibility(
            @PathParam("post_id") long post_id,
            @PathParam("comment_id") long comment_id,
            @Valid VisibilityDTO dto) throws MyEntityNotFoundException {
        Comment comment = commentBean.updateVisibility(post_id, comment_id, dto.getVisible());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Visibilidade do comentário atualizada com sucesso");
        response.put("comment_id", comment.getId());
        response.put("visible", comment.isVisible());
        response.put("updated_at", comment.getUpdatedAt());

        return Response.ok(response).build();
    }

    //EP20 - Ocultar ou mostrar publicação
    @PUT
    @Authenticated
    @RolesAllowed({"Responsavel", "Administrador"})
    @Path("/{post_id}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("post_id") long post_id, @Valid VisibilityDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.updateVisibility(post_id, dto.getVisible());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Visibilidade da publicação atualizada com sucesso");
        response.put("post_id", publication.getId());
        response.put("visible", publication.isVisible());
        response.put("updated_at", publication.getUpdatedAt());

        return Response.ok(response).build();
    }


    //EP08- histórico de edições das publicações
    @GET
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Path("/{post_id}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPostHistory(
            @PathParam("post_id") long postId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("10") int limit
    ) throws MyEntityNotFoundException {

        String userId = securityContext.getUserPrincipal().getName();

        List<PublicationEdit> edits = publicationBean.getPostHistory(postId, userId, page, limit);

        // devolve JSON no formato do enunciado (sem DTO)
        List<Map<String, Object>> response = edits.stream().map(e -> {
            JsonStructure changes;
            try (JsonReader reader = Json.createReader(new StringReader(e.getChangesJson()))) {
                changes = reader.read();
            }

            return Map.of(
                    "edit_id", e.getId(),
                    "post_id", e.getPublication().getId(),
                    "edited_at", e.getEditedAt(),
                    "edited_by", Map.of(
                            "id", e.getEditedBy().getId(),
                            "name", e.getEditedBy().getName()
                    ),
                    "changes", changes
            );
        }).toList();

        return Response.ok(response).build();
    }

    //EP 33 editar publicacao
    @PUT
    @Path("/{id}")
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editPost(@PathParam("id") Long id, MultipartFormDataInput input) throws IOException {

        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());

        Publication publication = publicationBean.edit(id, input, user_id);

        return Response.ok(PublicationDTO.from(publication)).build();
    }


    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("id") long id) {

        File fileData = publicationBean.getPublicationFile(id);

        return Response.ok(fileData).header("Content-Disposition", "attachment; filename=\"" + fileData.getName() + "\"").build();
    }

    @DELETE
    @Path("/{id}/file")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response removePublicationFile(@PathParam("id") Long id) {

        Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());

        Publication publication = publicationBean.removeFile(id, userId);

        return Response.ok(PublicationDTO.from(publication)
        ).build();
    }


}