package edu.stevens.cs522.bookstore.activities;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.providers.BookProvider;

public class BookStoreActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER_ID = 1;
    private static final int AUTHOR_LOADER_ID = 2;

    // Use this when logging errors and warnings.
    @SuppressWarnings("unused")
    private static final String TAG = BookStoreActivity.class.getCanonicalName();

    // These are request codes for subactivity request calls
    static final private int ADD_REQUEST = 1;
    static final private int SEARCH_REQUEST = 0;
    static final private int CHECKOUT_REQUEST = 2;

    @SuppressWarnings("unused")

    // There is a reason this must be an ArrayList instead of a List.
    private ArrayList<Book> shoppingCart = new ArrayList<Book>();

    private ArrayAdapter<Book> adapter;
    private ActionMode mActionMode;

    private BookProvider provider;

    private Cursor cursor;

    private custCurAdapter curAdapter;

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // TODO Set the layout (use cart.xml layout)
        setContentView(R.layout.cart);
        list = (ListView) findViewById(android.R.id.list);

        filldata();
        LoaderManager lm = getLoaderManager();
        lm.initLoader(BOOK_LOADER_ID, null, this);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        list.setEmptyView(emptyText);

        // TODO check if there is saved UI state, and if so, restore it (i.e. the cart contents)
        curAdapter = new custCurAdapter(this,cursor);
        list.setAdapter(curAdapter);

        //Code below was trial to give menu options on longpress
        //uncommenting it would override the context menu option that we are going with right now
        //This is left out intentionally as later on we  might want to use multiple deletion.


        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(mActionMode!=null)
                    onListItemSelect(position);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                onListItemSelect(i);
                Toast.makeText(getApplicationContext(),"this"+1,Toast.LENGTH_LONG).show();
                return true;

            }
        });

    }

    private void filldata(){
        //loader on create fills it with query method
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        String[] projection = {BookProvider._BOOK_DB + "." + BookContract._id,
                               BookContract._title, AuthorContract._first_name };
        switch (loaderId){
            case BOOK_LOADER_ID:
                return new CursorLoader(this, BookContract.CONTENT_URI,projection,null,null,null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case BOOK_LOADER_ID:
                curAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case BOOK_LOADER_ID:
                curAdapter.swapCursor(null);
                break;
        }
    }

   private class custCurAdapter extends ResourceCursorAdapter {

       Context context;
       Cursor cursor;

       private SparseBooleanArray mSelectedItemIds;

        public custCurAdapter(Context context, Cursor cursor){
            super(context,R.layout.cart_row,cursor, 0);
            mSelectedItemIds = new SparseBooleanArray();
            this.context = context;
            this.cursor  = cursor;
        }

      //  @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

       private class ViewHolder {
           TextView txtTitle, txtIsbn, txtPrice, txtAuthor;
       }
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
           if(convertView==null){
               LayoutInflater inflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
               convertView = inflater.inflate(R.layout.cart_row, null);
               holder = new ViewHolder();

               holder.txtTitle  = (TextView)convertView.findViewById(R.id.cart_row_title);
               holder.txtIsbn = (TextView)convertView.findViewById(R.id.cart_row_isbn);
               holder.txtPrice = (TextView)convertView.findViewById(R.id.cart_row_price);
               holder.txtAuthor = (TextView)convertView.findViewById(R.id.cart_row_author);
               convertView.setTag(holder);
           }else {
               holder = (ViewHolder) convertView.getTag();
           }

           cursor = (Cursor)getItem(position);
           holder.txtTitle.setText(BookContract.getTitle(cursor));
           holder.txtIsbn.setText(BookContract.getIsbn(cursor));
           holder.txtPrice.setText(BookContract.getPrice(cursor));
           holder.txtTitle.setText(AuthorContract.getAllauthors(cursor));
           convertView.setBackgroundColor(mSelectedItemIds.get(position)?0x9934B5E4:Color.TRANSPARENT);
           return convertView;
       }

       public void removeSelection(){
           mSelectedItemIds = new SparseBooleanArray();
           notifyDataSetChanged(cursor);
       }

       public void selectView(int position, boolean value){
          if(value)
              mSelectedItemIds.put(position,value);
           else
              mSelectedItemIds.delete(position);
           notifyDataSetChanged(cursor);
       }

       public void toggleSelection(int position) {
            selectView(position, !mSelectedItemIds.get(position));
       }

       public Cursor notifyDataSetChanged(Cursor cur){
           super.notifyDataSetChanged();
           return cur;
       }

       public SparseBooleanArray getSelectedIds() {
           return mSelectedItemIds;
       }

       public int getSelectedCount(){
           return mSelectedItemIds.size();
       }

   }


    private void onListItemSelect(int position){
        curAdapter.toggleSelection(position);
        boolean hasCheckedItems = curAdapter.getSelectedCount() >0;
        if(hasCheckedItems && mActionMode==null)
            mActionMode = startActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode!=null)
            mActionMode.finish();
        if (mActionMode!=null)
            mActionMode.setTitle(String.valueOf(curAdapter.getSelectedCount()) + " selected");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO provide ADD, DELETE and CHECKOUT options
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // TODO
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.add:
                Intent addIntent = new Intent(getApplicationContext(), AddBookActivity.class);
                startActivityForResult(addIntent, ADD_REQUEST);
                //On receiving results we have to add it to our list
                break;

            case R.id.checkout:
                //Pass the total number of books
                Intent IntentCheck = new Intent(getApplicationContext(), CheckoutActivity.class);

                startActivityForResult(IntentCheck, CHECKOUT_REQUEST);
                break;

            case R.id.delete:  //handled through context menu

                finish();
                break;
        }
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // TODO Handle results from the Search and Checkout activities.
        Book book;
        StringBuffer str = new StringBuffer();
        if (resultCode==RESULT_OK) {

            switch (requestCode) {

                // Use SEARCH_REQUEST and CHECKOUT_REQUEST codes to distinguish the cases.
                case SEARCH_REQUEST:
                    // SEARCH: add the book that is returned to the shopping cart.
                    book = intent.getExtras().getParcelable(constants.book);
                    addBook(book);
                    Toast.makeText(getApplicationContext(), R.string.Msg_added, Toast.LENGTH_SHORT).show();
                    break;

                case CHECKOUT_REQUEST:
                    // CHECKOUT: empty the shopping cart.
                    int rows = getContentResolver().delete(BookContract.CONTENT_URI, null, null);
                    String s = getResources().getString(R.string.Msg_thanks);

                    s = String.format(s,rows);
                    new AlertDialog.Builder(this).setTitle(R.string.thanks).setMessage(s).setIcon(R.drawable.ic_menu_icon).setNeutralButton("Close", null).show();
                    break;

                case ADD_REQUEST:
                    book = intent.getExtras().getParcelable(constants.book);
                    addBook(book);

                    Toast.makeText(getApplicationContext(), R.string.Msg_added, Toast.LENGTH_SHORT).show();
                    break;
            }
        }else{
            Toast.makeText(getApplicationContext(), R.string.Msg_cancel, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO save the shopping cart contents (which should be a list of parcelables).
        //no need since we are using database now
        super.onSaveInstanceState(savedInstanceState);
    }

    public Uri addBook(Book book){
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
        Uri uri = getContentResolver().insert(BookContract.CONTENT_URI,values);

        for(int i= 0; i < book.authors.size(); i++){
            ContentValues authValues = new ContentValues();
            book.authors.get(i).writeToProvider(authValues);
            authValues.put(AuthorContract._book_fk,uri.getLastPathSegment());
            getContentResolver().insert(AuthorContract.CONTENT_URI,authValues);
        }

        return uri;
    }

    private class ActionModeCallback implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.bookstore_context,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            SparseBooleanArray selected;
            Cursor cur;

            switch (menuItem.getItemId()){
                case R.id.del:
                    selected = curAdapter.getSelectedIds();

                    //for (Integer i:choice ) {
                    for (int i = (selected.size() -1); i>=0;i--) {
                        if (selected.valueAt(i)) {
                            curAdapter.getCursor().moveToPosition(selected.keyAt(i));
                            cur = curAdapter.getCursor();
                            String id = cur.getString(cur.getColumnIndexOrThrow(BookContract._id));
                            getContentResolver().delete(BookContract.ContentUri(id), null, null);
                        }
                    }
                    actionMode.finish();
                    return true;

                case R.id.info:
                    selected = curAdapter.getSelectedIds();

                    //Call the display activity
                    Intent bookinfo = new Intent(BookStoreActivity.this, DisplayActivity.class);
                    int i;
                    for(i = 0; i <= (selected.size()-1); i++) {
                        if(selected.get(i))
                            break;
                    }

                    Book book = new Book((Cursor)list.getItemAtPosition(i));
                    bookinfo.putExtra(constants.book,book);
                    startActivity(bookinfo);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            curAdapter.removeSelection();
            mActionMode = null;
        }
    }

}