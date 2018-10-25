package jackg.myreceipts;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.bignerdranch.android.myreceipts.R;

public class Help extends AppCompatActivity {


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        WebView webView = findViewById(R.id.help_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("https://en.wikipedia.org/wiki/Receipt");

        Button btnBack = findViewById(R.id.back_to_receipts_btn);
        btnBack.setOnClickListener(v -> finish());


    }


}