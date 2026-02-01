package br.com.andersondesouza.filedroid.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import br.com.andersondesouza.filedroid.R;
import br.com.andersondesouza.filedroid.databinding.DialogEditTextBinding;

public class EditTextDialog extends DialogFragment {

    private DialogEditTextBinding binding;
    private OnEditTextDialogClickListener listener;

    private String title = "";
    private String hint = "";

    private int titleResId;
    private int hintResId;

    public EditTextDialog(String title, String hint) {
        this.title = title;
        this.hint = hint;
    }

    public EditTextDialog(int titleResId, int hintResId) {
        this.titleResId = titleResId;
        this.hintResId = hintResId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogEditTextBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
            .setView(binding.getRoot())
            .setPositiveButton(R.string.done, (dialog, which) -> {
                if (listener != null) {
                    listener.onEditTextDialogClick(dialog, which, binding.editText);
                }
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                if (listener != null) {
                    listener.onEditTextDialogClick(dialog, which, binding.editText);
                }
            });

        if (titleResId != 0) {
            builder.setTitle(titleResId);
        } else {
            builder.setTitle(title);
        }

        if (hintResId != 0) {
            binding.editText.setHint(hintResId);
        } else {
            binding.editText.setHint(hint);
        }

        return builder.create();
    }

    public void setOnEditTextDialogClickListener(OnEditTextDialogClickListener listener) {
        this.listener = listener;
    }

    public interface OnEditTextDialogClickListener {
        void onEditTextDialogClick(DialogInterface dialog, int which, EditText editText);
    }

    public static EditTextDialog createEditTextDialog(String title, String hint, OnEditTextDialogClickListener onEditTextDialogClickListener) {
        EditTextDialog dialog = new EditTextDialog(title, hint);
        dialog.setOnEditTextDialogClickListener(onEditTextDialogClickListener);
        return dialog;
    }

    public static EditTextDialog createEditTextDialog(int titleResId, int hintResId, OnEditTextDialogClickListener onEditTextDialogClickListener) {
        EditTextDialog dialog = new EditTextDialog(titleResId, hintResId);
        dialog.setOnEditTextDialogClickListener(onEditTextDialogClickListener);
        return dialog;
    }

    public static void showEditTextDialog(FragmentManager fragmentManager, String title, String hint, OnEditTextDialogClickListener onEditTextDialogClickListener) {
        EditTextDialog dialog = new EditTextDialog(title, hint);
        dialog.setOnEditTextDialogClickListener(onEditTextDialogClickListener);
        dialog.show(fragmentManager, "edittextdialog");
    }

    public static void showEditTextDialog(FragmentManager fragmentManager, int titleResId, int hintResId, OnEditTextDialogClickListener onEditTextDialogClickListener) {
        EditTextDialog dialog = new EditTextDialog(titleResId, hintResId);
        dialog.setOnEditTextDialogClickListener(onEditTextDialogClickListener);
        dialog.show(fragmentManager, "edittextdialog");
    }

}
