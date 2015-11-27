package ch.epfl.sweng.calamar.utils;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.CalamarApplication;

@RunWith(JUnit4.class)
public class StorageManagerTest {

    @Rule
    private TemporaryFolder temp = new TemporaryFolder();

    private CalamarApplication app;


    @Before
    public void setUp() {
        app = CalamarApplication.getInstance();
        app.resetPreferences();
    }

    @Test
    public void testNumberIncreases() {

    }
}
