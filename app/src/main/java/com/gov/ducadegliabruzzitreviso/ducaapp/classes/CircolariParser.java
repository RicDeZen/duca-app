package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.util.Log;
import android.util.Xml;

import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class CircolariParser {
    private static final String ns = null;
    private FilterList<Filterable> items = new FilterList<>();

    public FilterList<Filterable> parse(InputStream in) throws XmlPullParserException, IOException {
        if(in == null) return items;
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            skipToContent(br);
            line = br.readLine();
            while(!line.contains("<div style=")){
                //la riga che ho scelto come fine è il tag div blabla
                parser.setInput(new StringReader(line));
                parser.defineEntityReplacementText("ntilde", "\u00F1");
                parser.defineEntityReplacementText("nbsp", " ");
                readFeed(parser);
                line = br.readLine();
            }
        }
        finally {
            in.close();
        }
        return items;
    }

    public FilterList<Filterable> parseByBundle(InputStream in) throws XmlPullParserException, IOException {
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            skipToContent(br);
            line = br.readLine();
            while(!line.contains("<div style=")){
                //la riga che ho scelto come fine è il tag div blabla
                parser.setInput(new StringReader(line));
                parser.defineEntityReplacementText("ntilde", "\u00F1");
                parser.defineEntityReplacementText("nbsp", " ");
                readBundle(parser);
                line = br.readLine();
            }
        }
        catch(XmlPullParserException e){
            return items;
        }
        finally {
            in.close();
        }
        return items;
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        boolean firstOfBundle = true;
        try {
            int current = parser.next();
            while (current != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (name.equals("a")) {
                        if(firstOfBundle){
                            items.add(readCircolareBetter(parser, ""));
                            firstOfBundle = false;
                        }
                    }
                }
                try{
                    current = parser.next();
                }catch(XmlPullParserException e){
                }

            }
        }catch(XmlPullParserException e){ }
    }

    private void readBundle(XmlPullParser parser) throws XmlPullParserException, IOException {
        FilterList<Circolare> bundle = new FilterList<>();
        boolean firstOfBundle = true;
        int current = parser.next();
        try {
            while (current != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (name.equals("a")) {
                        if(firstOfBundle){
                            bundle.add(readCircolareBetter(parser, ""));
                            firstOfBundle = false;
                        }
                        else{
                            bundle.add(readCircolareBetter(parser, ((Circolare)bundle.get(0)).titolo.substring(0, 22)));
                        }
                    }
                }
                try{
                    current = parser.next();
                }catch(XmlPullParserException e){
                }

            }
        }catch(XmlPullParserException e){ }
        items.add(bundle);
    }

    private Object readFirst(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("a")) {
                    return readCircolareBetter(parser, "");
                }
            }
        }catch(XmlPullParserException e){ }
        return null;
    }

    private Circolare readCircolare(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "a");
        String link = parser.getAttributeValue(null, "href");
        String title = readText(parser);
        return new Circolare(title, link);
    }

    private Circolare readCircolareBetter(XmlPullParser parser, String contesto) throws XmlPullParserException, IOException {
        //lettura di tutto ciò che è dentro a <a>
        parser.require(XmlPullParser.START_TAG, ns, "a");
        String link = parser.getAttributeValue(null, "href");
        String title = contesto;
        if (!contesto.equals("")) title = title + " ";
        try {
            while (parser.getEventType() != XmlPullParser.END_TAG || !parser.getName().equals("a")) {
                if (parser.getEventType() == XmlPullParser.TEXT) title = title + parser.getText();
                parser.next();
            }
        }catch(XmlPullParserException e){
        }
        return new Circolare(title, link);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        } else {
            if (parser.getName().equals("em")) {
                return readText(parser);
            } else {
                if (parser.getName().equals("i")) {
                    return readText(parser);
                }
            }
        }
        return result;
    }

    private void skipToContent(BufferedReader br){
        try{
            String line = br.readLine();
            //while(!line.contains("id=\"contenuto\"")){
            while(!line.contains("class=\"page-wrapper\"")){
                line = br.readLine();
            }
        }
        catch(IOException e){
             
        }
    }
}
