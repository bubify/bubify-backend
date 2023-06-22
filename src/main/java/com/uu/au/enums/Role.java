package com.uu.au.enums;

public enum Role {
    STUDENT("Student"),
    JUNIOR_TA("Junior_TA"),
    SENIOR_TA("Senior_TA"),
    TEACHER("Teacher"),
    SETUP_SYSTEM("SETUP_SYSTEM");

    private final String role;

    Role(final String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
}
