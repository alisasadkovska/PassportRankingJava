package com.alisasadkovska.passport.ui.scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.ImageUtil;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.common.Utils;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.COMFile;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.FaceImageInfo;
import org.jmrtd.lds.FaceInfo;
import org.jmrtd.lds.LDS;
import org.jmrtd.lds.MRZInfo;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.alisasadkovska.passport.common.Common.BIRTH_DATE;
import static com.alisasadkovska.passport.common.Common.EXPIRATION_DATE;
import static com.alisasadkovska.passport.common.Common.PASSPORT_NUMBER;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    TinyDB tinyDB;
    TextView textOutput;
    String passportNumber;
    String expirationDate;
    String birthDate;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private boolean encodePhotoToBase64 = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(R.layout.activity_scanner);

        textOutput = findViewById(R.id.textOutput);
        passportNumber = tinyDB.getString(PASSPORT_NUMBER);
        expirationDate = convertDate(tinyDB.getString(EXPIRATION_DATE));
        birthDate = convertDate(tinyDB.getString(BIRTH_DATE));

        textOutput.setText(passportNumber + " " + expirationDate + " " + birthDate);

        encodePhotoToBase64 = getIntent().getBooleanExtra("photoAsBase64", false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            Intent intent = new Intent(getApplicationContext(), this.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String[][] filter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
            adapter.enableForegroundDispatch(this, pendingIntent, null, filter);
            textOutput.setText(getString(R.string.put_phone_on_pass));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    private static String convertDate(String input) {
        if (input == null) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyMMdd", Locale.US)
                    .format(Objects.requireNonNull(new SimpleDateFormat("yyyy/MM/dd", Locale.US).parse(input)));
        } catch (ParseException e) {
            Log.w(ScannerActivity.class.getSimpleName(), e);
            return null;
        }
    }

    private static String exceptionStack(Throwable exception) {
        StringBuilder s = new StringBuilder();
        String exceptionMsg = exception.getMessage();
        if (exceptionMsg != null) {
            s.append(exceptionMsg);
            s.append(" - ");
        }
        s.append(exception.getClass().getSimpleName());
        StackTraceElement[] stack = exception.getStackTrace();

        if (stack.length > 0) {
            int count = 3;
            boolean first = true;
            boolean skip = false;
            String file = "";
            s.append(" (");
            for (StackTraceElement element : stack) {
                if (count > 0 && element.getClassName().startsWith("com.tananaev")) {
                    if (!first) {
                        s.append(" < ");
                    } else {
                        first = false;
                    }

                    if (skip) {
                        s.append("... < ");
                        skip = false;
                    }

                    if (file.equals(element.getFileName())) {
                        s.append("*");
                    } else {
                        file = element.getFileName();
                        s.append(file.substring(0, file.length() - 5)); // remove ".java"
                        count -= 1;
                    }
                    s.append(":").append(element.getLineNumber());
                } else {
                    skip = true;
                }
            }
            if (skip) {
                if (!first) {
                    s.append(" < ");
                }
                s.append("...");
            }
            s.append(")");
        }
        return s.toString();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = Objects.requireNonNull(intent.getExtras()).getParcelable(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
                if (passportNumber != null && !passportNumber.isEmpty() && expirationDate != null && !expirationDate.isEmpty() && birthDate != null && !birthDate.isEmpty()){
                    BACKeySpec bacKey = new BACKey(passportNumber, birthDate, expirationDate);
                    new ReadTask(IsoDep.get(tag), bacKey).execute();
                } else {
                    Toasty.error(this, getString(R.string.error_input), Toasty.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(ScannerActivity.this, NfcScannerActivity.class);
                    startActivity(intent1);
                }
            }
        }
    }



    @SuppressLint("StaticFieldLeak")
    private class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private BACKeySpec bacKey;

        ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }

        private DG1File dg1File;
        private String imageBase64;
        private Bitmap bitmap;

        @Override
        protected Exception doInBackground(Void... params) {
            try {

                CardService cardService = CardService.getInstance(isoDep);
                cardService.open();

                PassportService service = new PassportService(cardService);
                service.open();

                boolean paceSucceeded = false;
                try {
                    CardAccessFile cardAccessFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                    Collection<PACEInfo> paceInfos = cardAccessFile.getPACEInfos();
                    if (paceInfos != null && paceInfos.size() > 0) {
                        PACEInfo paceInfo = paceInfos.iterator().next();
                        service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()));
                        paceSucceeded = true;
                    } else {
                        paceSucceeded = true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }

                service.sendSelectApplet(paceSucceeded);

                if (!paceSucceeded) {
                    try {
                        service.getInputStream(PassportService.EF_COM).read();
                    } catch (Exception e) {
                        service.doBAC(bacKey);
                    }
                }

                LDS lds = new LDS();

                CardFileInputStream comIn = service.getInputStream(PassportService.EF_COM);
                lds.add(PassportService.EF_COM, comIn, comIn.getLength());
                COMFile comFile = lds.getCOMFile();

                CardFileInputStream sodIn = service.getInputStream(PassportService.EF_SOD);
                lds.add(PassportService.EF_SOD, sodIn, sodIn.getLength());
                SODFile sodFile = lds.getSODFile();

                CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                dg1File = lds.getDG1File();

                CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
                lds.add(PassportService.EF_DG2, dg2In, dg2In.getLength());
                DG2File dg2File = lds.getDG2File();

                List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
                List<FaceInfo> faceInfos = dg2File.getFaceInfos();
                for (FaceInfo faceInfo : faceInfos) {
                    allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
                }

                if (!allFaceImageInfos.isEmpty()) {
                    FaceImageInfo faceImageInfo = allFaceImageInfos.iterator().next();

                    int imageLength = faceImageInfo.getImageLength();
                    DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                    byte[] buffer = new byte[imageLength];
                    dataInputStream.readFully(buffer, 0, imageLength);
                    InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);

                    bitmap = ImageUtil.decodeImage(
                            ScannerActivity.this, faceImageInfo.getMimeType(), inputStream);
                    imageBase64 = Base64.encodeToString(buffer, Base64.DEFAULT);
                }

            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result == null) {

                Intent intent;
                if (getCallingActivity() != null) {
                    intent = new Intent();
                } else {
                    intent = new Intent(ScannerActivity.this, ResultActivity.class);
                }

                MRZInfo mrzInfo = dg1File.getMRZInfo();

                intent.putExtra(ResultActivity.KEY_FIRST_NAME, mrzInfo.getSecondaryIdentifier().replace("<", ""));
                intent.putExtra(ResultActivity.KEY_LAST_NAME, mrzInfo.getPrimaryIdentifier().replace("<", ""));
                intent.putExtra(ResultActivity.KEY_GENDER, mrzInfo.getGender().toString());
                intent.putExtra(ResultActivity.KEY_STATE, mrzInfo.getIssuingState());
                intent.putExtra(ResultActivity.KEY_NATIONALITY, mrzInfo.getNationality());

                if (bitmap != null) {
                    if (encodePhotoToBase64) {
                        intent.putExtra(ResultActivity.KEY_PHOTO_BASE64, imageBase64);
                    } else {
                        double ratio = 320.0 / bitmap.getHeight();
                        int targetHeight = (int) (bitmap.getHeight() * ratio);
                        int targetWidth = (int) (bitmap.getWidth() * ratio);

                        intent.putExtra(ResultActivity.KEY_PHOTO,
                                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false));
                    }
                }

                if (getCallingActivity() != null) {
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    startActivity(intent);
                }

            } else {
                textOutput.setText(exceptionStack(result));
            }
        }

    }
}
