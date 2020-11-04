package win.lioil.bluetooth.activity;

import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class BaseActivity extends FragmentActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
