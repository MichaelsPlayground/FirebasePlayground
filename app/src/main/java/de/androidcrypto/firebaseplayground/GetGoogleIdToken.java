package de.androidcrypto.firebaseplayground;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

public class GetGoogleIdToken extends AsyncTask<Void, Void, String> {
    // taken from https://stackoverflow.com/a/47293464/8166854
    private final Context context;

    public GetGoogleIdToken(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            String scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE;
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            return GoogleAuthUtil.getToken(context, account.getAccount(), scope, new Bundle());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GoogleAuthException e) {
            e.printStackTrace();
        }
        return null;
    }
}
