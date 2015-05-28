package org.talend.dataquality.duplicating;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper of java.util.Random with possibility of getting the seed.
 */
public class RandomWrapper extends Random {

    private static final long serialVersionUID = 1L;

    private Random random;

    private final AtomicLong seed;

    public Random getRandom() {
        return random;
    }

    public long getSeed() {
        return seed.get();
    }

    public RandomWrapper() {
        this(seedUniquifier() ^ System.nanoTime());
    }

    private static long seedUniquifier() {
        // L'Ecuyer, "Tables of Linear Congruential Generators of
        // Different Sizes and Good Lattice Structure", 1999
        for (;;) {
            long current = seedUniquifier.get();
            long next = current * 181783497276652981L;
            if (seedUniquifier.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);

    public RandomWrapper(long seed) {
        this.seed = new AtomicLong(seed);
        random = new Random(seed);
    }

    @Override
    protected int next(int bits) {
        // Ugh, can't delegate this method -- it's protected
        // Callers can't use it and other methods are delegated, so shouldn't matter
        throw new UnsupportedOperationException();
    }

    @Override
    public void nextBytes(byte[] bytes) {
        random.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    @Override
    public int nextInt(int n) {
        return random.nextInt(n);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return random.nextGaussian();
    }

}
