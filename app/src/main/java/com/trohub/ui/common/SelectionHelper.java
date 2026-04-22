package com.trohub.ui.common;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.List;

public final class SelectionHelper {
    private SelectionHelper() {}

    public static void bindOptions(AutoCompleteTextView view, List<IdLabelOption> options) {
        ArrayAdapter<IdLabelOption> adapter = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_dropdown_item_1line,
                options
        );
        view.setAdapter(adapter);
    }

    public static Long findIdByText(List<IdLabelOption> options, String rawText) {
        if (rawText == null) return null;
        String text = rawText.trim();
        if (text.isEmpty()) return null;
        for (IdLabelOption option : options) {
            if (option != null && option.toString().equalsIgnoreCase(text)) {
                return option.getId();
            }
        }
        return null;
    }

    public static String findLabelById(List<IdLabelOption> options, Long id) {
        if (id == null) return "";
        for (IdLabelOption option : options) {
            if (option != null && option.getId() != null && option.getId().equals(id)) {
                return option.toString();
            }
        }
        return "";
    }
}
