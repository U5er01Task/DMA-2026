package com.university.app;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public final class StreamProvider extends ContentProvider {
  public static final Uri URI = Uri.parse("content://com.university.app.stream/data");

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public ParcelFileDescriptor openFile(Uri u, String mode) throws java.io.FileNotFoundException {
    if (!"r".equals(mode) || !"/data".equals(u.getPath()))
      throw new java.io.FileNotFoundException();
    try {
      ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
      Intent i = new Intent(getContext(), PipeWriterService.class).putExtra("output", pipe[1]);
      getContext().startService(i);
      pipe[1].close();
      return pipe[0];
    } catch (Exception e) {
      throw new java.io.FileNotFoundException(e.toString());
    }
  }

  @Override
  public String getType(Uri u) {
    return "text/plain";
  }

  @Override
  public Cursor query(Uri u, String[] p, String s, String[] a, String order) {
    return null;
  }

  @Override
  public Uri insert(Uri u, ContentValues v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri u, String s, String[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri u, ContentValues v, String s, String[] a) {
    throw new UnsupportedOperationException();
  }
}
