// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.standardization.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.core.ui.utils.PluginUtil;
import org.talend.dataquality.standardization.main.HandLuceneImplTest;

/**
 * DOC sizhaoliu class global comment. Detailled comment
 */
public class FirstNameStandardizeTest {

    private final static String indexfolder = PluginUtil.getPluginInstallPath(HandLuceneImplTest.PLUGIN_ID).concat(
            "data/TalendGivenNames_index"); // $NON-NLS-1$ //$NON-NLS-1$

    private static IndexSearcher searcher = null;

    private static Analyzer searchAnalyzer = null;

    private static FirstNameStandardize fnameStandardize = null;

    private static final String inputName = "Michel"; //$NON-NLS-1$

    private static final String[][] expected = { { "Michel", "AUS", "MICHEL", "MICHEL", "MICHEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            { "Michel", "BEL", "MICHEL", "MICHEL", "MICHEL" }, { "Michel", "DEU", "MICHEL", "MICHEL", "MICHEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Michel", "ESP", "MICHEL", "MICHEL", "MICHEL" }, { "Michel", "FRA", "MICHEL", "MICHEL", "MICHEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Michel", "ITA", "MICHELA", "MICHELA", "MICHELA" }, { "Michel", "RUS", "MICHEL", "MICHEL", "MICHEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Michel", "USA", "MICHEL", "MICHEL", "MICHEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

            { "Adrian", "AUS", "ADRIAN", "ADRIAN", "ADRIAN" }, { "Adrian", "BEL", "ADRIAN", "ADRIAN", "ADRIAN" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Adrian", "DEU", "MARIAN", "MARIAN", "MARIAN" }, { "Adrian", "ESP", "ADRIAN", "ADRIAN", "ADRIAN" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Adrian", "FRA", "ADRIAN", "ADRIAN", "ADRIAN" }, { "Adrian", "ITA", "ADRIANO", "ADRIANO", "ADRIANO" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            { "Adrian", "RUS", "BRIAN", "BRIAN", "BRIAN" }, { "Adrian", "USA", "ADRIAN", "ADRIAN", "ADRIAN" }, }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

    private static final String[][] expected_fuzzy = { { "Alessandra", "ALESSANDRA", "ALESSANDRA" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            { "Antonino", "ANTONINO", "ANTONINO" }, { "amar", "AMAR", "AMAR" }, { "jan", "JAN", "JAN" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            { "James", "JAMES", "JAMES" }, { "Keith", "KEITH", "KEITH" }, { "guy", "GUY", "GUY" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            { "roland", "ROLAND", "ROLAND" }, { "Angela", "ANGELA", "ANGELA" }, { "Joe", "JOE", "JOE" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            { "eric", "ERIC", "ERIC" }, { "francesco", "FRANCESCO", "FRANCESCO" }, { "Manfred", "MANFRED", "MANFRED" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            { "malathi", "", "MALACHI" }, { "Aly", "ALY", "ALY" }, { "sreedhar", "", "" }, { "Louann", "LOUANN", "LOUANN" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
            { "Elif", "ELIF", "ELIF" }, { "Sreenivas", "", "" }, { "subhash", "SUBHASH", "SUBHASH" }, { "Dara", "DARA", "DARA" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
            { "Gabor", "GABOR", "GABOR" }, { "Jill", "JILL", "JILL" }, { "Michael", "MICHAEL", "MICHAEL" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            { "bhargav", "", "BHARGAW" }, { "nonya", "", "NONNA" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    /**
     * DOC sizhaoliu Comment method "setUpBeforeClass".
     * 
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Directory dir = FSDirectory.open(new File(indexfolder));
        searcher = new IndexSearcher(dir);
        searchAnalyzer = new SimpleAnalyzer();
        fnameStandardize = new FirstNameStandardize(searcher, searchAnalyzer, 10);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (searcher != null) {
            searcher.close();
        }
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.standardization.query.FirstNameStandardize#replaceName(java.lang.String, boolean)}.
     */
    @Test
    public void testReplaceName() {
        try {
            String res = fnameStandardize.replaceName(inputName, true);
            System.out.println("testReplaceName:\n" + res); //$NON-NLS-1$
            assertEquals("MICHEL", res); //$NON-NLS-1$

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.standardization.query.FirstNameStandardize#replaceNameWithCountryGenderInfo(java.lang.String, java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testReplaceNameWithCountryGenderInfo() {
        try {

            System.out.println("\ntestReplaceNameWithCountryGenderInfo:"); //$NON-NLS-1$
            System.out.println("Name\tCountry\tNon-gender\tMale\tFemale"); //$NON-NLS-1$
            for (String[] testCase : expected) {
                String res, resF, resM = ""; //$NON-NLS-1$
                System.out.print("{\"" + testCase[0] + "\", \"" + testCase[1] + "\", \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                // results for query without gender info
                res = fnameStandardize.replaceNameWithCountryInfo(testCase[0], testCase[1], true);
                System.out.print(res + "\", \""); //$NON-NLS-1$
                assertEquals(testCase[2], res);

                // results for female first name query
                resF = fnameStandardize.replaceNameWithCountryGenderInfo(testCase[0], testCase[1], "F", true); //$NON-NLS-1$
                System.out.print(resF + "\", \""); //$NON-NLS-1$
                assertEquals(testCase[3], resF);

                // results for female first name query
                resM = fnameStandardize.replaceNameWithCountryGenderInfo(testCase[0], testCase[1], "M", true); //$NON-NLS-1$
                System.out.println(resM + "\"},"); //$NON-NLS-1$
                assertEquals(testCase[4], resM);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testReplaceNameWithFuzzyOption() {
        try {

            System.out.println("\ntestReplaceNameWithFuzzyOption:"); //$NON-NLS-1$
            System.out.println("Name\tNon-fuzzy\tFuzzy"); //$NON-NLS-1$
            for (String[] testCase : expected_fuzzy) {
                String res = ""; //$NON-NLS-1$
                System.out.print("{\"" + testCase[0] + "\", \""); //$NON-NLS-1$ //$NON-NLS-2$

                // results for non-country, non-fuzzy match
                res = fnameStandardize.replaceName(testCase[0], false);
                System.out.print(res + "\", \""); //$NON-NLS-1$
                assertEquals(testCase[1], res);

                // results for non-country, fuzzy match
                res = fnameStandardize.replaceName(testCase[0], true);
                System.out.print(res + "\"},\n"); //$NON-NLS-1$
                assertEquals(testCase[2], res);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
