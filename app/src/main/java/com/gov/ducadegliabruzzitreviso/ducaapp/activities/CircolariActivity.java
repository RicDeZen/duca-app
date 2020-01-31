package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.Circolare;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.CircolariParser;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.FilterArrayList;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.MyUtils;
import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

public class CircolariActivity extends AppCompatActivity {

    private String data_path;
    private Context context;
    private FilterArrayList<Filterable> ITEMS = new FilterArrayList<>();
    private FilterArrayList<Filterable> BACKUP = new FilterArrayList<>();
    private SwipeRefreshLayout refreshLayout;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private boolean searching = false;
    private boolean canProceed = true;
    private String url_circolari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing the needed data.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circolari);
        context = this.getApplicationContext();
        data_path = context.getFilesDir() + "/DucaApp";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        url_circolari = sharedPreferences.getString("pref_url_circolari", "");

        //Initializing Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_circolari);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe2);
        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.pureRed,
                R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchView.setQuery("", true);
                if (MyUtils.checkNetwork(context) && canProceed)
                    new CircolariTask().execute(url_circolari);
                else refreshLayout.setRefreshing(false);
            }
        });

        //Initializing search bar
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Cerca");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDocuments(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    filterDocuments(newText);
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                filterDocuments("");
                return true;
            }
        });

        //Initializing list
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_circolari);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyRecyclerAdapter());

        //Updating the documents
        update();
    }

    /**
     * Method starting a task to update the document list.
     */
    private void update() {
        CircolariLoader myTask = new CircolariLoader();
        try {
            myTask.execute(data_path + "/circolari.htm");
        } catch (IndexOutOfBoundsException e) {
            canProceed = false;
        }
        if (MyUtils.checkNetwork(context) && canProceed) new CircolariTask().execute(url_circolari);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        menu.getItem(0).setIcon(R.drawable.search_icon);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Method defining menu actions.
     *
     * @param item The clicked menu item.
     * @return true if the action was handled, false if it was not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item:
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.URL_sito)));
                startActivity(intent);
                return true;
            case R.id.menu_search:
                if (!searching) {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.requestFocus();
                    item.setIcon(R.drawable.x_icon);
                    searching = true;
                } else {
                    searchView.setQuery("", false);
                    searchView.setVisibility(View.GONE);
                    item.setIcon(R.drawable.search_icon);
                    CircolariLoader c = new CircolariLoader();
                    c.execute(data_path + "/circolari.htm");
                    searching = false;
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Class defining a background task to download the documents web page to a file.
     */
    private class CircolariTask extends AsyncTask<String, Integer, Void> {
        private File output_file;
        boolean success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            output_file = new File(data_path + "/circolari.htm");
            FileWriter fw;
            PrintWriter pw;
            URL url;
            try {
                url = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
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
                success = true;
            } catch (MalformedURLException e) {
                //e.printStackTrace();
                success = false;
            } catch (SocketTimeoutException e) {
                //e.printStackTrace();
                success = false;
            } catch (IOException e) {
                //e.printStackTrace();
                success = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (success) new CircolariLoader().execute(output_file.getAbsolutePath());
        }
    }

    /**
     * Class defining a background task to load the documents into a list
     */
    private class CircolariLoader extends AsyncTask<String, Integer, Void> {
        CircolariParser parser = new CircolariParser();
        InputStream is_to_parse = null;
        FilterArrayList<Filterable> result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(String... strings) {
            File source;
            try {
                source = new File(strings[0]);
                is_to_parse = new FileInputStream(source);
            } catch (Exception e) {
                return null;
            }
            //se il file non è presente l'app non è mai stata avviata con internet quindi non ho
            // dati
            try {
                result = parser.parseByBundle(is_to_parse);
            } catch (Exception e) {
                //e.printStackTrace();
                return null;
            }
            if (result != null && result.size() > 0) {
                ITEMS = result;
                BACKUP = ITEMS;
            }
            try {
                is_to_parse.close();
            } catch (IOException e) {
                //Ok but why would it get thrown tho
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Class defining the list Adapter for non filtered documents.
     */
    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout thisView;

            public MyViewHolder(View convertView) {
                super(convertView);
                thisView = (LinearLayout) convertView.findViewById(R.id.linear_layout_circolari);
            }

            public void clear() {
                thisView.removeAllViews();
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View convertView =
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circolari_intermedio, viewGroup, false);
            return new MyViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            List bundle = (List) ITEMS.get(i);
            LinearLayout l = myViewHolder.thisView;
            for (int j = 0; j < bundle.size(); j++) {
                View item = getLayoutInflater().inflate(R.layout.circolari_item, null);
                final Circolare c = (Circolare) bundle.get(j);
                TextView textView = (TextView) item.findViewById(R.id.TextView_circolare);
                textView.setText(c.titolo);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(c.indirizzo));
                        startActivity(intent);
                    }
                });
                l.addView(item);
            }
        }

        @Override
        public void onViewRecycled(@NonNull MyViewHolder holder) {
            holder.clear();
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return ITEMS.size();
        }
    }

    /**
     * Class defining the adapter for filtered documents.
     */
    public class MyFilterAdapter extends RecyclerView.Adapter<MyFilterAdapter.MyFilterHolder> {
        public class MyFilterHolder extends RecyclerView.ViewHolder {
            public LinearLayout thisView;

            public MyFilterHolder(View convertView) {
                super(convertView);
                thisView = (LinearLayout) convertView.findViewById(R.id.linear_layout_circolari);
            }

            public void clear() {
                thisView.removeAllViews();
            }
        }

        @NonNull
        @Override
        public MyFilterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View convertView =
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circolari_intermedio, viewGroup, false);
            return new MyFilterHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyFilterHolder myViewHolder, final int i) {
            List bundle = (List) ITEMS.get(i);
            LinearLayout l = myViewHolder.thisView;
            for (int j = 0; j < bundle.size(); j++) {
                View item = getLayoutInflater().inflate(R.layout.circolari_item_filter, null);
                final Circolare c = (Circolare) bundle.get(j);
                TextView textView = (TextView) item.findViewById(R.id.TextView_circolare);
                SpannableString ss = span(c.titolo, searchView.getQuery().toString());
                textView.setText(ss, TextView.BufferType.SPANNABLE);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(c.indirizzo));
                        startActivity(intent);
                    }
                });
                l.addView(item);
            }
        }

        @Override
        public void onViewRecycled(@NonNull MyFilterHolder holder) {
            holder.clear();
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return ITEMS.size();
        }
    }

    /**
     * Method to filter documents.
     *
     * @param query The String for which to filter.
     */
    private void filterDocuments(String query) {
        refreshLayout.setRefreshing(true);
        ITEMS = BACKUP.filter(query);
        recyclerView.setVisibility(View.GONE);
        if (query.equals("")) recyclerView.setAdapter(new MyRecyclerAdapter());
        else recyclerView.setAdapter(new MyFilterAdapter());
        recyclerView.setVisibility(View.VISIBLE);
        refreshLayout.setRefreshing(false);
    }

    /**
     * Method to mark a query string in a target string.
     *
     * @param s     The string on which the mark should be applied.
     * @param query The query to mark on the string.
     * @return The String, with spans wherever the query string is found.
     */
    private SpannableString span(String s, String query) {
        SpannableString ss = new SpannableString(s);
        s = s.toLowerCase();
        query = query.toLowerCase();
        int start_index = 0;
        int end_index = 0;
        boolean found = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == query.charAt(0)) {
                start_index = i;
                for (int j = 0; j < query.length() && j + i < s.length(); j++) {
                    if (s.charAt(i + j) != query.charAt(j)) break;
                    if (j == query.length() - 1) {
                        end_index = i + j + 1;
                        found = true;
                    }
                }
            }
            if (found) break;
        }
        if (found)
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                    start_index, end_index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }
}