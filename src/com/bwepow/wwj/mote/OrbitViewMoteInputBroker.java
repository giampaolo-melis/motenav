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

import com.bwepow.wgapp.input.IRState;
import com.bwepow.wwj.awt.AWTInputHandler;
import com.bwepow.wwj.awt.OrbitViewInputBroker;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.awt.Component;
import java.awt.geom.Dimension2D;
import java.util.Vector;

/**
 *
 * @author Giampaolo Melis
 */
public class OrbitViewMoteInputBroker extends OrbitViewInputBroker {

    private Vector<IRState> motePosition;
    private AWTInputHandler handler;

    public OrbitViewMoteInputBroker() {
        super();
    }

    public OrbitViewMoteInputBroker(AWTInputHandler handler) {
        super();
        this.handler = handler;
    }

    public Vector<IRState> getMotePosition() {
        return motePosition;
    }

    public IRState getMotePosition(int spot) {
        return motePosition.elementAt(spot);
    }

    public void setMotePosition(IRState motePosition, int spot) {
        this.motePosition.setElementAt(motePosition, spot);
    }

    Dimension2D getTargetSize() {
        Dimension2D d = new Dimension2D() {

            double w = 0.0;
            double h = 0.0;

            @Override
            public double getWidth() {
                return w;
            }

            @Override
            public double getHeight() {
                return h;
            }

            @Override
            public void setSize(double width, double height) {
                w = width;
                h = height;
            }
        };

        Component c = (Component) getWwd();
        if (c != null) {
            d.setSize(
                    c.getWidth(),
                    c.getHeight());
        }
        return d;
    }

    private boolean areGestureExactly(MoteGesture gesture, int gestureType) {
        return gestureType == gesture.getType();
    }

    private void constrainPointToComponentBounds(IRState spot, Component c) {
        if (c != null) {
            double x = spot.getX();
            double y = spot.getY();
            double w = c.getWidth();
            double h = c.getHeight();
            if (x < 0.0) {
                x = 0.0;
            }
            if (y < 0.0) {
                y = 0.0;
            }
            if (x > w) {
                x = w;
            }
            if (y > h) {
                y = h;
            }
            spot.setX(x);
            spot.setY(y);
        }
    }

    /*private int getClientHeight() {
        int h = 0;
        Component c = (Component) getWwd();
        if (c != null) {
            h = c.getHeight();
        }
        return h;
    }*/

    private MoteGesture resolveGesture(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition) {
        return MoteGesture.getInstance(prevMotePosition, curMotePosition);
    }

    private void setCenterLatLon_NS(Angle latitude, Angle longitude) {
        if (getView() == null) {
            return;
        }
        Position p = new Position(latitude, longitude, getView().getCenterPosition().getElevation());
        setCenterPosition(p, false, 0.0d);
    }

    private void setCenterLatLon_NS(MoteGesture gesture, Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        IRState moteMove = gesture.computeMotePosition(moteDelta);
        if (prevMotePosition != null && curMotePosition != null && moteMove != null) {
            IRState prev = gesture.computeMotePosition(prevMotePosition);
            IRState cur = gesture.computeMotePosition(curMotePosition);
            Position prevPosition = computePositionAtPoint(prev.getX(), prev.getY());
            Position curPosition = computePositionAtPoint(cur.getX(), cur.getY());
            // Keep selected position under cursor.
            if (prevPosition != null && curPosition != null) {
                if (!prevPosition.equals(curPosition)) {
                    if (isLockHeading()) {
                        setCenterLatLon_NS(
                                getView().getCenterPosition().getLatitude().add(prevPosition.getLatitude()).subtract(curPosition.getLatitude()),
                                getView().getCenterPosition().getLongitude().add(prevPosition.getLongitude()).subtract(curPosition.getLongitude()));
                    }
                }
            } // Cursor is off the globe, simulate globe dragging.
            else {
                if (isLockHeading()) {
                    double sinHeading = getView().getHeading().sin();
                    double cosHeading = getView().getHeading().cos();
                    double latFactor = (cosHeading * moteMove.getY() + sinHeading * moteMove.getX()) / 10.0;
                    double lonFactor = (sinHeading * moteMove.getY() - cosHeading * moteMove.getX()) / 10.0;
                    Angle latChange = computeLatOrLonChange(latFactor, false);
                    Angle lonChange = computeLatOrLonChange(lonFactor, false);
                    setCenterLatLon_NS(
                            getView().getCenterPosition().getLatitude().add(latChange),
                            getView().getCenterPosition().getLongitude().add(lonChange));
                }
                // Cursor went off the globe. Clear the selected position to ensure a new one will be
                // computed if the cursor returns to the globe.
                clearSelectedPosition();
            }
        }
    }

