/*
 * The MIT License (MIT)
 *
 * Copyright © 2022 Sven Homburg, <homburgs@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hsofttec.intellij.querytester.services;

import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.utils.DimensionConverter;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Service(Service.Level.PROJECT)
@State(
        name = "QueryHistory",
        storages = {@Storage("querytester.xml")}
)
public final class HistoryService implements PersistentStateComponent<HistoryService.HistoryState> {

    private static final List<HistoryModifiedEventListener> listeners = new ArrayList<>();
    private final SettingsState SETTINGS_STATE = SettingsService.getSettings();
    private final HistoryState historyState = new HistoryState();

    @Override
    public HistoryState getState() {
        return historyState;
    }

    @Override
    public void loadState(@NotNull HistoryService.HistoryState state) {
        XmlSerializerUtil.copyBean(state, this.historyState);
    }

    public Dimension getDialogDimension() {
        return historyState.dialogDimension;
    }

    public void setDialogDimension(Dimension dialogDimension) {
        historyState.dialogDimension = dialogDimension;
    }

    public List<String> getQueryList() {
        return historyState.historyList;
    }

    public void addListener(HistoryModifiedEventListener listener) {
        listeners.add(listener);
    }

    public void addQuery(@NotNull String nqlQuery) {
        if (nqlQuery.trim().isEmpty()) {
            return;
        }

        nqlQuery = nqlQuery.replaceAll("\\t\\n\\r", " ");

        for (String query : historyState.historyList) {
            if (query.equals(nqlQuery)) {
                return;
            }
        }

        if (historyState.historyList.size() >= SETTINGS_STATE.getMaxHistorySize()) {
            historyState.historyList.removeFirst();
        }
        historyState.historyList.add(nqlQuery);

        for (HistoryModifiedEventListener listener : listeners) {
            listener.notifyAdd(nqlQuery);
        }
    }

    private long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static class HistoryState {
        public List<String> historyList = new ArrayList<>();
        @OptionTag(converter = DimensionConverter.class)
        public Dimension dialogDimension = new Dimension(200, 200);
    }
}
