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

package com.bwepow.wwj.mote;

import com.bwepow.wgapp.input.DiscoveryListener;
import com.bwepow.wgapp.input.IRController;
import com.bwepow.wgapp.input.MoteController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WRIREvent;

/**
 *
 * @author Giampaolo Melis
 */
public class MoteControllerImpl implements MoteController {

    DiscoveryListener discoveryListener = null;
    IRController irc = null;
    WiiRemote mote = null;
    
    private boolean discovering;
    private OrbitViewMoteInputBroker broker;
    
    public void discover() {
        if (discovering) return;
        setMessage("Discovering wiimote ...");
        WiiRemoteJ.setConsoleLoggingAll();
        discoveryListener = new DiscoveryListener(this);
        setDiscovering(true);
        WiiRemoteJ.findRemotes(discoveryListener);
    }

    public void config(WiiRemote mote) {
        setMessage("Comfiguring mote ...");
        try {
            this.mote = mote;
            mote.setLEDIlluminated(0, true);
            mote.setIRSensorEnabled(true, WRIREvent.BASIC);
            mote.addWiiRemoteListener(new IRListener(irc, broker));
            setDiscovering(false);
        } catch (IOException ex) {
            Logger.getLogger(MoteControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MoteControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(MoteControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setMessage(String message) {
        System.out.println(message);
    }

    public void close() {
        setMessage("Closiing mote ...");
        if (mote != null)
            mote.disconnect();
        else WiiRemoteJ.stopFind();
    }

    public boolean isDiscovering() {
        return discovering;
    }

    private void setDiscovering(boolean discovering) {
        this.discovering = discovering;
    }

    public IRController getIrc() {
        return irc;
    }

    public void setIrc(IRController irc) {
        this.irc = irc;
    }

    public OrbitViewMoteInputBroker getBroker() {
        return broker;
    }

    public void setBroker(OrbitViewMoteInputBroker broker) {
        this.broker = broker;
    }
}
