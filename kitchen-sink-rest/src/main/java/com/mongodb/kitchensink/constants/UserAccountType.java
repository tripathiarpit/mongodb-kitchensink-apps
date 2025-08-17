package com.mongodb.kitchensink.constants;

public enum UserAccountType {
    USER_ACCOUNT_TYPE("USER"),
    ADMIN_ACCOUNT_TYPE("ADMIN");
    private final String type;
    UserAccountType(String type) {
       this.type = type;
    }

    public String getType() {
        return type;
    }
}