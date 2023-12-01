package br.ufrn.imd.player.ControlAuth;

import br.ufrn.imd.player.ControlMain.FreeControl;
import br.ufrn.imd.player.ControlMain.PremiumControl;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import javafx.util.Duration;
import br.ufrn.imd.player.models.UserFree;
import br.ufrn.imd.player.models.VipUser;

public class LoginControl implements Initializable {

    @FXML
    protected Label goRegister;
    @FXML
    protected MFXButton btnAcessar;
    @FXML
    protected TextField usuario;
    @FXML
    protected PasswordField password;
    protected Scene cenavip,cenacomum, cenaregistro;
    protected PremiumControl controllerVIP;
    protected FreeControl controllerComum;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader vipLoader = new FXMLLoader(getClass().getResource("/panels/PlayerVIP.fxml"));
            Parent rootVIP = vipLoader.load();
            cenavip = new Scene(rootVIP);
            controllerVIP = vipLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FXMLLoader comumLoader = new FXMLLoader(getClass().getResource("/panels/PlayerComum.fxml"));
            Parent rootComum = comumLoader.load();
            cenacomum = new Scene(rootComum);
            controllerComum = comumLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<UserFree> readUsersFromFile() {
        try (Reader reader = new FileReader("registros.json")) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<List<UserFree>>() {}.getType();
            List<UserFree> users = gson.fromJson(reader, userListType);

            if (users == null) {
                System.out.println("Arquivo existe, mas está vazio.");
                return new ArrayList<>();
            }

            return users;
        } catch (IOException e) {
            System.out.println("Erro ao tentar ler o arquivo.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @FXML
    protected void switchScene(MouseEvent event) throws IOException {
        System.out.print("Go Register");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/panels/registro.fxml"));
        Parent root = loader.load();

        Scene newScene = new Scene(root);

        // Se você quiser fechar a janela atual quando abrir a nova janela
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setScene(newScene);
    }

    @FXML
    protected void Login() throws IOException {
        if (password.getText().isBlank() || usuario.getText().isBlank()) {
            if (password.getText().isBlank() && !usuario.getText().isBlank()) {
                password.getStyleClass().add("borda-vermelha");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> password.getStyleClass().remove("borda-vermelha"))
                );
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            } else if (usuario.getText().isBlank() && !password.getText().isBlank()) {
                usuario.getStyleClass().add("borda-vermelha");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> usuario.getStyleClass().remove("borda-vermelha"))
                );
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            } else {
                usuario.getStyleClass().add("borda-vermelha");
                password.getStyleClass().add("borda-vermelha");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            usuario.getStyleClass().remove("borda-vermelha");
                            password.getStyleClass().remove("borda-vermelha");
                        })
                );
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            }


        } else {
            UserFree Logado = verificarCredenciais(usuario.getText(), password.getText());
            if (Logado != null) {
                abrirCena(Logado);
            } else {
                // Credenciais incorretas, exibir mensagem de erro
            }
        }
    }

    protected void abrirCena(UserFree logado) {
        Scene currentScene = usuario.getScene();
        Stage currentStage = (Stage) currentScene.getWindow();

        if (logado.isVIP) {
            VipUser vip = new VipUser(
                    logado.getUsername(),
                    logado.getSenha(),
                    true,
                    logado.getId(),
                    logado.getUserIcon(),
                    logado.getNomeCompleto(),
                    logado.getEmail()
            );
            currentStage.setScene(cenavip);
            controllerVIP.setLogado(vip);
        } else {
            currentStage.setScene(cenacomum);
            controllerComum.setLogado(logado);
        }
    }
    @FXML
    protected UserFree verificarCredenciais(String usuario, String senha) {
        List<UserFree> existingUsers = readUsersFromFile();
        for(UserFree p:existingUsers){
            if (p.getUsername().equals(usuario) && p.getSenha().equals(senha)) {
                return p;
            }
        }

        return null;
    }

}