package com.example.multitradex.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public interface DeleteListener {
        void onConfirmDelete();
    }

    private DeleteListener listener;

    public ConfirmDeleteDialogFragment(DeleteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) listener.onConfirmDelete();
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
