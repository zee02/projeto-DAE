package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.CommentBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.RatingBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.io.IOException;
import java.util.List;

@Path("tags")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class TagService {

    @EJB
    private TagBean tagBean;


    // EP32 - Consultar Todas as tags

    @GET
    @RolesAllowed({"Administrador"})
    public List<TagDTO> getAllPublicPosts() {

        return TagDTO.from(tagBean.findAll());
    }


}