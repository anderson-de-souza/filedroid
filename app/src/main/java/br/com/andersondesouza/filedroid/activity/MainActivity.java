package br.com.andersondesouza.filedroid.activity;

import android.Manifest;
import android.app.AlertDialog;
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

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import br.com.andersondesouza.filedroid.FiledroidApplication;
import br.com.andersondesouza.filedroid.R;
import br.com.andersondesouza.filedroid.adapter.ExternalStorageAdapter;
import br.com.andersondesouza.filedroid.databinding.ActivityMainBinding;
import br.com.andersondesouza.filedroid.dialog.EditTextDialog;
import br.com.andersondesouza.filedroid.viewmodel.ExternalStorageViewModel;
import br.com.andersondesouza.filedroid.viewmodel.FileBufferViewModel;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {

    private ActivityMainBinding binding;
    private ActionMode actionMode;

    private ExternalStorageViewModel externalStorageViewModel;
    private FileBufferViewModel fileBufferViewModel;

    private ExternalStorageAdapter externalStorageAdapter;

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

        ViewModelProvider viewModelProvider = new ViewModelProvider(this);

        externalStorageViewModel = viewModelProvider.get(ExternalStorageViewModel.class);
        fileBufferViewModel = viewModelProvider.get(FileBufferViewModel.class);

        externalStorageAdapter = new ExternalStorageAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
                if (Environment.isExternalStorageManager()) {
                    loadFiles();
                }
            });

            launcher.launch(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), state -> {
                if (state) {
                    loadFiles();
                }
            });

            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        } else {
            loadFiles();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), state -> {
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
                if (!fileBufferViewModel.isEmptyFileBuffer() && externalStorageViewModel.isRoot()) {
                    fileBufferViewModel.clearFileBuffer();
                    externalStorageAdapter.setBlockedSelectionMode(false);
                } else {
                    externalStorageViewModel.backToParent();
                }
            }
        });

        binding.itemCopy.setOnClickListener(view -> {
            onBottomAppBarItemClick(view);
        });

        binding.itemCut.setOnClickListener(view -> {
            onBottomAppBarItemClick(view);
        });

        binding.itemPaste.setOnClickListener(view -> {
            onBottomAppBarItemClick(view);
        });

        fileBufferViewModel.observeFileBuffer(this, fileBuffer -> {
            updateToolbar();
            finishBottomAppBar();
            finishSupportActionMode();
        });

    }

    private void loadFiles() {

        externalStorageAdapter.setOnItemClickListener(holder -> {
            externalStorageViewModel.updateCurrentDirectory(holder.getFile());
        })

        .setOnSelectionModeStartListener(adapter -> {
            startSupportActionMode();
            startBottomAppBar();
        })

        .setOnItemSelectedListener((adapter, viewHolder, selectedItems, itemChanged) -> {
            updateSupportActionMode();
        })

        .setOnSelectionModeEndListener(adapter -> {
            finishSupportActionMode();
            finishBottomAppBar();
        });

        binding.listView.setHasFixedSize(true);
        binding.listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.listView.setAdapter(externalStorageAdapter);

        externalStorageViewModel.observeCurrentDirectory(this, file -> {
            File[] children = file.listFiles();

            if (children != null) {
                externalStorageAdapter.submitList(Arrays.asList(children));
            } else {
                externalStorageAdapter.submitList(new ArrayList<>());
            }

            enableHomeAsUp();
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
            if (!fileBufferViewModel.isEmptyFileBuffer() && externalStorageViewModel.isRoot()) {
                fileBufferViewModel.clearFileBuffer();
                externalStorageAdapter.setBlockedSelectionMode(false);
            } else {
                externalStorageViewModel.backToParent();
            }
        }

        if (item.getItemId() == R.id.create_new_file) {

            EditTextDialog.showEditTextDialog(getSupportFragmentManager(), R.string.new_file, R.string.hint_new_file, (dialog, which, editText) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    File file = new File(externalStorageViewModel.getCurrentDirectory(), editText.getText().toString());

                    if (!file.exists()) {
                        File parent = file.getParentFile();

                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }

                        try {
                            file.createNewFile();
                            externalStorageViewModel.updateCurrentDirectory();
                        } catch (IOException exception) {
                        }

                    }
                } else {
                    dialog.dismiss();
                }
            });

        } else if (item.getItemId() == R.id.create_new_directory) {

            EditTextDialog.showEditTextDialog(getSupportFragmentManager(), R.string.new_file, R.string.hint_new_directory, (dialog, which, editText) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    File file = new File(externalStorageViewModel.getCurrentDirectory(), editText.getText().toString());
                    if (!file.isDirectory()) {
                        file.mkdirs();
                        externalStorageViewModel.updateCurrentDirectory();
                    }
                } else {
                    dialog.dismiss();
                }
            });

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitle(getResources().getQuantityString(
            R.plurals.selected_items,
            externalStorageAdapter.getSelectedItemCount(),
            externalStorageAdapter.getSelectedItemCount()
        ));
        menu.findItem(R.id.select_all).setVisible(!externalStorageAdapter.areAllItemsSelected());
        menu.findItem(R.id.deselect_all).setVisible(externalStorageAdapter.areAllItemsSelected());
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
        if (item.getItemId() == R.id.select_all) {
            externalStorageAdapter.selectAll();
            actionMode.invalidate();

        } else if (item.getItemId() == R.id.deselect_all) {
            externalStorageAdapter.deselectAll();
            actionMode.invalidate();

        } else if (item.getItemId() == R.id.delete) {
            int count = externalStorageAdapter.getSelectedItemCount();

            new AlertDialog.Builder(this)
                .setTitle(R.string.are_you_sure)
                .setMessage(getResources().getQuantityString(R.plurals.delete_selected_items, count, count))
                .setPositiveButton(R.string.yes, (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        } else if (item.getItemId() == R.id.rename) {
            EditTextDialog.showEditTextDialog(
                    getSupportFragmentManager(),
                    R.string.rename_files, R.string.hint_rename_pattern,
                    (dialog, buttonId, editText) -> {

                if (buttonId == DialogInterface.BUTTON_POSITIVE) {

                    int count = externalStorageAdapter.getSelectedItemCount();

                    new AlertDialog.Builder(this)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(getResources().getQuantityString(R.plurals.rename_selected_items, count, count))
                        .setPositiveButton(R.string.yes, (childDialog, which) -> {

                        })
                        .setNegativeButton(R.string.cancel, (childDialog, which) -> dialog.dismiss())
                        .create()
                        .show();

                } else {
                    dialog.dismiss();
                }

            });

        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        externalStorageAdapter.exitSelectionMode();
    }

    private void resetBottomAppBar() {
        binding.itemCopy.setActivated(false);
        binding.itemCut.setActivated(false);
        binding.itemPaste.setActivated(false);
    }

    private void startBottomAppBar() {
        resetBottomAppBar();
        if (binding.bottomAppBar.getVisibility() != View.VISIBLE) {
            binding.bottomAppBar.setVisibility(View.VISIBLE);
        }
    }

    private void finishBottomAppBar() {
        if (binding.bottomAppBar.getVisibility() != View.GONE && fileBufferViewModel.isEmptyFileBuffer()) {
            resetBottomAppBar();
            binding.bottomAppBar.setVisibility(View.GONE);
        }
    }

    private void updateBottomAppBar(View view) {
        binding.itemCopy.setActivated(view.equals(binding.itemCopy) && !view.isActivated());
        binding.itemCut.setActivated(view.equals(binding.itemCut) && !view.isActivated());
    }

    private void startSupportActionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(this);
        }
    }

    private void updateSupportActionMode() {
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    private void finishSupportActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void onBottomAppBarItemClick(View view) {
        updateBottomAppBar(view);
        if (view.isActivated() && externalStorageAdapter.isSelectionMode()) {
            fileBufferViewModel.updateFileBuffer(externalStorageAdapter.getSelectedItems());
            externalStorageAdapter.setBlockedSelectionMode(true);
        } else if (view.equals(binding.itemPaste)){
            fileBufferViewModel.clearFileBuffer();
            externalStorageAdapter.setBlockedSelectionMode(false);
        }
    }

    private void updateToolbar() {
        if (fileBufferViewModel.isEmptyFileBuffer()) {
            binding.toolbar.setTitle(R.string.app_name);
        } else {
            binding.toolbar.setTitle(R.string.does_it_paste_here);
        }
        enableHomeAsUp();
    }

    private void enableHomeAsUp() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!externalStorageViewModel.isRoot() || !fileBufferViewModel.isEmptyFileBuffer());
    }


}