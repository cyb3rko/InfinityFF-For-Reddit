package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuGroupTitleBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerSubscribedThingBinding;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SubscribedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_SUBSCRIBED_SUBREDDIT = 2;

    private RequestManager glide;
    private int primaryTextColor;
    private int secondaryTextColor;
    private boolean collapseSubscribedSubredditsSection;
    private boolean hideSubscribedSubredditsSection;
    private ArrayList<SubscribedSubredditData> subscribedSubreddits = new ArrayList<>();
    private NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

    public SubscribedSubredditsRecyclerViewAdapter(RequestManager glide,
                                                   CustomThemeWrapper customThemeWrapper,
                                                   SharedPreferences navigationDrawerSharedPreferences,
                                                   NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        collapseSubscribedSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_SUBSCRIBED_SUBREDDITS_SECTION, false);
        hideSubscribedSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBSCRIBED_SUBREDDITS_SECTIONS, false);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_MENU_GROUP_TITLE : VIEW_TYPE_SUBSCRIBED_SUBREDDIT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MENU_GROUP_TITLE) {
            return new MenuGroupTitleViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nav_drawer_menu_group_title, parent, false));
        } else {
            return new SubscribedThingViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nav_drawer_subscribed_thing, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuGroupTitleViewHolder) {
            ((MenuGroupTitleViewHolder) holder).binding.titleTextView.setText(R.string.subscriptions);
            if (collapseSubscribedSubredditsSection) {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
            }

            holder.itemView.setOnClickListener(view -> {
                if (collapseSubscribedSubredditsSection) {
                    collapseSubscribedSubredditsSection = !collapseSubscribedSubredditsSection;
                    notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, subscribedSubreddits.size());
                } else {
                    collapseSubscribedSubredditsSection = !collapseSubscribedSubredditsSection;
                    notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, subscribedSubreddits.size());
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof SubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = subscribedSubreddits.get(position - 1);
            String subredditName = subreddit.getName();
            String iconUrl = subreddit.getIconUrl();
            ((SubscribedThingViewHolder) holder).binding.thingNameTextView.setText(subredditName);
            if (iconUrl != null && !iconUrl.equals("")) {
                glide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((SubscribedThingViewHolder) holder).binding.thingIconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((SubscribedThingViewHolder) holder).binding.thingIconGifImageView);
            }

            holder.itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (hideSubscribedSubredditsSection) {
            return 0;
        }
        return subscribedSubreddits.isEmpty() ? 0 : (collapseSubscribedSubredditsSection ? 1 : subscribedSubreddits.size() + 1);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubscribedThingViewHolder) {
            glide.clear(((SubscribedThingViewHolder) holder).binding.thingIconGifImageView);
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        this.subscribedSubreddits = (ArrayList<SubscribedSubredditData>) subscribedSubreddits;
        notifyDataSetChanged();
    }

    class MenuGroupTitleViewHolder extends RecyclerView.ViewHolder {
        private final ItemNavDrawerMenuGroupTitleBinding binding;

        MenuGroupTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNavDrawerMenuGroupTitleBinding.bind(itemView);
            binding.titleTextView.setTextColor(secondaryTextColor);
            binding.collapseIndicatorImageView.setColorFilter(secondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class SubscribedThingViewHolder extends RecyclerView.ViewHolder {
        private final ItemNavDrawerSubscribedThingBinding binding;

        SubscribedThingViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNavDrawerSubscribedThingBinding.bind(itemView);
            binding.thingNameTextView.setTextColor(primaryTextColor);
        }
    }
}
