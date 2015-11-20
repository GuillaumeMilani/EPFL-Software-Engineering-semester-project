package ch.epfl.sweng.calamar.map;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static ch.epfl.sweng.calamar.item.Item.Type.SIMPLETEXTITEM;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    public static final String TAG = MapFragment.class.getSimpleName();

    //TODO : add two buttons begin checks stop checks
    // that will : checklocation settings + startlocation updates
    //TODO : manage activity lifecycle : start stop location updates when not needed, plus many potential problems


    private Map<Item,Marker> markers;

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    // however google play services are checked at app startup...and
    // maps fragment will do all the necessary if gplay services apk not present
    // see comment on setupMapIfNeeded
    // ....maybe delegate all the work to the map fragment, I think google has correctly done the job...

    private GPSProvider gpsProvider;
    private final GPSProvider.Observer gpsObserver = new GPSProvider.Observer() {
        @Override
        public void update(Location newLocation) {
            assert map != null :
                    "map should be initialized and ready before accessed by location updater";
            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng myLoc = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
            map.moveCamera(CameraUpdateFactory.zoomTo(18.0f));
        }
    };



    // The condition get updated when the location change and if the value(true/false) of the
    // condition change -> The item is updated, if all are true
    // -> we get updated and update the value of the marker on the map.
    private final Item.Observer itemObserver = new Item.Observer() {
        @Override
        public void update(Item item) {
            Marker updatedMarker = markers.get(item);
            if(item.getCondition().getValue()) {
                //TODO : Maybe better to let the item display himself ? ( issue #91 )
                switch (item.getType()) {
                    case SIMPLETEXTITEM:
                        SimpleTextItem textItem = (SimpleTextItem)item;
                        updatedMarker.setTitle(textItem.getMessage());
                        updatedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected Item type (" + item.getType() + ")");
                }
            } else {
                updatedMarker.setTitle("Locked");
                updatedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
        }
    };



    public MapFragment() {
        // Required empty public constructor
    }

    // *********************************************************************************************
    // map fragment lifecycle callbacks
    // https://developer.android.com/guide/components/fragments.html
    /**
     * Replaces the "onCreate" method from an Activity
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        markers = new HashMap<>();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().findViewById(R.id.addNewItemButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        setUpMapIfNeeded(); // if needed, onMapReady is called with the map
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMyLocationEnabled(true);
        addAllItemToMap();
        setUpGPS(); // register to the GPSProvider location updates
    }

    @Override
    public void onStop() {
        super.onStop();
        //TODO
        // think about when should we start stop locationUpdates
        // on user demand ? via buttons, adds interaction and "user control"
        // or on create / stop ??
        // gpsProvider.stopLocationUpdates();
    }
    // *********************************************************************************************


    /**
     * Call by fragment_map.xml
     *
     * TOTO : To remove once pull request 90 is ready.
     *
     */
    public void addItem() {
        //TODO : add a real item
        User bob = new User(1, "bob");
        User alice = new User(2, "alice");
        Location l = new Location("Calamar");
        //TODO : Be sure that the location is correct
        gpsProvider.getLastLocation().getLatitude();
        l.setLatitude(gpsProvider.getLastLocation().getLatitude());
        l.setLongitude(gpsProvider.getLastLocation().getLongitude());

        addItemToMap(new SimpleTextItem(10, bob, alice, new Date(), new PositionCondition(l, 5), "Password : calamar42"));
    }

    private void addAllItemToMap(){
        List<Item> item = getItemToDisplay();

        for(Item i : item){
            addItemToMap(i);
        }

    }

    /**
     * Add all item to the googleMap, and fill the map markers
     */
    private void addItemToMap(Item i) {
        //TODO : WRONG, we have to change this once the issue #56 is solved.
        PositionCondition pos = (PositionCondition)i.getCondition();

        Location l = pos.getLocation();

        MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(l.getLatitude(), l.getLongitude()));

        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        marker.title("Locked");

        Condition condition = i.getCondition();

        i.addObserver(itemObserver);

        markers.put(i,map.addMarker(marker));

    }

    /**
     * Return the list of all item we want to display.
     *
     * @return list of all item we want to display.
     */
    private List<Item> getItemToDisplay() {
        //TODO
        return null;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpGPS()} once when {@link #map} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpGPS() {
        gpsProvider = GPSProvider.getInstance();
        gpsProvider.addObserver(gpsObserver);
        gpsProvider.startLocationUpdates(getActivity());
    }
}
