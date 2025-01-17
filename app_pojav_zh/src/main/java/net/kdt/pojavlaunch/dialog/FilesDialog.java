package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class FilesDialog extends Dialog {
    private View.OnClickListener mMoreClick;
    private Button mMoreButton;
    private final boolean mCancel, mMore, mShare, mRename, mDelete;
    private final String mMessageText, mMoreText;
    private final FileListView mFileListView;
    private final File mFile;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, FileListView fileListView, File file) {
        super(context);
        this.mFileListView = fileListView;
        this.mFile = file;

        this.mCancel = true;
        this.mShare = filesButton.share;
        this.mRename = filesButton.rename;
        this.mDelete = filesButton.delete;
        this.mMore = filesButton.more;

        this.mMessageText = filesButton.messageText;
        this.mMoreText = filesButton.moreButtonText;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_files);
        init();
    }

    private void init() {
        TextView mMessage = findViewById(R.id.zh_files_message);
        Button mCancelButton = findViewById(R.id.zh_files_cancel);
        Button mShareButton = findViewById(R.id.zh_files_share);
        Button mRenameButton = findViewById(R.id.zh_files_rename);
        Button mDeleteButton = findViewById(R.id.zh_files_delete);
        mMoreButton = findViewById(R.id.zh_files_more);

        mCancelButton.setVisibility(mCancel ? View.VISIBLE : View.GONE);
        mShareButton.setVisibility(mShare ? View.VISIBLE : View.GONE);
        mRenameButton.setVisibility(mRename ? View.VISIBLE : View.GONE);
        mDeleteButton.setVisibility(mDelete ? View.VISIBLE : View.GONE);
        mMoreButton.setVisibility(mMore ? View.VISIBLE : View.GONE);

        if (this.mMessageText != null) mMessage.setText(this.mMessageText);
        mCancelButton.setOnClickListener(view -> FilesDialog.this.dismiss());
        mShareButton.setOnClickListener(view -> {
            shareFile(getContext(), mFile.getName(), mFile.getAbsolutePath());
            FilesDialog.this.dismiss();
        });
        mRenameButton.setOnClickListener(view -> {
            renameFileListener(getContext(), mFileListView, mFile, false);
            FilesDialog.this.dismiss();
        });
        mDeleteButton.setOnClickListener(view -> {
            deleteFileListener(getContext(), mFileListView, mFile, false);
            FilesDialog.this.dismiss();
        });

        if (this.mMoreText != null) mMoreButton.setText(this.mMoreText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mMore || this.mMoreClick != null) mMoreButton.setOnClickListener(this.mMoreClick);
    }

    public void setMoreButtonClick(View.OnClickListener click) {
        this.mMoreClick = click;
    }

    public static class FilesButton {
        public boolean cancel, share, rename, delete, more;
        public String messageText, moreButtonText;
        public void setButtonVisibility(boolean shareButton, boolean renameButton, boolean deleteButton, boolean moreButton) {
            this.share = shareButton;
            this.rename = renameButton;
            this.delete = deleteButton;
            this.more = moreButton;
        }
    }
}
