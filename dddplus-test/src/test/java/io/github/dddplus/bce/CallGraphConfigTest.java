package io.github.dddplus.bce;

import io.github.dddplus.ast.model.CallGraphEntry;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class CallGraphConfigTest {

    @Test
    void fromFile() throws FileNotFoundException {
        try {
            CallGraphConfig.fromFile("../doc/none");
            fail();
        } catch (FileNotFoundException expected) {
        }

        CallGraphConfig config = CallGraphConfig.fromFile("../doc/callgraph.json");
        assertTrue(config.getIgnore().getEnumClazz());
        assertNotNull(config.getIgnore().getCalleeMethods());
        assertTrue(config.useSimpleClassName());
    }

    @Test
    void ignoreCaller() throws FileNotFoundException {
        CallGraphConfig config = CallGraphConfig.fromFile("../doc/callgraph.json");
        MethodVisitor m = new MethodVisitor("io.git.dddplus.jdbc", "io.git.dddplus.jdbc.FooDao", "query", config);
        CallGraphEntry entry = new CallGraphEntry(config, "io.git.dddplus.jdbc.FooDao", "query", "io.git.dddplus.jdbc.impl.Bar", "bar");
        assertTrue(config.ignoreCaller(m));
        assertTrue(config.ignoreInvokeInstruction(m, null, entry));
    }

}