package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.award.Award;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemAwardBinding;

public class AwardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Award> awards;
    private RequestManager glide;
    private ItemOnClickListener itemOnClickListener;
    private int primaryTextColor;
    private int secondaryTextColor;

    public interface ItemOnClickListener {
        void onClick(Award award);
    }

    public AwardRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                    ItemOnClickListener itemOnClickListener) {
        awards = Award.getAvailableAwards();
        this.glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AwardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_award, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AwardViewHolder) {
            Award award = awards.get(position);
            glide.load(award.getIconUrl()).into(((AwardViewHolder) holder).binding.iconImageView);
            ((AwardViewHolder) holder).binding.nameTextView.setText(award.getName());
            ((AwardViewHolder) holder).binding.descriptionTextView.setText(award.getDescription());
            ((AwardViewHolder) holder).binding.coinTextView.setText(Integer.toString(award.getCoinPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return awards.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof AwardViewHolder) {
            glide.clear(((AwardViewHolder) holder).binding.iconImageView);
            ((AwardViewHolder) holder).binding.nameTextView.setText("");
            ((AwardViewHolder) holder).binding.descriptionTextView.setText("");
        }
    }

    class AwardViewHolder extends RecyclerView.ViewHolder {
        private final ItemAwardBinding binding;

        public AwardViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAwardBinding.bind(itemView);

            binding.nameTextView.setTextColor(primaryTextColor);
            binding.descriptionTextView.setTextColor(secondaryTextColor);
            binding.coinTextView.setTextColor(primaryTextColor);

            itemView.setOnClickListener(view -> {
                itemOnClickListener.onClick(awards.get(getBindingAdapterPosition()));
            });
        }
    }
}
