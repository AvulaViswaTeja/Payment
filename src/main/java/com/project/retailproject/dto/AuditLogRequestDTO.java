package com.project.retailproject.dto;

public class AuditLogRequestDTO {
    private Long userId;
    private String action;
    private String userName;

    public AuditLogRequestDTO() {}
    public AuditLogRequestDTO(String action) {
        this.userId = 0L;
        this.userName = "SYSTEM";
        this.action = action;
    }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
