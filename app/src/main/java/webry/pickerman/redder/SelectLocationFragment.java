package webry.pickerman.redder;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;

public class SelectLocationFragment extends Fragment implements OnMapReadyCallback, Constants  {

    public static final int RESULT_OK = -1;

    private CardView mSelectLocationTooltip;
    private ImageButton mCloseTooltipButton;

    private CardView mSelectLocationPromoTooltip;
    private ImageButton mCloseTooltipPromoButton;

    private GoogleMap mMap;

    private Marker mMarker;

    private LatLng location;

    private Double lat, lng;

    private String countryName = "Unknown", stateName = "Unknown", cityName = "Unknown";

    private Boolean action_new_item = false;

    public SelectLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        Intent i = getActivity().getIntent();

        lat = i.getDoubleExtra("lat", 0.000000);
        lng = i.getDoubleExtra("lng", 0.000000);

        action_new_item = i.getBooleanExtra("action_new_item", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_select_location, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSelectLocationTooltip = (CardView) rootView.findViewById(R.id.select_location_tooltip);
        mCloseTooltipButton = (ImageButton) rootView.findViewById(R.id.close_tooltip_button);

        location = new LatLng(lat, lng);

        if (App.getInstance().getTooltipsSettings().isAllowShowSelectLocationTooltip()) {

            mSelectLocationTooltip.setVisibility(View.VISIBLE);

        } else {

            mSelectLocationTooltip.setVisibility(View.GONE);
        }

        mCloseTooltipButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mSelectLocationTooltip.setVisibility(View.GONE);

                App.getInstance().getTooltipsSettings().setShowSelectLocationTooltip(false);
                App.getInstance().saveTooltipsSettings();
            }
        });

        // For promo tooltip | When new item create or edit item

        mSelectLocationPromoTooltip = (CardView) rootView.findViewById(R.id.select_location_promo_tooltip);
        mCloseTooltipPromoButton = (ImageButton) rootView.findViewById(R.id.close_tooltip_promo_button);

        if (action_new_item && App.getInstance().getTooltipsSettings().isAllowShowSelectLocationPromoTooltip()) {

            mSelectLocationPromoTooltip.setVisibility(View.VISIBLE);

        } else {

            mSelectLocationPromoTooltip.setVisibility(View.GONE);
        }

        mCloseTooltipPromoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mSelectLocationPromoTooltip.setVisibility(View.GONE);

                App.getInstance().getTooltipsSettings().setShowSelectLocationPromoTooltip(false);
                App.getInstance().saveTooltipsSettings();
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMarker = mMap.addMarker(new MarkerOptions().position(location).title(getString(R.string.msg_marker_select_location)).draggable(true)); //.draggable(true)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7));

        if (lat != 0.0 && lng != 0.0) {

            updateTitle();
        }

        setUpMap();
    }

    private void setUpMap() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                mMap.clear();

                cityName = "Unknown";
                countryName = "Unknown";
                stateName = "Unknown";

                getActivity().setTitle(getString(R.string.title_activity_select_location));

                // Add a marker in latLng and move the camera
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.msg_marker_select_location)).draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                lat = latLng.latitude;
                lng = latLng.longitude;

                updateTitle();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker arg0) {

                // TODO Auto-generated method stub

                Log.d("Marker", "Dragging");
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {

                // TODO Auto-generated method stub

                LatLng markerLocation = mMarker.getPosition();

                lat = markerLocation.latitude;
                lng = markerLocation.longitude;

                updateTitle();
            }

            @Override
            public void onMarkerDragStart(Marker arg0) {

                // TODO Auto-generated method stub

                Log.d("Marker", "Started");

            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

//                mMap.clear();
//
//                cityName = "Unknown";
//                countryName = "Unknown";
//                stateName = "Unknown";
//
//                getActivity().setTitle(getString(R.string.title_activity_select_location));
//
//                // Add a marker in latLng and move the camera
//                mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.msg_marker_select_location)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//
//                lat = latLng.latitude;
//                lng = latLng.longitude;
//
//                updateTitle();
            }
        });

        //mMap.setMyLocationEnabled(true);
//                mMap.getUiSettings().setCompassEnabled(true);
//                mMap.getUiSettings().setZoomControlsEnabled(true);
//                mMap.getMaxZoomLevel();
//                mMap.getMinZoomLevel();
//                mMap.getUiSettings();
//                mMap.animateCamera(CameraUpdateFactory.zoomIn());
//                mMap.animateCamera(CameraUpdateFactory.zoomOut());
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
    }

    private void updateTitle() {

        getAddress(new LatLng(lat, lng));

        if (countryName.equals("Unknown") || cityName.equals("Unknown")) {

            getActivity().setTitle(getString(R.string.title_activity_select_location));

        } else {

            getActivity().setTitle(countryName + ", " + cityName);
        }
    }

    public void getAddress(LatLng location) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.US);

        try {

            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);

            if (addresses != null && addresses.size() > 0) {

                cityName = addresses.get(0).getLocality();
                stateName = addresses.get(0).getAdminArea();
                countryName = addresses.get(0).getCountryName();

                if (cityName == null) {

                    cityName = "Unknown";

                } else {

                    if (cityName.trim().length() == 0) {

                        cityName = "Unknown";
                    }
                }

                if (stateName == null) {

                    stateName = "Unknown";

                } else {

                    if (stateName.trim().length() == 0) {

                        stateName = "Unknown";
                    }
                }

                if (countryName == null) {

                    countryName = "Unknown";

                } else {

                    if (countryName.trim().length() == 0) {

                        countryName = "Unknown";
                    }
                }

            } else {

                cityName = "Unknown";
                stateName = "Unknown";
                countryName = "Unknown";
            }

            Log.d("Geocoder",  countryName + " | " + stateName + " | " + cityName);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_set_location, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_set_location: {

                getAddress(new LatLng(lat, lng));

                if (countryName.equals("Unknown") || cityName.equals("Unknown")) {

                    Toast.makeText(getActivity(), getString(R.string.message_incorrect_location), Toast.LENGTH_SHORT).show();

                } else {

                    Intent i = new Intent();
                    i.putExtra("lat", lat);
                    i.putExtra("lng", lng);

                    i.putExtra("countryName", countryName);
                    i.putExtra("stateName", stateName);
                    i.putExtra("cityName", cityName);

                    if (getActivity() != null) {

                        getActivity().setResult(RESULT_OK, i);
                        getActivity().finish();
                    }
                }

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
