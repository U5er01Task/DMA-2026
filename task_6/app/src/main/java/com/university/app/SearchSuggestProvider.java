package com.university.app;

import android.app.SearchManager;
import android.content.*;
import android.database.*;
import android.net.Uri;

public final class SearchSuggestProvider extends ContentProvider {
  public static final String AUTHORITY = "com.university.app.suggest";

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(
      Uri uri, String[] projection, String selection, String[] arguments, String sort) {
    String q = arguments != null && arguments.length > 0 ? arguments[0] : uri.getLastPathSegment();
    if (q == null || q.equals(SearchManager.SUGGEST_URI_PATH_QUERY)) q = "";
    String[] columns = {
      "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };
    MatrixCursor out = new MatrixCursor(columns);
    try (Cursor c =
        getContext()
            .getContentResolver()
            .query(
                NotesProvider.URI,
                new String[] {"_id", "title"},
                "category=? AND title LIKE ?",
                new String[] {"public_search", "%" + q + "%"},
                "_id DESC")) {
      if (c != null) {
        int n = 0;
        while (c.moveToNext() && n++ < 10) {
          long id = c.getLong(0);
          out.addRow(
              new Object[] {
                id, c.getString(1), ContentUris.withAppendedId(NotesProvider.URI, id).toString()
              });
        }
      }
    }
    return out;
  }

  @Override
  public String getType(Uri u) {
    return SearchManager.SUGGEST_MIME_TYPE;
  }

  @Override
  public Uri insert(Uri u, ContentValues v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri u, ContentValues v, String s, String[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri u, String s, String[] a) {
    throw new UnsupportedOperationException();
  }
}
