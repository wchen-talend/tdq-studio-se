package org.talend.dataquality.datamasking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.talend.dataquality.duplicating.RandomWrapper;

public abstract class DataMasker<TIn, TOut> {

    private long seed;

    private RandomWrapper rnd;

    private static Logger log = Logger.getLogger(DataMasker.class);

    public DataMasker(long seed) {
        this.seed = seed;
    }

    protected abstract TOut generateOutput(TIn v, boolean isOriginal);

    public List<TOut> process(TIn v) {
        List<TOut> reslutList = new ArrayList<TOut>();

        reslutList.add(generateOutput(v, true));
        reslutList.add(generateOutput(v, false));

        return reslutList;
    }

    public Random getRandom() {
        if (rnd == null) {
            rnd = new RandomWrapper(seed);

            if (log.isInfoEnabled()) {
                log.info("Seed is set to " + seed);
            }
        }

        return rnd;
    }
}