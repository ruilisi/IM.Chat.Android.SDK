package com.chat.android.im.config;

import android.content.Context;

import com.chat.android.im.helper.IMCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan on 2020/9/30.
 */
public class IM {
    private static final String BUSINESS_ID = "BUSINESS_ID";
    private static final Map<String, IM> map = new HashMap();

    private IM(Context context) {
        RLS.getInstance().init(context.getApplicationContext(), BUSINESS_ID);
    }

    public static IM getInstance(Context context) {
        IM im = map.get(BUSINESS_ID);
        if (im == null) {
            synchronized (IM.class) {
                im = map.get(BUSINESS_ID);
                if (im == null) {
                    im = new IM(context);
                    map.put(BUSINESS_ID, im);
                }
            }
        }
        return im;
    }

    public void setUiConfig(UnifyUiConfig uiConfig) {
        RLS.getInstance().setUiConfig(uiConfig);
    }

    public void setDataConfig(UnifyDataConfig dataConfig) {
        RLS.getInstance().setDataConfig(dataConfig);
    }

    public void openChat(IMCallback imCheck) {
        UnifyDataConfig dataConfig = RLS.getInstance().getDataConfig();
        if (dataConfig.getBase() == null || dataConfig.getId() == null || dataConfig.getRid() == null || dataConfig.getToken() == null) {
            imCheck.onFailure("Incomplete parameters");
        } else {
            imCheck.onSuccess();
            RLS.getInstance().go();
        }
    }

}
