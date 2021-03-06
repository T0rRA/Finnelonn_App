package com.bachelor_group54.funnregistrering;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class FragmentRegistrereFunn extends Fragment {
    private View view; //This view will be used to access elements contained inside the fragment page (like getting text from an editText)
    private Bitmap picture;
    private double latitude = 200; //Initializing latitude variable
    private double longitude = 200; //Initializing longitude variable
    private Funn funn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registrere_lose_funn, container, false); //Loads the page from the XML file
        LinearLayout navbarRegistrereFunn = view.findViewById(R.id.navbar_registrere_funn); //Gets the navbar layout for this view
        navbarRegistrereFunn.setBackground(getContext().getDrawable(R.drawable.navbar_btn_selected_background)); //Setts color on the navbar indicating what page you are on
        setTextWatchers();
        funn = new Funn();
        return view;
    }

// Sets input validation
    public void setTextWatchers() {
        EditText title = view.findViewById(R.id.nytt_funn_tittel_et);
        EditText description = view.findViewById(R.id.nytt_funn_beskrivelse_et);

        title.addTextChangedListener(new InputValidater(getContext(), true, false, false, 1, 20, title));
        description.addTextChangedListener(new InputValidater(getContext(), true, false, false, 0, 100, description));
    }

    public void gpsBtn() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE); //Gets the location manager from the system

        Location gps_loc = null;
        Location network_loc = null;

        //Checks for the necessary permissions for getting GPS Location form the system
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //Gets location from the GPS
        } else {
            //No permission ask for it
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        //Checks for the necessary permissions for getting Network Location form the system
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //Gets location from the network (can be used as a backup)
        } else {
            //No permission ask for it
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        if (gps_loc != null) { //Gets location from the GPS if the gps_loc is not null
            latitude = gps_loc.getLatitude();
            longitude = gps_loc.getLongitude();
        } else if (network_loc != null) { //Gets location from the network if network_loc is not null, only if the GPS was not found
            latitude = network_loc.getLatitude();
            longitude = network_loc.getLongitude();
        } //If nether network or gps can provide the location the default values of 0 and 0 is used instead, should be handled in the real program

        TextView textView = view.findViewById(R.id.gps_tv_nytt_funn); //Finds the textView on the main app screen
        textView.setText("Lat: " + latitude + " Long: " + longitude); //Sets the text to show the latitude and longitude of the current location of the device
    }

    @Override
    //This method is called when the users accepts or denies the permission request from the gps button.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //If the requestCode is 1 then the request comes from the gpsBtn.
            //If the request comes from the requestBtn and permission is granted by the user, then we need to call the gpsBtn metode to get the GPS coordinates.
            gpsBtn();
        }
    }

    int CAMERA_PIC_REQUEST = 1337; //Setting the request code for the camera intent, this is used to identify the result when it is returned after taking the picture in onActivityResult.

    //This method opens the camera app when clicking the "Take image" button
    public void bildeBtn() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //Makes an intent of the image capture type
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST); //Starts the camera app and waits for the result
    }

    @Override
    //This method receives the image from the camera app and setts the ImageView to that image.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST) { //If the requestCode matches that of the startActivityForResult of the cameraIntent we know it is the camera app that is returning it's data.
            try { //May produce null pointers if the picture is not taken
                picture = (Bitmap) data.getExtras().get("data"); //Gets the picture from the camera app and saves it as a Bitmap
            } catch (NullPointerException e) {
                Toast.makeText(getContext(), "Picture not taken", Toast.LENGTH_LONG).show(); //Prints a message to the user, explaining that no picture was taken
                return; //Return if there is no picture
            }

            ImageView imageView = view.findViewById(R.id.preview_bilde_nytt_funn); //Finds the ImageView
            imageView.setImageBitmap(picture); //Sets the ImageView to the picture taken from the camera app
        }

        super.onActivityResult(requestCode, resultCode, data); //Calls the super's onActivityResult (Required by Android)
    }

    public Funn registrerFunnBtn() {
        //Checks the checkboxes and returns error message if permission was not granted by the owner
        CheckBox checkBox = view.findViewById(R.id.checkbox_grunneier_reg);
        funn.setTillatelseGitt(checkBox.isChecked());
        if(!funn.isTillatelseGitt()){Toast.makeText(getContext(), "Grunneier må gi tilatelse til søking", Toast.LENGTH_LONG).show(); return null;}

        funn.setBilde(picture);

        EditText title = view.findViewById(R.id.nytt_funn_tittel_et); //Finds the editText containing the title
        funn.setTittel(title.getText().toString()); //Puts the title in the finds object

        EditText description = view.findViewById(R.id.nytt_funn_beskrivelse_et); //Finds the editText containing the description
        funn.setBeskrivelse(description.getText().toString());//Adds the description to the find

        //Sets latitude and longitude, NOTE default values for both are 200
        funn.setLatitude(latitude);
        funn.setLongitude(longitude);
        //Sets kommune and fylke for the find
        getAddressFromLocation(latitude, longitude);

        funn.setDato(makeDate());

        //If there are errors in anny of the fields do not save the find
        if (title.getError() != null && description.getError() != null) {
            Toast.makeText(getContext(), getString(R.string.feil_i_innputfelter) + "tittel og beskrivelse", Toast.LENGTH_LONG).show();
            return null;
        }

        if (title.getError() != null) {
            Toast.makeText(getContext(), getString(R.string.feil_i_innputfelter) + "tittel", Toast.LENGTH_LONG).show();
            return null;
        }

        //Checks if title is empty
        if (title.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.tomt_felt) + "tittel", Toast.LENGTH_LONG).show();
            return null;
        }

        if (description.getError() != null) {
            Toast.makeText(getContext(), getString(R.string.feil_i_innputfelter) + "beskrivelse", Toast.LENGTH_LONG).show();
            return null;
        }

        sentFindToBackend();
        return funn;
    }
    public void onCheckboxClickediRegBtn() {
        final CheckBox checkBox = view.findViewById(R.id.checkbox_grunneier_reg);

        funn.setTillatelseGitt(checkBox.isChecked());
    }

    //Makes date String
    public String makeDate(){
        Date currentTime = Calendar.getInstance().getTime();
        String day = currentTime.getDate() + "";
        if(currentTime.getDate() < 10) {
            day = "0" + currentTime.getDate();
        }

        String month = (currentTime.getMonth() + 1) + "";
        if(currentTime.getMonth() < 9) {
            month = "0" + (currentTime.getMonth() + 1);
        }

        return day + "/" + month + "/" + (currentTime.getYear() + 1900);
    }

    public void sentFindToBackend(){
        User user = User.getInstance();

        //The Map params contains all the key value pairs of the jsonObject that we send to the database
        Map<String,String> params = new HashMap<String, String>();
        params.put("tittel", makeStringNonNull(funn.getTittel()));
        params.put("beskrivelse", makeStringNonNull(funn.getBeskrivelse()));
        params.put("image" , makeStringNonNull(ImageSaver.makeBase64FromBitmap(picture)));
        params.put("funndato" , makeStringNonNull(funn.getDato()));
        params.put("kommune", makeStringNonNull(funn.getKommune()));
        params.put("fylke" , makeStringNonNull(funn.getFylke()));
        params.put("funndybde" , funn.getFunndybde()+"");
        params.put("gjenstand_markert_med" , makeStringNonNull(funn.getGjenstandMerking()));
        params.put("koordinat" , funn.getLatitude() + "N " + funn.getLongitude() + "W");
        params.put("datum" , makeStringNonNull(funn.getDatum()));
        params.put("areal_type" , makeStringNonNull(funn.getArealType()));

        params.put("brukernavn" , user.getUsername());

        params.put("innGBNr.gb_nr" , makeStringNonNull(funn.getGbnr()));
        params.put("innGBNr.grunneier.Fornavn" , makeStringNonNull(funn.getGrunneierFornavn()));
        params.put( "innGBNr.grunneier.Etternavn" , makeStringNonNull(funn.getGrunneierEtternavn()));
        params.put("innGBNr.grunneier.Adresse" , makeStringNonNull(funn.getGrunneierAdresse()));
        params.put("innGBNr.grunneier.Postnr" , makeStringNonNull(funn.getGrunneierPostNr()));
        params.put("innGBNr.grunneier.Poststed" , makeStringNonNull(funn.getGrunneierPostSted()));
        params.put("innGBNr.grunneier.Tlf" , makeStringNonNull(funn.getGrunneierTlf()));
        params.put("innGBNr.grunneier.Epost" , makeStringNonNull(funn.getGrunneierEpost()));

        SendToServer.postRequest(getContext(), params, "Funn/RegistrerFunn", FragmentList.getFragmentMineFunn());
    }

    //Makes sure that the database never recives null values, but puts "null" strings instead
    public String makeStringNonNull(String s){
        return s == null || s.equals("") ? "null" : s;
    }

    //Takes the latitude and longitude of the gps coordinates and sets the kommune and fylke of the find
    public void getAddressFromLocation(double lat, double lng) {
        Geocoder coder = new Geocoder(getContext());
        List<Address> locations;
        try{
            locations = coder.getFromLocation(lat, lng, 1); //Uses the Geocoder class to get the Address from the long and lat values.
            if(locations == null) {
                return;
            }
            Address address = locations.get(0);
            funn.setKommune(address.getSubAdminArea()); //Sets the kommune
            funn.setFylke(address.getAdminArea()); //Sets the fylke
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Resets the fields, called when registering new find so it is empty next time the user wants to register a find
    public void clearFields() {
        funn = new Funn();
        EditText titleEt = view.findViewById(R.id.nytt_funn_tittel_et);
        titleEt.setText("");
        EditText descriptionEt = view.findViewById(R.id.nytt_funn_beskrivelse_et);
        descriptionEt.setText("");
        TextView gpsTv = view.findViewById(R.id.gps_tv_nytt_funn);
        gpsTv.setText("");
        ImageView imageView = view.findViewById(R.id.preview_bilde_nytt_funn);
        imageView.setImageBitmap(null);
        picture = null;
    }
}
