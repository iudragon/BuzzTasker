package dragon.bakuman.iu.buzztasker;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SignInActivity extends AppCompatActivity {

    Button customerButton, driverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        customerButton = findViewById(R.id.button_customer);
        driverButton = findViewById(R.id.button_driver);

        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                customerButton.setTextColor(getResources().getColor(android.R.color.white));

                driverButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                driverButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                driverButton.setTextColor(getResources().getColor(android.R.color.white));

                customerButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                customerButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        Button buttonLogin = findViewById(R.id.button_login_fb);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ColorDrawable customerbuttonColor = (ColorDrawable) customerButton.getBackground();
                if (customerbuttonColor.getColor() == getResources().getColor(R.color.colorAccent)) {
                    Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                    startActivity(intent);

                } else {

                    Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
