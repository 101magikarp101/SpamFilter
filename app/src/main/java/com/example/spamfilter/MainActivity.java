package com.example.spamfilter;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.spamfilter.databinding.ActivityMainBinding;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    private ActivityResultLauncher<Intent> requestRoleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(TAG, "Role granted");
                } else {
                    Log.d(TAG, "Role not granted");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar2);

        requestRole();

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result;
                if (saveSettings()) {
                    result = "Settings Saved";
                } else {
                    result = "Invalid Format";
                }
                Snackbar.make(view, result, Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.floatingActionButton)
                        .setAction("Action", null).show();
            }
        });
    }

    private void requestRole() {
        RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            requestRoleLauncher.launch(intent);
        }
    }

    private boolean saveSettings() {
        SpamFilterSettings.callBlocking = binding.switch1.isChecked();
        SpamFilterSettings.allowRepeated = binding.switch2.isChecked();
        try {
            Integer.parseInt(binding.editTextNumber.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, "Invalid number format");
            return false;
        }
        Log.d(TAG, "Settings saved: " + SpamFilterSettings.callBlocking + " " + SpamFilterSettings.allowRepeated + " " + SpamFilterSettings.repeatedWithinMinutes);
        return true;
    }
}