package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

public class FeedItem implements Filterable {
    public final String title;
    public final String link;
    public final String description;
    public final String date;
    public boolean expanded;

    public FeedItem(String title, String description, String link, String date) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.date = date;
        this.expanded = false;
    }

    public void flip(){ expanded = !expanded; }

    @Override
    public boolean contains(String s) {
        return title.contains(s);
    }
}
