package pt.ipleiria.estg.dei.ei.dae.academics.ws;

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

        String email = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.create(input, email);

        return Response.status(Response.Status.CREATED).entity(PublicationDTO.from(publication)).build();
    }

    @POST
    @Authenticated
    @Path("/{post_id}/ratings")
        public Response giveRating(@PathParam("post_id")Long post_id, @Valid RatingDTO ratingDTO) throws MyEntityNotFoundException {
        String email = securityContext.getUserPrincipal().getName();

        Rating rating = ratingBean.giveRating(post_id, email, ratingDTO.getRating());


        return Response.ok(RatingDTO.from(rating)).build();
    }

    @POST
    @Authenticated
    @Path("/{post_id}/comments")
    public Response createComment(@PathParam("post_id")Long post_id, @Valid CommentDTO commentDTO) throws MyEntityNotFoundException {
        String email = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.findWithComments(post_id);

        commentBean.create(publication, email, commentDTO);


        PublicationWithCommentsDTO dto = PublicationWithCommentsDTO.from(publication);

        return Response.ok(dto).build();
    }


    @POST
    @Authenticated
    @Path("/{post_id}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response associateTag(@PathParam("post_id")Long post_id, TagDTO tags) throws MyEntityNotFoundException {

        tagBean.associateTagToPublication(tags, post_id);

        Publication publication = publicationBean.findWithTags(post_id);

        return Response.ok(PublicationDTO.from(publication)).build();
    }



}