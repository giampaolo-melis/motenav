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

package com.bwepow.wgapp.input;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WiiRemoteDiscoveredEvent;
import wiiremotej.event.WiiRemoteDiscoveryListener;

/**
 *
 * @author Giampaolo Melis
 */
public class DiscoveryListener implements WiiRemoteDiscoveryListener {

    private WiiRemote remote = null;
    private MoteController controller;

    public DiscoveryListener() {
    }

    public DiscoveryListener(MoteController controller) {
        this.controller = controller;
    }

    public MoteController getController() {
        return controller;
    }

    public void setController(MoteController controller) {
        this.controller = controller;
    }

    public WiiRemote getRemote() {
        return remote;
    }
    
    public void wiiRemoteDiscovered(WiiRemoteDiscoveredEvent evt) {
        if (controller != null) {
            controller.setMessage("Mote " + evt.getNumber() + " discovered.");
        }
        if (evt.getNumber() == 0) {
            remote = evt.getWiiRemote();
            discoveryCompleted();
        }
    }

    public void findFinished(int numMotes) {
        if (controller != null) {
            controller.setMessage("Discovery finished. Discovered Motes: " + numMotes + ".");
        }
    }
   
    public void discoveryCompleted() {
        WiiRemoteJ.stopFind();
        if (controller != null && remote != null) {
            controller.config(remote);
            controller.setMessage("Mote discovered and configured.");
        } else if (controller != null) {
            controller.setMessage("Mote was not found.");
        }
    }

}
