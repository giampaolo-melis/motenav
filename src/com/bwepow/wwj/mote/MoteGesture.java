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
import java.util.Vector;

/**
 *
 * @author Giampaolo Melis
 */
public class MoteGesture {

    public static final int MOTE_NONE_GESTURE = 0;
    public static final int MOTE_PAN_GESTURE = 1;
    public static final int MOTE_ZOOM_GESTURE = 2;
    public static final int MOTE_ROTATE_GESTURE = 3;
    public static final int MOTE_TILT_GESTURE = 4;
    public static final double MIN_SENS = 1.5;
    public static final double MAX_TILT_X_SENS = 3.5;

    static MoteGesture getInstance(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition) {
        MoteGesture gesture = new MoteGesture();
        gesture.guessType(prevMotePosition, curMotePosition);
        return gesture;
    }
    private int type;

    public MoteGesture() {
    }

    public int getType() {
        return type;
    }

    public IRState computeMotePosition(Vector<IRState> moteDelta) {
        IRState pos = null;
        if (type == MOTE_PAN_GESTURE) {
            pos = moteDelta.get(0);
        } else if (type == MOTE_TILT_GESTURE) {
            pos = moteDelta.get(1);
        } else if (type == MOTE_ZOOM_GESTURE) {
            pos = new IRState();
            double x1 = moteDelta.get(0).getX();
            double x2 = moteDelta.get(1).getX();
            double sign = StrictMath.signum(x1);// * StrictMath.signum(x2);
            //System.out.println("Direction: " + sign);
            pos.setX(sign*(StrictMath.abs(x1) + StrictMath.abs(x2))/2.0);
        } else {
        }
        return pos;
    }

    public Vector<IRState> getMoteDelta(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition) {
        Vector<IRState> moteDelta = new Vector<IRState>();
        if (curMotePosition != null && prevMotePosition != null) {
            if (type == MOTE_PAN_GESTURE) {
                IRState delta = new IRState();
                delta.setX(curMotePosition.get(0).getX() - prevMotePosition.get(0).getX());
                delta.setY(curMotePosition.get(0).getY() - prevMotePosition.get(0).getY());
                moteDelta.addElement(delta);
            } else if (type == MOTE_ZOOM_GESTURE || type == MOTE_ROTATE_GESTURE || type == MOTE_TILT_GESTURE) {
                IRState[] prevLR = getLRSpot(prevMotePosition);
                IRState[] currLR = getLRSpot(curMotePosition);
                IRState[] diffLR = getDeltaLR(currLR, prevLR);
                moteDelta.addElement(diffLR[0]);
                moteDelta.addElement(diffLR[1]);
            }
        }
        return moteDelta;
    }

    private IRState[] getDeltaLR(IRState[] currLR, IRState[] prevLR) {
        IRState[] sa = new IRState[2];
        if (prevLR.length == 2 && currLR.length == 2 &&
                prevLR[0] != null && prevLR[1] != null &&
                currLR[0] != null && currLR[1] != null) {
            sa[0] = new IRState();
            sa[1] = new IRState();
            sa[0].setFound(true);
            sa[0].setSize(currLR[0].getSize());
            sa[0].setX(currLR[0].getX() - prevLR[0].getX());
            sa[0].setY(currLR[0].getY() - prevLR[0].getY());
            sa[1].setFound(true);
            sa[1].setSize(currLR[1].getSize());
            sa[1].setX(currLR[1].getX() - prevLR[1].getX());
            sa[1].setY(currLR[1].getY() - prevLR[1].getY());
        }
        return sa;
    }

    private IRState[] getLRSpot(Vector<IRState> irlist) {
        IRState[] sa = new IRState[2];
        int c = 0;
        for (IRState s : irlist) {
            if (s.isFound()) {
                sa[c++] = s;
            }
            if (c == 2) {
                break;
            }
        }
        if (c == 2) {
            if (sa[1].getX() < sa[0].getX()) {
                IRState tmp = sa[0];
                sa[0] = sa[1];
                sa[1] = tmp;
            }
        }
        return sa;
    }

    private void guessType(Vector<IRState> prevMotePosition, Vector<IRState> curMotePosition) {
        int numblobs = 0;
        int preblobs = 0;
        if (prevMotePosition != null) {
            for (IRState s : prevMotePosition) {
                if (s.isFound()) {
                    preblobs++;
                }
            }
        }
        for (IRState s : curMotePosition) {
            if (s.isFound()) {
                numblobs++;
            }
        }
        type = MOTE_NONE_GESTURE;
        if (numblobs == 1 && preblobs == 1) {
            type = MOTE_PAN_GESTURE;
        } else if (numblobs == 2 && preblobs == 2) {

            //System.out.println("Blobs are 2!");

            IRState[] prevLR = getLRSpot(prevMotePosition);
            IRState[] currLR = getLRSpot(curMotePosition);
            IRState[] diffLR = getDeltaLR(currLR, prevLR);

            //System.out.println("prevLR = L" + prevLR[0] + ", R" + prevLR[1]);
            //System.out.println("currLR = L" + currLR[0] + ", R" + currLR[1]);
            //System.out.println("diffLR = L" + diffLR[0] + ", R" + diffLR[1]);

            if ((diffLR[0].getX() > 0.0 && diffLR[1].getX() < 0.0) ||
                    (diffLR[0].getX() < 0.0 && diffLR[1].getX() > 0.0)) {
                if ((diffLR[0].getY() >= MIN_SENS && diffLR[1].getY() <= MIN_SENS) ||
                        (diffLR[0].getY() <= MIN_SENS && diffLR[1].getY() >= MIN_SENS)) {
                    type = MOTE_ROTATE_GESTURE;
                } else {
                    type = MOTE_ZOOM_GESTURE;
                }
            } else if ((StrictMath.abs(diffLR[0].getX()) <= MIN_SENS && StrictMath.abs(diffLR[1].getX()) <= MAX_TILT_X_SENS) &&
                    (StrictMath.abs(diffLR[0].getY()) <= MIN_SENS && StrictMath.abs(diffLR[1].getY()) != 0.0)) {
                type = MOTE_TILT_GESTURE;
            }

            //System.out.println("GEST: " + TYPES[type]);
        } else if (numblobs == 3 && preblobs == 3) {
        } else if (numblobs == 4 && preblobs == 4) {
        }
    }
    static final String[] TYPES = {
        "MOTE_NONE_GESTURE",
        "MOTE_PAN_GESTURE",
        "MOTE_ZOOM_GESTURE",
        "MOTE_ROTATE_GESTURE",
        "MOTE_TILT_GESTURE"
    };
}
