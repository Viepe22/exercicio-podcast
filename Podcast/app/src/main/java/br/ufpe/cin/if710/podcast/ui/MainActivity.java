package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.receiver.DownloadReceiver;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast

    private ListView items;

    MyDownloadReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // update list
        new ProviderTask().execute();
        new DownloadXmlTask().execute(RSS_FEED);

        receiver = new MyDownloadReceiver();
        receiver.disableNotifications();
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    @Override
    protected void onResume() {
        Log.d("ON_RESUME", "Resuming MainActivity.");
        super.onResume();

        receiver.disableNotifications();
    }

    @Override
    protected void onPause() {
        Log.d("ON_PAUSE", "Pausing MainActivity.");
        receiver.enableNotifications();

        super.onPause();
    }

    public class MyDownloadReceiver extends DownloadReceiver {

        public static final String NOTIFICATION_TITLE = "Download complete!";
        public static final String NOTIFICATION_TEXT = "Your Podcast download was successfully completed!";

        public static final int NOTIFICATION_ID = 1;

        private int showNotification;

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            List<ItemFeed> list = readDB();
            updatePodcastItems(list);
            //new ProviderTask().execute();

            if (showNotification == 1) {
                /*Intent activityIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);

                // define a notificacao
                Notification.Builder notification = new Notification.Builder(context)
                        .setContentTitle(NOTIFICATION_TITLE)
                        .setContentText(NOTIFICATION_TEXT)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                // envia a notificacao para o notification manager
                notificationManager.notify(NOTIFICATION_ID, notification.build());*/
            } else {
                Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show();
            }

        }

        public void enableNotifications() {
            showNotification = 1;
        }

        public void disableNotifications() {
            showNotification = 0;
        }
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));
                for (ItemFeed itemFeed : itemList) {
                    ContentValues values = new ContentValues();

                    values.put(PodcastProviderContract.DATE, getValidString(itemFeed.getPubDate()));
                    values.put(PodcastProviderContract.DESCRIPTION, getValidString(itemFeed.getDescription()));
                    values.put(PodcastProviderContract.DOWNLOAD_LINK, getValidString(itemFeed.getDownloadLink()));
                    values.put(PodcastProviderContract.EPISODE_LINK, getValidString(itemFeed.getLink()));
                    values.put(PodcastProviderContract.TITLE, getValidString(itemFeed.getTitle()));
                    values.put(PodcastProviderContract.EPISODE_URI, "NONE");
                    values.put(PodcastProviderContract.EPISODE_DOWNLOAD_ID, 0);

                    Uri uri = getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, values);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            //Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            //atualizar o list view
            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
        }

        private String getValidString(String str) {
            return str != null ? str : "";
        }
    }

    private class ProviderTask extends AsyncTask<Void, Void, List<ItemFeed>> {
        @Override
        protected List<ItemFeed> doInBackground(Void... params) {
            return readDB();
        }
        @Override
        protected void onPostExecute(List<ItemFeed> itemFeeds) {
            updatePodcastItems(itemFeeds);
        }
    }

    private List<ItemFeed> readDB() {
        List<ItemFeed> list = new ArrayList<>();
        Cursor c = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI,null,null,null,null);
        if (c != null) {
            c.moveToFirst();
            while (c.moveToNext()) {
                ItemFeed itemFeed = new ItemFeed(
                        c.getInt(c.getColumnIndex(PodcastProviderContract._ID)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.TITLE)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_LINK)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.DATE)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.DESCRIPTION)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.DOWNLOAD_LINK)),
                        c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_URI)),
                        c.getLong(c.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_ID))
                );
                list.add(itemFeed);
            }
            c.close();
        }
        return list;
    }
    public void updatePodcastItems(List<ItemFeed> feed) {
        //Adapter Personalizado
        XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

        items.setAdapter(adapter);
        items.setTextFilterEnabled(true);

        Log.d("LIST_FROM_DB", "List Updated");
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}
