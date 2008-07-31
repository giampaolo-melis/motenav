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

package com.bwepow.wwj.layers;

import com.bwepow.wgapp.input.IRController;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Map;
import javax.media.opengl.GL;

/**
 *
 * @author Giampaolo Melis
 */
public class MoteSpotLayer extends AbstractLayer implements IRController {
    
    /**
     * On window resize, scales the crosshair icon to occupy a constant relative size of the viewport.
     */
    public final static String RESIZE_STRETCH = "com.bwepow.wwj.layers.MoteSpotLayer.ResizeStretch";
    /**
     * On window resize, scales the crosshair icon to occupy a constant relative size of the viewport, but not larger than
     * the icon's inherent size scaled by the layer's icon scale factor.
     */
    public final static String RESIZE_SHRINK_ONLY = "com.bwepow.wwj.layers.MoteSpotLayer.ResizeShrinkOnly";
    /**
     * Does not modify the crosshair icon size when the window changes size.
     */
    public final static String RESIZE_KEEP_FIXED_SIZE = "com.bwepow.wwj.layers.MoteSpotLayer.ResizeKeepFixedSize";
    
    public final static int DEFAULT_SPOT_SIZE = 15;

    private Map<Integer, SpotIcon> spotList = new Hashtable<Integer, SpotIcon>();
    
    private double iconScale = 1d;
    
    private double toViewportScale = 1d;
    
    private String resizeBehavior = RESIZE_KEEP_FIXED_SIZE;

    public MoteSpotLayer() {
        this.setOpacity(0.8);
        addSpot(0, Color.RED);
        addSpot(1, Color.GREEN);
        addSpot(2, Color.BLUE);
        addSpot(3, Color.MAGENTA);
        
        /*addSpot(4, Color.RED);
        SpotIcon spot = spotList.get(4);
        spot.setX(0.4);
        spot.setY(0.5);
        spot.setVisible(true);
        System.out.println("SPOT VISIBLE.");*/
    }
    
    void addSpot(int id, Color color) {
        SpotIcon spot = new SpotIcon();
        spot.setId(id);
        spot.setColor(color);
        spot.setWidth(DEFAULT_SPOT_SIZE);
        spot.setHeight(DEFAULT_SPOT_SIZE);
        spot.setX(0.0);
        spot.setY(0.0);
        spot.setVisible(false);
        spotList.put(id, spot);
    }

    @Override
    protected void doRender(DrawContext dc) {
        for (SpotIcon spot : spotList.values()) {
            dc.addOrderedRenderable(spot);
        }
    }

    private Vec4 computeLocation(SpotIcon spot, Rectangle viewport, double scale) {
        double width = this.getScaledIconWidth(spot);
        double height = this.getScaledIconHeight(spot);

        double scaledWidth = scale * width;
        double scaledHeight = scale * height;

        double x = spot.getX() * ((double)viewport.width);
        double y = spot.getY() * ((double)viewport.height);

        /*if (this.locationCenter != null)
        {*/
            x = x - scaledWidth / 2;
            y = y - scaledHeight / 2;
        /*}
        else  viewport center
        {
            x = viewport.getWidth() / 2 - scaledWidth / 2;
            y = viewport.getHeight() / 2 - scaledHeight / 2;
        }*/

        return new Vec4(x, y, 0);
    }

