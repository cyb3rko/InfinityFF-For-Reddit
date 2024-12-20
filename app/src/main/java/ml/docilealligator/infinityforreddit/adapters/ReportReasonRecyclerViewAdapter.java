package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.ReportReason;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemReportReasonBinding;

public class ReportReasonRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ReportReason> generalReasons;
    private ArrayList<ReportReason> rules;
    private int primaryTextColor;
    private int colorAccent;

    public ReportReasonRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper, ArrayList<ReportReason> generalReasons) {
        this.generalReasons = generalReasons;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReasonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_reason, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReasonViewHolder) {
            ReportReason reportReason;
            if (position >= generalReasons.size()) {
                reportReason = rules.get(holder.getBindingAdapterPosition() - generalReasons.size());
            } else {
                reportReason = generalReasons.get(holder.getBindingAdapterPosition());
            }
            ((ReasonViewHolder) holder).binding.reasonTextView.setText(reportReason.getReportReason());
            ((ReasonViewHolder) holder).binding.checkBox.setChecked(reportReason.isSelected());
        }
    }

    @Override
    public int getItemCount() {
        return rules == null ? generalReasons.size() : rules.size() + generalReasons.size();
    }

    public void setRules(ArrayList<ReportReason> reportReasons) {
        this.rules = reportReasons;
        notifyDataSetChanged();
    }

    public ReportReason getSelectedReason() {
        if (rules != null) {
            for (ReportReason reportReason : rules) {
                if (reportReason.isSelected()) {
                    return reportReason;
                }
            }
        }

        for (ReportReason reportReason : generalReasons) {
            if (reportReason.isSelected()) {
                return reportReason;
            }
        }

        return null;
    }

    public ArrayList<ReportReason> getGeneralReasons() {
        return generalReasons;
    }

    public ArrayList<ReportReason> getRules() {
        return rules;
    }

    class ReasonViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportReasonBinding binding;

        ReasonViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReportReasonBinding.bind(itemView);

            binding.reasonTextView.setTextColor(primaryTextColor);
            binding.checkBox.setButtonTintList(ColorStateList.valueOf(colorAccent));

            binding.checkBox.setOnClickListener(view -> {
                for (int i = 0; i < generalReasons.size(); i++) {
                    if (generalReasons.get(i).isSelected()) {
                        generalReasons.get(i).setSelected(false);
                        notifyItemChanged(i);

                    }
                }

                if (rules != null) {
                    for (int i = 0; i < rules.size(); i++) {
                        if (rules.get(i).isSelected()) {
                            rules.get(i).setSelected(false);
                            notifyItemChanged(i + generalReasons.size());
                        }
                    }
                }

                if (getBindingAdapterPosition() >= generalReasons.size()) {
                    rules.get(getBindingAdapterPosition() - generalReasons.size()).setSelected(binding.checkBox.isChecked());
                } else {
                    generalReasons.get(getBindingAdapterPosition()).setSelected(binding.checkBox.isChecked());
                }
            });

            itemView.setOnClickListener(view -> binding.checkBox.performClick());
        }
    }
}
