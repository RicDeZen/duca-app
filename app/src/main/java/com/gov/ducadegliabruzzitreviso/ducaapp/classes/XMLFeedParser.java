package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.os.Build;
import android.text.Html;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class used to parse the data coming from an InputStream into a list of Feed items.
 *
 * @author Riccardo De Zen
 */
public class XMLFeedParser {
    private static final String ns = null;

    public FilterArrayList<FeedItem> parse(InputStream in) throws XmlPullParserException,
            IOException {
        if (in == null) return new FilterArrayList<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private FilterArrayList<FeedItem> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        FilterArrayList<FeedItem> items = new FilterArrayList<>();

        //parser.require(XmlPullParser.START_TAG, ns, "xml");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                parser.next();
            }
        }
        if (items.size() == 0) items.add(new FeedItem("Errore di elaborazione", "", "", ""));
        return items;
    }

    public Object getFirst(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFirst(parser);
        } finally {
            in.close();
        }
    }

    private Object readFirst(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("item")) {
                return readItem(parser);
            } else {
                parser.next();
            }
        }
        return null;
    }

    private FeedItem readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String description = null;
        String link = null;
        String date = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("content:encoded")) {
                description = readDescription(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("pubDate")) {
                date = readDate(parser);
            } else {
                skip(parser);
            }
        }
        return new FeedItem(title, description, link, date);
    }

    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        if (Build.VERSION.SDK_INT >= 24) {
            title = (Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY)).toString();
        } else {
            title = (Html.fromHtml(title)).toString();
        }
        return title;
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    private String readDescription(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "content:encoded");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "content:encoded");
        /*if(Build.VERSION.SDK_INT >= 24){
            description = (Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)).toString();
        }
        else{
            description = (Html.fromHtml(description)).toString();
        }*/
        return description;
    }

    private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        String day_month = date.substring(5, 11);
        String year = date.substring(12, 16);
        date = day_month + "\n" + year;
        return date;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
