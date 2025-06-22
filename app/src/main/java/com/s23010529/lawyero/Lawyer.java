package com.s23010529.lawyero;

public class Lawyer {
    private String name;
    private String type;
    private String about;

    public Lawyer(String name, String type, String about) {
        this.name = name;
        this.type = type;
        this.about = about;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getAbout() { return about; }
}
