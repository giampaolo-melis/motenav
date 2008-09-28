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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import com.bwepow.wgapp.input.*;
import com.bwepow.wgapp.wwj.gui.BookmarkPanel;
import com.bwepow.wgapp.wwj.gui.PositionBean;
import com.bwepow.wwj.awt.CustomAWTInputHandler;
import com.bwepow.wwj.layers.MoteSpotLayer;
import com.bwepow.wwj.mote.*;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.StatusBarMGRS;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.EyePositionIterator;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author Giampaolo Melis
 */
public class MoteNavApp extends ApplicationTemplate {

    public static AppFrame getFrame() {
        return AppFrame.getFrame();
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private BookmarkPanel bookmarkPanel;
        private static AppFrame frame;

        public static AppFrame getFrame() {
            return frame;
        }

        public AppFrame() {
            super(true, true, false);

            frame = this;

            this.getWwjPanel().remove(this.getStatusBar());

            StatusBar sb = new StatusBarMGRS();
            sb.setEventSource(this.getWwd());
            this.getWwjPanel().add(sb, BorderLayout.SOUTH);

            bookmarkPanel = new BookmarkPanel();

            this.getLayerPanel().add(bookmarkPanel, BorderLayout.SOUTH);

            setupLayers();

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());

            this.getWwd().getView().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    JobScheduler.scheduleUpdateCoord(AppFrame.this, 650);
                }
            });

            bookmarkPanel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    AppFrame.this.flyToBookmark();
                }
            });

            bookmarkPanel.getSrButton().addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("Pressed SR button");
                    AppFrame.this.startRecognize();
                }
            });

            bookmarkPanel.getMcButton().addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("Pressed MC button");
                    AppFrame.this.connect();
                    AppFrame.this.bookmarkPanel.getMcButton().setEnabled(false);
                }
            });

            //TODO Add jump to coordinate panel

            //TODO Extends bookmark data with pitch and heading

        // Add go to coordinate input panel
        //this.getLayerPanel().add(new GoToCoordinatePanel(this.getWwd()), BorderLayout.SOUTH);
        }

        void flyToBookmark() {
            flyToBookmark(true);
        }

        void flyToBookmark(PositionBean pos) {
            System.out.println("POS: " + pos);
            if (pos == null) {
                return;
            }
            Angle lat = Angle.fromDegrees(pos.getLatitude());
            Angle lon = Angle.fromDegrees(pos.getLongitude());
            double alt = pos.getAltitude();
            Position eye = new Position(lat, lon, alt);
            OrbitView view = (OrbitView) getWwd().getView();
            ViewStateIterator vsi = new EyePositionIterator(
                    4000, view.getEyePosition(), eye);
            getWwd().getView().applyStateIterator(vsi);
        }

        void flyToBookmark(boolean smooth) {
            PositionBean pos = bookmarkPanel.getSelectedBookmark();
            flyToBookmark(pos, smooth);
        }

        void flyToBookmark(PositionBean pos, boolean smooth) {
            System.out.println("POS: " + pos);
            if (pos == null) {
                return;
            }

            Angle lat = Angle.fromDegrees(pos.getLatitude());
            Angle lon = Angle.fromDegrees(pos.getLongitude());
            double alt = pos.getAltitude();
            Angle pitch = Angle.fromDegrees(pos.getPitch());
            Angle heading = Angle.fromDegrees(pos.getHeading());
            double zoom = pos.getZoom();

            OrbitView view = (OrbitView) getWwd().getView();

            Position start = view.getCenterPosition();
            Position end = new Position(lat, lon, alt);

            Angle startHeading = view.getHeading();
            Angle startPitch = view.getPitch();
            double startZoom = view.getZoom();

            if (smooth) {
                Globe globe = getWwd().getModel().getGlobe();

                ViewStateIterator vsi = FlyToOrbitViewStateIterator.createPanToIterator(
                        globe,
                        start, end,
                        startHeading, heading, startPitch, pitch,
                        startZoom, zoom, 5500l);

                getWwd().getView().applyStateIterator(vsi);
            } else {
                view.setCenterPosition(end);
                view.setHeading(heading);
                view.setPitch(pitch);
                view.setZoom(zoom);
                repaint();
            }
        }
        
        private void startRecognize() {
            System.out.println("Starting recognizer...");
            RecognizerThread rt = new RecognizerThread(bookmarkPanel);
            rt.start();
            bookmarkPanel.getSrButton().setEnabled(false);
        }

        void updateBookmark() {
            OrbitView view = (OrbitView) getWwd().getView();
            Position pos = view.getCenterPosition();
            PositionBean cpb = bookmarkPanel.getCurrentPositionBean();
            cpb.setLatitude(pos.getLatitude().getDegrees());
            cpb.setLongitude(pos.getLongitude().getDegrees());
            cpb.setAltitude(pos.getElevation());
            cpb.setHeading(view.getHeading().getDegrees());
            cpb.setPitch(view.getPitch().getDegrees());
            cpb.setZoom(view.getZoom());
        }

        private void setupLayers() {
            double minLatitude = 44.3975;
            double maxLatitude = 44.4177;
            double minLongitude = 8.9064;
            double maxLongitude = 8.9351;

            String imagePath = "images/jug_genova_icon.png";
            Sector sector = Sector.fromDegrees(minLatitude, maxLatitude, minLongitude, maxLongitude);

            SurfaceImage si = new SurfaceImage(imagePath, sector);
            si.setOpacity(0.8);

            RenderableLayer layer = new RenderableLayer();
            layer.setName("JUG Layer");
            layer.addRenderable(si);

            MoteSpotLayer msl = new MoteSpotLayer();

            insertBeforeCompass(this.getWwd(), layer);
            insertBeforeCompass(this.getWwd(), msl);
        }

        public void connect() {
            try {
                MoteController controller = null;
                controller = (MoteController) Class.forName("com.bwepow.wwj.mote.MoteControllerImpl").newInstance();
                setIRC(controller);
                setBroker(controller);
                if (!controller.isDiscovering()) {
                    controller.discover();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(MoteNavApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MoteNavApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MoteNavApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void setBroker(MoteController controller) {
            MoteControllerImpl mci = (MoteControllerImpl) controller;
            CustomAWTInputHandler ia = ((CustomAWTInputHandler) getWwd().getInputHandler());
            OrbitViewMoteInputBroker broker = (OrbitViewMoteInputBroker) ia.getViewInputBroker();
            mci.setBroker(broker);
        }

        private void setIRC(MoteController controller) {
            MoteControllerImpl mci = (MoteControllerImpl) controller;
            LayerList layers = getWwd().getModel().getLayers();
            for (Layer layer : layers) {
                if (layer instanceof MoteSpotLayer) {
                    mci.setIrc((MoteSpotLayer) layer);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("gov.nasa.worldwind.config.file", "config/CustomWorldwind.properties");
        Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, CustomAWTInputHandler.class.getName());
        Configuration.setValue(AVKey.DATA_FILE_CACHE_CONFIGURATION_FILE_NAME, "config/CustomDataFileCache.xml");
        try {
            UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceRavenLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new SaveBookmarks());
        WorldWind.getNetworkStatus().setOfflineMode(true);
        System.out.println("Starting MoteNav...");
        ApplicationTemplate.start("JUG Genova Demo Application (MoteSpots)", AppFrame.class);
    }

    static final class JobScheduler {

        private static final int POOL_SIZE = 5;
        private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(POOL_SIZE);
        private static ScheduledFuture<UpdateCoordJob> scheduleUpdateCoord = null;

        private JobScheduler() {
        }

        public static void scheduleUpdateCoord(AppFrame frame, long initialDelay) {
            if (scheduleUpdateCoord == null || scheduleUpdateCoord.isDone()) {
                //System.err.println("Scheduled " + System.currentTimeMillis());
            stopUpdateCoord();
            UpdateCoordJob updateCoordJob = new UpdateCoordJob(frame);
            scheduleUpdateCoord = (ScheduledFuture<UpdateCoordJob>) SCHEDULER.schedule(
                    updateCoordJob, initialDelay, TimeUnit.MILLISECONDS);
           }
        }

        public static void stopUpdateCoord() {
            if (scheduleUpdateCoord != null) {
                scheduleUpdateCoord.cancel(false);
            }
        }
    }

    static final class UpdateCoordJob implements Runnable {
        private AppFrame frame;

        public UpdateCoordJob(AppFrame frame) {
            this.frame = frame;
        }
        
        public void run() {
            frame.updateBookmark();
        }
    }
}
