package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UserFlair;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemUserFlairBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UserFlairRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private CustomThemeWrapper customThemeWrapper;
    private ArrayList<UserFlair> userFlairs;
    private ItemClickListener itemClickListener;

    public UserFlairRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper, ArrayList<UserFlair> userFlairs,
                                        ItemClickListener itemClickListener) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.userFlairs = userFlairs;
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(UserFlair userFlair, boolean editUserFlair);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserFlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_flair, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserFlairViewHolder) {
            if (position == 0) {
                ((UserFlairViewHolder) holder).binding.userFlairHtmlTextView.setText(R.string.clear_user_flair);
                ((UserFlairViewHolder) holder).binding.editUserFlairImageView.setVisibility(View.GONE);
            } else {
                UserFlair userFlair = userFlairs.get(holder.getBindingAdapterPosition() - 1);
                if (userFlair.getHtmlText() == null || userFlair.getHtmlText().equals("")) {
                    ((UserFlairViewHolder) holder).binding.userFlairHtmlTextView.setText(userFlair.getText());
                } else {
                    Utils.setHTMLWithImageToTextView(((UserFlairViewHolder) holder).binding.userFlairHtmlTextView, userFlair.getHtmlText(), true);
                }
                if (userFlair.isEditable()) {
                    ((UserFlairViewHolder) holder).binding.editUserFlairImageView.setVisibility(View.VISIBLE);
                } else {
                    ((UserFlairViewHolder) holder).binding.editUserFlairImageView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return userFlairs == null ? 1 : userFlairs.size() + 1;
    }

    class UserFlairViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserFlairBinding binding;

        public UserFlairViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemUserFlairBinding.bind(itemView);

            binding.userFlairHtmlTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            binding.editUserFlairImageView.setColorFilter(customThemeWrapper.getPrimaryTextColor(), android.graphics.PorterDuff.Mode.SRC_IN);

            if (activity.typeface != null) {
                binding.userFlairHtmlTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                if (getBindingAdapterPosition() == 0) {
                    itemClickListener.onClick(null, false);
                } else {
                    itemClickListener.onClick(userFlairs.get(getBindingAdapterPosition() - 1), false);
                }
            });

            binding.editUserFlairImageView.setOnClickListener(view -> {
                itemClickListener.onClick(userFlairs.get(getBindingAdapterPosition() - 1), true);
            });
        }
    }
}
