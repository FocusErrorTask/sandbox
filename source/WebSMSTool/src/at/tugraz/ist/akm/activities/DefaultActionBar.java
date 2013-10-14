package at.tugraz.ist.akm.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.PreferencesActivity;

public class DefaultActionBar extends Activity
{

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i = null;
        switch (item.getItemId())
        {
        case R.id.actionbar_about:
            i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        case R.id.actionbar_settings:
            i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
            return true;
        default:
            i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
