package pt.ipleiria.estg.dei.ei.dae.academics.dtos;


import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.sql.Array;
import java.util.ArrayList;

public class TagDTO {

    private ArrayList<Long> tags;


    public ArrayList<Long> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Long> tags) {
        this.tags = tags;
    }
}