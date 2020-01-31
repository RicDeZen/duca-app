package com.gov.ducadegliabruzzitreviso.ducaapp.interfaces;

import java.util.List;

/**
 * Interface defining a List able to filter its content based on some String.
 *
 * @param <E> Type of data contained in the List.
 * @author Riccardo De Zen
 */
public interface FilterList<E> extends List<E>, Filterable{
    /**
     * Method to get the contents of the list that match a given query.
     *
     * @param query The String to use as query.
     * @return A FilterArrayList containing only the items that match the query.
     */
    FilterList<E> filter(String query);
}
