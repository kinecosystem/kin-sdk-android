package kin.backupandrestore.qr;

import android.graphics.Bitmap;
import android.net.Uri;
import androidx.test.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class QRFileUriHandlerImplTest {

    private QRFileUriHandlerImpl fileUriHandler;

    @Before
    public void setup() {
        fileUriHandler = new QRFileUriHandlerImpl(InstrumentationRegistry.getContext());
    }

    @Test
    public void saveAndLoad() throws Exception {
        Bitmap bitmap = TestUtils.loadBitmapFromResource(this.getClass(), "qr_test.png");
        Uri uri = fileUriHandler.saveFile(bitmap);
        Bitmap loadedBitmap = fileUriHandler.loadFile(uri);
        assertThat(bitmap.sameAs(loadedBitmap), equalTo(true));
    }

    @Test
    public void saveFile_Success() throws Exception {
        Bitmap bitmap = TestUtils.loadBitmapFromResource(this.getClass(), "qr_test.png");
        Uri uri = fileUriHandler.saveFile(bitmap);
        assertThat(uri, notNullValue());
        assertThat(uri.toString(), equalTo("content://kin.backupandrestore.test" +
                ".KinRecoveryFileProvider/qr_codes/backup_qr.png"));
    }

}
