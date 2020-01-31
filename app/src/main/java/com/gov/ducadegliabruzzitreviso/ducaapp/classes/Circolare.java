package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import androidx.annotation.Nullable;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

public class Circolare implements Filterable {
    public String titolo;
    public String indirizzo;
    public Circolare(String titolo, String indirizzo){
        this.titolo = titolo;
        this.indirizzo = indirizzo;
    }

    @Override
    public boolean matches(String s) {
        return (titolo.toLowerCase()).contains(s.toLowerCase());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Circolare){
            Circolare f = (Circolare)obj;
            return f.titolo.equals(this.titolo);
        }
        return false;
    }
}
