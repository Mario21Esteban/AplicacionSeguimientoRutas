package com.example.seguimientorutas;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterDialogFragment extends DialogFragment {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, cancelButton;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_register, container, false);

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Referencias a los elementos de la vista
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Manejar el registro de usuarios
        registerButton.setOnClickListener(v -> registerUser());

        // Manejar la cancelación
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Registro exitoso. Por favor, inicie sesión.", Toast.LENGTH_LONG).show();
                        dismiss(); // Cerrar el DialogFragment
                    } else {
                        Toast.makeText(getContext(), "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false); // Evitar que se cierre al tocar fuera del diálogo
        return dialog;
    }
}