package jackg.myreceipts;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import jackg.myreceipts.database.ReceiptBaseHelper;
import jackg.myreceipts.database.ReceiptCursorWrapper;
import jackg.myreceipts.database.ReceiptDbSchema;
import jackg.myreceipts.database.ReceiptDbSchema.ReceiptTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReceiptLab {

    @SuppressLint("StaticFieldLeak")
    private static ReceiptLab sReceiptLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public  static ReceiptLab get(Context context) {
        if (sReceiptLab == null) {
            sReceiptLab = new ReceiptLab(context);
        }
        return sReceiptLab;
    }

    private ReceiptLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ReceiptBaseHelper(mContext).getWritableDatabase();
    }

    public void addReceipt(Receipt r) {
        ContentValues values = getContentValues(r);

        mDatabase.insert(ReceiptTable.NAME, null, values);
    }

    public void deleteReceipt (UUID receiptId)
    {
        String uuidString = receiptId.toString();
        mDatabase.delete(ReceiptDbSchema.ReceiptTable.NAME, ReceiptDbSchema.ReceiptTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public List<Receipt> getReceipt() {
        List<Receipt> receipts = new ArrayList<>();

        try (ReceiptCursorWrapper cursor = queryReceipt(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                receipts.add(cursor.getReceipt());
                cursor.moveToNext();
            }
        }
        return receipts;
    }

    public Receipt getReceipt(UUID id) {

        try (ReceiptCursorWrapper cursor = queryReceipt(
                ReceiptTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getReceipt();
        }
    }

    public File getPhotoFile(Receipt receipt) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, receipt.getPhotoFilename());
    }

    public void updateReceipt(Receipt receipt) {
        String uuidString = receipt.getId().toString();
        ContentValues values = getContentValues(receipt);

        mDatabase.update(ReceiptTable.NAME, values, ReceiptTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private ReceiptCursorWrapper queryReceipt(String whereClause, String[] whereArgs) {
        @SuppressLint("Recycle") Cursor cursor = mDatabase.query(
                ReceiptTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new ReceiptCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Receipt receipt) {
        ContentValues values = new ContentValues();
        values.put(ReceiptTable.Cols.UUID, receipt.getId().toString());
        values.put(ReceiptTable.Cols.TITLE, receipt.getTitle());
        values.put(ReceiptTable.Cols.DATE, receipt.getDate().getTime());
        values.put(ReceiptTable.Cols.SHOP_NAME, receipt.getShopName());
        values.put(ReceiptTable.Cols.COMMENT, receipt.getComment());


        return values;
    }
}
