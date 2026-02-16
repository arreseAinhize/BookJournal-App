package eus.arreseainhize.bookjounal.utils;

import android.content.Context;
import androidx.annotation.StringRes;

import eus.arreseainhize.bookjounal.R;

public class StringResourceHelper {

    private static StringResourceHelper instance;
    private Context context;

    private StringResourceHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized StringResourceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StringResourceHelper(context);
        }
        return instance;
    }

    public String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    public String getString(@StringRes int resId, Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }

    // Métodos específicos para mensajes comunes
    public String getCompleteFieldsError() {
        return getString(R.string.error_complete_all_fields);
    }

    public String getPasswordMismatchError() {
        return getString(R.string.error_password_mismatch);
    }

    public String getPasswordLengthError() {
        return getString(R.string.error_password_length);
    }

    public String getWelcomeMessage() {
        return getString(R.string.success_welcome);
    }

    public String getAccountCreatedMessage() {
        return getString(R.string.success_account_created);
    }
}
