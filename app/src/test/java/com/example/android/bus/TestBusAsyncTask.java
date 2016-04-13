package com.example.android.bus;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dell on 4/12/2016.
 */
public class TestBusAsyncTask extends AndroidTestCase {

    BusAsyncTask task;
    ArrayList<BusInfo> list;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        task = new BusAsyncTask();
        list = new ArrayList<>();
    }

    public void testJson() {
        String location = "10.852132,106.623353";
        try {
            list = task.execute(location).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        assertEquals("Failed " + list.size(), 90, list.size());
    }

}
