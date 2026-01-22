package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class RatingBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    public Rating find(long postId, User user) {

        List<Rating> results = em.createNamedQuery(
                        "findRatingByPublicationAndUser",
                        Rating.class
                )
                .setParameter("postId", postId)
                .setParameter("user", user)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }


    public Rating giveRating(long postId, String user_id, Integer score) throws MyEntityNotFoundException {

        User user = userBean.find(user_id);

        Rating existentRating = this.find(postId, user);
        Publication publication = publicationBean.findWithTags(postId);
        Rating newRating;
        if (existentRating == null) {


            if (publication == null) {
                throw new MyEntityNotFoundException( "Publication with id " + postId + " not found");
            }

            newRating = new Rating();
            newRating.setUser(user);
            newRating.setPublication(publication);
            newRating.setScore(score);

            publication.addRating(newRating);

            em.persist(newRating);

            // atualizar métricas da publicação
            publication.recalculateRatings();
            em.merge(publication);

            // Registrar no histórico
            try {
                publicationBean.seedHistory(
                    publication.getId(),
                    user.getEmail(),
                    java.util.Map.of("rating", "Avaliação recebida: " + score + " estrelas por " + user.getName())
                );
            } catch (Exception e) {
                // Não falhar se histórico não for gravado
            }

            return newRating;
        } else {
            int oldScore = existentRating.getScore();
            existentRating.setScore(score);
            em.merge(existentRating);

            //recalcular métricas
            publication.recalculateRatings();
            em.merge(publication);

            // Registrar no histórico
            try {
                publicationBean.seedHistory(
                    publication.getId(),
                    user.getEmail(),
                    java.util.Map.of("rating", "Avaliação atualizada de " + oldScore + " para " + score + " estrelas por " + user.getName())
                );
            } catch (Exception e) {
                // Não falhar se histórico não for gravado
            }

            return existentRating;
        }



    }
}