package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {

    private PodcastDBHelper dbHelper;

    public PodcastProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedRows = 0;
        // se a uri eh valida, realiza a query recuperando a qtd de linhas deletadas
        if (isEpisodeTableUri(uri)) {
            deletedRows = dbHelper.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE, selection, selectionArgs);
        }
        return deletedRows;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri contentUri = null;
        // se a uri eh valida, insere no BD recuperando o id
        if(isEpisodeTableUri(uri)){
            long id = dbHelper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE, null, values);
            contentUri = Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }
        // retorna a URI de acesso ao item criado
        return contentUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = PodcastDBHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        // se a uri eh valida, realiza a query
        if (isEpisodeTableUri(uri)) {
            cursor = dbHelper.getReadableDatabase().query(
                    PodcastDBHelper.DATABASE_TABLE,
                    projection,
                    selection,
                    selectionArgs,
                    null, null,
                    sortOrder
            );
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updatedRows = 0;
        // se a uri eh valida, realiza a query recuperando a qtd de linhas deletadas
        if (isEpisodeTableUri(uri)) {
            updatedRows = dbHelper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        }
        return updatedRows;
    }

    // Verifica se a uri se refere a tabela de episodios
    private boolean isEpisodeTableUri(Uri uri){
        return uri.getLastPathSegment().equals(PodcastProviderContract.EPISODE_TABLE);
    }
}
