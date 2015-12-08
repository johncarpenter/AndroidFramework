package com.twolinessoftware.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.events.OnErrorEvent;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.utils.ValidationUtil;
import com.twolinessoftware.utils.ViewUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by John on 2015-04-02.
 */
public class LoginFragment extends  BaseFragment{

    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @Bind(R.id.edit_password)
    EditText mEditPassword;

    @Inject
    AccountManager mAccountManager;

    @Inject
    NetworkManager mNetworkManager;

    @Inject
    EventBus mEventBus;

    @Override
    protected int setContentView() {
        return R.layout.fragment_login;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getBaseActivity().setTitle(R.string.login_fragment_title);

        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.login_menu, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prepopulateAccount();
        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));

        mEditPassword.requestFocus();

        mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if (hasfocus) {
                mEditEmail.setText("");
            }
            mEditEmail.setOnFocusChangeListener(null);
        });
    }

    private void prepopulateAccount() {

        Account[] accounts = mAccountManager.getAccounts();
        for (Account account : accounts)
        {
            if(ValidationUtil.isValidEmail(account.name)){
                mEditEmail.setText(account.name);
                break;
            }
        }

    }

    @OnClick(R.id.button_login)
    public void onClickLogin(View view){
        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();

        if (validateFields(email,password)) {
            mNetworkManager.authenticate(mEditEmail.getText().toString(), mEditPassword.getText().toString());
        }
    }

    private boolean validateFields(String email, String password) {

        if(email == null || email.isEmpty() || password == null || password.isEmpty()){
            mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_EMPTY_FIELDS));
            return false;
        }

        if(!ValidationUtil.isValidEmail(email)){
            mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_INVALID_EMAIL));
            return false;
        }
        return true;
    }

    @OnClick(R.id.text_forgot_password)
    public void onClickForgotPassword(View view){
        getBaseActivity().showFragment(new ResetPasswordFragment(),true);
    }

}
