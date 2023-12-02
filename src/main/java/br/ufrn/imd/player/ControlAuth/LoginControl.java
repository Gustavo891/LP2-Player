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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import br.ufrn.imd.player.models.UserFree;
import br.ufrn.imd.player.models.VipUser;

/**
 * Controlador para a tela de login.
 */
public class LoginControl implements Initializable {

    @FXML
    protected Label goRegister;
    @FXML
    protected MFXButton btnAcessar;
    @FXML
    protected TextField usuario;
    @FXML
    protected PasswordField password;
    protected Scene cenavip, cenacomum, cenaregistro;
    protected PremiumControl controllerVIP;
    protected FreeControl controllerComum;
    @FXML
    protected Pane incorreto;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        incorreto.setVisible(false);
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

    /**
     * Lê os usuários registrados a partir de um arquivo JSON.
     *
     * @return Lista de usuários registrados.
     * @throws IOException Exceção de entrada/saída.
     */
    protected List<UserFree> readUsersFromFile() throws IOException {
        File file = new File("registros.json");

        if (!file.exists()) {
            // Se o arquivo não existe, cria um arquivo vazio
            file.createNewFile();
        }

        try (Reader reader = new FileReader("registros.json")) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<List<UserFree>>() {
            }.getType();
            List<UserFree> users = gson.fromJson(reader, userListType);

            if (users == null) {
                System.out.println("Arquivo existe, mas está vazio.");
                return new ArrayList<>();
            }

            return users;
        } catch (IOException e) {
            System.out.println("O arquivo registros.json não pode ser criado");
            return new ArrayList<>();
        }
    }

    /**
     * Alterna para a cena de registro.
     *
     * @param event Evento de mouse que desencadeou o método.
     * @throws IOException Exceção de entrada/saída.
     */
    @FXML
    protected void switchScene(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/panels/registro.fxml"));
        Parent root = loader.load();

        Scene newScene = new Scene(root);

        // Se você quiser fechar a janela atual quando abrir a nova janela
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setScene(newScene);
    }

    @FXML
    protected HBox hboxsenha, hboxusuario;
    @FXML
    protected Label labelusuario, labelsenha;

    /**
     * Efetua o login do usuário.
     *
     * @throws IOException Exceção de entrada/saída.
     */
    @FXML
    protected void Login() throws IOException {
        if (password.getText().isBlank() || usuario.getText().isBlank()) {
            if (password.getText().isBlank() && !usuario.getText().isBlank()) {
                labelsenha.setStyle("-fx-text-fill: red");
                hboxsenha.setStyle("-fx-border-color: red");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            hboxsenha.setStyle("-fx-border-color: black");
                            labelsenha.setStyle("-fx-text-fill: black");
                        })
                );
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            } else if (usuario.getText().isBlank() && !password.getText().isBlank()) {
                hboxusuario.setStyle("-fx-border-color: red");
                labelusuario.setStyle("-fx-text-fill: red");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            hboxusuario.setStyle("-fx-border-color: black");
                            labelusuario.setStyle("-fx-text-fill: black");
                        }));
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            } else {
                hboxusuario.setStyle("-fx-border-color: red");
                hboxsenha.setStyle("-fx-border-color: red");
                labelusuario.setStyle("-fx-text-fill: red");
                labelsenha.setStyle("-fx-text-fill: red");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            hboxusuario.setStyle("-fx-border-color: black");
                            hboxsenha.setStyle("-fx-border-color: black");
                            labelusuario.setStyle("-fx-text-fill: black");
                            labelsenha.setStyle("-fx-text-fill: black");
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
                incorreto.setVisible(true);
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e ->
                                incorreto.setVisible(false))
                );
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
            }
        }
    }

    /**
     * Abre a cena correspondente ao tipo de usuário logado.
     *
     * @param logado Usuário logado.
     */
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

    /**
     * Verifica as credenciais do usuário.
     *
     * @param usuario Usuário informado.
     * @param senha   Senha informada.
     * @return Usuário correspondente às credenciais ou null se não encontrado.
     * @throws IOException Exceção de entrada/saída.
     */
    @FXML
    protected UserFree verificarCredenciais(String usuario, String senha) throws IOException {
        List<UserFree> existingUsers = readUsersFromFile();
        for (UserFree p : existingUsers) {
            if (p.getUsername().equals(usuario) && p.getSenha().equals(senha)) {
                return p;
            }
        }
        return null;
    }
}
