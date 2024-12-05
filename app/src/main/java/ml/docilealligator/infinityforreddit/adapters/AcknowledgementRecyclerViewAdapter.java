package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.ItemAcknowledgementBinding;
import ml.docilealligator.infinityforreddit.settings.Acknowledgement;

public class AcknowledgementRecyclerViewAdapter extends RecyclerView.Adapter<AcknowledgementRecyclerViewAdapter.AcknowledgementViewHolder> {
    private ArrayList<Acknowledgement> acknowledgements;
    private SettingsActivity activity;

    public AcknowledgementRecyclerViewAdapter(SettingsActivity activity, ArrayList<Acknowledgement> acknowledgements) {
        this.activity = activity;
        this.acknowledgements = acknowledgements;
    }

    @NonNull
    @Override
    public AcknowledgementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AcknowledgementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_acknowledgement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AcknowledgementViewHolder holder, int position) {
        Acknowledgement acknowledgement = acknowledgements.get(holder.getBindingAdapterPosition());
        if (acknowledgement != null) {
            holder.binding.nameTextView.setText(acknowledgement.getName());
            holder.binding.introductionTextView.setText(acknowledgement.getIntroduction());
            holder.itemView.setOnClickListener(view -> {
                if (activity != null) {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    intent.setData(acknowledgement.getLink());
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return acknowledgements == null ? 0 : acknowledgements.size();
    }

    class AcknowledgementViewHolder extends RecyclerView.ViewHolder {
        private final ItemAcknowledgementBinding binding;

        AcknowledgementViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAcknowledgementBinding.bind(itemView);

            binding.nameTextView.setTextColor(activity.customThemeWrapper.getPrimaryTextColor());
            binding.introductionTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
        }
    }
}
