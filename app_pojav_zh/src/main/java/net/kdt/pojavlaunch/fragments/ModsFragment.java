package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddModButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
    private String mRootPath;
    private TextView mFilePathView;

    public ModsFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("jar"),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                        //使用AsyncTask在后台线程中执行文件复制
                        new CopyFile().execute(result);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();
        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(false);
        mFileListView.lockPathAt(new File(mRootPath));
        mFileListView.refreshPath();

        mFilePathView.setText(getString(R.string.zh_profile_mods));
        mAddModButton.setText(getString(R.string.zh_profile_mods_add_mod));

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                String fileName = file.getName();
                String fileParent = file.getParent();
                String disableString = "(" + getString(R.string.zh_profile_mods_disable) + ")";

                FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                filesButton.setButtonVisibility(true, true, true, true);
                filesButton.messageText = getString(R.string.zh_file_message);
                if (fileName.endsWith(".jar")) filesButton.moreButtonText = getString(R.string.zh_profile_mods_disable);
                else if (fileName.endsWith(".disabled")) filesButton.moreButtonText = getString(R.string.zh_profile_mods_enable);

                FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, mFileListView, file);
                //检测后缀名，以设置正确的按钮
                if (fileName.endsWith(".jar")) {
                    filesDialog.setMoreButtonClick(v -> {
                        File newFile = new File(fileParent, disableString + fileName + ".disabled");
                        boolean disable = file.renameTo(newFile);
                        if (disable) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_disabled) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                        filesDialog.dismiss();
                    });
                }
                else if (fileName.endsWith(".disabled")) {
                    filesDialog.setMoreButtonClick(v -> {
                        int index = fileName.indexOf(disableString);
                        if (index == -1) index = 0;
                        else if (index == 0) index = disableString.length();
                        File newFile = new File(fileParent, fileName.substring(index, fileName.lastIndexOf('.')));
                        boolean disable = file.renameTo(newFile);
                        if (disable) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_enabled) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                        filesDialog.dismiss();
                    });
                }

                filesDialog.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                PojavZHTools.shareFileAlertDialog(requireContext(), file);
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mAddModButton.setOnClickListener(v -> {
            String suffix = ".jar";
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        });
        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mod_title));
            builder.setMessage(getString(R.string.zh_help_mod_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            copyFileInBackground(requireContext(), uris, mRootPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(requireContext(), getString(R.string.zh_profile_mods_added_mod), Toast.LENGTH_SHORT).show();
            mFileListView.refreshPath();
        }
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddModButton = view.findViewById(R.id.zh_files_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
    }
}

