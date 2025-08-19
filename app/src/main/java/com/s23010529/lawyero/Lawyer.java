package com.s23010529.lawyero;


public class Lawyer {
    private String name;
    private String type;
    private String about;
    private String id; // Firestore document ID


    public Lawyer(String name, String type, String about, String id) {
        this.name = name;
        this.type = type;
        this.about = about;
        this.id = id;
    }

    // Getter methods

    /** Returns the lawyer's name */
    public String getName() { return name; }

    /** Returns the lawyer's type */
    public String getType() { return type; }

    /** Returns the lawyer's description */
    public String getAbout() { return about; }

    /** Returns the Firestore document ID */
    public String getId() { return id; }
}
