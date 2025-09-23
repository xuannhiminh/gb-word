package com.pdf.pdfreader.pdfviewer.editor.screen.language;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pdf.pdfreader.pdfviewer.editor.databinding.ItemLanguage2Binding;
import com.pdf.pdfreader.pdfviewer.editor.databinding.ItemLanguageChildBinding;
import com.pdf.pdfreader.pdfviewer.editor.databinding.ItemLanguageGroupBinding;

import java.util.ArrayList;
import java.util.List;

public class Language2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ItemSelected> list;
    private final List<ItemSelected> displayList = new ArrayList<>();
    private final Context context;
    private String selected;
    private final OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(ItemSelected item);
    }
    public Language2Adapter(List<ItemSelected> list, Context context, String selected, OnLanguageSelectedListener listener) {
        this.list = list;
        this.context = context;
        this.selected = selected;
        this.listener = listener;
        for (ItemSelected item : list) {
            if (item.getGroup() == 1) {
                item.setExpanded(true);
            }
        }
        updateDisplayList();
    }

    public String getSelected() {
        return selected;
    }

    public List<ItemSelected> getDisplayList() {
        return displayList;
    }

    private ItemSelected findGroupByValue(String value) {
        for (ItemSelected item : list) {
            if (item.getGroup() == 1 && item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    private void updateDisplayList() {
        int oldSize = displayList.size();
        displayList.clear();
        for (ItemSelected item : list) {
            if (item.getGroup() == 1 || item.getGroup() == 0) {
                displayList.add(item);
            } else if (item.getParentGroup() != null) {
                ItemSelected parentGroup = findGroupByValue(item.getParentGroup());
                if (parentGroup != null && parentGroup.isExpanded()) {
                    displayList.add(item);
                }
            }
        }
        int newSize = displayList.size();

        if (oldSize == newSize) {
            notifyItemRangeChanged(0, newSize);
        } else if (oldSize < newSize) {
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else {
            notifyItemRangeChanged(0, newSize);
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position).getGroup();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            ItemLanguageGroupBinding groupBinding = ItemLanguageGroupBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new GroupViewHolder(groupBinding);
        } else if (viewType == 2) {
            ItemLanguageChildBinding childBinding = ItemLanguageChildBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new LanguageViewHolder(childBinding);
        } else {
            ItemLanguage2Binding languageBinding = ItemLanguage2Binding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new LanguageViewHolder(languageBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemSelected item = displayList.get(position);
        if (item.getGroup() == 1) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.binding.tvGroupName.setText(context.getString(item.getEntry()));
            groupHolder.binding.ivExpand.setRotation(item.isExpanded() ? 180 : 0);
            groupHolder.binding.getRoot().setOnClickListener(v -> {
                item.setExpanded(!item.isExpanded());
                updateDisplayList();
            });
        } else if (item.getGroup() != 1) { // Chỉ render các item không phải nhóm
            LanguageViewHolder languageHolder = (LanguageViewHolder) holder;
            bindData(item, languageHolder);
        }
    }

    private void bindData(ItemSelected item, LanguageViewHolder holder) {
        View root;
        String name = context.getString(item.getEntry());
        String nameEn = getLanguageNameEn(item.getValue());
        if (holder.binding2 != null) {
            root = holder.binding2.getRoot();
            holder.binding2.tvTitle.setText(name);
            holder.binding2.tvTitleEn.setText(nameEn);
            Glide.with(context)
                .load(Toolbox.getFlagCountry(item.getValue()))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into(holder.binding2.ivFlag);
            holder.binding2.rdSelect.setVisibility(View.VISIBLE);
            holder.binding2.rdSelect.setChecked(TextUtils.equals(item.getValue(), selected));
        } else {
            root = holder.bindingChild.getRoot();
            holder.bindingChild.tvTitle.setText(name);
            holder.bindingChild.tvTitleEn.setText(nameEn);
            Glide.with(context)
                .load(Toolbox.getFlagCountry(item.getValue()))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into(holder.bindingChild.ivFlag);
            holder.bindingChild.rdSelect.setVisibility(View.VISIBLE);
            holder.bindingChild.rdSelect.setChecked(TextUtils.equals(item.getValue(), selected));
        }

        root.setOnClickListener(v -> {
            selected = item.getValue();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onLanguageSelected(item);
            }
        });
    }

    private String getLanguageNameEn(String code) {
        switch (code) {
            case "en-US":
                return "English (US)";
            case "en-UK":
                return "English (UK)";
            case "en-AU":
                return "English (Australia)";
            case "en-IN":
                return "English (India)";
            case "vi":
                return "Vietnamese";
            case "pt":
                return "Portuguese (Portugal)";
            case "pt-BR":
                return "Portuguese (Brazil)";
            case "es":
                return "Spanish";
            case "es-US":
                return "Spanish (US)";
            case "es-MX":
                return "Spanish (Latin America)";
            case "de":
                return "German";
            case "fr":
                return "French";
            case "fr-CA":
                return "French (Canada)";
            case "in":
                return "Indonesian";
            case "ar":
                return "Arabic";
            case "it":
                return "Italian";
            case "tr":
                return "Turkish";
            case "uk":
                return "Ukrainian";
            case "nl":
                return "Dutch";
            case "ja":
                return "Japanese";
            case "fa":
                return "Persian";
            case "zh":
                return "Chinese (Simplified)";
            case "zh-TW":
                return "Chinese (Traditional)";
            case "ko":
                return "Korean";
            case "ru":
                return "Russian";
            case "hi":
                return "Hindi";
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void filter(String query) {
        int oldSize = displayList.size();
        displayList.clear();
        if (TextUtils.isEmpty(query)) {
            displayList.addAll(list);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ItemSelected item : list) {
                String name = context.getString(item.getEntry());
                String nameEn = getLanguageNameEn(item.getValue());
                if (name.toLowerCase().contains(lowerQuery)
                        || item.getValue().toLowerCase().contains(lowerQuery)
                        || nameEn.toLowerCase().contains(lowerQuery)
                ) {
                    displayList.add(item);
                }
            }
        }
        int newSize = displayList.size();
        
        if (oldSize == newSize) {
            notifyItemRangeChanged(0, newSize);
        } else if (oldSize < newSize) {
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else {
            notifyItemRangeChanged(0, newSize);
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        }
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ItemLanguageGroupBinding binding;

        public GroupViewHolder(ItemLanguageGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ItemLanguage2Binding binding2;
        ItemLanguageChildBinding bindingChild;

        public LanguageViewHolder(ItemLanguage2Binding binding) {
            super(binding.getRoot());
            this.binding2 = binding;
        }

        public LanguageViewHolder(ItemLanguageChildBinding binding) {
            super(binding.getRoot());
            this.bindingChild = binding;
        }
    }
}
