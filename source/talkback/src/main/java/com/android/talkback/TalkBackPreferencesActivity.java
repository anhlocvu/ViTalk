/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.talkback;

import com.google.android.accessibility.talkback.R;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.accessibility.talkback.HatsSurveyRequester;
import com.google.android.accessibility.talkback.preference.base.TalkBackPreferenceFragment;
import com.google.android.accessibility.talkback.preference.base.TalkbackBaseFragment;
import com.google.android.accessibility.talkback.preference.search.TalkBackSearchIndexablesProvider;
import com.google.android.accessibility.utils.FormFactorUtils;
import com.google.android.accessibility.utils.PackageManagerUtils;
import com.google.android.accessibility.utils.preference.PreferencesActivity;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import com.google.android.accessibility.utils.SharedPreferencesUtils;
import android.provider.Settings;

/**
 * Activity used to set TalkBack's service preferences.
 *
 * <p>Never change preference types. This is because of AndroidManifest.xml setting
 * android:restoreAnyVersion="true", which supports restoring preferences from a new play-store
 * installed talkback onto a clean device with older bundled talkback.
 * REFERTO
 */
public class TalkBackPreferencesActivity extends PreferencesActivity
    implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        FragmentOnAttachListener {

  private static final String TAG = "PreferencesActivity";

  private HatsSurveyRequester hatsSurveyRequester;

  private void assignSearchFragment(Intent intent) {
    if (TalkBackSearchIndexablesProvider.isFromSearchIndexablesContract(intent)) {
      TalkBackSearchIndexablesProvider.assignToFragmentFromSearch(intent);
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Must be called before super.onCreate
    assignSearchFragment(getIntent());
    fillDefaultFragmentNameIfNecessary();
    getSupportFragmentManager().addFragmentOnAttachListener(this);

    String fragmentName = getIntent().getStringExtra(FRAGMENT_NAME);
    boolean isMainSettings = TextUtils.isEmpty(fragmentName);

    if (isMainSettings) {
      super.onCreate(savedInstanceState);
      if (getSupportActionBar() != null) {
        getSupportActionBar().hide();
      }
      setContentView(R.layout.activity_vitalk_preferences_expandable);
      initExpandableSettings();
    } else {
      super.onCreate(savedInstanceState);

      // Check RTL.
      boolean isLocaleRTL =
          TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
      boolean isRTL =
          getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

      if (isLocaleRTL && !isRTL) {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
      }

      // Request the HaTS.
      if (supportHatsSurvey()) {
        hatsSurveyRequester = new HatsSurveyRequester(this);
        hatsSurveyRequester.requestSurvey();

        HatsRequesterViewModel viewModel =
            new ViewModelProvider(this).get(HatsRequesterViewModel.class);
        viewModel.setHatsSurveyRequester(hatsSurveyRequester);
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    getSupportFragmentManager().removeFragmentOnAttachListener(this);
  }

  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    // Fall back to the default implementation in PreferenceFragmentCompat for other form factors.
    if (!FormFactorUtils.isAndroidWear()) {
      return false;
    }

    if (pref.getFragment() == null) {
      return false;
    }
    final Fragment fragment =
        getSupportFragmentManager()
            .getFragmentFactory()
            .instantiate(getClassLoader(), pref.getFragment());

    // WearPreferenceFragment couldn't be restored correctly, so we start a new Activity to
    // prevent the failure of UI restoration.
    if (fragment instanceof TalkbackBaseFragment) {
      Intent intent = new Intent(this, TalkBackSubSettings.class);
      intent.putExtra(FRAGMENT_NAME, pref.getFragment());
      intent.putExtra(FRAGMENT_ARGS, pref.getExtras());
      startActivity(intent);
      return true;
    }
    return false;
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent keyEvent) {
    if ((keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
        && (keyEvent.getAction() == KeyEvent.ACTION_UP)
        && (hatsSurveyRequester != null)
        && (hatsSurveyRequester.handleBackKeyPress())) {
      return false;
    }
    return super.dispatchKeyEvent(keyEvent);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    assignSearchFragment(getIntent());

    String fragmentName = intent.getStringExtra(FRAGMENT_NAME);
    PreferenceFragmentCompat fragment = getFragmentByName(fragmentName);
    fragment.setArguments(intent.getBundleExtra(FRAGMENT_ARGS));
    LogUtils.e(TAG, "onNewIntent/getContainerId()= %s", getContainerId());
    getSupportFragmentManager()
        .beginTransaction()
        .replace(getContainerId(), fragment, getFragmentTag())
        // Add root page to back-history
        .addToBackStack(/* name= */ null)
        .commit();
  }

  @Override
  protected PreferenceFragmentCompat createPreferenceFragment() {
    Intent intent = getIntent();
    String fragmentName = null;
    Bundle bundle = null;
    if (intent != null) {
      fragmentName = intent.getStringExtra(FRAGMENT_NAME);
      bundle = intent.getBundleExtra(FRAGMENT_ARGS);
    }
    PreferenceFragmentCompat fragment = getFragmentByName(fragmentName);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onAttachFragment(FragmentManager fragmentManager, Fragment fragment) {
    if ((fragment instanceof TalkbackBaseFragment)
        && (!(fragment instanceof TalkBackPreferenceFragment))) {
      dismissHatsSurvey();
    }
  }

  private static @Nullable PreferenceFragmentCompat getFragmentByName(String fragmentName) {
    if (TextUtils.isEmpty(fragmentName)) {
      return new TalkBackPreferenceFragment();
    }

    try {
      return (PreferenceFragmentCompat) Class.forName(fragmentName).newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      LogUtils.d(TAG, "Failed to load class: %s", fragmentName);
      return null;
    }
  }

  // We try to fill the default fragment name for WearPreferenceActivity to initiate it for Wear.
  private void fillDefaultFragmentNameIfNecessary() {
    if (!FormFactorUtils.isAndroidWear()) {
      return;
    }
    Intent intent = getIntent();
    if (intent == null) {
      return;
    }
    String fragmentName = intent.getStringExtra(FRAGMENT_NAME);
    if (TextUtils.isEmpty(fragmentName)) {
      intent.putExtra(FRAGMENT_NAME, TalkBackPreferenceFragment.class.getCanonicalName());
    }
  }

  @Override
  protected boolean supportHatsSurvey() {
    // Platform should support Hats if GMS core is available.
    return PackageManagerUtils.hasGmsCorePackage(this);
  }

  /** Dismisses Hats survey. */
  private void dismissHatsSurvey() {
    if (hatsSurveyRequester != null) {
      hatsSurveyRequester.dismissSurvey();
      hatsSurveyRequester = null;
    }
  }

  /**
   * A {@link ViewModel} which encapsulates {@link HatsSurveyRequester} for use in TalkBack setting
   * fragments.
   */
  public static class HatsRequesterViewModel extends ViewModel {
    private HatsSurveyRequester hatsSurveyRequester;

    public HatsSurveyRequester getHatsSurveyRequester() {
      return hatsSurveyRequester;
    }

    public void setHatsSurveyRequester(HatsSurveyRequester hatsSurveyRequester) {
      this.hatsSurveyRequester = hatsSurveyRequester;
    }
  }

  /** Activity to launch TalkBack settings fragment. It is used only for wear. */
  @Override
  protected boolean isDefaultFragmentTransactionHandled() {
    String fragmentName = getIntent().getStringExtra(FRAGMENT_NAME);
    return TextUtils.isEmpty(fragmentName);
  }

  private void initExpandableSettings() {
    // Back Button
    View btnBack = findViewById(R.id.btn_back);
    if (btnBack != null) {
      btnBack.setOnClickListener(v -> finish());
    }

    // Setup Category click listeners
    setupCategory(R.id.header_audio, R.id.container_audio, R.id.divider_audio, R.id.indicator_audio, "Âm thanh và giọng đọc");
    setupCategory(R.id.header_visual, R.id.container_visual, R.id.divider_visual, R.id.indicator_visual, "Hiển thị và tiêu điểm");
    setupCategory(R.id.header_controls, R.id.container_controls, R.id.divider_controls, R.id.indicator_controls, "Cử chỉ và điều khiển");
    setupCategory(R.id.header_typing, R.id.container_typing, R.id.divider_typing, R.id.indicator_typing, "Bàn phím và gõ phím");
    setupCategory(R.id.header_braille, R.id.container_braille, R.id.divider_braille, R.id.indicator_braille, "Chữ nổi Braille");
    setupCategory(R.id.header_advanced, R.id.container_advanced, R.id.divider_advanced, R.id.indicator_advanced, "Cài đặt nâng cao");

    SharedPreferences prefs = SharedPreferencesUtils.getSharedPreferences(this);

    // 1. Speech Volume
    updateSpeechVolumeDesc(prefs);
    View itemSpeechVolume = findViewById(R.id.item_speech_volume);
    if (itemSpeechVolume != null) {
      itemSpeechVolume.setOnClickListener(v -> showSpeechVolumeDialog(prefs));
    }

    // 2. Speech Rate
    updateSpeechRateDesc(prefs);
    View itemSpeechRate = findViewById(R.id.item_speech_rate);
    if (itemSpeechRate != null) {
      itemSpeechRate.setOnClickListener(v -> showSpeechRateDialog(prefs));
    }

    // 3. TTS Settings
    View itemTtsSettings = findViewById(R.id.item_tts_settings);
    if (itemTtsSettings != null) {
      itemTtsSettings.setOnClickListener(v -> {
        try {
          Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        } catch (Exception e) {
          Toast.makeText(this, "Không thể mở cài đặt Giọng đọc hệ thống", Toast.LENGTH_SHORT).show();
        }
      });
    }

    // 4. Verbosity
    View itemVerbosity = findViewById(R.id.item_verbosity);
    if (itemVerbosity != null) {
      itemVerbosity.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.VerbosityPrefFragment");
        startActivity(intent);
      });
    }

    // 5. Image Description
    View itemImageDesc = findViewById(R.id.item_image_description);
    if (itemImageDesc != null) {
      itemImageDesc.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.AutomaticDescriptionsFragment");
        startActivity(intent);
      });
    }

    // 6. Sound & Vibration
    View itemSoundVibration = findViewById(R.id.item_sound_vibration);
    if (itemSoundVibration != null) {
      itemSoundVibration.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.SoundAndVibrationFragment");
        startActivity(intent);
      });
    }

    // 7. Focus Indicator
    View itemFocusIndicator = findViewById(R.id.item_focus_indicator);
    if (itemFocusIndicator != null) {
      itemFocusIndicator.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.FocusIndicatorPrefFragment");
        startActivity(intent);
      });
    }

    // 8. Speech Overlay (Switch)
    SwitchCompat switchOverlay = findViewById(R.id.switch_speech_overlay);
    View itemSpeechOverlay = findViewById(R.id.item_speech_overlay);
    if (switchOverlay != null && itemSpeechOverlay != null) {
      String overlayKey = getString(R.string.pref_tts_overlay_key);
      boolean defaultOverlay = getResources().getBoolean(R.bool.pref_tts_overlay_default);
      boolean isOverlayEnabled = prefs.getBoolean(overlayKey, defaultOverlay);
      switchOverlay.setChecked(isOverlayEnabled);
      
      itemSpeechOverlay.setOnClickListener(v -> {
        boolean newVal = !switchOverlay.isChecked();
        switchOverlay.setChecked(newVal);
        prefs.edit().putBoolean(overlayKey, newVal).apply();
      });
    }

    // 9. Gestures
    View itemGestures = findViewById(R.id.item_gestures);
    if (itemGestures != null) {
      itemGestures.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.TalkBackGestureShortcutPreferenceFragment");
        startActivity(intent);
      });
    }

    // 10. Customize Menus
    View itemCustomizeMenus = findViewById(R.id.item_customize_menus);
    if (itemCustomizeMenus != null) {
      itemCustomizeMenus.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.CustomizeMenusFragment");
        startActivity(intent);
      });
    }

    // 11. Keyboard Shortcuts
    View itemKeyboardShortcuts = findViewById(R.id.item_keyboard_shortcuts);
    if (itemKeyboardShortcuts != null) {
      itemKeyboardShortcuts.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.TalkBackKeyboardShortcutPreferenceFragment");
        startActivity(intent);
      });
    }

    // 12. Gemini Settings
    View itemGeminiSettings = findViewById(R.id.item_gemini_settings);
    if (itemGeminiSettings != null) {
      itemGeminiSettings.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.GeminiSettingsFragment");
        startActivity(intent);
      });
    }

    // 13. Onscreen Keyboard
    View itemOnscreenKeyboard = findViewById(R.id.item_onscreen_keyboard);
    if (itemOnscreenKeyboard != null) {
      itemOnscreenKeyboard.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.OnScreenKeyboardFragment");
        startActivity(intent);
      });
    }

    // 14. Physical Keyboard
    View itemPhysicalKeyboard = findViewById(R.id.item_physical_keyboard);
    if (itemPhysicalKeyboard != null) {
      com.google.android.accessibility.utils.monitor.InputDeviceMonitor inputDeviceMonitor = 
          new com.google.android.accessibility.utils.monitor.InputDeviceMonitor(this);
      if (inputDeviceMonitor.hasPhysicalKeyboard()) {
        itemPhysicalKeyboard.setVisibility(View.VISIBLE);
        itemPhysicalKeyboard.setOnClickListener(v -> {
          try {
            Intent intent = new Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
          } catch (Exception e) {
            Toast.makeText(this, "Không thể mở Cài đặt bàn phím vật lý", Toast.LENGTH_SHORT).show();
          }
        });
      } else {
        itemPhysicalKeyboard.setVisibility(View.GONE);
      }
    }

    // 15. Braille Keyboard
    View itemBrailleKeyboard = findViewById(R.id.item_braille_keyboard);
    if (itemBrailleKeyboard != null) {
      itemBrailleKeyboard.setOnClickListener(v -> {
        try {
          Intent intent = new Intent();
          intent.setClassName(this, "com.google.android.accessibility.brailleime.settings.BrailleImePreferencesActivity");
          startActivity(intent);
        } catch (Exception e) {
          Toast.makeText(this, "Không thể mở cài đặt Bàn phím chữ nổi", Toast.LENGTH_SHORT).show();
        }
      });
    }

    // 16. Braille Display
    View itemBrailleDisplay = findViewById(R.id.item_braille_display);
    if (itemBrailleDisplay != null) {
      itemBrailleDisplay.setOnClickListener(v -> {
        try {
          Intent intent = new Intent();
          intent.setClassName(this, "com.google.android.accessibility.braille.brailledisplay.settings.BrailleDisplaySettingsActivity");
          startActivity(intent);
        } catch (Exception e) {
          Toast.makeText(this, "Không thể mở cài đặt Màn hình chữ nổi", Toast.LENGTH_SHORT).show();
        }
      });
    }

    // 17. Advanced Settings
    View itemAdvancedSettings = findViewById(R.id.item_advanced_settings);
    if (itemAdvancedSettings != null) {
      itemAdvancedSettings.setOnClickListener(v -> {
        Intent intent = new Intent(this, TalkBackSubSettings.class);
        intent.putExtra(FRAGMENT_NAME, "com.google.android.accessibility.talkback.preference.base.AdvancedSettingFragment");
        startActivity(intent);
      });
    }
  }

  private void setupCategory(int headerId, int containerId, int dividerId, int indicatorId, String title) {
    View header = findViewById(headerId);
    View container = findViewById(containerId);
    View divider = findViewById(dividerId);
    ImageView indicator = findViewById(indicatorId);
    if (header == null || container == null || indicator == null) return;

    header.setOnClickListener(v -> {
      boolean isExpanded = container.getVisibility() == View.VISIBLE;
      if (isExpanded) {
        container.setVisibility(View.GONE);
        if (divider != null) divider.setVisibility(View.GONE);
        indicator.setRotation(90);
        header.setContentDescription(title + ". Danh mục. Đã thu gọn.");
      } else {
        container.setVisibility(View.VISIBLE);
        if (divider != null) divider.setVisibility(View.VISIBLE);
        indicator.setRotation(270);
        header.setContentDescription(title + ". Danh mục. Đã mở rộng.");
        header.announceForAccessibility("Đã mở rộng danh mục " + title);
      }
    });
  }

  private void updateSpeechVolumeDesc(SharedPreferences prefs) {
    TextView tvDesc = findViewById(R.id.tv_speech_volume_desc);
    if (tvDesc == null) return;
    String key = getString(R.string.pref_speech_volume_key);
    String defaultVal = getString(R.string.pref_speech_volume_default);
    String val = prefs.getString(key, defaultVal);
    
    String label = "Phù hợp với âm lượng giọng nói";
    if ("75".equals(val)) label = "75% âm lượng giọng nói";
    else if ("50".equals(val)) label = "50% âm lượng giọng nói";
    else if ("25".equals(val)) label = "25% âm lượng giọng nói";
    
    tvDesc.setText("Độ to: " + label + " (Nhấn để thay đổi)");
  }

  private void showSpeechVolumeDialog(SharedPreferences prefs) {
    String key = getString(R.string.pref_speech_volume_key);
    String defaultVal = getString(R.string.pref_speech_volume_default);
    String val = prefs.getString(key, defaultVal);
    
    String[] entries = {
        "Phù hợp với âm lượng giọng nói",
        "75% âm lượng giọng nói",
        "50% âm lượng giọng nói",
        "25% âm lượng giọng nói"
    };
    String[] values = {"100", "75", "50", "25"};
    
    int checkedItem = 0;
    for (int i = 0; i < values.length; i++) {
        if (values[i].equals(val)) {
            checkedItem = i;
            break;
        }
    }
    
    new AlertDialog.Builder(this, R.style.ViTalkMenuTheme)
        .setTitle("Chọn âm lượng giọng đọc")
        .setSingleChoiceItems(entries, checkedItem, (dialog, which) -> {
            prefs.edit().putString(key, values[which]).apply();
            updateSpeechVolumeDesc(prefs);
            dialog.dismiss();
        })
        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
        .show();
  }

  private void updateSpeechRateDesc(SharedPreferences prefs) {
    TextView tvDesc = findViewById(R.id.tv_speech_rate_desc);
    if (tvDesc == null) return;
    String key = getString(R.string.pref_speech_rate_key);
    String defaultVal = getString(R.string.pref_speech_rate_default);
    String val = prefs.getString(key, defaultVal);
    
    String label = val;
    try {
        float rate = Float.parseFloat(val);
        label = Math.round(rate * 100) + "%";
        if (rate == 1.0f) {
            label += " (Bình thường)";
        }
    } catch (Exception ignored) {}
    
    tvDesc.setText("Tốc độ: " + label + " (Nhấn để thay đổi)");
  }

  private void showSpeechRateDialog(SharedPreferences prefs) {
    String key = getString(R.string.pref_speech_rate_key);
    String defaultVal = getString(R.string.pref_speech_rate_default);
    String val = prefs.getString(key, defaultVal);
    
    String[] entries = {"60%", "80%", "100% (Bình thường)", "150%", "200%", "250%", "300%", "350%", "400%"};
    String[] values = {"0.6", "0.8", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0"};
    
    int checkedItem = 2; // Default 1.0 (100%)
    for (int i = 0; i < values.length; i++) {
        if (values[i].equals(val)) {
            checkedItem = i;
            break;
        }
    }
    
    new AlertDialog.Builder(this, R.style.ViTalkMenuTheme)
        .setTitle("Chọn tốc độ giọng đọc")
        .setSingleChoiceItems(entries, checkedItem, (dialog, which) -> {
            prefs.edit().putString(key, values[which]).apply();
            updateSpeechRateDesc(prefs);
            dialog.dismiss();
        })
        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
        .show();
  }

  public static class TalkBackSubSettings extends TalkBackPreferencesActivity {}
}
