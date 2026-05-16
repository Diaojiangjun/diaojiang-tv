package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.DialogHelpBinding;
import com.fongmi.android.tv.databinding.ItemHelpCategoryBinding;
import com.fongmi.android.tv.databinding.ItemHelpQaBinding;
import com.fongmi.android.tv.utils.HelpContent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class HelpDialog extends BaseDialog {

    private DialogHelpBinding binding;

    public static HelpDialog create() {
        return new HelpDialog();
    }

    public void show(Fragment fragment) {
        for (Fragment f : fragment.getChildFragmentManager().getFragments())
            if (f instanceof BottomSheetDialogFragment) return;
        show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogHelpBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        List<HelpContent.Category> categories = HelpContent.get().getCategories();
        
        for (HelpContent.Category category : categories) {
            addCategoryView(category);
        }
    }

    private void addCategoryView(HelpContent.Category category) {
        ItemHelpCategoryBinding categoryBinding = ItemHelpCategoryBinding.inflate(
                LayoutInflater.from(requireContext()), binding.contentContainer, false);
        
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
                    LayoutInflater.from(requireContext()), categoryBinding.contentCategory, false);
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

    @Override
    protected void initEvent() {
    }
}
