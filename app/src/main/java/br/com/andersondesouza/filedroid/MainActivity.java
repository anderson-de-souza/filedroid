package br.com.andersondesouza.filedroid;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.com.andersondesouza.filedroid.action.CopyFileAction;
import br.com.andersondesouza.filedroid.action.CreateDirectoryAction;
import br.com.andersondesouza.filedroid.action.CreateFileAction;
import br.com.andersondesouza.filedroid.action.DeleteFileAction;
import br.com.andersondesouza.filedroid.action.FileActionManager;
import br.com.andersondesouza.filedroid.action.MoveFileAction;
import br.com.andersondesouza.filedroid.action.RenameFileAction;
import br.com.andersondesouza.filedroid.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {

    private ActivityMainBinding binding;
    private ActionMode actionMode;

    private ExternalStorageViewModel externalStorageViewModel;
    private ExternalStorageAdapter externalStorageAdapter;

    private List<File> fileBuffer = new ArrayList<>();

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

        externalStorageViewModel = new ViewModelProvider(this).get(ExternalStorageViewModel.class);
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
                externalStorageViewModel.backToParent();
            }
        });

        binding.itemCopy.setOnClickListener(view -> {
            view.setActivated(true);
            binding.itemCut.setActivated(false);
            if (externalStorageAdapter.isSelectionMode()) {
                fileBuffer.addAll(externalStorageAdapter.getSelectedItems());
                actionMode.finish();
            }
        });

        binding.itemCut.setOnClickListener(view -> {
            view.setActivated(true);
            binding.itemCopy.setActivated(false);
            if (externalStorageAdapter.isSelectionMode()) {
                fileBuffer.addAll(externalStorageAdapter.getSelectedItems());
                actionMode.finish();
            }
        });

        binding.itemPaste.setOnClickListener(view -> {

            if (!externalStorageAdapter.isSelectionMode()) {

                if (binding.itemCopy.isActivated()) {

                    FileActionManager.addToQueue(new CopyFileAction(fileBuffer, externalStorageViewModel.getCurrentDirectory())
                        .setOnProgressListener((files, failFiles, file, success) -> externalStorageViewModel.updateCurrentDirectory())
                        .setOnEndListener((files, failFiles) -> fileBuffer.clear()));

                }

                if (binding.itemCut.isActivated()) {

                    FileActionManager.addToQueue(new MoveFileAction(fileBuffer, externalStorageViewModel.getCurrentDirectory())
                        .setOnProgressListener((files, failFiles, file, success) -> externalStorageViewModel.updateCurrentDirectory())
                        .setOnEndListener((files, failFiles) -> fileBuffer.clear()));

                }

                binding.itemCopy.setActivated(false);
                binding.itemCut.setActivated(false);

                binding.bottomAppBar.setVisibility(View.GONE);
                binding.toolbar.setTitle(R.string.app_name);

            }

        });

    }

    private void loadFiles() {

        externalStorageAdapter.setOnItemClickListener(holder -> externalStorageViewModel.updateCurrentDirectory(holder.getFile()));
        externalStorageAdapter.setOnItemSelectedListener((selectedItems, itemChanged) -> {
            if (actionMode == null) {
                actionMode = startSupportActionMode(this);
            } else {
                actionMode.invalidate();
            }
        });

        binding.listView.setHasFixedSize(true);
        binding.listView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );
        binding.listView.setAdapter(externalStorageAdapter);

        externalStorageViewModel.observeCurrentDirectory(this, file -> {

            File[] children = file.listFiles();

            if (children != null) {
                externalStorageAdapter.submitList(Arrays.asList(children));
            } else {
                externalStorageAdapter.submitList(Collections.EMPTY_LIST);
            }

            if (!externalStorageViewModel.isRoot()) {
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
            externalStorageViewModel.backToParent();
        }

        if (item.getItemId() == R.id.create_new_file) {

            EditTextDialog.createEditTextDialog(R.string.new_file, R.string.hint_new_file, (dialog, editText, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    FileActionManager.addToQueue(new CreateFileAction(externalStorageViewModel.getCurrentDirectory(), editText.getText().toString())
                        .setOnProgressListener((files, failFiles, file, success) -> {
                            if (success) {
                                externalStorageViewModel.updateCurrentDirectory();
                            }
                        }));

                } else {
                    dialog.dismiss();
                }

            }).show(getSupportFragmentManager(), "create_new_file");

        } else if (item.getItemId() == R.id.create_new_directory) {

            EditTextDialog.createEditTextDialog(R.string.new_file, R.string.hint_new_directory, (dialog, editText, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    FileActionManager.addToQueue(new CreateDirectoryAction(externalStorageViewModel.getCurrentDirectory(), editText.getText().toString())
                        .setOnProgressListener((files, failFiles, file, success) -> {
                            if (success) {
                                externalStorageViewModel.updateCurrentDirectory();
                            }
                        }));

                } else {
                    dialog.dismiss();
                }

            }).show(getSupportFragmentManager(), "create_new_directory");

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_action_mode, menu);
        binding.bottomAppBar.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        mode.setTitle(
            getResources().getQuantityString(
                R.plurals.selected_items,
                externalStorageAdapter.getSelectedItemCount(),
                externalStorageAdapter.getSelectedItemCount()
            )
        );

        menu.findItem(R.id.select_all).setVisible(!externalStorageAdapter.areAllItemsSelected());
        menu.findItem(R.id.deselect_all).setVisible(externalStorageAdapter.areAllItemsSelected());

        if (externalStorageAdapter.hasNoSelectedItems()) {
            actionMode.finish();
        }

        return true;

    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if (item.getItemId() == R.id.select_all) {
            externalStorageAdapter.selectAll();
            mode.invalidate();
        }

        else if (item.getItemId() == R.id.deselect_all) {
            externalStorageAdapter.deselectAll();
            mode.invalidate();
        }

        else if (item.getItemId() == R.id.delete) {

            int count = externalStorageAdapter.getSelectedItemCount();

            new AlertDialog.Builder(this)
                .setTitle(R.string.are_you_sure)
                .setMessage(getResources().getQuantityString(R.plurals.delete_selected_items, count, count))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    FileActionManager.addToQueue(new DeleteFileAction(externalStorageAdapter.getSelectedItems())
                        .setOnProgressListener((files, failFiles, file, success) -> externalStorageViewModel.updateCurrentDirectory())
                        .setOnEndListener((files, failFiles) -> actionMode.finish()));
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        } else if (item.getItemId() == R.id.rename) {

            EditTextDialog.createEditTextDialog(R.string.rename_files, R.string.hint_rename_pattern, (dialog, editText, buttonId) -> {

                if (buttonId == DialogInterface.BUTTON_POSITIVE) {

                    int count = externalStorageAdapter.getSelectedItemCount();

                    new AlertDialog.Builder(this)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(getResources().getQuantityString(R.plurals.rename_selected_items, count, count))
                        .setPositiveButton(R.string.yes, (childDialog, which) -> {
                            FileActionManager.addToQueue(new RenameFileAction(externalStorageAdapter.getSelectedItems(), editText.getText().toString(), "{i}")
                                .setOnProgressListener((files, failFiles, file, success) -> externalStorageViewModel.updateCurrentDirectory())
                                .setOnEndListener((files, failFiles) -> actionMode.finish()));
                        })
                        .setNegativeButton(R.string.cancel, (childDialog, which) -> dialog.dismiss())
                        .create()
                        .show();

                } else {
                    dialog.dismiss();
                }

            }).show(getSupportFragmentManager(), "rename_file");

        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        externalStorageAdapter.exitSelectionMode();

        if (!fileBuffer.isEmpty()) {
            binding.toolbar.setTitle(R.string.does_it_paste_here);
        } else {
            binding.bottomAppBar.setVisibility(View.GONE);
        }

        actionMode = null;
    }

}