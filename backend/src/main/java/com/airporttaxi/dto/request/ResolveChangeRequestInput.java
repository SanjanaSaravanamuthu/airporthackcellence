package com.airporttaxi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ResolveChangeRequestInput {

    @NotBlank(message = "Action status is required (ACCEPTED or REJECTED)")
    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    public ResolveChangeRequestInput() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
