package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class PublicationBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserBean userBean;

    private static final String UPLOAD_DIR = "/tmp/uploads";

    public void create(String title, String scientificArea, String authorEmail) {


        User author = userBean.findOrFail(authorEmail);

        Publication publication = new Publication(title, scientificArea, true,"Resumo AI", "file", author);

        em.persist(publication);
    }



    public Publication create(MultipartFormDataInput input, String authorEmail) throws IOException {

        Map<String, List<InputPart>> formParts = input.getFormDataMap();


        if (!formParts.containsKey("title") || !formParts.containsKey("scientific_area") ||  !formParts.containsKey("file")) {
            throw new WebApplicationException(
                    "Missing required fields",
                    Response.Status.BAD_REQUEST
            );
        }

        String title = formParts.get("title").get(0).getBodyAsString();
        String area = formParts.get("scientific_area").get(0).getBodyAsString();

        String summary = null;
        if (formParts.containsKey("summary")) {
            summary = formParts.get("summary").get(0).getBodyAsString();
        }

        InputPart filePart = formParts.get("file").get(0);
        String fileName = PublicationUtils.getFileName(filePart.getHeaders());
        InputStream fileData = filePart.getBody(InputStream.class, null);

        User author = userBean.findOrFail(authorEmail);

        // Guardar ficheiro
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        Path path = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.createDirectories(path.getParent());
        Files.copy(fileData, path, StandardCopyOption.REPLACE_EXISTING);

        // Summary automático
        if (summary == null || summary.isBlank()) {
            summary = "Resumo pendente de geração automática.";
        }

        Publication publication = new Publication(title, area, false,summary, path.toString(), author);

        em.persist(publication);
        return publication;
    }


    public List<Publication> getAllPublic() {
        return em.createNamedQuery("getAllPublicPosts", Publication.class).getResultList();
    }

    public Publication find(Long id) {
        return em.find(Publication.class, id);
    }


}