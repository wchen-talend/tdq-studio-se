package org.talend.dataquality.datamasking;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataquality.duplicating.RandomWrapper;

public abstract class DataMasker<TIn, TOut> {

    private static RandomWrapper rnd = null;

    public DataMasker() {
        setRnd(new RandomWrapper());
    }

    public DataMasker(long seed) {
        setRnd(new RandomWrapper(seed));
    }

    protected abstract TOut generateOutput(TIn v, boolean isOriginal);

    public List<TOut> process(TIn v) {
        List<TOut> reslutList = new ArrayList<TOut>();

        reslutList.add(generateOutput(v, true));
        reslutList.add(generateOutput(v, false));

        return reslutList;
    }

    public static RandomWrapper getRnd() {
        return rnd;
    }

    public static void setRnd(RandomWrapper rnd) {
        DataMasker.rnd = rnd;
    }
}