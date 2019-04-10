/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.accesspoint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.Security;
import com.vrem.wifianalyzer.wifi.model.Strength;
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * 生成view, 并填充数据
 */
public class AccessPointDetail {
    private static final int VENDOR_SHORT_MAX = 10;
    private static final int VENDOR_LONG_MAX = 30;

    /**
     * 生成view, 并填充数据
     */
    View makeView(View convertView, ViewGroup parent, @NonNull WiFiDetail wiFiDetail, boolean isChild) {
        AccessPointViewType accessPointViewType = MainContext.INSTANCE.getSettings().getAccessPointView();
        return makeView(convertView, parent, wiFiDetail, isChild, accessPointViewType);
    }

    /**
     * 生成view, 并填充数据
     * @param accessPointViewType 视图类型: COMPLETE 和 COMPACT
     */
    View makeView(View convertView, ViewGroup parent, @NonNull WiFiDetail wiFiDetail, boolean isChild, @NonNull AccessPointViewType accessPointViewType) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = MainContext.INSTANCE.getLayoutInflater();
            view = layoutInflater.inflate(accessPointViewType.getLayout(), parent, false);
        }

        // 填充数据
        setViewCompact(view, wiFiDetail, isChild);

        // 附加数据
        if (view.findViewById(R.id.capabilities) != null) {
            setViewExtra(view, wiFiDetail);
            setViewVendorShort(view, wiFiDetail.getWiFiAdditional());
        }

        return view;
    }

    public View makeViewDetailed(@NonNull WiFiDetail wiFiDetail) {
        View view = MainContext.INSTANCE.getLayoutInflater().inflate(R.layout.access_point_view_popup, null);

        setViewCompact(view, wiFiDetail, false);
        setViewExtra(view, wiFiDetail);
        setViewVendorLong(view, wiFiDetail.getWiFiAdditional());
        enableTextSelection(view);

        return view;
    }

    private void enableTextSelection(View view) {
        view.<TextView>findViewById(R.id.ssid).setTextIsSelectable(true);
        view.<TextView>findViewById(R.id.vendorLong).setTextIsSelectable(true);
    }

    /**
     * 填充数据
     */
    private void setViewCompact(@NonNull View view, @NonNull WiFiDetail wiFiDetail, boolean isChild) {
        Context context = view.getContext();

        // ssid (bssid)
        view.<TextView>findViewById(R.id.ssid).setText(wiFiDetail.getTitle());

        WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();
        Strength strength = wiFiSignal.getStrength();

        // 安全图标, 🔒
        Security security = wiFiDetail.getSecurity();
        ImageView securityImage = view.findViewById(R.id.securityImage);
        securityImage.setImageResource(security.getImageResource());
        securityImage.setColorFilter(ContextCompat.getColor(context, R.color.icons_color));

        // 信号强度
        TextView textLevel = view.findViewById(R.id.level);
        textLevel.setText(String.format(Locale.ENGLISH, "%ddBm", wiFiSignal.getLevel()));
        textLevel.setTextColor(ContextCompat.getColor(context, strength.colorResource()));

        // 信道
        view.<TextView>findViewById(R.id.channel)
            .setText(wiFiSignal.getChannelDisplay());
        // 主频
        view.<TextView>findViewById(R.id.primaryFrequency)
            .setText(String.format(Locale.ENGLISH, "%d%s",
                wiFiSignal.getPrimaryFrequency(), WiFiSignal.FREQUENCY_UNITS));
        // 距离
        view.<TextView>findViewById(R.id.distance).setText(wiFiSignal.getDistance());

        if (isChild) {
            view.findViewById(R.id.tab).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.tab).setVisibility(View.GONE);
        }
    }

    /**
     * 填充数据, 附加数据
     */
    private void setViewExtra(@NonNull View view, @NonNull WiFiDetail wiFiDetail) {
        Context context = view.getContext();

        // 是否是配置过的, 😊
        view.<ImageView>findViewById(R.id.configuredImage)
            .setVisibility(wiFiDetail.getWiFiAdditional().isConfiguredNetwork() ? View.VISIBLE : View.GONE);

        // 信号强度
        WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();
        Strength strength = wiFiSignal.getStrength();
        ImageView imageView = view.findViewById(R.id.levelImage);
        imageView.setImageResource(strength.imageResource());
        imageView.setColorFilter(ContextCompat.getColor(context, strength.colorResource()));

        // 频率范围
        view.<TextView>findViewById(R.id.channel_frequency_range)
            .setText(Integer.toString(wiFiSignal.getFrequencyStart()) + " - " + Integer.toString(wiFiSignal.getFrequencyEnd()));
        // 频宽
        view.<TextView>findViewById(R.id.width)
            .setText("(" + Integer.toString(wiFiSignal.getWiFiWidth().getFrequencyWidth()) + WiFiSignal.FREQUENCY_UNITS + ")");
        // 加密方式
        view.<TextView>findViewById(R.id.capabilities)
            .setText(wiFiDetail.getCapabilities());
    }

    /**
     * 填充数据, 厂商名称
     */
    private void setViewVendorShort(@NonNull View view, @NonNull WiFiAdditional wiFiAdditional) {
        TextView textVendorShort = view.findViewById(R.id.vendorShort);
        String vendor = wiFiAdditional.getVendorName();
        if (StringUtils.isBlank(vendor)) {
            textVendorShort.setVisibility(View.GONE);
        } else {
            textVendorShort.setVisibility(View.VISIBLE);
            textVendorShort.setText(vendor.substring(0, Math.min(VENDOR_SHORT_MAX, vendor.length())));
        }
    }

    private void setViewVendorLong(@NonNull View view, @NonNull WiFiAdditional wiFiAdditional) {
        TextView textVendor = view.findViewById(R.id.vendorLong);
        String vendor = wiFiAdditional.getVendorName();
        if (StringUtils.isBlank(vendor)) {
            textVendor.setVisibility(View.GONE);
        } else {
            textVendor.setVisibility(View.VISIBLE);
            textVendor.setText(vendor.substring(0, Math.min(VENDOR_LONG_MAX, vendor.length())));
        }
    }

}
