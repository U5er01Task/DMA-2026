package com.university.app;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.io.File;

public final class StorageCleanupWorker extends Worker {
  public StorageCleanupWorker(@NonNull Context c, @NonNull WorkerParameters p) {
    super(c, p);
  }

  @NonNull
  public Result doWork() {
    File dir = new File(getApplicationContext().getCacheDir(), "sharing");
    int deleted = 0;
    File[] files = dir.listFiles();
    if (files != null)
      for (File f : files)
        if (f.isFile()
            && (f.getName().endsWith(".png") || f.getName().endsWith(".jpg"))
            && f.delete()) deleted++;
    Log.i("A80_14", "storage low temp images removed=" + deleted);
    return Result.success();
  }
}
