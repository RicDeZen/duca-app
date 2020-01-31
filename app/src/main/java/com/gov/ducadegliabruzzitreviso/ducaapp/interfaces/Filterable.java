package com.gov.ducadegliabruzzitreviso.ducaapp.interfaces;

/**
 * Interface defining an Object that can be part of a Filter List.
 * @author Riccardo De Zen.
 */
public interface Filterable {
    /**
     * @return true if the Object matches the query, false otherwise.
     */
    boolean matches(String query);
}
