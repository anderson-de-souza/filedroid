package br.com.andersondesouza.filedroid;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.andersondesouza.filedroid.databinding.ViewHolderFileBinding;

public class ExternalStorageAdapter extends ListAdapter<File, ExternalStorageAdapter.FileViewHolder> {

    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    private boolean isSelectionMode = false;
    private List<File> selectedItems = new ArrayList<File>();
    private OnItemSelectedListener onItemSelectedListener;

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
        if (isSelectionMode) {
            selectedItems.clear();
            selectedItems.addAll(getCurrentList());
            for (int i = 0; i < getItemCount(); i++) {
                notifyItemChanged(i);
            }
        }
    }

    public void deselectAll() {
        if (isSelectionMode) {
            List<File> currentList = getCurrentList();
            for (File file: selectedItems) {
                notifyItemChanged(currentList.indexOf(file));
            }
            selectedItems.removeAll(getCurrentList());
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void exitSelectionMode() {
        deselectAll();
        isSelectionMode = false;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        private File file;
        private ViewHolderFileBinding binding;

        public FileViewHolder(ViewHolderFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.getRoot().setOnClickListener(view -> {
                if (!isSelectionMode && onItemClickListener != null) {
                    onItemClickListener.onItemClick(this);
                }

                if (isSelectionMode) {

                    if (!selectedItems.contains(file)) {
                        selectedItems.add(file);
                        binding.cardView.setCardBackgroundColor(Color.LTGRAY);
                    } else {
                        selectedItems.remove(file);
                        binding.cardView.setCardBackgroundColor(Color.WHITE);
                    }

                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(selectedItems, file);
                    }

                    if (selectedItems.isEmpty()) {
                        isSelectionMode = false;
                    }

                }

            });

            this.binding.getRoot().setOnLongClickListener(view -> {

                if (!isSelectionMode) {
                    isSelectionMode = true;

                    selectedItems.add(file);
                    binding.cardView.setCardBackgroundColor(Color.LTGRAY);

                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(selectedItems, file);
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

    public interface OnItemSelectedListener {
        void onItemSelected(List<File> selectedItems, File itemChanged);
    }

}
