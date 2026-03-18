package com.example.food.ui.water;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.repository.WaterRepository;
import com.example.food.db.entity.WaterRecord;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    public interface OnItemDeleteListener {
        void onItemDeleted();
    }

    private final Context context;
    private final List<WaterRecord> waterRecords = new ArrayList<>();
    private WaterRepository waterRepository;
    private OnItemDeleteListener onItemDeleteListener;

    public WaterAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    public void setRepository(WaterRepository repository) {
        this.waterRepository = repository;
    }

    public void setData(List<WaterRecord> records) {
        List<WaterRecord> newRecords = records == null ? new ArrayList<>() : new ArrayList<>(records);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(this.waterRecords, newRecords));
        this.waterRecords.clear();
        this.waterRecords.addAll(newRecords);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_water, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaterRecord record = waterRecords.get(position);
        holder.timeTextView.setText(DateUtils.formatDate(record.getTime(), DateUtils.DATE_FORMAT_HM));
        holder.amountTextView.setText(String.format(Locale.CHINA, "%.0f ml", record.getAmount()));
        holder.deleteButton.setOnClickListener(v -> showDeleteDialog(record));
    }

    private void showDeleteDialog(WaterRecord record) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.water_delete_title)
                .setMessage(R.string.water_delete_message)
                .setPositiveButton(R.string.water_delete_action, (dialog, which) -> deleteWaterRecord(record))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteWaterRecord(WaterRecord record) {
        if (waterRepository == null) {
            Toast.makeText(context, R.string.water_delete_repo_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        waterRepository.delete(record, () -> {
            Toast.makeText(context, R.string.water_delete_success, Toast.LENGTH_SHORT).show();
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onItemDeleted();
            }
        });
    }

    @Override
    public int getItemCount() {
        return waterRecords.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView timeTextView;
        final TextView amountTextView;
        final TextView deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.tv_water_time);
            amountTextView = itemView.findViewById(R.id.tv_water_amount);
            deleteButton = itemView.findViewById(R.id.btn_delete_water);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private final List<WaterRecord> oldList;
        private final List<WaterRecord> newList;

        DiffCallback(List<WaterRecord> oldList, List<WaterRecord> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            WaterRecord oldItem = oldList.get(oldItemPosition);
            WaterRecord newItem = newList.get(newItemPosition);
            return Double.compare(oldItem.getAmount(), newItem.getAmount()) == 0
                    && oldItem.getTime().equals(newItem.getTime());
        }
    }
}