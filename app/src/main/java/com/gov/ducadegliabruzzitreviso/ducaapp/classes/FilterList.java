package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

import java.util.ArrayList;

public class FilterList<E extends Filterable> extends ArrayList<E> implements Filterable{
    public FilterList<E> filter(String s){
        if(s.equals("")) return this;
        FilterList<E> l = new FilterList<>();
        for(E e : this){
            if(e.contains(s)) l.add(e);
        }
        return l;
    }

    @Override
    public boolean contains(String s) {
        for(E e : this){
            if(e.contains(s)) return true;
        }
        return false;
    }
}
