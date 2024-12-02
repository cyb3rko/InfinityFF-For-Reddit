package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.TrendingSearch;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemTrendingSearchBinding;
import ml.docilealligator.infinityforreddit.post.Post;

public class TrendingSearchRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private ArrayList<TrendingSearch> trendingSearches;
    private CustomThemeWrapper customThemeWrapper;
    private RequestManager glide;
    private int imageViewWidth;
    private boolean dataSavingMode;
    private boolean disableImagePreview;
    private float mScale;
    private ItemClickListener itemClickListener;

    public TrendingSearchRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                             int imageViewWidth, boolean dataSavingMode, boolean disableImagePreview,
                                             ItemClickListener itemClickListener) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.glide = Glide.with(activity);
        this.imageViewWidth = imageViewWidth;
        this.dataSavingMode = dataSavingMode;
        this.disableImagePreview = disableImagePreview;
        mScale = activity.getResources().getDisplayMetrics().density;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrendingSearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TrendingSearchViewHolder) {
            TrendingSearch trendingSearch = trendingSearches.get(position);
            if (dataSavingMode && disableImagePreview) {
                ((TrendingSearchViewHolder) holder).binding.imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
            } else {
                Post.Preview preview = getSuitablePreview(trendingSearch.previews);
                if (preview != null) {
                    ((TrendingSearchViewHolder) holder).binding.imageWrapperRelativeLayout.setVisibility(View.VISIBLE);
                    if (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0) {
                        int height = (int) (400 * mScale);
                        ((TrendingSearchViewHolder) holder).binding.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ((TrendingSearchViewHolder) holder).binding.imageView.getLayoutParams().height = height;
                        preview.setPreviewWidth(imageViewWidth);
                        preview.setPreviewHeight(height);
                    } else {
                        ((TrendingSearchViewHolder) holder).binding.imageView
                                .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                    }
                    loadImage((TrendingSearchViewHolder) holder, preview);
                } else {
                    ((TrendingSearchViewHolder) holder).binding.imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                }
            }

            ((TrendingSearchViewHolder) holder).binding.titleTextView.setText(trendingSearch.displayString);
        }
    }

    @Override
    public int getItemCount() {
        return trendingSearches == null ? 0 : trendingSearches.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TrendingSearchViewHolder) {
            glide.clear(((TrendingSearchViewHolder) holder).binding.imageView);
            ((TrendingSearchViewHolder) holder).binding.imageWrapperRelativeLayout.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).binding.loadImageErrorRelativeLayout.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).binding.imageViewNoPreviewGallery.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Nullable
    private Post.Preview getSuitablePreview(ArrayList<Post.Preview> previews) {
        Post.Preview preview;
        if (!previews.isEmpty()) {
            int previewIndex;
            if (dataSavingMode && previews.size() > 2) {
                previewIndex = previews.size() / 2;
            } else {
                previewIndex = 0;
            }
            preview = previews.get(previewIndex);
            if (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000) {
                for (int i = previews.size() - 1; i >= 1; i--) {
                    preview = previews.get(i);
                    if (imageViewWidth >= preview.getPreviewWidth()) {
                        if (preview.getPreviewWidth() * preview.getPreviewHeight() <= 10_000_000) {
                            return preview;
                        }
                    } else {
                        int height = imageViewWidth / preview.getPreviewWidth() * preview.getPreviewHeight();
                        if (imageViewWidth * height <= 10_000_000) {
                            return preview;
                        }
                    }
                }
            }

            if (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000) {
                int divisor = 2;
                do {
                    preview.setPreviewWidth(preview.getPreviewWidth() / divisor);
                    preview.setPreviewHeight(preview.getPreviewHeight() / divisor);
                    divisor *= 2;
                } while (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000);
            }
            return preview;
        }

        return null;
    }

    private void loadImage(final TrendingSearchViewHolder holder, @NonNull Post.Preview preview) {
        String url = preview.getPreviewUrl();
        RequestBuilder<Drawable> imageRequestBuilder = glide.load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.binding.progressBar.setVisibility(View.GONE);
                holder.binding.loadImageErrorRelativeLayout.setVisibility(View.VISIBLE);
                holder.binding.loadImageErrorRelativeLayout.setOnClickListener(view -> {
                    holder.binding.progressBar.setVisibility(View.VISIBLE);
                    holder.binding.loadImageErrorRelativeLayout.setVisibility(View.GONE);
                    loadImage(holder, preview);
                });
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.binding.loadImageErrorRelativeLayout.setVisibility(View.GONE);
                holder.binding.progressBar.setVisibility(View.GONE);
                return false;
            }
        });

        if (imageViewWidth > preview.getPreviewWidth()) {
            imageRequestBuilder.override(preview.getPreviewWidth(), preview.getPreviewHeight()).into(holder.binding.imageView);
        } else {
            imageRequestBuilder.into(holder.binding.imageView);
        }
    }

    public void setTrendingSearches(ArrayList<TrendingSearch> trendingSearches) {
        if (trendingSearches != null) {
            this.trendingSearches = trendingSearches;
            notifyItemRangeInserted(0, trendingSearches.size());
        } else {
            int size = this.trendingSearches == null ? 0 : this.trendingSearches.size();
            this.trendingSearches = null;
            notifyItemRangeRemoved(0, size);
        }
    }

    class TrendingSearchViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrendingSearchBinding binding;

        public TrendingSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTrendingSearchBinding.bind(itemView);

            binding.imageViewNoPreviewGallery.setBackgroundColor(customThemeWrapper.getNoPreviewPostTypeBackgroundColor());
            binding.imageViewNoPreviewGallery.setColorFilter(customThemeWrapper.getNoPreviewPostTypeIconTint(), android.graphics.PorterDuff.Mode.SRC_IN);
            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(customThemeWrapper.getColorAccent()));
            binding.loadImageErrorTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                binding.titleTextView.setTypeface(activity.typeface);
                binding.loadImageErrorTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                itemClickListener.onClick(trendingSearches.get(getBindingAdapterPosition()));
            });
        }
    }

    public interface ItemClickListener {
        void onClick(TrendingSearch trendingSearch);
    }
}
