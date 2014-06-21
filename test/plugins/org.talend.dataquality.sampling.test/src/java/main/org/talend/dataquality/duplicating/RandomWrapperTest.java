package org.talend.dataquality.duplicating;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class RandomWrapperTest {

    @Test
    public void testRandomWrapper() {

        RandomWrapper randomWrapper = new RandomWrapper();

        Random random = new Random(randomWrapper.getSeed());
        RandomWrapper randomWrapper2 = new RandomWrapper(randomWrapper.getSeed());

        for (int i = 0; i < 100; i++) {
            int randomInt = randomWrapper.nextInt();

            assertEquals("distinct result random", randomInt, random.nextInt());
            assertEquals("distinct result randomWrapper2", randomInt, randomWrapper2.nextInt());

        }

    }

}
