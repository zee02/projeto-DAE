package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import java.io.Serializable;

public class ConfidentialDTO implements Serializable {
    private boolean confidential;

    public ConfidentialDTO() {}

    public ConfidentialDTO(boolean confidential) {
        this.confidential = confidential;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }
}
