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

package com.vrem.wifianalyzer.wifi.predicate;

import android.support.annotation.NonNull;

import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.model.Security;
import com.vrem.wifianalyzer.wifi.model.Strength;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FilterPredicate implements Predicate<WiFiDetail> {

    private final Predicate<WiFiDetail> predicate;

    /**
     * 过滤器, 包含ssid, 频段, 信号强度, 安全
     */
    private FilterPredicate(@NonNull Settings settings, @NonNull Set<WiFiBand> wiFiBands) {
        Predicate<WiFiDetail> ssidPredicate = makeSSIDPredicate(settings.getSSIDs());

        Predicate<WiFiDetail> wiFiBandPredicate = EnumUtils.predicate(WiFiBand.class, wiFiBands,
                wiFiBand -> wiFiDetail -> wiFiDetail.getWiFiSignal().getWiFiBand().equals(wiFiBand));

        Predicate<WiFiDetail> strengthPredicate = EnumUtils.predicate(Strength.class, settings.getStrengths(),
                strength -> wiFiDetail -> wiFiDetail.getWiFiSignal().getStrength().equals(strength));

        Predicate<WiFiDetail> securityPredicate = EnumUtils.predicate(Security.class, settings.getSecurities(),
                security -> wiFiDetail -> wiFiDetail.getSecurity().equals(security));

        List<Predicate<WiFiDetail>> predicates = Arrays.asList(ssidPredicate, wiFiBandPredicate, strengthPredicate, securityPredicate);

        // 排除掉 truePredicate 的, 所有过滤条件都需要满足
        this.predicate = PredicateUtils.allPredicate(CollectionUtils.select(predicates,
                object -> !PredicateUtils.truePredicate().equals(object)));
    }

    /**
     * 过滤规则
     */
    @NonNull
    public static Predicate<WiFiDetail> makeAccessPointsPredicate(@NonNull Settings settings) {
        return new FilterPredicate(settings, settings.getWiFiBands());
    }

    /**
     * 过滤规则
     */
    @NonNull
    public static Predicate<WiFiDetail> makeOtherPredicate(@NonNull Settings settings) {
        return new FilterPredicate(settings, Collections.singleton(settings.getWiFiBand()));
    }

    @Override
    public boolean evaluate(WiFiDetail object) {
        return predicate.evaluate(object);
    }

    @NonNull
    Predicate<WiFiDetail> getPredicate() {
        return predicate;
    }

    @NonNull
    private Predicate<WiFiDetail> makeSSIDPredicate(Set<String> ssids) {
        if (ssids.isEmpty()) {
            return PredicateUtils.truePredicate();
        }
        return PredicateUtils.anyPredicate(CollectionUtils.collect(ssids,
                ssid -> wiFiDetail -> wiFiDetail.getSSID().contains(ssid)));
    }
}
