package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.FilterList;
import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

import java.util.ArrayList;

public class FilterArrayList<E extends Filterable> extends ArrayList<E> implements FilterList<E> {
    /**
     * Method to filter this list.
     *
     * @param query The String to use as query.
     * @return A new list containing all the elements in this list that match {@code query}.
     */
    @Override
    public FilterArrayList<E> filter(String query) {
        FilterArrayList<E> l = new FilterArrayList<>();
        for (E element : this) {
            if (element.matches(query)) l.add(element);
        }
        return l;
    }

    /**
     * A list matches the query if at least one of its items does.
     *
     * @param s The query to use.
     * @return true if the result of {@link FilterArrayList#filter(String)} is different from zero.
     */
    @Override
    public boolean matches(String s) {
        return (this.filter(s).size() != 0);
    }
}
