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

import com.bwepow.wgapp.input.IRController;

import wiiremotej.IRLight;
import wiiremotej.WiiRemoteExtension;
import wiiremotej.event.WRAccelerationEvent;
import wiiremotej.event.WRButtonEvent;
import wiiremotej.event.WRCombinedEvent;
import wiiremotej.event.WRExtensionEvent;
import wiiremotej.event.WRIREvent;
import wiiremotej.event.WRStatusEvent;
import wiiremotej.event.WiiRemoteListener;

/**
 *
 * @author Giampaolo Melis
 */
public class IRListener implements WiiRemoteListener {

    private IRController controller;
    private IRProcessor processor = new IRProcessor();
    private OrbitViewMoteInputBroker broker;

    public IRListener() {
    }

    public IRListener(IRController controller, OrbitViewMoteInputBroker broker) {
        this.controller = controller;
        this.broker = broker;
    }

    public IRListener(IRController controller) {
        this.controller = controller;
    }

    public IRController getController() {
        return controller;
    }

    public void setController(IRController controller) {
        this.controller = controller;
    }

    public OrbitViewMoteInputBroker getBroker() {
        return broker;
    }

    public void setBroker(OrbitViewMoteInputBroker broker) {
        this.broker = broker;
    }
    
    public void buttonInputReceived(WRButtonEvent evt) {
        //System.out.println("Button event: " + evt);
    }

    public void statusReported(WRStatusEvent evt) {
        System.out.println("Status event: " + evt);
    }

    public void accelerationInputReceived(WRAccelerationEvent evt) {
        System.out.println("Accel event: " + evt);
    }

    public void IRInputReceived(WRIREvent evt) {
        if (controller == null) return;
        IRLight[] lights = evt.getIRLights();
        if (lights == null) return;
        if (processor.isAlive()) return;
        processor = new IRProcessor();
        processor.setController(controller);
        processor.setLights(lights);
        processor.setBroker(broker);
        //processor.setParser(parser);
        processor.start();
    }

    public void extensionInputReceived(WRExtensionEvent evt) {
        System.out.println("Extension event: " + evt);
    }

    public void extensionConnected(WiiRemoteExtension evt) {
        System.out.println("Extension connected event: " + evt);
    }

    public void extensionPartiallyInserted() {
        System.out.println("Extension partially inserted event.");
    }

    public void extensionUnknown() {
        System.out.println("Extension unknown event.");
    }

    public void extensionDisconnected(WiiRemoteExtension evt) {
        System.out.println("Extension disconnected event: " + evt);
    }

    public void combinedInputReceived(WRCombinedEvent evt) {
    }

    public void disconnected() {
        System.out.println("Disconnected event.");
    }

}
