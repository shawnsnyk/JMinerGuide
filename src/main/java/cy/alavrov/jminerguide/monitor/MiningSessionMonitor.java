/*
 * Copyright (c) 2015, Andrey Lavrov <lavroff@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cy.alavrov.jminerguide.monitor;

import cy.alavrov.jminerguide.data.DataContainer;
import cy.alavrov.jminerguide.data.character.EVECharacter;
import cy.alavrov.jminerguide.data.character.SimpleCharacter;
import cy.alavrov.jminerguide.util.winmanager.IEVEWindow;
import cy.alavrov.jminerguide.util.winmanager.IWindowManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitor for the EVE (mining and whatnot) sessions.
 * @author Andrey Lavrov <lavroff@gmail.com>
 */
public class MiningSessionMonitor {
    /**
     * A comparator to compare sessions by their char sequence.
     * Sessions with no chars receive Integer.MIN_VALUE sequence.
     */
    private final static Comparator<MiningSession> seqComparator = new Comparator<MiningSession>() {

        @Override
        public int compare(MiningSession o1, MiningSession o2) {
            int thisSeq = 0;
            int otherSeq = 0;
            
            ISessionCharacter sChar = o1.getSessionCharacter();
            if (sChar == null) {
                thisSeq = Integer.MIN_VALUE;
            } else {
                thisSeq = sChar.getCoreCharacter().getMonitorSequence();
            }
            
            sChar = o2.getSessionCharacter();
            if (sChar == null) {
                otherSeq = Integer.MIN_VALUE;
            } else {
                otherSeq = sChar.getCoreCharacter().getMonitorSequence();
            }
            
            // greater ones go in first, so reverse comparing.
            return Integer.valueOf(otherSeq).compareTo(thisSeq);            
        }
    };
    
    private final IWindowManager wManager;
    private final DataContainer dCont;
    private volatile IEVEWindow currentWindow = null;
    private final ConcurrentHashMap<IEVEWindow, MiningSession> sessions;

    public MiningSessionMonitor(IWindowManager wManager, DataContainer dCont) {
        this.wManager = wManager;
        this.sessions = new ConcurrentHashMap<>();
        this.dCont = dCont;
    }
    
    /**
     * Updates all available EVE window lists and creates/cleans up sessions
     * accordingly.
     */
    public void update() {
        currentWindow = wManager.getCurrentEVEWindow();
        
        Collection<MiningSession> sessionEntries = sessions.values();
        Iterator<MiningSession> iter = sessionEntries.iterator();
        while (iter.hasNext()) {
            MiningSession session = iter.next();
            
            session.updateWindow();
                                                
            if (!session.exists()) {
                iter.remove();
            } else {
                String name = session.getCharacterName();
                if (name == null) {
                    if (session.getSessionCharacter() != null) iter.remove();
                    // if a window lose it's logged in character (BUT HOW?!)
                    // we probably would be better destroying the session with all contents.                    
                } else {
                    ISessionCharacter curChar = session.getSessionCharacter();
                    if (curChar == null) {
                        // not very optimal, but will do for now, performance overhead is minimal.
                        EVECharacter newChar = dCont.getCharacterContainer().getCharacterByName(name); 
                        SimpleCharacter sChar = dCont.getSimplecCharacterCointainer().getCharacterByName(name);
                        if (newChar != null) {                            
                            session.createSessionCharacter(newChar, sChar, dCont);
                        } else {
                            session.createSessionCharacter(sChar);
                        }
                    } else if (!curChar.getCoreCharacter().getName().equals(name)) {
                        iter.remove();
                        // shouldn't happen ever! and if it does - kill it with fire.
                    }
                }
            }
        }
        
        List<IEVEWindow> windows = wManager.getEVEWindowList();
        for (IEVEWindow window : windows) {
            if (!sessions.containsKey(window)) {
                MiningSession newSession = new MiningSession(window);
                String name = newSession.getCharacterName();
                if (name != null) {
                    EVECharacter newChar = dCont.getCharacterContainer().getCharacterByName(name);        
                    SimpleCharacter sChar = dCont.getSimplecCharacterCointainer().getCharacterByName(name);
                    if (newChar != null) {
                        newSession.createSessionCharacter(newChar, sChar, dCont);
                    } else {
                        newSession.createSessionCharacter(sChar);
                    }
                }
                
                sessions.putIfAbsent(window, newSession);
            }
        }
    }

    /**
     * Returns the current EVE window, or null, if the current window is EVE's.
     * @return 
     */
    public MiningSession getCurrentSession() {
        IEVEWindow window = currentWindow;
        if (window == null) return null;
        
        MiningSession out = sessions.get(window);
        
        if (out == null) {            
            MiningSession newOut = new MiningSession(window);
            out = sessions.putIfAbsent(window, newOut);
            // will return null, if there's nothing here and new value was 
            // inserted successfully, otherwise will return stored value. 
            // We will probably not run into this, but it's better to be safe.
            if (out == null) out = newOut;
        }
        
        return out;
    }
        
    /**
     * Returns all of the sessions available at this moment.
     * Sessions are sorted by their sequence, unknown ones go to the end.
     * @return 
     */
    public List<MiningSession> getSessions() {
        ArrayList<MiningSession> out = new ArrayList<>();
        for (MiningSession session : sessions.values()) {
            out.add(session);
        }
        Collections.sort(out, seqComparator);
        return out;
    }
    
    /**
     * Returns true, if current window belongs to the asteroid monitor or a system (task switching, tile mouseover).
     * @return 
     */
    public boolean isMonitorOrSystemWindow() {
        return wManager.isMonitorOrSystemWindow();
    }
    
    /**
     * Minimizes monitor window.
     */
    public void minimizeMonitorWindow() {
        wManager.minimizeMonitorWindow();
    }
    
    /**
     * Restores monitor window, de-minimizing it if possible.
     */
    public void restoreMonitorWindow() {
        wManager.restoreMonitorWindow();
    }
    
    /**
     * Returns true, if popup on alerts is enabled, and any of the sessions
     * have some alerts to show, false otherwise. Session alerts detection is 
     * influenced by "ignore character" setting, just as any other alert detection.
     * @param settings
     * @return 
     */
    public boolean haveAlerts(AsteroidMonitorSettings settings) {
        if (!settings.isPopupOnAlert()) return false;
        for (MiningSession session : sessions.values()) {
            if (session.haveAlerts()) return true;
        }
        
        return false;
    }
}
