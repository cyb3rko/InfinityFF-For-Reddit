package ml.docilealligator.infinityforreddit.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFlairBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FlairBottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<FlairBottomSheetRecyclerViewAdapter.FlairViewHolder> {
    private BaseActivity activity;
    private ArrayList<Flair> flairs;
    private int flairTextColor;
    private ItemClickListener itemClickListener;

    public FlairBottomSheetRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                               ItemClickListener itemClickListener) {
        this.activity = activity;
        flairTextColor = customThemeWrapper.getPrimaryTextColor();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FlairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flair, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlairViewHolder holder, int position) {
        if (flairs.get(holder.getBindingAdapterPosition()).isEditable()) {
            holder.binding.editFlairImageView.setVisibility(View.VISIBLE);
            holder.binding.editFlairImageView.setOnClickListener(view -> {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_flair, null);
                EditText flairEditText = dialogView.findViewById(R.id.flair_edit_text_edit_flair_dialog);
                flairEditText.requestFocus();
                Utils.showKeyboard(activity, new Handler(), flairEditText);
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_flair)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            Flair flair = flairs.get(holder.getBindingAdapterPosition());
                            flair.setText(flairEditText.getText().toString());
                            itemClickListener.onClick(flair);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        }

        if (flairs.get(holder.getBindingAdapterPosition()).isEditable() && flairs.get(holder.getBindingAdapterPosition()).getText().equals("")) {
            holder.itemView.setOnClickListener(view -> holder.binding.editFlairImageView.performClick());
        } else {
            holder.itemView.setOnClickListener(view -> itemClickListener.onClick(flairs.get(holder.getBindingAdapterPosition())));
        }

        holder.binding.flairTextView.setText(flairs.get(holder.getBindingAdapterPosition()).getText());
    }

    @Override
    public int getItemCount() {
        return flairs == null ? 0 : flairs.size();
    }

    @Override
    public void onViewRecycled(@NonNull FlairViewHolder holder) {
        super.onViewRecycled(holder);
        holder.binding.editFlairImageView.setVisibility(View.GONE);
    }

    public void changeDataset(ArrayList<Flair> flairs) {
        this.flairs = flairs;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onClick(Flair flair);
    }

    class FlairViewHolder extends RecyclerView.ViewHolder {
        private final ItemFlairBinding binding;

        FlairViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFlairBinding.bind(itemView);
            binding.flairTextView.setTextColor(flairTextColor);

            if (activity.typeface != null) {
                binding.flairTextView.setTypeface(activity.typeface);
            }
        }
    }
}
