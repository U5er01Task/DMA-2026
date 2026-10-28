package com.university.app;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.io.File;

public final class CleanupWorker extends Worker {
  public CleanupWorker(@NonNull Context c, @NonNull WorkerParameters p) {
    super(c, p);
  }

  @NonNull
  @Override
  public Result doWork() {
    int deleted = 0;
    File[] files = getApplicationContext().getCacheDir().listFiles();
    if (files != null)
      for (File f : files) {
        if (f.isFile() && System.currentTimeMillis() - f.lastModified() > 604800000L && f.delete())
          deleted++;
      }
    Log.i("A80_8", "deleted=" + deleted);
    return Result.success();
  }
}
