package br.com.andersondesouza.filedroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import br.com.andersondesouza.filedroid.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ExternalStorageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (root, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        Window window = getWindow();
        View decorView = window.getDecorView();

        new WindowInsetsControllerCompat(window, decorView)
                .setAppearanceLightStatusBars(false);

        viewModel = new ViewModelProvider(this).get(ExternalStorageViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (activityResult) -> {
                if (Environment.isExternalStorageManager()) {
                    loadFiles();
                }
            });

            launcher.launch(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), (state) -> {
                if (state) {
                    loadFiles();
                }
            });

            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        } else {

            loadFiles();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), (state) -> {
                if (state) {
                    NotificationChannel channel = new NotificationChannel(FiledroidApplication.NOTIFICATION_CHANNEL_ID_EXCEPTIONS, "Exceptions", NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(channel);
                }
            });

            launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                viewModel.updateCurrentDir(viewModel.getCurrentDir().getParentFile());
            }
        });

    }

    private void loadFiles() {

        ExternalStorageAdapter adapter = new ExternalStorageAdapter();
        adapter.setOnItemClickListener(holder -> viewModel.updateCurrentDir(holder.getFile()));

        binding.listView.setHasFixedSize(true);
        binding.listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.listView.setAdapter(adapter);

        viewModel.observeCurrentDir(this, file -> {
            File[] childs = file.listFiles();
            if (childs != null) {
                adapter.submitList(Arrays.asList(childs));
            } else {
                adapter.submitList(Collections.EMPTY_LIST);
            }

            if (!viewModel.getCurrentDir().equals(viewModel.getExternalStorage())) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            binding.pathView.setText(file.getPath());

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            viewModel.updateCurrentDir(viewModel.getCurrentDir().getParentFile());
        }

        EditTextDialog editTextDialog = null;

        if (item.getItemId() == R.id.create_new_file) {

            editTextDialog = new EditTextDialog(R.string.new_file, R.string.hint_new_file);

            editTextDialog.setOnEditTextDialogClickListener((dialog, editText, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    viewModel.asyncCreateNewFile(editText.getText().toString());

                } else {
                    dialog.dismiss();
                }
            });

        } else if (item.getItemId() == R.id.create_new_directory) {

            editTextDialog = new EditTextDialog(R.string.new_file, R.string.hint_new_directory);

            editTextDialog.setOnEditTextDialogClickListener((dialog, editText, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    viewModel.asyncCreateNewDir(editText.getText().toString());

                } else {
                    dialog.dismiss();
                }
            });

        }

        if (editTextDialog != null) {
            editTextDialog.show(getSupportFragmentManager(), "create_new_archive");
        }

        return super.onOptionsItemSelected(item);
    }
}