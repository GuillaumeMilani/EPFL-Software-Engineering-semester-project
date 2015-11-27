package ch.epfl.sweng.calamar.map;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.utils.StorageCallbacks;
import ch.epfl.sweng.calamar.utils.StorageManager;


/**
 * A simple {@link Fragment} subclass holding the calamar map !.
 */
public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    public static final String TAG = MapFragment.class.getSimpleName();
    public static final String POSITIONKEY = MapFragment.class.getCanonicalName() + ":POSITION";

    //TODO : add two buttons begin checks stop checks
    // that will : checklocation settings + startlocation updates
    //TODO : manage activity lifecycle : start stop location updates when not needed, plus many potential problems
    //TODO : do we save state of fragment/map using a bundle ?


    //TODO : Use a bidirectional map ?
    private Map<Item, Marker> markers;
    // TODO : Create a set of items to avoid diplaying the same items multiple time
    private Map<Marker, Item> itemFromMarkers;
    private StorageManager storageManager;


    private GoogleMap map; // Might be null if Google Play services APK is not available.
    // however google play services are checked at app startup...and
    // maps fragment will do all the necessary if gplay services apk not present
    // see comment on setupMapIfNeeded
    // ....maybe delegate all the work to the map fragment, I think google has correctly done the job...

    private GPSProvider gpsProvider;
    private final GPSProvider.Observer gpsObserver = new GPSProvider.Observer() {
        @Override
        public void update(Location newLocation) {
            if (null == map) {
                throw new IllegalStateException(
                        "map should be initialized and ready before accessed by location updater");
            }
            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng myLoc = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
            map.moveCamera(CameraUpdateFactory.zoomTo(18.0f));
        }
    };


    // The condition is updated when the location change and if the value(true/false) of the
    // condition change -> The item is updated, if all are true
    // -> we get updated and update the value of the marker on the map.
    private final Item.Observer itemObserver = new Item.Observer() {
        @Override
        public void update(Item item) {
            Marker updatedMarker = markers.get(item);
            Bitmap icon;
            if (item.getCondition().getValue()) {
                updatedMarker.setTitle("Unlocked");
                icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.unlock);
            } else {
                updatedMarker.setTitle("Locked");
                icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.lock);
            }
            updatedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    };

    private LinearLayout detailsViewDialog;

    //When the condition is okay, we update the item description
    private final Item.Observer detailsItemObserver = new Item.Observer() {
        @Override
        public void update(Item item) {
            //Update the dialog with the new view.
            View itemView = item.getView(getActivity());
            detailsViewDialog.removeAllViews();
            detailsViewDialog.addView(itemView);
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
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        markers = new HashMap<>();
        itemFromMarkers = new HashMap<>();

        detailsViewDialog = new LinearLayout(getActivity());
        detailsViewDialog.setOrientation(LinearLayout.VERTICAL);
        storageManager = CalamarApplication.getInstance().getStorageManager();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // REFRESH BUTTON
        getView().findViewById(R.id.refreshButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAllItemsInRegionToMap();
            }
        });
        setUpMapIfNeeded();
    }

    // map setup here :
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(new MarkerWithStorageCallBackListener());
        setUpGPS();
        addAllItemsInRegionToMap();
    }


    @Override
    // is called after activity.onStop !!!
    // https://developer.android.com/guide/components/fragments.html#Lifecycle
    public void onStop() {

        // cause exception because client is disconnected in activity.onStop
        //gpsProvider.stopLocationUpdates();
        super.onStop();

        //TODO
        // think about when should we start stop locationUpdates
        // on user demand ? via buttons, adds interaction and "user control"
        // or on create / stop ??
    }
    // *********************************************************************************************

    private void addAllItemsInRegionToMap() {
        if (null != map) {
            VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
            new RefreshTask(visibleRegion, getActivity()).execute();
        } else {
            throw new IllegalStateException("map not ready when refresh");
        }

    }

    /**
     * Add an item to the googleMap, and fill the map markers
     */
    private void addItemToMap(Item i) {
        Condition condition = i.getCondition();
        if (condition.hasLocation()) {
            Location location = condition.getLocation();

            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()));

            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.lock));

            marker.title("Locked");

            i.addObserver(itemObserver);

            Marker finalMarker = map.addMarker(marker);
            markers.put(i, finalMarker);
            itemFromMarkers.put(finalMarker, i);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpGPS()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
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

    private void setUpGPS() {
        gpsProvider = GPSProvider.getInstance();
        gpsProvider.addObserver(gpsObserver);
        gpsProvider.startLocationUpdates(getActivity());
    }


    /**
     * Async task for refreshing / getting new localized items.
     */
    private class RefreshTask extends AsyncTask<Void, Void, List<Item>> {

        private final VisibleRegion visibleRegion;
        private final Activity context;

        public RefreshTask(VisibleRegion visibleRegion, Activity context) {
            if (null == visibleRegion || null == context) {
                throw new IllegalArgumentException("RefreshTask: visibleRegion or context is null");
            }
            this.visibleRegion = visibleRegion;
            this.context = context;
        }

        @Override
        protected List<Item> doInBackground(Void... v) {
            try {

                return DatabaseClientLocator.getDatabaseClient().getAllItems(
                        CalamarApplication.getInstance().getCurrentUser(),
                        new Date(0), //TODO need to ask others what are we going to do with the local database
                        //lastrefresh and etc...
                        //or we don't store them in local db and everytime we refresh we flush the map
                        //before re adding all the items in visible range
                        //I think it can make sense ..
                        visibleRegion);

            } catch (DatabaseClientException e) {
                Log.e(MapFragment.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            if (items != null) {

                for (Item item : items) {
                    addItemToMap(item);
                }

                Log.i(MapFragment.TAG, "map refreshed");

                Toast.makeText(context, R.string.refresh_message,
                        Toast.LENGTH_SHORT).show();

            } else {
                Log.e(MapFragment.TAG, "unable to refresh");
                AlertDialog.Builder newUserAlert = new AlertDialog.Builder(context);
                newUserAlert.setTitle(R.string.unable_to_refresh_message);
                newUserAlert.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                newUserAlert.show();
            }
        }

    }

    private class MarkerWithStorageCallBackListener implements GoogleMap.OnMarkerClickListener, StorageCallbacks {

        private AlertDialog dialog;
        private Item item;

        @Override
        public boolean onMarkerClick(Marker marker) {
            item = itemFromMarkers.get(marker);

            AlertDialog.Builder itemDescription = new AlertDialog.Builder(getActivity());
            itemDescription.setTitle(R.string.item_details_alertDialog_title);

            itemDescription.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    item.removeObserver(detailsItemObserver);
                }
            });

            //OnCancel is called when we press the back button.
            itemDescription.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    item.removeObserver(detailsItemObserver);
                }
            });

            //Create a new view
            detailsViewDialog = new LinearLayout(getActivity());
            detailsViewDialog.setOrientation(LinearLayout.VERTICAL);

            detailsViewDialog.addView(item.getView(getActivity()));


            itemDescription.setView(detailsViewDialog);

            item.addObserver(detailsItemObserver);

            dialog = itemDescription.show();

            return false;
        }


        @Override
        public void onItemRetrieved(Item i) {
            item = i;
            dialog.setView(item.getView(MapFragment.this.getActivity()));
        }

        @Override
        public void onDataRetrieved(byte[] data) {
            switch (item.getType()) {
                case SIMPLETEXTITEM:
                    break;
                case FILEITEM:
                    item = new FileItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), data, ((FileItem) item).getPath());
                    break;
                case IMAGEITEM:
                    item = new ImageItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), data, ((ImageItem) item).getPath());
                    break;
                default:
                    throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.unknown_item_type));
            }
            dialog.setView(item.getView(MapFragment.this.getActivity()));
        }
    }

}
