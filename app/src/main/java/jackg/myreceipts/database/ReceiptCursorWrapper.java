package jackg.myreceipts.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import jackg.myreceipts.Receipt;
import jackg.myreceipts.database.ReceiptDbSchema.ReceiptTable;

import java.util.Date;
import java.util.UUID;

public class ReceiptCursorWrapper extends CursorWrapper {
    public ReceiptCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Receipt getReceipt() {
        String uuidString = getString(getColumnIndex(ReceiptTable.Cols.UUID));
        String title = getString(getColumnIndex(ReceiptTable.Cols.TITLE));
        long date = getLong(getColumnIndex(ReceiptTable.Cols.DATE));
        String shopName = getString(getColumnIndex(ReceiptTable.Cols.SHOP_NAME));
        String comment = getString(getColumnIndex(ReceiptTable.Cols.COMMENT));


        Receipt receipt = new Receipt(UUID.fromString(uuidString));
        receipt.setTitle(title);
        receipt.setDate(new Date(date));
        receipt.setShopName(shopName);
        receipt.setComment(comment);


        return receipt;
    }
}
