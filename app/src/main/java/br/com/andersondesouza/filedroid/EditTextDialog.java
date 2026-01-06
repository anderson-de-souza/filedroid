package br.com.andersondesouza.filedroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import br.com.andersondesouza.filedroid.databinding.DialogEditTextBinding;

public class EditTextDialog extends DialogFragment {

    private String title = "";
    private String hint = "";

    private int titleResId;
    private int hintResId;

    private OnEditTextDialogClickListener listener;

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
        DialogEditTextBinding binding = DialogEditTextBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(binding.getRoot())
                .setPositiveButton(R.string.done, (dialog, which) -> {
                    if (listener != null) {
                        listener.onEditTextDialogClick(dialog, binding.editText, which);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (listener != null) {
                        listener.onEditTextDialogClick(dialog, binding.editText, which);
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
        void onEditTextDialogClick(DialogInterface dialog, EditText editText, int which);
    }

}
