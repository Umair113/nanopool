/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReferenceArray;


public abstract class AtomicCasArraySupport<T>
        extends CasArraySupport<T> implements CasArray<T> {
    protected final AtomicReferenceArray<T> array;
    
    public AtomicCasArraySupport(int size) {
        array = new AtomicReferenceArray<T>(size);
    }
    
    public boolean cas(int idx, T newValue, T oldValue) {
        return doCas(array, idx, newValue, oldValue);
    }

    public final T get(int idx) {
        return array.get(idx);
    }
    
    public final int length() {
        return array.length();
    }
    
    protected abstract boolean doCas(AtomicReferenceArray<T> array, int idx,
            T newValue, T oldValue);
}