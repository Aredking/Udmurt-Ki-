package com.udmurt.learn.model;

/** Кто учится в приложении: дошкольник или школьник. */
public enum Role {
    PRESCHOOL("Дошкольник", "Для самых маленьких: весёлые игры и картинки"),
    SCHOOL("Школьник", "Для школьников: больше слов, чтения и заданий");

    public final String label;
    public final String description;

    Role(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
