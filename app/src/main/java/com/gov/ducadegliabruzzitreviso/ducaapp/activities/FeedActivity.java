package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.FeedItem;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.FilterList;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.MyUtils;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.XMLFeedParser;

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

public class FeedActivity extends AppCompatActivity {
    private FilterList<FeedItem> ITEMS = new FilterList<>();
    String data_path;
    Context context;
    SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(NullPointerException e){
            int why = 0;
            //soffro ma non posso farci nulla;
        }
        context = this.getApplicationContext();
        data_path = context.getFilesDir() + "/DucaApp";
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe1);
        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.pureRed, R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(MyUtils.checkNetwork(context))new FeedTask().execute(getString(R.string.URL_feed));
                else refreshLayout.setRefreshing(false);
            }
        });

        XMLloader myTask = new XMLloader();
        myTask.execute(data_path + "/feed_file.xml");
        if(MyUtils.checkNetwork(context)) new FeedTask().execute(getString(R.string.URL_feed));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_feed);
        MyRecyclerAdapter adapter = new MyRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.URL_sito)));
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
            default: return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>{
        public class MyViewHolder extends RecyclerView.ViewHolder{
            public View thisView;
            public FeedItem thisItem;
            public MyViewHolder(View convertView){
                super(convertView);
                thisView = convertView.findViewById(R.id.feed_item_container);
            }
            public void bind(FeedItem item){
                thisItem = item;
                TextView textView_title = (TextView)thisView.findViewById(R.id.feed_title);
                TextView textView_description = (TextView)thisView.findViewById(R.id.feed_description);
                TextView textView_date = (TextView)thisView.findViewById(R.id.feed_date);
                textView_title.setText(thisItem.title);
                if(Build.VERSION.SDK_INT >= 24){
                    textView_description.setText(Html.fromHtml(thisItem.description, Html.FROM_HTML_MODE_LEGACY).toString());
                }
                else{
                    textView_description.setText(Html.fromHtml(thisItem.description).toString());
                }
                textView_date.setText(thisItem.date);
            }
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View convertView = getLayoutInflater().inflate(R.layout.feed_list_item, null);
            return new MyViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
            FeedItem feedItem = ITEMS.get(i);
            myViewHolder.bind(feedItem);
            myViewHolder.thisView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //myViewHolder.thisItem.flip();
                    //notifyItemChanged(i);
                    Intent intent = new Intent(context, InfoActivity.class);
                    intent.putExtra("title", myViewHolder.thisItem.title);
                    intent.putExtra("description", myViewHolder.thisItem.description);
                    intent.putExtra("date", myViewHolder.thisItem.date);
                    intent.putExtra("link", myViewHolder.thisItem.link);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() { return ITEMS.size(); }
    }

    private class FeedTask extends AsyncTask<String, Integer, Void>{
        private File output_file;
        boolean success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            output_file = new File(data_path + "/feed_file.xml");
            FileWriter fw;
            PrintWriter pw;
            URL url;
            try{
                url = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(5000);
                InputStream is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                fw = new FileWriter(output_file);
                pw = new PrintWriter(fw);
                String line;
                while((line = br.readLine()) != null){
                    pw.println(line);
                }
                fw.close();
                br.close();
                pw.close();
                is.close();
                success = true;
            }
            catch(MalformedURLException e){
                //e.printStackTrace();
                success = false;
                return null;
            }
            catch(SocketTimeoutException e){
                //e.printStackTrace();
                success = false;
                return null;
            }
            catch(IOException e){
                //e.printStackTrace();
                success = false;
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!success) Toast.makeText(context, "Errore", Toast.LENGTH_SHORT).show();
            new XMLloader().execute(data_path + "/feed_file.xml");
        }
    }

    private class XMLloader extends AsyncTask<String, Integer, Void>{
        XMLFeedParser parser = new XMLFeedParser();
        InputStream is_to_parse = null;
        FilterList<FeedItem> result = null;
        @Override
        protected Void doInBackground(String... strings) {
            File source;
            try {
                source = new File(strings[0]);
                is_to_parse = new FileInputStream(source);
            }
            catch(Exception e){return null;}
            //se non esiste il file non è mai stata avviata l'attività con internet quindi non ho dati
            try{
                result = parser.parse(is_to_parse);
            }catch(Exception e){
                //e.printStackTrace();
                return null;
            }
            if(result != null && result.size() > 0) ITEMS = result;
            try{
                is_to_parse.close();
            }catch(IOException e){
                //e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }
    }
}