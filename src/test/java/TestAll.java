import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class TestAll {

    @Test
    public void test() throws FileNotFoundException {
        InterpreterTests it = new InterpreterTests();
        it.testAll();

        MutationTest mt = new MutationTest();
        mt.testAll();

        ParserTest pt = new ParserTest();
        pt.testClone();
        pt.testSize();
        pt.testNodeAt();
        pt.testPrettyPrint();
        pt.testPrintAST();
        pt.testPrettyPrintAll();
        pt.testProgramIsNotNone();

        SensorTest st = new SensorTest();
        st.sensorTests();
    }
}