package tech.DevAsh.keyOS.Helpers.KioskHelpers;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**

 */
public class PasswordPromptSheet extends BottomSheetDialogFragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_prompt_sheet, container, false);
    }
}