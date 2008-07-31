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

import com.bwepow.wgapp.wwj.gui.PositionBean;
import com.bwepow.wgapp.wwj.gui.StaticData;
import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.util.List;

/**
 *
 * @author Giampaolo Melis
 */
public class SaveBookmarks extends Thread {

    public SaveBookmarks() {
        super("Save-Bookmarks");
    }

    @Override
    public void run() {
        try {
            List<PositionBean> bookms = StaticData.getBOOKMARKS();
            FileOutputStream out = new FileOutputStream("./bookmarks.xml");
            XMLEncoder enc = new XMLEncoder(out);
            enc.writeObject(bookms);
            enc.flush();
            enc.close();
        } catch (Exception ex) {
        }
    }

}
