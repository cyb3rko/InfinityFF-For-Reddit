package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemTranslationContributorBinding;
import ml.docilealligator.infinityforreddit.settings.Translation;

public class TranslationFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private int primaryTextColor;
    private int secondaryTextColor;
    private ArrayList<Translation> translationContributors;

    public TranslationFragmentRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        translationContributors = Translation.getTranslationContributors();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TranslationContributorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_translation_contributor, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TranslationContributorViewHolder) {
            Translation translation = translationContributors.get(position);
            if (translation.flagDrawableId < 0) {
                ((TranslationContributorViewHolder) holder).binding.countryFlagImageView.setImageDrawable(null);
            } else {
                ((TranslationContributorViewHolder) holder).binding.countryFlagImageView.setImageResource(translation.flagDrawableId);
            }
            ((TranslationContributorViewHolder) holder).binding.languageNameTextView.setText(translation.language);
            ((TranslationContributorViewHolder) holder).binding.contributorNamesTextView.setText(translation.contributors);
        }
    }

    @Override
    public int getItemCount() {
        return translationContributors.size();
    }

    class TranslationContributorViewHolder extends RecyclerView.ViewHolder {
        private final ItemTranslationContributorBinding binding;

        public TranslationContributorViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTranslationContributorBinding.bind(itemView);

            if (activity.typeface != null) {
                binding.languageNameTextView.setTypeface(activity.typeface);
                binding.contributorNamesTextView.setTypeface(activity.typeface);
            }

            binding.languageNameTextView.setTextColor(primaryTextColor);
            binding.contributorNamesTextView.setTextColor(secondaryTextColor);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://poeditor.com/join/project?hash=b2IRyfaJv6"));
                activity.startActivity(intent);
            });
        }
    }
}
