package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Stateless
public class PublicationBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserBean userBean;

    private static final String UPLOAD_DIR = "/tmp/uploads";

    public Publication create(String title, String area, String summary, String authorEmail, InputStream fileData, String fileName) throws IOException {
        User author = userBean.findOrFail(authorEmail);

        // 1. Gravar Ficheiro no Disco
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path path = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.createDirectories(path.getParent());
        Files.copy(fileData, path, StandardCopyOption.REPLACE_EXISTING);

        // 2. Se o resumo vier vazio, colocamos placeholder (Futuro: IA)
        if (summary == null || summary.isEmpty()) {
            summary = "Resumo pendente de geração automática.";
        }

        // 3. Criar e Persistir Entidade
        Publication publication = new Publication(title, area, summary, path.toString(), author);
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