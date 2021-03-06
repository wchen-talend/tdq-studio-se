import java.io.ByteArrayInputStream;

import Zql.ZStatement;
import Zql.ZqlParser;

public class StringDemo {

    public static void main(String args[]) {
        try {

            ZqlParser p = new ZqlParser();

            p.initParser(new ByteArrayInputStream(args[0].getBytes()));
            ZStatement st = p.readStatement();
            System.out.println(st.toString()); // Display the statement

        } catch (Exception e) {
            System.out.println(e);
        }
    }

};
