package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import java.util.List;

public class SearchPublicationDTO {
    private String title;
    private Long authorId;
    private String scientificArea;
    private List<Long> tags;
    private String dateFrom;
    private String dateTo;
    private Integer page;
    private Integer limit;
<<<<<<< Updated upstream
=======

    private String sortBy;    // ex: "createdAt", "title", "scientificArea"
    private String sortOrder;

>>>>>>> Stashed changes

    public SearchPublicationDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getScientificArea() {
        return scientificArea;
    }

    public void setScientificArea(String scientificArea) {
        this.scientificArea = scientificArea;
    }

    public List<Long> getTags() {
        return tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
<<<<<<< Updated upstream
=======

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
>>>>>>> Stashed changes
}
