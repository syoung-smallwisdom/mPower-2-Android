package org.sagebionetworks.research.mpower.authentication;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.google.common.base.Strings;

import org.sagebionetworks.research.mpower.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import dagger.android.AndroidInjection;

public class ExternalIdSignInActivity extends AppCompatActivity {
    ExternalIdSignInViewModel externalIdSignInViewModel;

    @Inject
    ExternalIdSignInViewModel.Factory externalIdSignInViewModelFactory;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.signIn)
    Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_id_sign_in);
        ButterKnife.bind(this);

        externalIdSignInViewModel = ViewModelProviders.of(this, externalIdSignInViewModelFactory.create())
                .get(ExternalIdSignInViewModel.class);

        externalIdSignInViewModel.getIsSignedInLiveData().observe(this, isSignedIn -> {
            if (isSignedIn) {
                finish();
            }
        });

        externalIdSignInViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            progressBar.setIndeterminate(isLoading);
        });

        externalIdSignInViewModel.getErrorMessageLiveData().observe(this, this::onErrorMessage);

        externalIdSignInViewModel.getIsExternalIdValid().observe(this, isValid -> signIn.setEnabled(isValid));
    }

    void onErrorMessage(String errorMessage) {
        if (Strings.isNullOrEmpty(errorMessage)) {
            return;
        }
        Snackbar
                .make(signIn, errorMessage, LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar snackbar) {
                        externalIdSignInViewModel.onErrorMessageConsumed();
                    }

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        // do some action on dismiss
                    }
                })
                .show();
    }

    @OnTextChanged(R.id.externalId)
    void onExternalIdChanged(CharSequence externalId) {
        externalIdSignInViewModel.setExternalId(externalId.toString());
    }

    @OnTextChanged(R.id.firstName)
    void onFirstNameChanged(CharSequence firstName) {
        externalIdSignInViewModel.setFirstName(firstName.toString());
    }

    @OnClick(R.id.signIn)
    void onSignInClicked() {
        externalIdSignInViewModel.doSignIn();
    }

    @OnCheckedChanged(R.id.skipConsent)
    void onSkipConsent(CheckBox skipConsentCheckBox) {
        externalIdSignInViewModel.setSkipConsent(skipConsentCheckBox.isChecked());
    }
}
