package com.uu.au.enums;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Locale;

@Component
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.getRole();
    }

    @Override
    public Role convertToEntityAttribute(String role) {
        return switch (role.toUpperCase()) {
            case "TEACHER" -> Role.TEACHER;
            case "SENIOR_TA" -> Role.SENIOR_TA;
            case "JUNIOR_TA" -> Role.JUNIOR_TA;
            case "STUDENT" -> Role.STUDENT;
            default -> null;
        };
    }
}
