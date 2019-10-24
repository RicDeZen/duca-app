package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class LinkTask extends AsyncTask<String, Integer, Void>{
    private Context c;
    private String home_url;
    private String circ_url = "";
    private String orari_url = "";
    private String orari_docenti = "";
    private String orari_classi = "";
    private String data_path;
    private static final String ns = null;
    private boolean error = false;

    public LinkTask(Context c){
        super();
        this.c = c;
    }
    @Override
    protected Void doInBackground(String... strings) {
        //strings[0] homepage url
        //strings[1] path directory principale
        try{
            if(strings.length < 2) return null;
            home_url = strings[0];
            data_path = strings[1];
            if(!DownloadFile(home_url, "/home_file.htm")) return null;
            //apro il file della home e cerco il link che contiene "circolari" nel proprio title
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new FileInputStream(new File(data_path+"/home_file.htm")), null);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
            parser.defineEntityReplacementText("ntilde", "\u00F1");
            parser.defineEntityReplacementText("nbsp", " ");
            parser.defineEntityReplacementText("raquo", " ");
            parser.nextTag();
            while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if(parser.getEventType() != XmlPullParser.START_TAG) {
                    try{parser.next();}
                    catch(XmlPullParserException e){ }
                    continue;
                }
                String name = parser.getName();
                String title = parser.getAttributeValue(ns, "title");
                if (name.equals("a") && title != null && title.toLowerCase().contains("circolari")) {
                    circ_url = parser.getAttributeValue(ns, "href");
                    break;
                }
                else{
                    try{
                        parser.next();
                    }catch(XmlPullParserException e){
                         
                    }
                }
            }
            //ricerca pagina con orari
            parser.setInput(new FileInputStream(new File(data_path+"/home_file.htm")), null);
            parser.nextTag();
            while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if(parser.getEventType() != XmlPullParser.START_TAG) {
                    try{parser.next();}
                    catch(XmlPullParserException e){ }
                    continue;
                }
                String name = parser.getName();
                String href = parser.getAttributeValue(ns, "href");
                String text;
                if(name.equals("a")){
                    while(parser.getEventType() != XmlPullParser.END_TAG) {
                        try {
                            parser.next();
                        } catch (XmlPullParserException e) { }
                        if (parser.getEventType() == XmlPullParser.TEXT) {
                            text = parser.getText();
                            if (text.toLowerCase().contains("orario scolastico")) orari_url = href;
                        }
                    }
                }
                else{
                    try{
                        parser.next();
                    }catch(XmlPullParserException e){}
                }
            }
            if(!DownloadFile(orari_url, "/orari_file.htm")) return null;
            //ricerca link degli orari
            parser.setInput(new FileInputStream(new File(data_path+"/orari_file.htm")), null);
            parser.nextTag();
            int i = 0;
            while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if(parser.getEventType() != XmlPullParser.START_TAG) {
                    try{parser.next();}
                    catch(XmlPullParserException e){ }
                    continue;
                }
                String name = parser.getName();
                String href = parser.getAttributeValue(ns, "href");
                String text;
                if(name.equals("a")){
                    while(parser.getEventType() != XmlPullParser.END_TAG) {
                        try {
                            parser.next();
                        } catch (XmlPullParserException e) { }
                        if (parser.getEventType() == XmlPullParser.TEXT) {
                            text = parser.getText();
                            if (text.toLowerCase().contains("per classi (")) orari_classi = href;
                            else if (text.toLowerCase().contains("per docenti ("))
                                orari_docenti = href;
                        } else {
                            try {
                                parser.next();
                            } catch (XmlPullParserException e) {
                            }
                        }
                    }
                }
                else{
                    try{
                        parser.next();
                    }catch(XmlPullParserException e){}
                }
            }
        }catch(FileNotFoundException e){   error = true; return null;}
        catch(XmlPullParserException e){   error = true; return null;}
        catch(IOException e){   error = true; return null;}
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pref_url_circolari", circ_url);
        editor.putString("pref_url_orario_classi", orari_classi);
        editor.putString("pref_url_orario_docenti", orari_docenti);
        editor.apply();
        //risultati salvati nelle shared Preferences
        return null;
    }

    public boolean hasError(){return error;}

    private boolean DownloadFile(String s, String name){
        //s = url della pagina
        //name = nome file
        boolean success;
        try{
            new File(data_path).mkdirs();
            URL url = new URL(s);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setConnectTimeout(5000);
            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(data_path+name);
            PrintWriter pw = new PrintWriter(fw);
            String line;
            while((line = br.readLine()) != null){
                pw.println(line);
            }
            fw.close();
            br.close();
            success = true;
        }
        catch(MalformedURLException e){
            return false;
        }
        catch(SocketTimeoutException e){
            return false;
        }
        catch(IOException e){
            return false;
        }
        return success;
    }
}
