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

import java.beans.*;
import java.io.Serializable;

/**
 * @author Giampaolo Melis
 */
public class PositionBean extends Object implements Serializable {

    private double latitude;
    public static final String PROPERTY_LATITUDE = "latitude";

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        double oldLatitude = this.latitude;
        this.latitude = latitude;
        propertySupport.firePropertyChange(PROPERTY_LATITUDE, oldLatitude, latitude);
    }
    
    private double longitude;
    public static final String PROPERTY_LONGITUDE = "longitude";

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        double oldLongitude = this.longitude;
        this.longitude = longitude;
        propertySupport.firePropertyChange(PROPERTY_LONGITUDE, oldLongitude, longitude);
    }
    
    private double altitude;
    public static final String PROPERTY_ALTITUDE = "altitude";

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        double oldAltitude = this.altitude;
        this.altitude = altitude;
        propertySupport.firePropertyChange(PROPERTY_ALTITUDE, oldAltitude, altitude);
    }
    
    private String bookmarkName;
    public static final String PROPERTY_BOOKMARK_NAME = "bookmarkName";

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        String oldBookmarkName = this.bookmarkName;
        this.bookmarkName = bookmarkName;
        propertySupport.firePropertyChange(PROPERTY_BOOKMARK_NAME, oldBookmarkName, bookmarkName);
    }

    private PropertyChangeSupport propertySupport;

    public PositionBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PositionBean other = (PositionBean) obj;
        if (this.bookmarkName != other.bookmarkName && (this.bookmarkName == null || !this.bookmarkName.equals(other.bookmarkName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.bookmarkName != null ? this.bookmarkName.hashCode() : 0);
        return hash;
    }
    
}
