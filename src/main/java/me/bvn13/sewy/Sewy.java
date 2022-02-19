/*
   Copyright 2022 Vyacheslav Boyko

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
package me.bvn13.sewy;

import me.bvn13.sewy.command.AbstractCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Supporting class providing protocol restrictions
 */
public final class Sewy {

    static final byte SEPARATOR = '\n';

    private static Sewy INSTANCE;
    private static final ReentrantLock LOCK = new ReentrantLock();

    private final List<Class<?>> registeredDataTypes = new CopyOnWriteArrayList<>();
    private byte[] separator = new byte[] { SEPARATOR };

    /**
     * Registers command in white list for further communications
     * @param clazz command class
     * @param <T> generic type
     */
    public static <T extends AbstractCommand> void register(Class<T> clazz) {
        getInstance().registeredDataTypes.add(clazz);
    }

    /**
     * Registers commands in white list for further communications
     * @param classes array of command classes
     * @param <T> generic type
     */
    public static <T extends AbstractCommand> void register(Class<T>[] classes) {
        for (Class<T> clazz: classes) {
            register(clazz);
        }
    }

    public static byte[] getSeparator() {
        return getInstance().separator;
    }

    public static void setSeparator(byte[] separator) {
        try {
            LOCK.lock();
            getInstance().separator = separator;
        } finally {
            LOCK.unlock();
        }
    }

    private Sewy() {
    }

    @SuppressWarnings("unchecked")
    static List<Class<AbstractCommand>> getRegisteredDataTypes() {
        final List<Class<AbstractCommand>> dataTypes = new ArrayList<>();
        for (Class<?> registeredDataType : INSTANCE.registeredDataTypes) {
            dataTypes.add((Class<AbstractCommand>) registeredDataType);
        }
        return dataTypes;
    }

    private static Sewy getInstance() {
        try {
            LOCK.lock();
            if (INSTANCE == null) {
                INSTANCE = new Sewy();
            }
            return INSTANCE;
        } finally {
            LOCK.unlock();
        }
    }

}
