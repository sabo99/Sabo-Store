package com.sabo.sabostore.BottomSheet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.UpdateProfileNameEvent;
import com.sabo.sabostore.R;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileNameSheetFragment extends BottomSheetDialogFragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private TextInputLayout tilName;
    private EditText etName;
    private Button btnSAVE, btnCANCEL;

    public static ProfileNameSheetFragment instance;

    public static ProfileNameSheetFragment getInstance() {
        if (instance == null)
            instance = new ProfileNameSheetFragment();
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();
        etName.setText(Common.currentUser.getName());
        etName.requestFocus();
        countSuffixText(etName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile_name_sheet, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());

        initViews(root);

        return root;
    }

    private void initViews(View root) {
        tilName = root.findViewById(R.id.tilName);
        etName = root.findViewById(R.id.etName);
        btnSAVE = root.findViewById(R.id.btnSAVE);
        btnCANCEL = root.findViewById(R.id.btnCANCEL);

//        etName.setbac

        btnCANCEL.setOnClickListener(v -> {
            instance.dismiss();
        });

        btnSAVE.setOnClickListener(v -> {
            String name = etName.getText().toString();
            updateName(name);
        });

    }

    private void updateName(String name) {
        if (TextUtils.isEmpty(name)){
            Toast.makeText(getContext(), "Name can't be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            instance.dismiss();

            HashMap<String, Object> updateName = new HashMap<>();
            updateName.put(Common.KEY_NAME, name);

            userRef.updateChildren(updateName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            EventBus.getDefault().postSticky(new UpdateProfileNameEvent(true, name));
                            Log.d("success", "Update Name Successfully.");
                        }
                        else
                            Log.d("failed", task.getException().getMessage());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("failure", e.getMessage());
                    });
        }
    }

    private void countSuffixText(EditText etName) {
        tilName.setSuffixText(String.valueOf(25 - etName.getText().toString().length()));

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countSuffix(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void countSuffix(CharSequence suffix) {
        int count = 0;
        if (suffix.length() < 26) {
            count = 25 - suffix.length();
            tilName.setSuffixText(String.valueOf(count));
        } else {
            tilName.setSuffixText("25");
            etName.endBatchEdit();
        }
    }
}
