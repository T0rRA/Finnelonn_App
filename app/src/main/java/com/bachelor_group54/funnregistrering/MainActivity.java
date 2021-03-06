package com.bachelor_group54.funnregistrering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FragmentList fragmentList;
    private FragmentRegistrereFunn fragmentRegistrereFunn;
    private FragmentRegistrereBruker fragmentRegistrereBruker;
    private FragmentMineFunn fragmentMineFunn;
    private FragmentEnkeltFunn fragmentEnkeltFunn;
    private FragmentInnstillinger fragmentInnstillinger;

    private FragmentManager fm;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private FragmentLogin fragmentLogin;
    private FragmentHjelp fragmentHjelp;
    private FragmentIntroPage fragmentIntroPage;
    private FragmentBruker fragmentBruker;

    private boolean loginPageOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //Prevents the toolbar from sitting on top of the keyboard when typing in EditTexts

        fm = getSupportFragmentManager();

        //Initializing the fragments needed inn the app
        fragmentRegistrereFunn = new FragmentRegistrereFunn();
        fragmentInnstillinger = new FragmentInnstillinger();
        fragmentMineFunn = new FragmentMineFunn();
        fragmentLogin = new FragmentLogin();

        //Adding fragments and MainActivity to the fragmentList object, to access it later from other classes
        fragmentList = FragmentList.getInstance();
        fragmentList.setMainActivity(this);
        fragmentList.setFragmentMineFunn(fragmentMineFunn);
        fragmentList.setContext(this);
        fragmentList.setFragmentLogin(fragmentLogin);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);

        //Only if you want to start on element 1 in the list, no need if starting at 0
        mPager.setCurrentItem(1);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {} //Needs to be overridden

            @Override
            //When the page is changed run the new page's onResume method to make sure it's up to date.
            public void onPageSelected(int position) {
                pagerAdapter.getItem(position).onResume();
            }

            @Override
            public void onPageScrollStateChanged(int state) {} //Needs to be overridden
        });

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>" + getString(R.string.app_name) + "</font>")); //Changes the color of the actionbar text

        //Opens introFragment if it's the first time the user opens the app else open the log in page
        SharedPreferences sharedpreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        boolean firstLogin = sharedpreferences.getBoolean("firstLogin", true);

        if(firstLogin){
            toIntroPageBtn();
        }else {
            openLoginPage();
        }

        //Variable that prevents the user from going back from the log in page
        loginPageOpen = true;
    }

    //The ScreenSlidePagerAdapter holds the fragment from the navigation bar and makes sliding between them possible.
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> fragmentListe = new ArrayList<>();

        public ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            //Adds the fragments to the slider
            fragmentListe.add(fragmentMineFunn);
            fragmentListe.add(fragmentRegistrereFunn);
            fragmentListe.add(fragmentInnstillinger);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentListe.get(position);
        }

        @Override
        public int getCount() {
            return fragmentListe.size();
        }
    }

    @Override
    //Makes the back button work as expected
    public void onBackPressed() {
        if (mPager.getVisibility() == View.GONE) {
            if(fragmentEnkeltFunn != null){ //Ask if the user wants to save when closing single found fragment
                saveDialog = new TextDialog(R.layout.dialog_save, "Vil du lagre endringene du har gjordt?");
                saveDialog.show(getSupportFragmentManager(), null);
            }

            if (!loginPageOpen || fm.getBackStackEntryCount() > 1) {
                closeFragment();
            }
            return;
        }
        if (mPager.getCurrentItem() == 1) {
            finish(); //If the main page is the current page exit the app
        } else {
            mPager.setCurrentItem(1); //Goes back to the main page
        }
    }

    //Yes and no buttons for the dialog box witch opens onBackPressed from fragmentEnkeltFunn
    private TextDialog saveDialog;

    public void saveDialogYes(View view) {
        fragmentEnkeltFunnLagreEndring(view);
        saveDialog.dismiss();
        fragmentEnkeltFunn = null;
    }

    public void saveDialogNo(View view) {
        saveDialog.dismiss();
        updateMineFunnList();
        fragmentEnkeltFunn = null;
    }

    //Methods and variables for the delete dialog box, witch opens when long pressing a item in the list on FragmentMineFunn.
    private Funn funn;
    private TextDialog deleteDialog;

    public void makeDeleteDialog(Funn funn){
        this.funn = funn;
        deleteDialog = new TextDialog(R.layout.dialog_delete, "Er du siker på at du vil slette " + funn.getTittel() + " ?");
        deleteDialog.show(getSupportFragmentManager(), null);
    }

    public void deleteDialogYes(View view) {
        SetJSON setJSON = new SetJSON(this, fragmentMineFunn);
        setJSON.execute("Funn/DeleteFunn", "funnID=" + funn.getFunnID());
        deleteDialog.dismiss();
    }

    public void deleteDialogNo(View view) {
        deleteDialog.dismiss();
    }

    public void infoBtn(View view) {
        fragmentHjelp = new FragmentHjelp();
        openFragment(fragmentHjelp);
    }

    //Buttons for FragmentRegistrereFunn
    public void bildeBtn(View view) {
        fragmentRegistrereFunn.bildeBtn();
    }

    public void gpsBtn(View view) {
        fragmentRegistrereFunn.gpsBtn();
    }

    public void registrerFunnBtn(View view) {
        Funn funn = fragmentRegistrereFunn.registrerFunnBtn(); //Registers the find, and gets the find object back
        if(funn == null){return;} //If the find is null then the find was not registered
        ((FragmentRegistrereFunn) pagerAdapter.getItem(mPager.getCurrentItem())).clearFields(); //Clears the fields in the register new find fragment
        mPager.setCurrentItem(0); //Goes to the found overview
        openEnkeltFunn(funn, fragmentMineFunn.getListSize() - 1); //Opens the find in the find list
    }

    //Opens find from list
    public void openEnkeltFunn(Funn funn, int position) {
        fragmentEnkeltFunn = new FragmentEnkeltFunn(funn, position);
        openFragment(fragmentEnkeltFunn);
    }

    //Buttons for the single found fragment
    //Saves the changes made to the find
    public void fragmentEnkeltFunnLagreEndring(View view) {
        fragmentEnkeltFunn.editFind(this);
        closeFragment();
    }

    public void fragmentEnkeltFunnUpdatePicture(View view) {
        fragmentEnkeltFunn.bildeBtn();
    }

    public void fragmentEnkeltFunnSendFunnmeldingBtn(View view) {
        fragmentEnkeltFunn.sendFunnmelding();
    }

    public void fragmentEnkeltFunnSendFunnskjemaBtn(View view) {
        fragmentEnkeltFunn.sendFunnskjema();
    }

    //Navigation bar buttons
    public void navbarRegistrereFunn(View view) {
        mPager.setCurrentItem(1);
    }

    public void navbarMineFunn(View view) {
        mPager.setCurrentItem(0);
    }

    public void updateMineFunnList(){
        fragmentMineFunn.getFinds();
    }

    public void navbarSettings(View view) {
        mPager.setCurrentItem(2);
    }

    //User fragment buttons
    public void regUserBtn(View view) {
        fragmentRegistrereBruker = new FragmentRegistrereBruker();
        openFragment(fragmentRegistrereBruker);
    }

    public void saveUserBtn(View view) {
        fragmentRegistrereBruker.saveUserBtn();
        fragmentLogin.stopProgressBar();
    }

    public void loginBtn(View view) {
        fragmentLogin.logInBtn();
    }

    public void openUserBtn(View view) {
        fragmentBruker = new FragmentBruker();
        openFragment(fragmentBruker);
    }

    public void logUtBtn(View view) {
        //Resets fragment login
        fragmentLogin = new FragmentLogin();
        FragmentList.getInstance().setFragmentLogin(fragmentLogin);

        //Setts the saved user login parameters to empty to prevent auto login
        SharedPreferences sharedpreferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("Username", "");
        editor.putString("Password", "");
        editor.apply();

        openLoginPage();
    }

    public void editUserBtn(View view) {
        fragmentBruker.editUser();
    }

    public void onCheckboxClickedBtn(View view){
        fragmentEnkeltFunn.onCheckboxClickedBtn();
    }

    public void onCheckboxClickediRegBtn(View view){
        fragmentRegistrereFunn.onCheckboxClickediRegBtn();
    }

    public void fragmentLoginRegBtn(View view) {
        fragmentRegistrereBruker = new FragmentRegistrereBruker();
        openFragment(fragmentRegistrereBruker);
    }

    public void toLoginPageBtn(View view){ //login page button
        openLoginPage();
        SharedPreferences sharedpreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("firstLogin", false);
        editor.apply();
    }

    public void autoLogIn(){
        SharedPreferences sharedpreferences = getSharedPreferences("User", MODE_PRIVATE);
        String username = sharedpreferences.getString("Username", "");
        String password = sharedpreferences.getString("Password", "");
        if(password.equals("") || username.equals("")){fragmentLogin.stopProgressBar(); return;}
        fragmentLogin.logIn(username, password);
    }

    public void openLoginPage(){
        closeFragment();
        openFragment(fragmentLogin);
        loginPageOpen = true;
        autoLogIn();
    }

    public void toIntroPageBtn(){
        fragmentIntroPage = new FragmentIntroPage();
        openFragment(fragmentIntroPage);
    }
    public void visFredningsTXT(){
        fragmentHjelp.visFredningsTXT();
    }
    public void viMeldepliktTXT(){
        fragmentHjelp.viMeldepliktTXT();
    }
    public void visSamfunnTXT(){
        fragmentHjelp.visSamfunnTXT();
    }
    public void visfunnetNoeTXT(){
        fragmentHjelp.visfunnetTXT();
    }



    public void openFragment(Fragment fragment) {
        mPager.setVisibility(View.GONE); //Sets the main fragments visibility to gone so that the user cannot se or interact with it

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction(); //Makes a fragment transaction that can be used to change the fragment
        fragmentTransaction.replace(R.id.layout, fragment); //Changes the fragment. R.id.layout is the main layout of the Activity that holds the fragment(MainActivity)
        fragmentTransaction.addToBackStack(""); //Adds the fragment to the fragment stack so it can be poped later
        fragmentTransaction.commit();
    }

    public void showPreferencesBtn(View view) { //settings/preference page button. Creates an intent using the SetPreferenceActivity class and starts an activity.
        Intent intent = new Intent(this, SetPreferenceActivity.class);
        startActivity(intent);
    }


    public void closeFragment() {
        fm.popBackStack();//Goes back to the slide fragments
        if(fm.getBackStackEntryCount() == 1) { //When getBackStackEntryCount() == 1, only the main view is the only one left
            loginPageOpen = false;
            mPager.setVisibility(View.VISIBLE); //Makes the main fragments visible again
        }
    }
}

