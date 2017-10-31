package com.example.magiapp.easyorder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magiapp.easyorder.data.FoodItem;
import com.example.magiapp.easyorder.data.FoodItemTableDataAdapter;
import com.example.magiapp.easyorder.data.SendData;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ConfirmOrderActivity extends AppCompatActivity {
    TableView table;
    private static final String[] TABLE_HEADERS = {"Type", "ID", "Name", "Price", "Qty."};
    FoodItemTableDataAdapter foodItemTableDataAdapter;
    List<FoodItem> orderList;
    TextView totalItem;
    TextView totalPrice;
    Button confirmOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        Intent i = getIntent();
        orderList = (ArrayList<FoodItem>) i.getSerializableExtra("menuList");
        table = (TableView<String>) findViewById(R.id.foodConfirmTable);
        totalItem = (TextView) findViewById(R.id.confirm_order_totalItem);
        totalPrice = (TextView) findViewById(R.id.confirm_order_totalPrice);
        confirmOrder = (Button) findViewById(R.id.b_confirm_order);
        confirmOrder.setOnClickListener(new OnConfirmOrderClicked());
        initTable(orderList);
        initNumberData();
    }


    /**
     * Initialize the Sortable table view with data from list
     */
    private void initTable(List orderList) {
        TableColumnWeightModel columnModel = new TableColumnWeightModel(5);
        columnModel.setColumnWeight(0, 2);
        columnModel.setColumnWeight(1, 1);
        columnModel.setColumnWeight(2, 4);
        columnModel.setColumnWeight(3, 2);
        columnModel.setColumnWeight(4, 2);
        table.setColumnModel(columnModel);
        table.setHeaderAdapter(new SimpleTableHeaderAdapter(this, TABLE_HEADERS));
        foodItemTableDataAdapter = new FoodItemTableDataAdapter(this, orderList, table);
        table.setDataAdapter(foodItemTableDataAdapter);
    }


    /**
     * Initialize number data below the table and calculate value such as totalPrice
     */
    private void initNumberData() {
        double price = 0;
        int items = 0;
        for (FoodItem item : orderList) {
            price += item.getQuantity() * item.getPrice();
            items += item.getQuantity();
        }
        totalItem.setText(items + "");
        totalPrice.setText(String.format("%.2f฿", price));
    }

    /**
     * Click Listener when click confirm order button
     */
    private class OnConfirmOrderClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            final MyAsyncTask task = new MyAsyncTask();
            task.execute("127.0.0.1");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    if ( task.getStatus() == AsyncTask.Status.RUNNING )
                        task.cancel(true);
                }
            }, 5000 );


        }
    }

    /**
     * Thread task for SendData class
     * - onPreExecute > Show loading spinning dialog.
     * - doInBackground > Start to sending data.
     * - onPostExecute > Dismiss(hide) the loading spinning dialog.
     * - onCancelled > cancel the doInBackground function and popup error dialog.
     */
    private class MyAsyncTask extends AsyncTask<String, List<FoodItem>, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected Boolean doInBackground(String... params) {
            SendData sendData = new SendData("192.168.1.15", orderList, 0);
            sendData.send();
            return sendData.isSuccess();

        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ConfirmOrderActivity.this);
            dialog = ProgressDialog.show(ConfirmOrderActivity.this, "Sending", "Loading");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            dialog.dismiss();
            Log.d("dialog", "Dismiss");
            Toast.makeText(ConfirmOrderActivity.this, "Data was sent successfully", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onCancelled() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmOrderActivity.this);
            builder.setMessage("Error connecting to server\n (Timed out)");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();
                }
            });
            dialog.dismiss();
            builder.show();
        }
    }


}