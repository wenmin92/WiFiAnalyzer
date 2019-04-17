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

package com.vrem.wifianalyzer.wifi.channelgraph;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.graphutils.GraphAdapter;
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewNotifier;
import com.vrem.wifianalyzer.wifi.model.WiFiData;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 信道图表(频率范围)适配器, 为每个 "频率范围" 创建 GraphView
 */
class ChannelGraphAdapter extends GraphAdapter {
    private final ChannelGraphNavigation channelGraphNavigation;

    ChannelGraphAdapter(@NonNull ChannelGraphNavigation channelGraphNavigation) {
        super(makeGraphViewNotifiers());
        this.channelGraphNavigation = channelGraphNavigation;
    }

    /**
     * 为每个 "频率范围" 创建一个 GraphViewNotifier;
     * 通过 GraphAdapter 获取 GraphView
     */
    private static List<GraphViewNotifier> makeGraphViewNotifiers() {
        List<GraphViewNotifier> graphViewNotifiers = new ArrayList<>();
        // 具体做法: 遍历频段, 再遍历每个频段的每个频率范围, 分别创建 GraphViewNotifier
        IterableUtils.forEach(EnumUtils.values(WiFiBand.class), new WiFiBandClosure(graphViewNotifiers));
        return graphViewNotifiers;
    }

    @Override
    public void update(@NonNull WiFiData wiFiData) {
        super.update(wiFiData);
        channelGraphNavigation.update(wiFiData);
    }

    /**
     * 遍历频段, 获取其中的频率范围
     */
    private static class WiFiBandClosure implements Closure<WiFiBand> {
        private final List<GraphViewNotifier> graphViewNotifiers;

        private WiFiBandClosure(@NonNull List<GraphViewNotifier> graphViewNotifiers) {
            this.graphViewNotifiers = graphViewNotifiers;
        }

        @Override
        public void execute(WiFiBand wiFiBand) {
            IterableUtils.forEach(wiFiBand.getWiFiChannels().getWiFiChannelPairs(), new WiFiChannelClosure(graphViewNotifiers, wiFiBand));
        }
    }

    /**
     * 遍历频段的频率范围, 每个范围对应一个 GraphViewNotifier
     */
    private static class WiFiChannelClosure implements Closure<Pair<WiFiChannel, WiFiChannel>> {
        private final List<GraphViewNotifier> graphViewNotifiers;
        private final WiFiBand wiFiBand;

        private WiFiChannelClosure(@NonNull List<GraphViewNotifier> graphViewNotifiers, @NonNull WiFiBand wiFiBand) {
            this.graphViewNotifiers = graphViewNotifiers;
            this.wiFiBand = wiFiBand;
        }

        @Override
        public void execute(Pair<WiFiChannel, WiFiChannel> wiFiChannelPair) {
            graphViewNotifiers.add(new ChannelGraphView(wiFiBand, wiFiChannelPair)); // ChannelGraphView 是 GraphViewNotifier 的实现
        }
    }
}
