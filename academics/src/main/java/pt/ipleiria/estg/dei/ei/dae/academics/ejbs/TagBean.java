package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagRequestDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;

import java.util.ArrayList;
import java.util.List;


@Stateless
public class TagBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    public Tag findAll() {

        List<Tag> results = em.createNamedQuery(
                "findAllTags",
                Tag.class
        ).getResultList();

        return results.isEmpty() ? null : results.get(0);
    }


    public Tag create(String nomeTag) throws IllegalArgumentException {

        if (nomeTag == null || nomeTag.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da tag não pode ser vazio");
        }
        Tag newTag = new Tag();
        newTag.setName(nomeTag);
        em.persist(newTag);
        return newTag;
    }


    public void associateTagToPublication(TagRequestDTO tagIds, long postId)
            throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException(
                    "Publication with id " + postId + " not found."
            );
        }

        for (Long tagId : tagIds.getTags()) {

            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new MyEntityNotFoundException(
                        "Tag with id " + tagId + " not found."
                );
            }


            if (!publication.getTags().contains(tag)) {
                publication.addTag(tag);
                tag.getPublications().add(publication);
            }
        }
    }


    public void dissociateTagsFromPublication(TagRequestDTO tagIds, long postId) throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication with id " + postId + " not found.");
        }

        for (Long tagId : tagIds.getTags()) {

            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new MyEntityNotFoundException("Tag with id " + tagId + " not found.");
            }


            if (publication.getTags().contains(tag)) {
                publication.removeTag(tag);
                tag.getPublications().remove(publication);
            }
        }
    }

}