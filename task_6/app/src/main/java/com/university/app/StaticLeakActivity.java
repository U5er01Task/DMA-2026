package com.university.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.lang.ref.WeakReference;

public final class StaticLeakActivity extends Activity {
  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    LeakProbeService.held = this;
    LeakProbeService.probe = new WeakReference<>(this);
    startService(new Intent(this, LeakProbeService.class));
    finish();
  }
}
