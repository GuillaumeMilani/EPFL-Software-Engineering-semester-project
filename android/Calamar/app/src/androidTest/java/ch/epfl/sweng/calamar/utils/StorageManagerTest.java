package ch.epfl.sweng.calamar.utils;


import android.support.test.InstrumentationRegistry;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.CalamarApplication;

@RunWith(JUnit4.class)
public class StorageManagerTest extends ApplicationTestCase<CalamarApplication> {

    @Rule
    private TemporaryFolder temp = new TemporaryFolder();

    private CalamarApplication app;

    public StorageManagerTest() {
        super(CalamarApplication.class);
    }

    @Before
    @Override
    public void setUp() {
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        getApplication();
    }

    @Test
    public void testNumberIncreases() {
        
    }
}
