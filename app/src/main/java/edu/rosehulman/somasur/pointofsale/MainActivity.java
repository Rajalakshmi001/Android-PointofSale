package edu.rosehulman.somasur.pointofsale;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private Item mCurrentItem;
    private Item mClearedItem;
    private TextView mNameTextView;
    private TextView mQuantityTextView;
    private TextView mDateTextView;
    private ArrayList<Item> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNameTextView = findViewById(R.id.name_text);
        mQuantityTextView = findViewById(R.id.quantity_text);
        mDateTextView = findViewById(R.id.date_text);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEditItem(false);
            }
        });

        registerForContextMenu(mNameTextView);
    }

    private void addEditItem(final boolean isEditing) {
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setTitle(R.string.add_item_dialog_title_text);
        View view = getLayoutInflater().inflate(R.layout.dialog_add, null, false);
        //Capture the data entered
        final EditText nameEditTextField = view.findViewById(R.id.edit_name);//from the inflated view we will get the view
        //variables inside function can be used only when it is final
        final EditText quantityEditTextField = view.findViewById(R.id.edit_quantity);
        final CalendarView calendarView = view.findViewById(R.id.calendar_view);
        final GregorianCalendar calendar = new GregorianCalendar();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
            }
        });

        myBuilder.setView(view);

        if (isEditing) {
            nameEditTextField.setText(mCurrentItem.getName());
            quantityEditTextField.setText(String.valueOf(mCurrentItem.getQuantity()));
            calendarView.setDate(mCurrentItem.getDeliveryDateTime());
        }
        myBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditTextField.getText().toString();
                int quantity = Integer.parseInt(quantityEditTextField.getText().toString());
                if (isEditing) {
                    mCurrentItem.setName(name);
                    mCurrentItem.setQuantity(quantity);
                    mCurrentItem.setDeliveryDate(new GregorianCalendar());
                } else {
                    mCurrentItem = new Item(name, quantity, calendar);
                    mItems.add(mCurrentItem);
                }
                showCurrentItem();
            }
        });
        myBuilder.setNegativeButton(android.R.string.cancel, null);//no listener required here because "null" is defaulted to close the dialog
        myBuilder.create().show();
    }

    private void showCurrentItem() {
        mNameTextView.setText(getString(R.string.name_format, mCurrentItem.getName()));
        mQuantityTextView.setText(getString(R.string.quantity_format, mCurrentItem.getQuantity()));
        mDateTextView.setText(getString(R.string.date_format, mCurrentItem.getDeliveryDateString()));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_context_edit:
                addEditItem(true);
                return true;
            case R.id.menu_context_remove:
                mItems.remove(mCurrentItem);
                mCurrentItem = new Item();
                showCurrentItem();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            return true;
        } else if (id == R.id.action_reset) {
            mClearedItem = mCurrentItem;
            mCurrentItem = new Item();
            showCurrentItem();
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Item cleared", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentItem = mClearedItem;
                    mClearedItem = null;
                    showCurrentItem();
                    Snackbar.make(findViewById(R.id.coordinator_layout), "Item is restored", Snackbar.LENGTH_LONG).show();
                }
            });
            snackbar.show();
            return true;
        } else if (id == R.id.action_search) {
            showSearchDialog();
            return true;
        } else if (id == R.id.action_clearAll) {
            clearAllItemsConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.search_dialog_title_text);
        builder.setSingleChoiceItems(getNames(), 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentItem = mItems.get(which);
                showCurrentItem();
                dialog.dismiss();
            }
        });

//        builder.setItems(getNames(), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mCurrentItem = mItems.get(which);
//                showCurrentItem();
//            }
//        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private String[] getNames() {
        String[] names = new String[mItems.size()];
        for (int i = 0; i < mItems.size(); i++) {
            names[i] = mItems.get(i).getName();
        }
        return names;
    }

    private void clearAllItemsConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove);
        builder.setMessage(R.string.confirmation_dialog_message);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mItems.clear();
                mCurrentItem = new Item();
                showCurrentItem();
            }
        });
        builder.create().show();
    }

}
