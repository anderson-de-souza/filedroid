package br.com.andersondesouza.filedroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import br.com.andersondesouza.filedroid.databinding.ViewHolderFileBinding;

public class ExternalStorageAdapter extends ListAdapter<File, ExternalStorageAdapter.FileViewHolder> {

    private OnItemClickListener listener;
    private LayoutInflater inflater;

    public ExternalStorageAdapter() {
        super(new DiffUtil.ItemCallback<File>() {

            @Override
            public boolean areItemsTheSame(File oldItem, File newItem) {
                return oldItem.getName().equals(newItem.getName());
            }

            @Override
            public boolean areContentsTheSame(File oldItem, File newItem) {
                return true;
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        private File file;
        private ViewHolderFileBinding binding;

        public FileViewHolder(ViewHolderFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(view -> {
                if (listener != null) {
                    listener.onItemClick(this);
                }
            });
        }

        public void bind(File file) {
            this.file = file;

            if (file.isDirectory()) {
                binding.fileIconView.setImageResource(R.drawable.ic_folder);
            } else if (file.isFile()) {
                binding.fileIconView.setImageResource(R.drawable.ic_drive_file);
            }

            binding.fileNameView.setText(file.getName());

        }

        public File getFile() {
            return file;
        }

    }

    public interface OnItemClickListener {
        void onItemClick(FileViewHolder viewHolder);
    }

}
