package com.example.uber2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.uber2.R;
import com.example.uber2.config.ConfiguracaoFirebase;
import com.example.uber2.helper.UsuarioFirebase;
import com.example.uber2.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    EditText editTextNome, editTextEmail, editTextSenha;
    Button buttonCadastra;
    Switch switchTipo;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editTextEmail = findViewById(R.id.editTextCadastroEmail);
        editTextNome = findViewById(R.id.editTextCadastroNome);
        editTextSenha = findViewById(R.id.editTextCadastroSenha);
        buttonCadastra = findViewById(R.id.buttonCadastrar);
        switchTipo = findViewById(R.id.switchTipoUsuario);

        buttonCadastra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCadastroUsuario();
            }
        });

    }


    public void validarCadastroUsuario(){

        String textoNome = editTextNome.getText().toString();
        String textoEmail = editTextEmail.getText().toString();
        String textoSenha = editTextSenha.getText().toString();

        if(!textoNome.isEmpty()){
            
            if (!textoEmail.isEmpty()){
                
                if (!textoSenha.isEmpty()){

                    Usuario usuario = new Usuario();

                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setNome(textoNome);
                    usuario.setTipo(verificaTipoUsuario());

                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(this, "Preencha o campo senha!", Toast.LENGTH_SHORT).show();
                }
                
            }else {
                Toast.makeText(this, "Preencha o campo e-mail", Toast.LENGTH_SHORT).show();
                editTextEmail.requestFocus();
            }
            
        }else {
            Toast.makeText(this, "Preencha o campo nome!", Toast.LENGTH_SHORT).show();
            editTextNome.requestFocus();
        }

    }


    public String verificaTipoUsuario(){
        return switchTipo.isChecked() ? "M" : "P";
    }

    public void cadastrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
          usuario.getEmail(),
          usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    try {

                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        if (verificaTipoUsuario() == "P"){
                            startActivity(new Intent(CadastroActivity.this, MapaPassageiroActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar Passageiro", Toast.LENGTH_SHORT).show();
                        }else{
                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar Motorista", Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{

                    String excecao = "";

                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor, digite um e-mail válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Esta conta já foi cadastrada!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}
