package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.App;
import com.github.catvod.utils.Asset;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HelpContent {

    @SerializedName("categories")
    private List<Category> categories;

    private static HelpContent instance;

    public static HelpContent get() {
        if (instance == null) {
            try {
                String json = Asset.read("help.json");
                instance = new Gson().fromJson(json, HelpContent.class);
            } catch (Exception e) {
                instance = new HelpContent();
            }
        }
        return instance;
    }

    public List<Category> getCategories() {
        return categories != null ? categories : new ArrayList<>();
    }

    public static class Category {
        @SerializedName("id")
        private String id;
        @SerializedName("title")
        private String title;
        @SerializedName("questions")
        private List<Question> questions;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public List<Question> getQuestions() {
            return questions != null ? questions : new ArrayList<>();
        }
    }

    public static class Question {
        @SerializedName("q")
        private String q;
        @SerializedName("a")
        private String a;

        public String getQ() {
            return q;
        }

        public String getA() {
            return a;
        }
    }
}
