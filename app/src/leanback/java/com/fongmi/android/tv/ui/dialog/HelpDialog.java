package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogHelpBinding;
import com.fongmi.android.tv.databinding.ItemHelpCategoryBinding;
import com.fongmi.android.tv.databinding.ItemHelpQaBinding;
import com.fongmi.android.tv.utils.HelpContent;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HelpDialog {

    private final DialogHelpBinding binding;
    private final AlertDialog dialog;

    public static HelpDialog create(Activity activity) {
        return new HelpDialog(activity);
    }

    public HelpDialog(Activity activity) {
        this.binding = DialogHelpBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.dialog_negative, null)
                .create();
    }

    public void show() {
        initView();
        
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (ResUtil.getScreenWidth() * 0.7f);
        params.height = (int) (ResUtil.getScreenHeight() * 0.8f);
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    private void initView() {
        List<HelpContent.Category> categories = HelpContent.get().getCategories();
        
        for (HelpContent.Category category : categories) {
            addCategoryView(category);
        }
    }

    private void addCategoryView(HelpContent.Category category) {
        ItemHelpCategoryBinding categoryBinding = ItemHelpCategoryBinding.inflate(
                LayoutInflater.from(dialog.getContext()), binding.contentContainer, false);
        
        categoryBinding.titleCategory.setText(category.getTitle());
        
        // Set click listener for category header
        categoryBinding.headerCategory.setOnClickListener(v -> {
            boolean isVisible = categoryBinding.contentCategory.getVisibility() == View.VISIBLE;
            categoryBinding.contentCategory.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            categoryBinding.arrowCategory.setRotation(isVisible ? 0 : 180);
        });

        // Add Q&A items
        for (HelpContent.Question question : category.getQuestions()) {
            ItemHelpQaBinding qaBinding = ItemHelpQaBinding.inflate(
                    LayoutInflater.from(dialog.getContext()), categoryBinding.contentCategory, false);
            qaBinding.textQuestion.setText(question.getQ());
            qaBinding.textAnswer.setText(question.getA());

            final boolean[] isExpanded = {false};
            qaBinding.headerQA.setOnClickListener(v -> {
                isExpanded[0] = !isExpanded[0];
                qaBinding.textAnswer.setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
                qaBinding.arrowQA.setRotation(isExpanded[0] ? 180 : 0);
            });

            categoryBinding.contentCategory.addView(qaBinding.getRoot());
        }

        binding.contentContainer.addView(categoryBinding.getRoot());
    }
}
