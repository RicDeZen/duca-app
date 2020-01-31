package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.threads.Timeout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity showing detailed info for a certain feed item.
 *
 * @author Riccardo De Zen
 */
public class InfoActivity extends AppCompatActivity {

    private Context context;
    private TextView feed_title;
    private TextView feed_desc;
    private TextView feed_date;
    private ImageButton feed_link;
    private View progressBar;
    private List<LinkedImage> images = new ArrayList<>();
    private List<String> iframes = new ArrayList<>();
    private List<downloadLink> links = new ArrayList<>();
    private TaskQueue<DownloadDrawableTask> queue = new TaskQueue<>();
    private String rawDesc = "";
    private LinearLayout layoutIframes;
    private LinearLayout layoutImages;
    private ImgDialogFragment dial;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing context, toolbar and other data
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Initializing views
        feed_title = (TextView) findViewById(R.id.TextView_info_title);
        feed_desc = (TextView) findViewById(R.id.TextView_info_desc);
        feed_date = (TextView) findViewById(R.id.TextView_info_date);
        feed_link = (ImageButton) findViewById(R.id.ImageButton_link);
        final Intent intent = getIntent();
        if (intent.getBooleanExtra("notif", false))
            new ParentFeedTask().execute(sharedPreferences.getString("pref_url_site", "https" +
                    "://www.liceoduca.edu.it") + "/feed");
        feed_title.setText(intent.getStringExtra("title"));
        feed_desc.setText(intent.getStringExtra("description"));
        feed_date.setText(intent.getStringExtra("date"));

