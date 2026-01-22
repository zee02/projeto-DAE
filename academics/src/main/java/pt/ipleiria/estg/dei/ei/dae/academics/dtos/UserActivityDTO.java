package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.UserActivity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UserActivityDTO implements Serializable {
    private long id;
    private String type;
    private String description;
    private String details;
    private Date timestamp;
    private String ipAddress;

    public UserActivityDTO() {
    }

    public UserActivityDTO(long id, String type, String description, String details, Date timestamp, String ipAddress) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.details = details;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
    }

    public static UserActivityDTO from(UserActivity activity) {
        return new UserActivityDTO(
            activity.getId(),
            activity.getType(),
            activity.getDescription(),
            activity.getDetails(),
            activity.getTimestamp(),
            activity.getIpAddress()
        );
    }

    public static List<UserActivityDTO> from(List<UserActivity> activities) {
        return activities.stream().map(UserActivityDTO::from).collect(Collectors.toList());
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
