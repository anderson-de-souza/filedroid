package br.com.andersondesouza.filedroid;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import br.com.andersondesouza.filedroid.databinding.ActivityExceptionBinding;
import br.com.andersondesouza.filedroid.databinding.ActivityMainBinding;

public class ExceptionActivity extends AppCompatActivity {

    private ActivityExceptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityExceptionBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (root, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        Window window = getWindow();
        View decorView = window.getDecorView();

        new WindowInsetsControllerCompat(window, decorView)
                .setAppearanceLightStatusBars(false);

        binding.messageView.setText(getIntent().getStringExtra("message"));
        binding.stackTraceView.setMovementMethod(new ScrollingMovementMethod());
        binding.stackTraceView.setText(getIntent().getStringExtra("stackTrace"));
    }
}