        layoutIframes = (LinearLayout) findViewById(R.id.info_iframes);
        layoutImages = (LinearLayout) findViewById(R.id.info_images);

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        //Initializing bottom button
        feed_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(intent.getStringExtra("link")));
                startActivity(browserIntent);
            }
        });
        htmlToLayout(intent.getStringExtra("description"));
        makeSpans();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item:
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.URL_sito)));
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class downloadLink {
        public final String link;
        public final int start_span;
        public final int end_span;

        public downloadLink(String link, int start, int end) {
            this.link = link;
            start_span = start;
            end_span = end;
        }
    }

    private class MyClickableSpan extends ClickableSpan {
        private final String myLink;

        private MyClickableSpan(final String link) {
            myLink = link;
        }

        @Override
        public void onClick(final View widget) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(myLink));
            startActivity(browserIntent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        }
    }

    private class TaskQueue<T extends AsyncTask<String, Integer, Void>> {
        List<T> l;
        List<String[]> s;

        public TaskQueue() {
            l = new ArrayList<>();
            s = new ArrayList<>();
        }

        public void enqueue(T t, String... args) {
            l.add(t);
            s.add(args);
        }

        public void next() {
            if (l.isEmpty()) return;
            else {
                T t = l.get(0);
                String[] args = s.get(0);
                l.remove(0);
                s.remove(0);
                t.execute(args);
            }
        }
    }

    private class LinkedImage {
        public Drawable drawable;
        public String link;
        public boolean linked;
        public int index;

        public LinkedImage(Drawable d) {
            drawable = d;
            link = null;
            linked = false;
        }

        public LinkedImage(Drawable d, String s) {
            drawable = d;
            link = s;
            linked = true;
        }
    }

    private void htmlToLayout(String s) {
        if (null == s || s.equals("")) return;
        String r = "", sLink = "";
        int i = 0, w1 = 0, w2 = 0;
        boolean link = false;
        boolean img = false;
        char[] b = s.toCharArray();
        while (i < b.length) {
            if (b[i] == '<') {
                i++;
                if (b[i] == 'p') {
                    i = tagEnd(b, i);
                    continue;
                }
                if (b[i] == 'a') {
                    //ho un link
                    link = true;
                    boolean href = false;
                    while (!href) {
                        if (b[i] == 'h') {
                            if (b[i + 1] == 'r') {
                                if (b[i + 2] == 'e') {
                                    if (b[i + 3] == 'f') {
                                        href = true;
                                        continue;
                                    }
                                }
                            }
                        }
                        i++;
                    }
                    //i è su 'h'
                    i += 6;
                    while (b[i] != '\"') {
                        sLink += b[i];
                        i++;
                    }
                    //ok ho il link e chiudo <a>
                    i = tagEnd(b, i);
                    continue;
                }
                if (b[i] == 'i') {
                    //ho una immagine oppure un iframe?
                    if (b[i + 1] == 'm') {
                        if (b[i + 2] == 'g') {
                            img = true;
                            i = readImg(b, i, sLink);
                            sLink = "";
                            link = false;
                            continue;
                        }
                    }
                    if (b[i + 1] == 'f') {
                        if (b[i + 2] == 'r') {
                            if (b[i + 3] == 'a') {
                                if (b[i + 4] == 'm') {
                                    if (b[i + 5] == 'e') {
                                        i = readIframe(b, i);
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
                if (b[i] == '/') {
                    if (b[i + 1] == 'p') r += "\n";
                    i = tagEnd(b, i);
                    continue;
                }
                if (b[i] == 'b' && b[i + 1] == 'r') {
                    r += "\n";
                    i = tagEnd(b, i);
                    continue;
                }
                i = tagEnd(b, i);
                continue;
            }
            //ciò che è arrivato qui sta fuori dai tag
            String temp = "";
            while (i < b.length && b[i] != '<') {
                temp += b[i];
                i++;
            }
            r += Html.fromHtml(temp);
            if (isSkippable(b, i)) continue;
            //devo usare w1 come indice iniziale e w2 come finale e mettere uno span o qualcosa
            w2 = r.length();
            if (link && !img) links.add(new downloadLink(sLink, w1, w2));
            //Log.d("link testuale", sLink);
            sLink = "";
            link = false;
            img = false;
            w1 = w2;
        }
        rawDesc = r.trim();
        queue.next();
    }

    private int readImg(char[] b, int i, String link) {
        StringBuilder sSrc = new StringBuilder();
        //String sSrc = "";
        boolean src = false;
        while (!src) {
            if (b[i] == 's') {
                if (b[i + 1] == 'r') {
                    if (b[i + 2] == 'c') {
                        src = true;
                        continue;
                    }
                }
            }
            i++;
        }
        //sono sulla 's'
        i += 5;
        while (b[i] != '\"') {
            sSrc.append(b[i]);
            i++;
        }
        //Log.d("InfoActivity", "Immagine da "+sSrc+" con link "+link);
        String[] strings;
        if (!(link.trim()).equals("")) {
            strings = new String[2];
            strings[0] = sSrc.toString();
            strings[1] = link;
        } else {
            strings = new String[1];
            strings[0] = sSrc.toString();
        }
        queue.enqueue(new DownloadDrawableTask(), strings);
        return tagEnd(b, i);
    }

    private int readIframe(char[] b, int i) {
        String htmlText = "<html><body><";
        while (b[i] != '>') {
            htmlText += b[i];
            i++;
        }
        htmlText += "></iframe></body></html>";
        htmlText = htmlText.replace("height=\"240\"", "height=\"100%\"");
        htmlText = htmlText.replace("width=\"283\"", "width=\"100%\"");
        iframes.add(htmlText);
        addIframe();
        return tagEnd(b, i);
    }

    private boolean isSkippable(char[] b, int i) {
        if (i >= b.length || b[i] != '<') return false;
        String s = "";
        i++;
        while (b[i] != '>') {
            s += b[i];
            i++;
        }
        s = s.replaceAll("\\s", "");
        if (s.equals("i") || s.equals("/i")) return true;
        if (s.equals("b") || s.equals("/b")) return true;
        if (s.equals("u") || s.equals("/u")) return true;
        if (s.equals("em") || s.equals("/em")) return true;
        if (s.equals("strong") || s.equals("/strong")) return true;
        return false;
    }

    private int tagEnd(char[] b, int i) {
        while (b[i] != '>') i++;
        i++;
        return i;
    }

    private void makeSpans() {
        SpannableString ss = new SpannableString(rawDesc);
        for (int j = 0; j < links.size(); j++) {
            downloadLink currentItem = (downloadLink) links.get(j);
            ss.setSpan(new MyClickableSpan(currentItem.link), currentItem.start_span,
                    currentItem.end_span, 0);
        }
        feed_desc.setMovementMethod(LinkMovementMethod.getInstance());
        feed_desc.setText(ss, TextView.BufferType.SPANNABLE);
    }

    private class DownloadDrawableTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                InputStream is = (InputStream) new URL(strings[0]).getContent();
                Drawable d = Drawable.createFromStream(is, "src_name");
                if (strings.length > 1) {
                    images.add(new LinkedImage(d, strings[1]));
                    //Log.e("Immagine", "SI' link");
                } else images.add(new LinkedImage(d));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            //listViewImages.setAdapter(new CustomAdapterImages());
            addImage();
            queue.next();
        }
    }

    private class DownloadDialogDrawableTask extends AsyncTask<String, Integer, Void> {
        Drawable d;
        int i;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                InputStream is = (InputStream) new URL(strings[0]).getContent();
                d = Drawable.createFromStream(is, "src_name");
                i = Integer.parseInt(strings[1]);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ImageView img;
            try {
                img = (ImageView) dial.getDialog().findViewById(R.id.imageView_dialog);
            } catch (NullPointerException e) {
                return;
            }
            img.setImageDrawable((d != null) ? d : images.get(i).drawable);
            dial.getProgressBar().setVisibility(View.GONE);
        }
    }

    private void addImage() {
        final LinkedImage image = images.get(images.size() - 1);
        final int index = images.size() - 1;
        View convertView = getLayoutInflater().inflate(R.layout.info_image_item, null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.info_imageview);
        imageView.setImageDrawable(image.drawable);
        if (Build.VERSION.SDK_INT >= 21) imageView.setClipToOutline(true);
        if (image.linked) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(image.link));
                    //startActivity(browserIntent);
                    dial = new ImgDialogFragment();
                    dial.show(getSupportFragmentManager(), "ImageZoom");
                    DownloadDialogDrawableTask dialogTask = new DownloadDialogDrawableTask();
                    dialogTask.execute(images.get(index).link, "" + index);
                }
            });
        }
        layoutImages.addView(convertView);
    }

    private void addIframe() {
        View convertView = getLayoutInflater().inflate(R.layout.info_iframe_item, null);
        WebView webView = (WebView) convertView.findViewById(R.id.info_webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadData(iframes.get(iframes.size() - 1), "text/html", "utf-8");
        layoutIframes.addView(convertView);
    }

    private class ParentFeedTask extends AsyncTask<String, Integer, Void> {
        private File output_file;
        boolean success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            output_file = new File(getApplicationContext().getFilesDir() + "/DucaApp/feed_file" +
                    ".xml");
            FileWriter fw;
            PrintWriter pw;
            URL url;
            try {
                new Timeout(this, 5000).start();
                url = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(2000);
                InputStream is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                fw = new FileWriter(output_file);
                pw = new PrintWriter(fw);
                String line;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
                fw.close();
                br.close();
                pw.close();
                is.close();
                success = true;
            } catch (MalformedURLException e) {
                //e.printStackTrace();
            } catch (SocketTimeoutException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public static class ImgDialogFragment extends DialogFragment {
        View progressBar;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_image, null);
            progressBar = view.findViewById(R.id.progressBar_dialog);
            builder.setView(view);
            return builder.create();
        }

        public View getProgressBar() {
            return progressBar;
        }
    }
}