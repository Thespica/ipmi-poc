/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.common;

import java.util.Date;
import java.util.Random;

/**
 * Random number generator for the library.
 */
public final class Randomizer {
    private static final Random RANDOM = new Random(new Date().getTime());

    /**
     * Private constructor. This only a namespace class and should not be instantiated.
     */
    private Randomizer() {
    }

    /**
     * Get a random integer from {@link #RANDOM}.
     *
     * @return generated random integer
     */
    public static int getInt() {
        return RANDOM.nextInt();
    }
}
