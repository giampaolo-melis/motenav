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
import com.bwepow.wgapp.input.IRState;
import gov.nasa.worldwind.WorldWindow;
import java.awt.geom.Dimension2D;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import wiiremotej.IRLight;

/**
 *
 * @author Giampaolo Melis
 */
public class IRProcessor extends Thread {

    private static int count = 0;
    private IRController controller;
    private IRLight[] lights;
    private OrbitViewMoteInputBroker broker;

    public IRProcessor() {
        super("IRProcessor-" + (count++));
        setPriority(MIN_PRIORITY);
    }

    public IRController getController() {
        return controller;
    }

    public void setController(IRController controller) {
        this.controller = controller;
    }

    public IRLight[] getLights() {
        return lights;
    }

    public void setLights(IRLight[] lights) {
        this.lights = lights;
    }

    public OrbitViewMoteInputBroker getBroker() {
        return broker;
    }

    public void setBroker(OrbitViewMoteInputBroker broker) {
        this.broker = broker;
    }

    @Override
    public void run() {
        try {
            if (controller == null || lights == null) {
                controller = null;
                lights = null;
                broker = null;
                return;
            }
            double size = 0.0;
            double x = 0.0;
            double y = 0.0;
            int ir = 0;
            for (IRLight light : lights) {
                if (light != null && ir < 4) {
                    size = light.getSize();
                    x = 1.0 - light.getX();
                    y = light.getY();
                    if (ir == 0) {
                        controller.setIR1Location(x, y);
                        controller.setIR1Size(size, size);
                        controller.setIR1Visible(true);
                    } else if (ir == 1) {
                        controller.setIR2Location(x, y);
                        controller.setIR2Size(size, size);
                        controller.setIR2Visible(true);
                    } else if (ir == 2) {
                        controller.setIR3Location(x, y);
                        controller.setIR3Size(size, size);
                        controller.setIR3Visible(true);
                    } else if (ir == 3) {
                        controller.setIR4Location(x, y);
                        controller.setIR4Size(size, size);
                        controller.setIR4Visible(true);
                    }
                } else if (light == null) {
                    if (ir == 0) {
                        controller.setIR1Visible(false);
                    } else if (ir == 1) {
                        controller.setIR2Visible(false);
                    } else if (ir == 2) {
                        controller.setIR3Visible(false);
                    } else if (ir == 3) {
                        controller.setIR4Visible(false);
                    }
                }
                ir++;
            }
            //System.err.println("BROKER: " + broker);
            if (broker != null) {
                WorldWindow wwd = broker.getWorldWindow();
                if (wwd == null) {
                    wwd = broker.getHandler().getWorldWindow();
                    broker.setWorldWindow(wwd);
                }
                //System.err.println("WWD: " + wwd);
                if (wwd != null && wwd.getSceneController() != null) {
                    int gesture = updateWWD(lights, broker);
                    if (gesture == MoteGesture.MOTE_NONE_GESTURE)
                        wwd.redraw();
                }
            }

        } catch (Throwable ex) {
            Logger.getLogger(IRProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            controller = null;
            lights = null;
            broker = null;
        }
    }

    private int updateWWD(IRLight[] lights, OrbitViewMoteInputBroker broker) throws Throwable {
        Dimension2D size = broker.getTargetSize();
        Vector<IRState> irlist = new Vector<IRState>();
        for (IRLight light : lights) {
            IRState irs = new IRState();
            if (light == null) {
                irs.setFound(false);
            } else {
                irs.setFound(true);
                irs.setX((1.0 - light.getX()) * size.getWidth());
                irs.setY((1.0 - light.getY()) * size.getHeight());
                irs.setSize(light.getSize());
            }
            irlist.add(irs);
        }
        return broker.moteDragged(irlist);
    }
}
