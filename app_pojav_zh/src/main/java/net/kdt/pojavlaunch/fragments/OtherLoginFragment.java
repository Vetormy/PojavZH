package net.kdt.pojavlaunch.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.kdt.mcgui.MineButton;
import com.kdt.mcgui.MineEditText;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.login.AuthResult;
import net.kdt.pojavlaunch.login.OtherLoginApi;
import net.kdt.pojavlaunch.login.Servers;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtherLoginFragment extends Fragment {
    public static final String TAG = "OtherLoginFragment";
    private ProgressDialog mProgressDialog;
    private Spinner mServerSpinner;
    private MineEditText mUserEditText;
    private MineEditText mPassEditText;
    private MineButton mLoginButton;
    private TextView mRegister;
    private ImageButton mAddServer, mHelpButton;
    private File mServersFile;
    private Servers mServers;
    private List<String> mServerList;
    public String mCurrentBaseUrl;
    private String mCurrentRegisterUrl;
    private ArrayAdapter<String> mServerSpinnerAdapter;


    public OtherLoginFragment() {
        super(R.layout.fragment_other_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        mServersFile = new File(Tools.DIR_GAME_HOME, "servers.json");
        mProgressDialog = new ProgressDialog(requireContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        refreshServer();

        mHelpButton.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_other_login_title));
            builder.setMessage(getString(R.string.zh_help_other_login_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });

        mServerSpinner.setAdapter(mServerSpinnerAdapter);
        mServerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!Objects.isNull(mServers)) {
                    for (Servers.Server server : mServers.getServer()) {
                        if (server.getServerName().equals(mServerList.get(i))) {
                            mCurrentBaseUrl = server.getBaseUrl();
                            mCurrentRegisterUrl = server.getRegister();
                            Log.e("test", "currentRegisterUrl:" + mCurrentRegisterUrl);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mAddServer.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.zh_other_login_add_server))
                    .setItems(new String[]{getString(R.string.zh_other_login_external_login), getString(R.string.zh_other_login_uniform_pass)}, (d, i) -> {
                        EditText editText = new EditText(requireContext());
                        editText.setMaxLines(1);
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        AlertDialog dialog1 = new AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.zh_tip))
                                .setView(editText)
                                .setPositiveButton(getString(R.string.zh_confirm), (dialogInterface, i1) -> {
                                    mProgressDialog.show();
                                    PojavApplication.sExecutorService.execute(() -> {
                                        String data = OtherLoginApi.getINSTANCE().getServeInfo(i==0?editText.getText().toString():"https://auth.mc-user.com:233/" + editText.getText().toString());
                                        requireActivity().runOnUiThread(() -> {
                                            mProgressDialog.dismiss();
                                            if (!Objects.isNull(data)) {
                                                try {
                                                    Servers.Server server = new Servers.Server();
                                                    JSONObject jsonObject = new JSONObject(data);
                                                    JSONObject meta = jsonObject.optJSONObject("meta");
                                                    server.setServerName(meta.optString("serverName"));
                                                    server.setBaseUrl(editText.getText().toString());
                                                    if (i == 0) {
                                                        JSONObject links = meta.optJSONObject("links");
                                                        server.setRegister(links.optString("register"));
                                                    } else {
                                                        server.setBaseUrl("https://auth.mc-user.com:233/" + editText.getText().toString());
                                                        server.setRegister("https://login.mc-user.com:233/" + editText.getText().toString() + "/loginreg");
                                                    }
                                                    if (Objects.isNull(mServers)) {
                                                        mServers = new Servers();
                                                        mServers.setServer(new ArrayList<>());
                                                    }
                                                    mServers.getServer().add(server);
                                                    Tools.write(mServersFile.getAbsolutePath(), Tools.GLOBAL_GSON.toJson(mServers, Servers.class));
                                                    refreshServer();
                                                    mCurrentBaseUrl = server.getBaseUrl();
                                                    mCurrentRegisterUrl = server.getRegister();
                                                } catch (Exception e) {
                                                    Log.e("test", e.toString());
                                                }
                                            }
                                        });
                                    });

                                })
                                .setNegativeButton(getString(android.R.string.cancel), null)
                                .create();
                        if (i == 0) {
                            editText.setHint(getString(R.string.zh_other_login_yggdrasil_api));
                        } else {
                            editText.setHint(getString(R.string.zh_other_login_32_bit_server));
                        }
                        dialog1.show();
                    })
                    .setNegativeButton(getString(android.R.string.cancel), null)
                    .create();
            dialog.show();
        });

        mRegister.setOnClickListener(v -> {
            if (!Objects.isNull(mCurrentRegisterUrl)) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri url = Uri.parse(mCurrentRegisterUrl);
                intent.setData(url);
                startActivity(intent);
            }
        });

        mLoginButton.setOnClickListener(v->{
            mProgressDialog.show();
            PojavApplication.sExecutorService.execute(()->{
                String user= mUserEditText.getText().toString();
                String pass= mPassEditText.getText().toString();
                if (!user.isEmpty() && !pass.isEmpty()){
                    try {
                        OtherLoginApi.getINSTANCE().setBaseUrl(mCurrentBaseUrl);
                        OtherLoginApi.getINSTANCE().login(getContext(), user, pass, new OtherLoginApi.Listener() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                requireActivity().runOnUiThread(()->{
                                    mProgressDialog.dismiss();
                                    MinecraftAccount account=new MinecraftAccount();
                                    account.accessToken=authResult.getAccessToken();
                                    account.baseUrl= mCurrentBaseUrl;
                                    account.account= mUserEditText.getText().toString();
                                    account.password= mPassEditText.getText().toString();
                                    account.expiresAt=System.currentTimeMillis()+30*60*1000;
                                    if (!Objects.isNull(authResult.getSelectedProfile())){
                                        account.username=authResult.getSelectedProfile().getName();
                                        account.profileId=authResult.getSelectedProfile().getId();
                                        ExtraCore.setValue(ExtraConstants.OTHER_LOGIN_TODO, account);
                                        Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
                                    } else {
                                        List<String> list=new ArrayList<>();
                                        for(AuthResult.AvailableProfiles profiles:authResult.getAvailableProfiles()){
                                            list.add(profiles.getName());
                                        }
                                        String[] items=list.toArray(new String[0]);
                                        AlertDialog dialog=new AlertDialog.Builder(requireContext())
                                                .setTitle("")
                                                .setItems(items,(d,i)->{
                                                    for(AuthResult.AvailableProfiles profiles:authResult.getAvailableProfiles()){
                                                        if(profiles.getName().equals(items[i])){
                                                            account.profileId=profiles.getId();
                                                            account.username=profiles.getName();
                                                        }
                                                    }
                                                    ExtraCore.setValue(ExtraConstants.OTHER_LOGIN_TODO, account);
                                                    Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
                                                })
                                                .setNegativeButton(getString(android.R.string.cancel),null)
                                                .create();
                                        dialog.show();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(String error) {
                                requireActivity().runOnUiThread(()->{
                                    mProgressDialog.dismiss();
                                    AlertDialog dialog=new AlertDialog.Builder(requireContext())
                                            .setTitle(getString(R.string.zh_warning))
                                            .setTitle(getString(R.string.zh_other_login_error) + error)
                                            .setPositiveButton(getString(R.string.zh_confirm),null)
                                            .create();
                                    dialog.show();
                                });
                            }
                        });
                    } catch (IOException e) {
                        requireActivity().runOnUiThread(()-> mProgressDialog.dismiss());
                        Log.e("login",e.toString());
                    }
                }
            });
        });
    }

    private void bindViews(@NonNull View view) {
        mHelpButton = view.findViewById(R.id.zh_other_login_help_button);
        mServerSpinner = view.findViewById(R.id.server_spinner);
        mUserEditText = view.findViewById(R.id.login_edit_email);
        mPassEditText = view.findViewById(R.id.login_edit_password);
        mLoginButton = view.findViewById(R.id.login_button);
        mRegister = view.findViewById(R.id.register);
        mAddServer = view.findViewById(R.id.add_server);
    }

    public void refreshServer() {
        if (Objects.isNull(mServerList)) {
            mServerList = new ArrayList<>();
        } else {
            mServerList.clear();
        }
        if (mServersFile.exists()) {
            try {
                mServers = new Gson().fromJson(Tools.read(mServersFile.getAbsolutePath()), Servers.class);
                mCurrentBaseUrl = mServers.getServer().get(0).getBaseUrl();
                for (Servers.Server server : mServers.getServer()) {
                    mServerList.add(server.getServerName());
                }
            } catch (IOException ignored) {

            }
        }
        if (Objects.isNull(mServers)) {
            mServerList.add(getString(R.string.zh_other_login_no_server));
        }
        if (Objects.isNull(mServerSpinnerAdapter)) {
            mServerSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mServerList);
        } else {
            mServerSpinnerAdapter.notifyDataSetChanged();
        }

    }
}