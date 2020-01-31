package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import androidx.annotation.Nullable;

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

    @Override
    public boolean matches(String s) {
        return title.contains(s);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof FeedItem){
            FeedItem f = (FeedItem)obj;
            return f.title.equals(this.title);
        }
        return false;
    }
}
