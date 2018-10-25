package jackg.myreceipts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bignerdranch.android.myreceipts.R;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReceiptFragment extends Fragment {

    private static final String ARG_RECEIPT_ID = "receipt_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 2;

    private Receipt mReceipt;
    private File mPhotoFile;
    private Button mDateButton;
    private ImageView mPhotoView;


    public static ReceiptFragment newInstance(UUID receiptId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECEIPT_ID, receiptId);

        ReceiptFragment fragment = new ReceiptFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        UUID receiptID = (UUID) getArguments().getSerializable(ARG_RECEIPT_ID);
        mReceipt = ReceiptLab.get(getActivity()).getReceipt(receiptID);
        mPhotoFile = ReceiptLab.get(getActivity()).getPhotoFile(mReceipt);

    }

    @Override
    public void onPause() {
        super.onPause();

        ReceiptLab.get(getActivity()).updateReceipt(mReceipt);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_receipt, container, false);

        EditText titleField = v.findViewById(R.id.receipt_title);
        titleField.setText(mReceipt.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mDateButton = v.findViewById(R.id.receipt_date);
        updateDate();
        mDateButton.setOnClickListener(view -> {
            FragmentManager manager = getFragmentManager();
            DatePickerFragment dialog = DatePickerFragment.newInstance(mReceipt.getDate());
            dialog.setTargetFragment(ReceiptFragment.this, REQUEST_DATE);
            assert manager != null;
            dialog.show(manager, DIALOG_DATE);
        });

        EditText shopName = v.findViewById(R.id.receipt_shop);
        shopName.setText(mReceipt.getShopName());
        shopName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setShopName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        EditText comment = v.findViewById(R.id.receipt_comment);
        comment.setText(mReceipt.getComment());
        comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        Button deleteButton = v.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v1 -> {
            ReceiptLab.get(getActivity()).deleteReceipt(mReceipt.getId());
            Objects.requireNonNull(getActivity()).finish();
        });


        Button reportButton = v.findViewById(R.id.receipt_report);
        reportButton.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, getReceiptReport());
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.receipt_report_subject));
            i = Intent.createChooser(i, getString(R.string.send_report));
            startActivity(i);
        });


        PackageManager packageManager = Objects.requireNonNull(getActivity()).getPackageManager();


        ImageButton photoButton = v.findViewById(R.id.receipt_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhtoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        photoButton.setEnabled(canTakePhtoto);
        photoButton.setOnClickListener(view -> {
            Uri uri = FileProvider.getUriForFile(getActivity(), "com.bignerdranch.android.myreceipts.fileprovider", mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo activity : cameraActivities) {
                getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(captureImage, REQUEST_PHOTO);
        });
        mPhotoView = v.findViewById(R.id.receipt_photo);
        updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mReceipt.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()), "com.bignerdranch.android.myreceipts.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mReceipt.getDate().toString());
    }

    private String getReceiptReport() {

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mReceipt.getDate()).toString();

        return getString(R.string.receipt_report, mReceipt.getTitle(), dateString, mReceipt.getShopName(), mReceipt.getComment());
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), Objects.requireNonNull(getActivity()));
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