    private double computeScale(SpotIcon spot, Rectangle viewport) {
        if (this.resizeBehavior.equals(RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.toViewportScale) * viewport.width / this.getScaledIconWidth(spot));
        }
        else if (this.resizeBehavior.equals(RESIZE_STRETCH))
        {
            return (this.toViewportScale) * viewport.width / this.getScaledIconWidth(spot);
        }
        else if (this.resizeBehavior.equals(RESIZE_KEEP_FIXED_SIZE))
        {
            return 1d;
        }
        else
        {
            return 1d;
        }
    }

    private void draw(DrawContext dc, SpotIcon spot) {
        GL gl = dc.getGL();

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT | GL.GL_TRANSFORM_BIT | GL.GL_VIEWPORT_BIT | GL.GL_CURRENT_BIT);
            attribsPushed = true;
            
            if (!spot.isVisible())
                return;

            Texture iconTexture = dc.getTextureCache().get(spot);
            if (iconTexture == null) {
                this.initializeTexture(dc, spot);
                iconTexture = dc.getTextureCache().get(spot);
                if (iconTexture == null) {
                    // TODO: log warning
                    return;
                }
            }

            gl.glEnable(GL.GL_TEXTURE_2D);
            iconTexture.bind();

            gl.glColor4d(1d, 1d, 1d, this.getOpacity());
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = this.getScaledIconWidth(spot);
            double height = this.getScaledIconHeight(spot);

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            double scale = this.computeScale(spot, viewport);
            Vec4 locationSW = this.computeLocation(spot, viewport, scale);

            gl.glTranslated((int) locationSW.x, (int) locationSW.y, (int) locationSW.z);
            gl.glScaled(scale, scale, 1);

            TextureCoords texCoords = iconTexture.getImageTexCoords();
            gl.glScaled(width, height, 1d);
            dc.drawUnitQuad(texCoords);
        } finally {
            if (projectionPushed) {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed) {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed) {
                gl.glPopAttrib();
            }
        }
    }

    static final Stroke stroke = new BasicStroke(3.0f);
    
    private BufferedImage getIcon(SpotIcon spot) {
        int w = (int)spot.getWidth();
        int h = (int)spot.getHeight();
        BufferedImage img = new BufferedImage(
                w + 1, h + 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape s = new Ellipse2D.Double(1, 1, spot.getWidth()-3, spot.getHeight()-3);
        g2.setColor(spot.getColor());
        g2.fill(s);
        g2.setStroke(stroke);
        g2.setColor(Color.WHITE);
        g2.draw(s);
        return img;
    }

    private double getScaledIconHeight(SpotIcon spot) {
        return ((double)spot.getHeight()) * iconScale;
    }

    private double getScaledIconWidth(SpotIcon spot) {
        return ((double)spot.getWidth()) * iconScale;
    }

    private void initializeTexture(DrawContext dc, SpotIcon spot) {

        Texture iconTexture = dc.getTextureCache().get(spot);
        if (iconTexture != null) {
            return;
        }
        
        BufferedImage iconImage = getIcon(spot);
        iconTexture = TextureIO.newTexture(iconImage, false);
        iconTexture.bind();
        spot.setWidth(iconTexture.getWidth());
        spot.setHeight(iconTexture.getHeight());
        dc.getTextureCache().put(spot, iconTexture);

        GL gl = dc.getGL();
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);//_MIPMAP_LINEAR);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        // Enable texture anisotropy
        int[] maxAnisotropy = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    public double getIconScale() {
        return iconScale;
    }

    public void setIconScale(double iconScale) {
        this.iconScale = iconScale;
    }

    public String getResizeBehavior() {
        return resizeBehavior;
    }

    public void setResizeBehavior(String resizeBehavior) {
        this.resizeBehavior = resizeBehavior;
    }

    public double getToViewportScale() {
        return toViewportScale;
    }

    public void setToViewportScale(double toViewportScale) {
        this.toViewportScale = toViewportScale;
    }

    private class SpotIcon implements OrderedRenderable {

        private boolean visible = false;
        private double x;
        private double y;
        private double width;
        private double height;
        
        private int id;
        
        private Color color;

        public double getDistanceFromEye() {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint) {
            // Not implemented
        }

        public void render(DrawContext dc) {
            MoteSpotLayer.this.draw(dc, this);
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public double getHeight() {
            return height == -1 ? DEFAULT_SPOT_SIZE : height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getWidth() {
            return width == -1 ? DEFAULT_SPOT_SIZE : width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
        
    }
    
    @Override
    public String toString() {
        return "MoteSpot";
    }

    public void setIR1Size(double width, double height) {
        SpotIcon spot = spotList.get(0);
        spot.setWidth(width);
        spot.setHeight(height);
    }

    public void setIR1Location(double x, double y) {
        SpotIcon spot = spotList.get(0);
        spot.setX(x);
        spot.setY(y);
    }

    public void setIR2Size(double width, double height) {
        SpotIcon spot = spotList.get(1);
        spot.setWidth(width);
        spot.setHeight(height);
    }

    public void setIR2Location(double x, double y) {
        SpotIcon spot = spotList.get(1);
        spot.setX(x);
        spot.setY(y);
    }

    public void setIR3Visible(boolean visible) {
        SpotIcon spot = spotList.get(2);
        spot.setVisible(visible);
    }

    public void setIR3Size(double width, double height) {
        SpotIcon spot = spotList.get(2);
        spot.setWidth(width);
        spot.setHeight(height);
    }

    public void setIR3Location(double x, double y) {
        SpotIcon spot = spotList.get(2);
        spot.setX(x);
        spot.setY(y);
    }

    public void setIR4Visible(boolean visible) {
        SpotIcon spot = spotList.get(3);
        spot.setVisible(visible);
    }

    public void setIR4Size(double width, double height) {
        SpotIcon spot = spotList.get(3);
        spot.setWidth(width);
        spot.setHeight(height);
    }

    public void setIR4Location(double x, double y) {
        SpotIcon spot = spotList.get(3);
        spot.setX(x);
        spot.setY(y);
    }

    public void setIR1Visible(boolean visible) {
        SpotIcon spot = spotList.get(0);
        spot.setVisible(visible);
    }

    public void setIR2Visible(boolean visible) {
        SpotIcon spot = spotList.get(1);
        spot.setVisible(visible);
    }
}
