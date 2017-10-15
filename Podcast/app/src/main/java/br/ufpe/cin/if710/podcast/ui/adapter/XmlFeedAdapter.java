package br.ufpe.cin.if710.podcast.ui.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.io.PodcastProviderHelper;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;
    Context context;
    //ViewHolder holder;

    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        this.linkResource = resource;
        this.context = context;
    }

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button item_action;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);

            holder = new ViewHolder();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
        holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
        holder.item_action = (Button) convertView.findViewById(R.id.item_action);

        final ItemFeed itemFeed = getItem(position);
        holder.item_title.setText(itemFeed.getTitle());
        holder.item_date.setText(itemFeed.getPubDate());

        if (itemFeed.getDownloadUri().equals("NONE")) {
            holder.item_action.setEnabled(itemFeed.getDownloadID() == 0);
            holder.item_action.setText("BAIXAR");
        } else {
            holder.item_action.setEnabled(true);
            holder.item_action.setText("TOCAR");
        }

        holder.item_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemFeed.getDownloadUri().equals("NONE")) {
                    Toast.makeText(getContext(),"Baixando podcast", Toast.LENGTH_SHORT).show();
                    // solicita o download manager do sistema para fazer o download do podcast
                    DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    // coloca o download numa fila e recupera seu ID para recuperar o arquivo posteriormente
                    long downloadID = downloadManager.enqueue(new DownloadManager.Request(Uri.parse(itemFeed.getDownloadLink())));
                    // salva o ID no BD
                    PodcastProviderHelper.updateDownloadID(getContext(), itemFeed.getId(), downloadID);

                    holder.item_action.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Tocando podcast", Toast.LENGTH_SHORT).show();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EpisodeDetailActivity.class);
                intent.putExtra(EpisodeDetailActivity.ITEM_FEED, getItem(position));
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}