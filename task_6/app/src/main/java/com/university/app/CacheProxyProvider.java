package com.university.app;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.util.Log;

public final class CacheProxyProvider extends ContentProvider {
  public static final Uri URI = Uri.parse("content://com.university.app.proxy/items");

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri u, String[] projection, String s, String[] args, String order) {
    if (!"/items".equals(u.getPath())) throw new IllegalArgumentException(u.toString());
    Cursor c =
        getContext()
            .getContentResolver()
            .query(NotesProvider.URI, projection, "category=?", new String[] {"proxy"}, order);
    if (c != null && c.getCount() == 0) {
      try {
        getContext().startService(new Intent(getContext(), CacheFetchService.class));
      } catch (IllegalStateException e) {
        Log.w("A80_33", "background start restricted", e);
      }
    }
    if (c != null) c.setNotificationUri(getContext().getContentResolver(), URI);
    return c;
  }

  @Override
  public String getType(Uri u) {
    return "vnd.android.cursor.dir/vnd.com.university.app.cache";
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