    private void setHeading_NS(MoteGesture gesture, Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        
        double theta = AffineTransformSolver.getTheta(
                prevMotePosition.get(0).getX(),
                prevMotePosition.get(0).getY(),
                prevMotePosition.get(1).getX(),
                prevMotePosition.get(1).getY(),
                curMotePosition.get(0).getX(),
                curMotePosition.get(0).getY(),
                curMotePosition.get(1).getX(),
                curMotePosition.get(1).getY());
        //System.out.println("Theta: " + theta);
        
        Angle newHeading = Angle.ZERO;
        
        if (getView() != null) {
            Angle heading = getView().getHeading();
            Angle change = Angle.fromRadians(-theta);
            newHeading = heading.add(change);
        }
        
        setHeading(newHeading, false, 0.0);
    }

    private void setPitch_NS(MoteGesture gesture, Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        IRState moteMove = gesture.computeMotePosition(moteDelta);
        //Angle newPitch = computeNewPitch(moteMove.getY(), false);
        Angle newPitch = Angle.ZERO;
        
        if (getView() != null) {
            //System.out.println("MoteMove: " + moteMove.getY());
            double coeff = 1.0/3.2;
        //if (slow)
          //  coeff /= 4.0;
            Angle pitch = getView().getPitch();
            Angle change = Angle.fromDegrees(coeff * moteMove.getY());
            newPitch = pitch.add(change);
        }
        setPitch(newPitch, false, 0.0);
    }

    private void setZoom_NS(MoteGesture gesture, Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        IRState moteMove = gesture.computeMotePosition(moteDelta);
        double zoomDelta = moteMove.getX() / 10d;
        double newZoom = computeNewZoom(zoomDelta, false);
        setZoom(newZoom, false, 0.0);
    }

    //private void setHeading_NS(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        /*double headingDirection = 1;
            IRState cur = gesture.computeMotePosition(curMotePosition);
            if (cur.getY() < (getClientHeight() / 2)) {
                headingDirection = -1;
            }
            Angle newHeading = computeNewHeading(headingDirection * moteMove.getX(), false);*/
    //}

    //private void setPitch_NS(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        /* Angle newPitch = computeNewPitch(moteMove.getY(), false);*/
    //}

    //private void setZoom_NS(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition, Vector<IRState> moteDelta) {
        /*  Reduce the amount of zoom changed by mouse movement.
            double scaledMouseY = moteMove.getY() / 10d;
            double newZoom = computeNewZoom(scaledMouseY, false) */
        
    //}
    
    private void updateMotePosition(Vector<IRState> spot) {
        motePosition = new Vector<IRState>();
        for (IRState p : spot) {
            motePosition.add(getUpdateState(p));
        }
    }

    private IRState getUpdateState(IRState spot) {
        IRState result = new IRState();
        result.setX(spot.getX());
        result.setY(spot.getY());
        result.setSize(spot.getSize());
        result.setFound(spot.isFound());
        if (getWwd() instanceof Component) {
            constrainPointToComponentBounds(result, (Component) getWwd());
        }
        return result;
    }

    public int moteDragged(Vector<IRState> spot) {
        if (getWwd() == null) {
            return MoteGesture.MOTE_NONE_GESTURE;
        }

        if (getView() == null) {
            return MoteGesture.MOTE_NONE_GESTURE;
        }

        Vector<IRState> prevMotePosition = getMotePosition();

        updateMotePosition(spot);

        Vector<IRState> curMotePosition = getMotePosition();

        MoteGesture gesture = resolveGesture(prevMotePosition, curMotePosition);

        Vector<IRState> moteDelta = gesture.getMoteDelta(prevMotePosition, curMotePosition);

        // Compute the current selected position if none exists.
        if (getSelectedPosition() == null) {
            this.updateSelectedPosition();
        }

        if (areGestureExactly(gesture, MoteGesture.MOTE_PAN_GESTURE)) {
            setCenterLatLon_NS(gesture, prevMotePosition, curMotePosition, moteDelta);
        } else if (areGestureExactly(gesture, MoteGesture.MOTE_ROTATE_GESTURE)) {
            setHeading_NS(gesture, prevMotePosition, curMotePosition, moteDelta);
        } else if (areGestureExactly(gesture, MoteGesture.MOTE_TILT_GESTURE)) {
            setPitch_NS(gesture, prevMotePosition, curMotePosition, moteDelta);
        } else if (areGestureExactly(gesture, MoteGesture.MOTE_ZOOM_GESTURE)) {
            setZoom_NS(gesture, prevMotePosition, curMotePosition, moteDelta);
        }
        return gesture.getType();
    }

    public AWTInputHandler getHandler() {
        return handler;
    }
}
