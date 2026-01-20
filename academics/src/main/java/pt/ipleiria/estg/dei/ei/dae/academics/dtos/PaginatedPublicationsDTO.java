package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import java.util.List;

public class PaginatedPublicationsDTO<PublicationDTO> {
    private List<PublicationDTO> data;
    private long total;

    public PaginatedPublicationsDTO(List<PublicationDTO> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<PublicationDTO> getData() { return data; }
    public long getTotal() { return total; }
}
