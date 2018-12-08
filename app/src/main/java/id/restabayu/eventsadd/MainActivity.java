package id.restabayu.eventsadd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.FragmentTransaction;
import android.app.FragmentManager;

public class MainActivity extends AppCompatActivity {


        private FragmentManager fm;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.classifieds_layout);


            Toolbar tb = findViewById(R.id.toolbar);
            setSupportActionBar(tb);
            tb.setSubtitle("Admin Panel");

            fm = getFragmentManager();
            addClassifiedEventFrgmt();
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_menu, menu);
            return true;
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.add_ad_m:
                    addClassifiedEventFrgmt();
                    return true;
                case R.id.view_ads_m:
                    viewClassifiedEventFrgmt();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        public void addClassifiedEventFrgmt(){
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.adds_frame, new AddClassifiedFragment());
            ft.commit();
        }
        public void viewClassifiedEventFrgmt(){
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.adds_frame, new ViewClassifiedFragment());
            ft.commit();
        }
}