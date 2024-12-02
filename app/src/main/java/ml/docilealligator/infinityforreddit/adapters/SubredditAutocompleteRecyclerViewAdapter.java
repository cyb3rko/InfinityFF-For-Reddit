package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemSubredditListingBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;

public class SubredditAutocompleteRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private List<SubredditData> subreddits;
    private RequestManager glide;
    private CustomThemeWrapper customThemeWrapper;
    private ItemOnClickListener itemOnClickListener;

    public SubredditAutocompleteRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                                    ItemOnClickListener itemOnClickListener) {
        this.activity = activity;
        glide = Glide.with(activity);
        this.customThemeWrapper = customThemeWrapper;
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            glide.load(subreddits.get(position).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) holder).binding.subredditIconGifImageView);
            ((SubredditViewHolder) holder).binding.subredditNameTextView.setText(subreddits.get(position).getName());
            ((SubredditViewHolder) holder).binding.subscriberCountTextView.setText(activity.getString(R.string.subscribers_number_detail, subreddits.get(position).getNSubscribers()));
        }
    }

    @Override
    public int getItemCount() {
        return subreddits == null ? 0 : subreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).binding.subredditIconGifImageView);
        }
    }

    public void setSubreddits(List<SubredditData> subreddits) {
        this.subreddits = subreddits;
        notifyDataSetChanged();
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubredditListingBinding binding;

        public SubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSubredditListingBinding.bind(itemView);

            binding.subscribeImageView.setVisibility(View.GONE);
            binding.checkbox.setVisibility(View.GONE);

            binding.subredditNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            binding.subscriberCountTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                binding.subredditNameTextView.setTypeface(activity.typeface);
                binding.subscriberCountTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                itemOnClickListener.onClick(subreddits.get(getBindingAdapterPosition()));
            });
        }
    }

    public interface ItemOnClickListener {
        void onClick(SubredditData subredditData);
    }
}
