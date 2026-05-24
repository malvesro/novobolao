package com.opendev.bolao.util.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte valores booleanos para CHAR(1) ('T'/'F') no banco de dados.
 * Utilizado para manter compatibilidade com o esquema legado.
 */
@Converter
public class BooleanCharConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return "F";
        }
        return attribute ? "T" : "F";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return false;
        }
        return "T".equalsIgnoreCase(dbData);
    }
}
