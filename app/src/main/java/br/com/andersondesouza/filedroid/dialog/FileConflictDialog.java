package br.com.andersondesouza.filedroid.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import br.com.andersondesouza.filedroid.R;
import br.com.andersondesouza.filedroid.databinding.DialogFileConflictBinding;

public class FileConflictDialog extends DialogFragment {

    private DialogFileConflictBinding binding;

    private OnFileConflictDialogClickListener listener;

    private String originPath;
    private String targetPath;

    public FileConflictDialog(String originPath, String targetPath) {
        this.originPath = originPath;
        this.targetPath = targetPath;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogFileConflictBinding.inflate(getLayoutInflater());
        binding.originPathTextView.setText(originPath);
        binding.targetPathTextView.setText(targetPath);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
            .setTitle(R.string.file_conflict)
            .setView(binding.getRoot())
            .setPositiveButton(R.string.rename, (dialog, which) -> {
                if (listener != null) {
                    listener.onFileConflictDialogClick(dialog, which, binding.repeatForAllFilesCheckbox.isChecked());
                }
            })
            .setNeutralButton(R.string.cancel, (dialog, which) -> {
                if (listener != null) {
                    listener.onFileConflictDialogClick(dialog, which, binding.repeatForAllFilesCheckbox.isChecked());
                }
            })
            .setNegativeButton(R.string.skip, (dialog, which) -> {
                if (listener != null) {
                    listener.onFileConflictDialogClick(dialog, which, binding.repeatForAllFilesCheckbox.isChecked());
                }
            });

        return builder.create();
    }

    public void setOnFileConflictDialogClickListener(OnFileConflictDialogClickListener listener) {
        this.listener = listener;
    }

    public interface OnFileConflictDialogClickListener {
        void onFileConflictDialogClick(DialogInterface dialog, int which, boolean repeatForAll);
    }

    public static FileConflictDialog createFileConflictDialog(String originPath, String targetPath, OnFileConflictDialogClickListener listener) {
        var dialog = new FileConflictDialog(originPath, targetPath);
        dialog.setOnFileConflictDialogClickListener(listener);
        return dialog;
    }

    public static void showFileConflictDialog(FragmentManager fragmentManager, String originPath, String targetPath, OnFileConflictDialogClickListener listener) {
        var dialog = new FileConflictDialog(originPath, targetPath);
        dialog.setOnFileConflictDialogClickListener(listener);
        dialog.show(fragmentManager, "edittextdialog");
    }

}
