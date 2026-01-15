package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("posts")
@Produces(MediaType.APPLICATION_JSON)
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
    @Path("/")
    @Authenticated
    public List<PublicationDTO> getAllPublicPosts() {
        return PublicationDTO.from(publicationBean.getAllPublic());
    }

    // EP01 - Criar Publicação (Multipart)

    @POST
    @Authenticated
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPost(MultipartFormDataInput input) throws IOException {

        String user_id = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.create(input, user_id);

        return Response.status(Response.Status.CREATED).entity(PublicationDTO.from(publication)).build();
    }

    //EP03
    @POST
    @Authenticated
    @Path("/{post_id}/ratings")
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
    public Response createComment(@PathParam("post_id") long post_id, @Valid CommentDTO commentDTO) throws MyEntityNotFoundException {
        String user_id = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.findWithComments(post_id);

        Comment comment = commentBean.create(publication, user_id, commentDTO.getComment());


        CommentDTO dto = CommentDTO.from(comment);

        return Response.ok(dto).build();
    }

    //EP06
    @POST
    @Authenticated
    @Path("/{post_id}/tags")
    public Response associateTag(@PathParam("post_id") long post_id, TagRequestDTO tags) throws MyEntityNotFoundException {


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
    public Response disassociateTag(@PathParam("post_id") long post_id, TagRequestDTO tags) throws MyEntityNotFoundException {


        tagBean.dissociateTagsFromPublication(tags, post_id);
        Publication publication = publicationBean.findWithTags(post_id);
        var publicationDTO = PublicationDTO.from(publication);
        publicationDTO.setTags(TagDTO.from(publication.getTags()));
        return Response.ok(publicationDTO).build();
    }

    //EP02 - Corrigir resumo gerado por IA
    @PATCH
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Path("/{post_id}/summary")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSummary(@PathParam("post_id") long post_id, @Valid SummaryDTO summaryDTO) throws MyEntityNotFoundException {
        String user_id = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.updateSummary(post_id, user_id, summaryDTO.getSummary());

        return Response.ok(PublicationDTO.from(publication)).build();
    }

    //EP10 - Ordenar lista de publicações
    @POST
    @Authenticated
    @RolesAllowed({"Colaborador", "Responsavel", "Administrador"})
    @Path("/sort")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSortedPublications(@Valid SortRequestDTO sortRequest) {
        List<Publication> publications = publicationBean.getAllPublicSorted(
                sortRequest.getSort_by(),
                sortRequest.getOrder()
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
}