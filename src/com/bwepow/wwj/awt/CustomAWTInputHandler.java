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

package com.bwepow.wwj.awt;

import com.bwepow.wwj.mote.OrbitViewMoteInputBroker;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import java.awt.event.MouseEvent;

/**
 *
 * @author Giampaolo Melis
 */
public class CustomAWTInputHandler extends AWTInputHandler {

    SelectListener customTarget = null;
    
    public CustomAWTInputHandler() {
        this(null);
    }
    
    public CustomAWTInputHandler(SelectListener customTarget) {
        super();
        this.customTarget = customTarget;
        OrbitViewMoteInputBroker broker = new OrbitViewMoteInputBroker(this);
        setViewInputBroker(broker);
    }

    @Override
    public OrbitViewInputBroker getViewInputBroker() {
        return super.getViewInputBroker();
    }
    
    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        //System.out.println("GOT: MOUSECLICKED");
        WorldWindow ww = getEventSource();
        
        if (ww == null || mouseEvent == null)
            return;
        
        PickedObjectList pickedObjects = ww.getObjectsAtCurrentPosition();
        
        PickedObject top = null;
        
        boolean popupTrigger = false;
        
        if (MouseEvent.BUTTON3 == mouseEvent.getButton() &&
                mouseEvent.getClickCount() % 2 == 1 &&
                pickedObjects != null && pickedObjects.size() > 0) {
            top = pickedObjects.getTopPickedObject();
            if (top != null && top.isTerrain())
                popupTrigger = true;
        }
        
        //System.out.println("OBJECTS: " + pickedObjects);
        //System.out.println("TOP OBJECT: " + top);
        //System.out.println("POPUP: " + popupTrigger);
        
        if (popupTrigger) {
            PickedObjectList pol = new PickedObjectList();
            pol.add(top);
            callSelectListeners(new SelectEvent(ww, SelectEvent.RIGHT_CLICK,
                    mouseEvent, pol));
        } else {
            super.mouseClicked(mouseEvent);
        }
    }

    public SelectListener getCustomTarget() {
        return customTarget;
    }

    public void setCustomTarget(SelectListener customTarget) {
        this.customTarget = customTarget;
    }
    
    @Override
    protected void callSelectListeners(SelectEvent event) {
        if (customTarget != null) customTarget.selected(event);
    }
}
