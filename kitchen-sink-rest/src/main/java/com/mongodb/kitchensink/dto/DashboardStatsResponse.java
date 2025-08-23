package com.mongodb.kitchensink.dto;


public class DashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long pendingVerifications;
    private long firstTimeLogins;
    private long newUsersThisMonth;
    private long adminUsers;
    private long bothAdminAndUser;

    public DashboardStatsResponse(long totalUsers, long activeUsers, long pendingVerifications, long firstTimeLogins, long newUsersThisMonth, long adminUsers, long regularUsers, long bothAdminAndUser) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.pendingVerifications = pendingVerifications;
        this.firstTimeLogins = firstTimeLogins;
        this.newUsersThisMonth = newUsersThisMonth;
        this.adminUsers = adminUsers;
        this.regularUsers = regularUsers;
        this.bothAdminAndUser = bothAdminAndUser;
    }

    private long regularUsers;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public long getFirstTimeLogins() {
        return firstTimeLogins;
    }

    public void setFirstTimeLogins(long firstTimeLogins) {
        this.firstTimeLogins = firstTimeLogins;
    }

    public long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(long adminUsers) {
        this.adminUsers = adminUsers;
    }

    public long getRegularUsers() {
        return regularUsers;
    }

    public void setRegularUsers(long regularUsers) {
        this.regularUsers = regularUsers;
    }

    public long getBothAdminAndUser() {
        return bothAdminAndUser;
    }

    public void setBothAdminAndUser(long bothAdminAndUser) {
        this.bothAdminAndUser = bothAdminAndUser;
    }
}
