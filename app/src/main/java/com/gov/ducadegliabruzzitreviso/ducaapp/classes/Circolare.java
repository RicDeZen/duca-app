package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

public class Circolare implements Filterable {
    public String titolo;
    public String indirizzo;
    public Circolare(String titolo, String indirizzo){
        this.titolo = titolo;
        this.indirizzo = indirizzo;
    }

    @Override
    public boolean contains(String s) {
        return (titolo.toLowerCase()).contains(s.toLowerCase());
    }
}