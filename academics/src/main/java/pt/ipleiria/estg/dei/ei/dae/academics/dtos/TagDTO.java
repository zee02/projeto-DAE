package pt.ipleiria.estg.dei.ei.dae.academics.dtos;


import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagDTO {

    private long id;
    private String name;

    public static TagDTO from(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.id = tag.getId();
        dto.name = tag.getName();
        return dto;
    }

    public static List<TagDTO> from(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::from)
                .collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /* getters */
}
