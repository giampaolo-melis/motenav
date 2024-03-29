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

package com.bwepow.wgapp.wwj.gui;

import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Giampaolo Melis
 */
public class StaticData {
    
    private static final List<PositionBean> BOOKMARKS = new Vector<PositionBean>();
    
    static {
        try {
            FileInputStream in = new FileInputStream("./bookmarks.xml");
            XMLDecoder dec = new XMLDecoder(in);
            List<PositionBean> decoded = (List<PositionBean>) dec.readObject();
            dec.close();
            BOOKMARKS.addAll(decoded);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private  StaticData() {
    }

    public static List<PositionBean> getBOOKMARKS() {
        return BOOKMARKS;
    }
}
