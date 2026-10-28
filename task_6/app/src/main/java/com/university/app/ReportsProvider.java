package com.university.app;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ReportsProvider extends ContentProvider {
  public static final String AUTHORITY = "com.university.app.reports";
  public static final Uri BASE = Uri.parse("content://" + AUTHORITY + "/report");

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public String getType(Uri uri) {
    String fmt = uri.getQueryParameter("format");
    return "csv".equals(fmt) ? "text/csv" : "application/pdf";
  }

  @Override
  public String[] getStreamTypes(Uri uri, String filter) {
    if (!"report".equals(uri.getLastPathSegment())) return null;
    String[] supported = {"application/pdf", "text/csv"};
    if (filter == null || "*/*".equals(filter)) return supported;
    java.util.ArrayList<String> out = new java.util.ArrayList<>();
    for (String mime : supported)
      if (android.content.ClipDescription.compareMimeTypes(mime, filter)) out.add(mime);
    return out.toArray(new String[0]);
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    if (!"r".equals(mode)) throw new FileNotFoundException("read only");
    if (!"report".equals(uri.getPathSegments().isEmpty() ? "" : uri.getPathSegments().get(0)))
      throw new FileNotFoundException("Unknown URI");
    String type = uri.getQueryParameter("format");
    boolean csv = "csv".equals(type);
    File f = new File(getContext().getCacheDir(), "report." + (csv ? "csv" : "pdf"));
    try (FileOutputStream out = new FileOutputStream(f)) {
      out.write(
          csv
              ? "id,value\n1,Android80\n".getBytes(StandardCharsets.UTF_8)
              : TaskActions.verySmallPdf());
    } catch (Exception e) {
      throw new FileNotFoundException(e.toString());
    }
    Log.i("A80_28", "opened: " + f);
    return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
  }

  @Override
  public Cursor query(Uri u, String[] p, String s, String[] a, String so) {
    return null;
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
