package models;

import enums.Country;

public class Account extends Person {

    private Country country;

    public Account(String name, int age, String birthdate, String email, Country country) {
        super(name, age, birthdate, email);
        this.country = country;
    }

    public Country getCountry() { return country; }
    public void setLocation(Country c) { this.country = c; }

    public void setName(String n) { this.name = n; }
    public void setAge(int a) { this.age = a; }
    public void setBirthdate(String b) { this.birthdate = b; }
    public void setEmail(String e) { this.email = e; }

    @Override
    public String toString() {
        return "Name: " + name +
               "\nAge: " + age +
               "\nBirthYear: " + birthdate +
               "\nEmail: " + email +
               "\nCountry: " + country;
    }
}
