/*******************************************************************************
 *
 * MoteNav - multimodal interface for WWJ
 * =================================
 *
 * Copyright (C) 2008 by Giampaolo Melis
 * Project home page: http://code.google.com/p/motenav/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.bwepow.wwj;

import com.bwepow.wgapp.wwj.gui.BookmarkPanel;
import com.bwepow.wgapp.wwj.gui.PositionBean;
import com.bwepow.wwj.speech.SpeechProcessor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdesktop.observablecollections.ObservableList;

/**
 *
 * @author Giampaolo Melis
 */
public class RecognizerThread extends Thread {

    private BookmarkPanel bmp;

    public RecognizerThread(BookmarkPanel panel) {
        super("Recognize-Thread");
        this.setPriority(MIN_PRIORITY);
        bmp = panel;
    }

    @Override
    public void run() {
        try {
            final SpeechProcessor js = new SpeechProcessor();
            js.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    ObservableList<PositionBean> bookmarks;
                    if (SpeechProcessor.PROPERTY_COMMAND.equals(evt.getPropertyName())) {
                        String command = js.getCommand();
                        String[] tk = command.split(" ");
                        if ("take".equalsIgnoreCase(tk[0]) || "grab".equalsIgnoreCase(tk[0])) {
                            System.out.println("Taking bookmark");
                            bmp.grabNewPosition();
                        } else {
                            String id = tk[tk.length - 1];
                            bookmarks = RecognizerThread.this.bmp.getBookmarks();
                            PositionBean loc = null;
                            for (PositionBean bean : bookmarks) {
                                if (bean.getBookmarkName().equalsIgnoreCase(id)) {
                                    loc = bean;
                                    break;
                                }
                            }
                            System.out.println("LOC: " + loc);
                            if (loc != null) {
                                bmp.setSelectedBookmark(loc);
                                MoteNavApp.getFrame().flyToBookmark(loc);
                            }
                        }
                    }
                }
            });
            js.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
