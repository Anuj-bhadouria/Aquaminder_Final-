package com.iar.myapplication;

public class Request {
    private String requestId;
    private String userId;
    private String plumberId;
    private String issueDescription;
    private String imageUrl;
    private String status; // "PENDING", "ACCEPTED", "COMPLETED"
    private String address;
    private String userContact;

    public Request() { }

    public Request(String requestId, String userId, String plumberId, String issueDescription, String imageUrl, String status, String address, String userContact) {
        this.requestId = requestId;
        this.userId = userId;
        this.plumberId = plumberId;
        this.issueDescription = issueDescription;
        this.imageUrl = imageUrl;
        this.status = status;
        this.address = address;
        this.userContact = userContact;
        this.requestId = requestId;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPlumberId() { return plumberId; }
    public void setPlumberId(String plumberId) { this.plumberId = plumberId; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }
}