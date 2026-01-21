package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagDTO {

    private long id;

    @NotBlank(message = "O nome da tag não pode estar vazio")
    @Size(max = 50, message = "O nome da tag não pode ter mais de 50 caracteres")
    private String name;

    private boolean visible = true;

    // Para receber lista de IDs de tags (associar/desassociar)
    private ArrayList<Long> tags;

    public TagDTO() {
    }

    public TagDTO(String name) {
        this.name = name;
    }

    public static TagDTO from(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.id = tag.getId();
        dto.name = tag.getName();
        dto.visible = tag.isVisible();
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

    public ArrayList<Long> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Long> tags) {
        this.tags = tags;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
