package com.sabo.sabostore.BottomSheet;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sabo.sabostore.EventBus.ActionProfilePhotoEvent;
import com.sabo.sabostore.R;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilePhotoSheetFragment extends BottomSheetDialogFragment {

    private static ProfilePhotoSheetFragment instance;
    private LinearLayout removePhoto, changePhoto;

    public static ProfilePhotoSheetFragment getInstance() {
        if (instance == null)
            instance = new ProfilePhotoSheetFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile_photo_sheet, container, false);

        initViews(root);

        return root;
    }

    private void initViews(View root) {
        removePhoto = root.findViewById(R.id.removePhoto);
        changePhoto = root.findViewById(R.id.changePhoto);

        removePhoto.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new ActionProfilePhotoEvent(true, false));
            instance.dismiss();
        });

        changePhoto.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new ActionProfilePhotoEvent(false, true));
            instance.dismiss();
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((View) getView().getParent()).setBackgroundColor(Color.TRANSPARENT);
    }
}
