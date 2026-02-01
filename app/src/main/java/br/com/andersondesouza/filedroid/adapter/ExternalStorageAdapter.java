package br.com.andersondesouza.filedroid.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.andersondesouza.filedroid.R;
import br.com.andersondesouza.filedroid.databinding.ViewHolderFileBinding;

public class ExternalStorageAdapter extends ListAdapter<File, ExternalStorageAdapter.FileViewHolder> {

    private LayoutInflater inflater;

    private boolean selectionMode = false;
    private boolean blockedSelectionMode = false;

    private List<File> selectedItems = new ArrayList<>();

    private OnItemClickListener onItemClickListener;
    private OnSelectionModeStartListener onSelectionModeStartListener;
    private OnItemSelectedListener onItemSelectedListener;
    private OnSelectionModeEndListener onSelectionModeEndListener;

    public ExternalStorageAdapter() {
        super(new DiffUtil.ItemCallback<File>() {

            @Override
            public boolean areItemsTheSame(File oldItem, File newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(File oldItem, File newItem) {
                return oldItem.getName().equals(newItem.getName());
            }

        });
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent != null && inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }

        ViewHolderFileBinding binding = ViewHolderFileBinding.inflate(inflater, parent, false);
        FileViewHolder holder = new FileViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.bind(getFileAt(position));
    }

    public File getFileAt(int position) {
        return getItem(position);
    }

    public void submitSelectedItems(List<File> items) {
        if (selectionMode) {
            selectedItems.clear();
            selectedItems.addAll(items);

            for (int i = 0; i < selectedItems.size(); i++) {
                int index = getCurrentList().indexOf(selectedItems.get(i));
                if (index != -1) {
                    notifyItemChanged(index);
                }
            }

        }
    }

    public List<File> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public boolean areAllItemsSelected() {
        return getItemCount() == selectedItems.size();
    }

    public boolean hasNoSelectedItems() {
        return selectedItems.isEmpty();
    }

    public void selectAll() {
        if (selectionMode) {
            selectedItems.clear();
            selectedItems.addAll(getCurrentList());

            for (int i = 0; i < getItemCount(); i++) {
                notifyItemChanged(i);
            }
        }
    }

    public void deselectAll() {
        if (selectionMode) {
            List<File> currentList = getCurrentList();
            for (File file: selectedItems) {
                notifyItemChanged(currentList.indexOf(file));
            }
            selectedItems.clear();

            selectionMode = false;
            if (onSelectionModeEndListener != null) {
                onSelectionModeEndListener.onSelectionModeEnd(this);
            }
        }
    }

    public void setBlockedSelectionMode(boolean blockedSelectionMode) {
        this.blockedSelectionMode = blockedSelectionMode;
    }

    public boolean isBlockedSelectionMode() {
        return blockedSelectionMode;
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void exitSelectionMode() {
        if (selectionMode) {
            deselectAll();
            selectionMode = false;
            if (onSelectionModeEndListener != null) {
                onSelectionModeEndListener.onSelectionModeEnd(this);
            }
        }
    }

    public ExternalStorageAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public ExternalStorageAdapter setOnSelectionModeStartListener(OnSelectionModeStartListener onSelectionModeStartListener) {
        this.onSelectionModeStartListener = onSelectionModeStartListener;
        return this;
    }

    public ExternalStorageAdapter setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        return this;
    }

    public ExternalStorageAdapter setOnSelectionModeEndListener(OnSelectionModeEndListener onSelectionModeEndListener) {
        this.onSelectionModeEndListener = onSelectionModeEndListener;
        return this;
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        private File file;
        private ViewHolderFileBinding binding;

        public FileViewHolder(ViewHolderFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.getRoot().setOnClickListener(view -> {
                if (!selectionMode && onItemClickListener != null) {
                    onItemClickListener.onItemClick(this);
                }

                if (selectionMode && !blockedSelectionMode) {

                    if (!selectedItems.contains(file)) {
                        selectedItems.add(file);
                        binding.cardView.setCardBackgroundColor(Color.LTGRAY);
                    } else {
                        selectedItems.remove(file);
                        binding.cardView.setCardBackgroundColor(Color.WHITE);
                    }

                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(ExternalStorageAdapter.this, this, selectedItems, file);
                    }

                    if (selectedItems.isEmpty()) {
                        selectionMode = false;

                        if (onSelectionModeEndListener != null) {
                            onSelectionModeEndListener.onSelectionModeEnd(ExternalStorageAdapter.this);
                        }
                    }

                }

            });

            this.binding.getRoot().setOnLongClickListener(view -> {
                if (!selectionMode && !blockedSelectionMode) {
                    selectionMode = true;

                    if (onSelectionModeStartListener != null) {
                        onSelectionModeStartListener.onSelectionModeStart(ExternalStorageAdapter.this);
                    }

                    selectedItems.add(file);
                    binding.cardView.setCardBackgroundColor(Color.LTGRAY);

                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(ExternalStorageAdapter.this, this, selectedItems, file);
                    }

                }

                return true;
            });

        }

        public void bind(File file) {
            this.file = file;

            toggleItemCardColor();

            if (file.isDirectory()) {
                binding.fileIconView.setImageResource(R.drawable.ic_folder);
            } else if (file.isFile()) {
                binding.fileIconView.setImageResource(R.drawable.ic_drive_file);
            }

            binding.fileNameView.setText(file.getName());
        }

        private void toggleItemCardColor() {
            if (selectedItems.contains(file)) {
                binding.cardView.setCardBackgroundColor(Color.LTGRAY);
            } else {
                binding.cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        public File getFile() {
            return file;
        }

    }

    public interface OnItemClickListener {
        void onItemClick(FileViewHolder viewHolder);
    }

    public interface OnSelectionModeStartListener {
        void onSelectionModeStart(ExternalStorageAdapter adapter);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ExternalStorageAdapter adapter, FileViewHolder viewHolder, List<File> selectedItems, File itemChanged);
    }

    public interface OnSelectionModeEndListener {
        void onSelectionModeEnd(ExternalStorageAdapter adapter);
    }

}
