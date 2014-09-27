package universe.constellation.orion.viewer.opds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import universe.constellation.orion.viewer.R;

/**
 * Created by mike on 9/21/14.
 */
public class NewOPDSDialog extends DialogFragment {

    public static NewOPDSDialog newInstance() {
        NewOPDSDialog newOPDSDialog = new NewOPDSDialog();
        return newOPDSDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.opds_new, container, false);
        getDialog().setTitle("New entry");
        Button ok = (Button) view.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences opds = getActivity().getSharedPreferences("opds", Context.MODE_PRIVATE);
                EditText name = (EditText) getDialog().findViewById(R.id.opds_name);
                EditText url = (EditText) getDialog().findViewById(R.id.opds_url);
                SharedPreferences.Editor edit = opds.edit();
                edit.putString(url.getText().toString(), name.getText().toString());
                edit.commit();
                dismiss();
            }
        });
        Button cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}
