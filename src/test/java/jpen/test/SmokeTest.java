package jpen.test;

import junit.framework.*;
import jpen.*;
import javax.swing.*;

public class SmokeTest extends TestCase {

    public SmokeTest(String name) {
      super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPenManager() throws Exception {
        JComponent component = new JPanel();
	PenManager penManager = new PenManager(component);
    }
}

