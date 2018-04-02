package com.example.android.projectinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.projectinventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by arturoahernandez on 3/20/18.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* Flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        long id = cursor.getLong(cursor.getColumnIndex("_id"));

        //Form the content URI for specific pet clicked by appending the ID
        final Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        //Find individual views that we want to change
        TextView nameView = (TextView) view.findViewById(R.id.item_name);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        final TextView quantityView = (TextView) view.findViewById(R.id.item_quantity);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        //Find the columns of the data we are interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        //Read the pet attiributes from the cursor to the pet
        final String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);


        //Update the text views with the attributes for the current pet
        nameView.setText(context.getString(R.string.name) + ": " + productName);
        priceView.setText(context.getString(R.string.price) + ": " +productPrice);
        quantityView.setText(context.getString(R.string.quantity) + ": " + productQuantity);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int productQuantityInt = Integer.parseInt(productQuantity);
                if (productQuantityInt > 0){
                    productQuantityInt --;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, String.valueOf(productQuantityInt));
                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                } else {
                    Toast.makeText(context, "Quantity is already at 0!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
