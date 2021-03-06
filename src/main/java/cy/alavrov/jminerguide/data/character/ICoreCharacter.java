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
package cy.alavrov.jminerguide.data.character;

import cy.alavrov.jminerguide.data.harvestable.BasicHarvestable;
import java.util.HashSet;

/**
 * Core character for ISessionCharacter implementations.
 * @author Andrey Lavrov <lavroff@gmail.com>
 */
public interface ICoreCharacter {
    public String getName();
    
    public void setMonitorSequence(int monitorSequence);
    public int getMonitorSequence();
    
    public HashSet<BasicHarvestable> getAsteroidFilter();
    public void addHarvestableToFilter(BasicHarvestable type);
    public void removeHarvestableFromFilter(BasicHarvestable type);
    public void clearAsteroidFilter();
    public void allOnAsteroidFilter();
    
    public boolean isMonitorIgnore();
    public void setMonitorIgnore(boolean monitorIgnore);
    public boolean isMonitorSimple();
    public void setMonitorSimple(boolean monitorSimple);
}
