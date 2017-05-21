package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 5/3/2017.
 */

public class NewBucketDialogFragment extends DialogFragment {

    @BindView(R.id.new_bucket_name) EditText bucketName;
    @BindView(R.id.new_bucket_description) EditText bucketDescription;

    public static final String TAG = "NewBucketDialogFragment";
    public static final String KEY_BUCKET_NAME = "bucket_name";
    public static final String KEY_BUCKET_DESCRIPTION = "bucket_description";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_bucket, null);
        ButterKnife.bind(this, view);

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra(KEY_BUCKET_NAME, bucketName.getText().toString());
                        intent.putExtra(KEY_BUCKET_DESCRIPTION, bucketDescription.getText().toString());
                        //use setTargetFragment & getTargetFragment for data transfer between two Fragments
                        //similar idea to startActivityForResult, which is for data transfer between two Activities
                        getTargetFragment().onActivityResult(BucketListFragment.DIALOG_REQ_CODE, Activity.RESULT_OK, intent);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

    }

}
