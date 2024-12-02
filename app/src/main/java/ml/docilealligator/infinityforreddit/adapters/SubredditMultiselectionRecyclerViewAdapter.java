package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemSubscribedSubredditMultiSelectionBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditWithSelection;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class SubredditMultiselectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity activity;
    private ArrayList<SubredditWithSelection> subscribedSubreddits;
    private RequestManager glide;
    private int primaryTextColor;
    private int colorAccent;

    public SubredditMultiselectionRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubscribedSubredditViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscribed_subreddit_multi_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubscribedSubredditViewHolder) {
            ((SubscribedSubredditViewHolder) holder).binding.nameTextView.setText(subscribedSubreddits.get(position).getName());
            glide.load(subscribedSubreddits.get(position).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubscribedSubredditViewHolder) holder).binding.iconGifImageView);
            ((SubscribedSubredditViewHolder) holder).binding.checkbox.setChecked(subscribedSubreddits.get(position).isSelected());
            ((SubscribedSubredditViewHolder) holder).binding.checkbox.setOnClickListener(view -> {
                if (subscribedSubreddits.get(position).isSelected()) {
                    ((SubscribedSubredditViewHolder) holder).binding.checkbox.setChecked(false);
                    subscribedSubreddits.get(position).setSelected(false);
                } else {
                    ((SubscribedSubredditViewHolder) holder).binding.checkbox.setChecked(true);
                    subscribedSubreddits.get(position).setSelected(true);
                }
            });
            ((SubscribedSubredditViewHolder) holder).itemView.setOnClickListener(view ->
                    ((SubscribedSubredditViewHolder) holder).binding.checkbox.performClick());
        }
    }

    @Override
    public int getItemCount() {
        return subscribedSubreddits == null ? 0 : subscribedSubreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubscribedSubredditViewHolder) {
            glide.clear(((SubscribedSubredditViewHolder) holder).binding.iconGifImageView);
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        this.subscribedSubreddits = SubredditWithSelection.convertSubscribedSubreddits(subscribedSubreddits);
        notifyDataSetChanged();
    }

    public ArrayList<String> getAllSelectedSubreddits() {
        ArrayList<String> selectedSubreddits = new ArrayList<>();
        for (SubredditWithSelection s : subscribedSubreddits) {
            if (s.isSelected()) {
                selectedSubreddits.add(s.getName());
            }
        }
        return selectedSubreddits;
    }

    class SubscribedSubredditViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubscribedSubredditMultiSelectionBinding binding;

        SubscribedSubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSubscribedSubredditMultiSelectionBinding.bind(itemView);
            binding.nameTextView.setTextColor(primaryTextColor);
            binding.checkbox.setButtonTintList(ColorStateList.valueOf(colorAccent));

            if (activity.typeface != null) {
                binding.nameTextView.setTypeface(activity.typeface);
            }
        }
    }
}