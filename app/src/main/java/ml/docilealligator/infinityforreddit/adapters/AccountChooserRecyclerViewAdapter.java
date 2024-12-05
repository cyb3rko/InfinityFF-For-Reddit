package ml.docilealligator.infinityforreddit.adapters;

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
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerAccountBinding;

public class AccountChooserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Account> accounts;
    private RequestManager glide;
    private int primaryTextColor;
    private ItemClickListener itemClickListener;

    public AccountChooserRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper,
                                             RequestManager glide, ItemClickListener itemClickListener) {
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_drawer_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            glide.load(accounts.get(position).getProfileImageUrl())
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                    .into(((AccountViewHolder) holder).binding.profileImage);
            ((AccountViewHolder) holder).binding.usernameTextView.setText(accounts.get(position).getAccountName());
            holder.itemView.setOnClickListener(view ->
                    itemClickListener.onClick(accounts.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return accounts == null ? 0 : accounts.size();
    }

    public void changeAccountsDataset(List<Account> accounts) {
        this.accounts = (ArrayList<Account>) accounts;
        notifyDataSetChanged();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        private final ItemNavDrawerAccountBinding binding;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNavDrawerAccountBinding.bind(itemView);
            binding.usernameTextView.setTextColor(primaryTextColor);
        }
    }

    public interface ItemClickListener {
        void onClick(Account account);
    }
}
