package com.example.android.projectinventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.projectinventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

/**
 * Created by arturoahernandez on 3/19/18.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Image view for the product thumbnail
    private ImageView imageView;

    //Take photos button
    private Button takePhoto;

//    Edit text for the product name
    private EditText nameEditText;

//    Edit text field to enter item price
    private EditText priceEditText;

//    Text view to display quantity
    private TextView quantityView;

//    Minus button to subtract (1) from the quantity
    private Button minusButton;

//    Plus sign to add (1) to the quantity
    private Button plusButton;

//    Order button
    private Button orderButton;

//    Enter initial quantity on hand
    private TextView initialQuantity;

//    EditView for add amount
    private EditText addAmount;

    //Button for delete product
    private Button deleteProduct;

    //Bitmap for image
    private Bitmap bitmap;

    //Int for existing loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product (null if it's a new pet) */
    private Uri currentProductUri;

    private boolean productHasChanged = false;

    //
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
// the view, and we change the mPetHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that launched the activity
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        //Find all relevant views that we will read user input from
        nameEditText = (EditText) findViewById(R.id.edit_name);
        priceEditText = (EditText) findViewById(R.id.edit_price);
        imageView = (ImageView) findViewById(R.id.product_image);
        quantityView = (TextView) findViewById(R.id.quantity_number);
        minusButton = (Button) findViewById(R.id.minus_button);
        plusButton = (Button) findViewById(R.id.plus_button);
        orderButton = (Button) findViewById(R.id.order_button);
        initialQuantity = (TextView) findViewById(R.id.initital_amount_text);
        addAmount = (EditText) findViewById(R.id.add_amount);
        deleteProduct = (Button) findViewById(R.id.delete_product);
        takePhoto = (Button) findViewById(R.id.take_photo);

        //Set an on click listener on takePhoto
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Use a helper class to send an intent to camera
                dispatchTakePictureIntent();
                Toast.makeText(getApplicationContext(), "Click!", Toast.LENGTH_SHORT).show();
            }
        });

        //set an on click listener to minus button
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the string from the quantity view
                String productQuantityString = quantityView.getText().toString().trim();
                int productQuantityInt = Integer.parseInt(productQuantityString);

                //Check that the quantity is greater than 0 so it doesn't go negative
                if (productQuantityInt > 0){
                    //Subtract 1 from quantity
                    productQuantityInt --;
                    //Set the content values and update the quantity
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, String.valueOf(productQuantityInt));
                    int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
                } else {
                    Toast.makeText(getApplicationContext(), "Quantity is already at 0!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //set an on click listener to plus button
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the string of the quantity and convert to int
                String productQuantityString = quantityView.getText().toString().trim();
                int productQuantityInt = Integer.parseInt(productQuantityString);
                //Add one to quantity
                productQuantityInt ++;
                //set content values and update the quantity
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, String.valueOf(productQuantityInt));
                int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            }
        });

        //Set an onclick listener to the orderButto
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the string of the product name first
                String nameOrderText = nameEditText.getText().toString().trim();
                //Send an intent to email and put data into email
                Intent orderButtonIntent = new Intent(Intent.ACTION_SEND);
                orderButtonIntent.setData(Uri.parse("mailto:"));
                orderButtonIntent.setType("*/*");
                orderButtonIntent.putExtra(Intent.EXTRA_SUBJECT, "Order Product");
                orderButtonIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_product) + " " + nameOrderText);
                if (orderButtonIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(orderButtonIntent);
                }
            }
        });

        //Set an on click listener to the deleteProduct
        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the postivie and negative buttons on the dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setMessage(R.string.delete_dialog_msg);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Delete" button, so delete the pet.
                        deleteProduct();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the pet.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Set onTouchListeners to alert when something is being changed
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);

        //If the intent does not contain a Uri we know we are creating a new pet
        if (currentProductUri == null){
            setTitle(getString(R.string.editor_activity_add_product));
            quantityView.setVisibility(View.GONE);
            minusButton.setVisibility(View.GONE);
            plusButton.setVisibility(View.GONE);
            deleteProduct.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            //else we are editing an existing pet
            setTitle(getString(R.string.editor_activity_edit_product));
            initialQuantity.setVisibility(View.GONE);
            addAmount.setVisibility(View.GONE);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    // Get new product and save it to the database
    private Boolean saveProduct() {
        //Get the strings for all views and edit texts
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityView.getText().toString().trim();
        String addAmountString = addAmount.getText().toString().trim();


        //if there is no bitmap then there will be an exception, catch the NullPointerException
        //and alert user that they cannot continue the save process unless they take a product photo
        try {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (NullPointerException e){
            Toast.makeText(this, "Please take a product photo", Toast.LENGTH_SHORT).show();
            return false;
        }

        //convert the bitmap into a bytearray[] to save as BLOB in the table
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] img = bos.toByteArray();

        //If the current product Uri is null it will check that all fields are filled in
        if (currentProductUri == null){
            if (nameString.equals("") || priceString.equals("") || addAmountString.equals("")) {
                Toast.makeText(this, "Please fill all fields to continue", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            //If this is an existing product, we know we have a bitmap so we only check that the name
            //and price edit texts have not changed, quantity changes are checked elsewhere
            if (nameString.equals("") || priceString.equals("")) {
                Toast.makeText(this, "Please fill all fields to continue", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!addAmountString.equals("")){
            int amountInt = Integer.parseInt(addAmountString);
            int quantityInt = Integer.parseInt(quantityString);
            quantityInt = amountInt + quantityInt;
            quantityString = String.valueOf(quantityInt);
        }

        //Set the content values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, img);

        //determine if this is a new or existing pet by checking if uri is null
        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, R.string.editor_insert_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_product_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            //otherwise this is an existing pet
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.editor_insert_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_product_successful, Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                // Save pro to the database
                boolean saveProduct = saveProduct();
                if (saveProduct){
                    finish();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            byte[] byteArray = cursor.getBlob(imageColumnIndex);
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(price);
            quantityView.setText(Integer.toString(quantity));
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Update the views on the screen with the values from the database
        nameEditText.setText("");
        priceEditText.setText("");
        quantityView.setText(Integer.toString(0));
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void dispatchTakePictureIntent() {
        //Send an intent to the camera and request image capture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //After the picture s taken, the onActivityResult is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Get the data (bitmap) and set the imageView to the bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

        }
    }
}
