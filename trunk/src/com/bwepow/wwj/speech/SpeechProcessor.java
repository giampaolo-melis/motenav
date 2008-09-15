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

/*
 * Based on Sphinx 4 jsapi demo
 */

package com.bwepow.wwj.speech;

import edu.cmu.sphinx.jsapi.JSGFGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;

public class SpeechProcessor {
    private Recognizer recognizer;
    private JSGFGrammar jsgfGrammarManager;
    private Microphone microphone;
        private String command;
    public static final String PROPERTY_COMMAND = "command";

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        String oldCommand = this.command;
        this.command = command;
        propertyChangeSupport.firePropertyChange(PROPERTY_COMMAND, oldCommand, command);
    }

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    private boolean running = true;
    public static final String PROPERTY_RUNNING = "running";

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        boolean oldRunning = this.running;
        this.running = running;
        propertyChangeSupport.firePropertyChange(PROPERTY_RUNNING, oldRunning, running);
    }

    public SpeechProcessor() throws 
            IOException, PropertyException, InstantiationException {

        final String configResource = "jsgf.config.xml";
        ClassLoader loader = this.getClass().getClassLoader();
        
        URL url = loader.getResource(configResource);
        if (url == null)
            url = ClassLoader.getSystemResource(configResource);

        System.out.println("Loading config file: " + url);
        
        ConfigurationManager cm = new ConfigurationManager(url);

        // retrive the recognizer, jsgfGrammar and the microphone from
        // the configuration file.

        recognizer = (Recognizer) cm.lookup("recognizer");
        jsgfGrammarManager = (JSGFGrammar) cm.lookup("jsgfGrammar");
        microphone = (Microphone) cm.lookup("microphone");
    }

    public void execute() throws IOException, GrammarException  {
        System.out.println("JSGF Demo Version 1.0\n");

        System.out.print(" Loading recognizer ...");
        recognizer.allocate();
        System.out.println(" Ready");

        if (microphone.startRecording()) {
            loadAndRecognize("locations");
        } else {
            System.out.println("Can't start the microphone");
        }

        System.out.print("\nDone. Cleaning up ...");
        recognizer.deallocate();

        System.out.println(" Goodbye.\n");
        System.exit(0);
    }

    private void loadAndRecognize(String grammarName) throws
            IOException, GrammarException  {
        jsgfGrammarManager.loadJSGF(grammarName);
        dumpSampleSentences(grammarName);
        recognizeAndReport();
    }

    private void recognizeAndReport() throws GrammarException {
        boolean done = false;


        while (!done && isRunning())  {
            Result result = recognizer.recognize();
            String bestResult = result.getBestFinalResultNoFiller();
            RuleGrammar ruleGrammar = jsgfGrammarManager.getRuleGrammar();
            RuleParse ruleParse = ruleGrammar.parse(bestResult, null);
            if (ruleParse != null) {
                System.out.println("\n  " + bestResult + "\n");
                setCommand(bestResult);
                done = isExit(ruleParse);
            } 
        }
    }

    private boolean isExit(RuleParse ruleParse) {
        String[] tags = ruleParse.getTags();

        for (int i = 0; tags != null && i < tags.length; i++) {
            if (tags[i].trim().equals("exit")) {
                return true;
            }
        }
        return  false;
    }

    private void dumpSampleSentences(String title) {
        System.out.println(" ====== " + title + " ======");
        System.out.println("Speak one of: \n");
        jsgfGrammarManager.dumpRandomSentences(200);
        System.out.println(" ============================");
    }
}
