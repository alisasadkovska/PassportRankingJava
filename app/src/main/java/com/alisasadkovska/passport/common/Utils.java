package com.alisasadkovska.passport.common;

import android.app.Activity;
import com.alisasadkovska.passport.R;

/**
 * Created by Death on 22/12/2017.
 */

public class Utils {

    private final static int THEME_LIGHT = 0;
    private final static int THEME_DARK = 1;


    public static void onActivityCreateSetTheme(Activity activity, int sTheme)
    {
        switch (sTheme)
        {
            default:
            case THEME_LIGHT:
                activity.setTheme(R.style.AppTheme);
                break;
            case THEME_DARK:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
    }
}
