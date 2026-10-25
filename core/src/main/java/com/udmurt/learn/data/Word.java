package com.udmurt.learn.data;

/** Одно слово-словарная статья: удмуртское слово, перевод и картинка. */
public class Word {

    public final String udmurt;
    public final String russian;
    /** Путь к картинке в ассетах (например, images/1F408.png) или null. */
    public final String image;
    public final String category;
    /** true — слово относится к «школьному» уровню (усложняет задания). */
    public final boolean school;

    public Word(String udmurt, String russian, String image, String category, boolean school) {
        this.udmurt = udmurt;
        this.russian = russian;
        this.image = image;
        this.category = category;
        this.school = school;
    }

    public boolean hasPicture() {
        return image != null && !image.isEmpty();
    }

    @Override
    public String toString() {
        return udmurt + " = " + russian;
    }
}