package models;

public class Person {
    protected String name;
    protected int age;
    public String birthdate;
    protected String email;

    public Person(String name, int age, String birthdate, String email) {
        this.name = name;
        this.age = age;
        this.birthdate = birthdate;
        this.email = email;
    }

    // REQUIRED getters
public String getName() { return name; }
public int getAge() { return age; }
public String getBirthdate() { return birthdate; }
public String getEmail() { return email; }

}
