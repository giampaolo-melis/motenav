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

/**
 *
 * @author Giampaolo Melis
 */
public interface IRController {
    public void setIR1Size(double width, double height);
    public void setIR1Location(double x, double y);
    public void setIR2Size(double width, double height);
    public void setIR2Location(double x, double y);
    public void setIR3Visible(boolean visible);
    public void setIR3Size(double width, double height);
    public void setIR3Location(double x, double y);
    public void setIR4Visible(boolean visible);
    public void setIR4Size(double width, double height);
    public void setIR4Location(double x, double y);
    public void setIR1Visible(boolean visible);
    public void setIR2Visible(boolean visible);
}
