package br.ufrn.imd.player.ControlAuth;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.StageStyle;
import br.ufrn.imd.player.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import javafx.util.Duration;
import br.ufrn.imd.player.models.UserFree;

/**
 * Controlador para a tela de registro de usuários.
 */
public class RegistroControl implements Initializable {

    @FXML
    protected Pane pnlLogin, painelVIP, painelCriada;
    @FXML
    protected MFXButton botaoRegistrar, botaoAdquirir, botaoVIP, botaoVoltar;
    @FXML
    protected TextField regUsuario, regNome, regEmail;
    @FXML
    protected PasswordField regSenha;
    @FXML
    protected HBox CaixaEmail, CaixaNome, CaixaUsuario, CaixaSenha;
    @FXML
    protected Label contaVIP, labelNome, labelEmail, labelUsuario, labelSenha, labelVIP, nomeCompleto, usuario, senha, email;

    protected boolean adquiriuVIP;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pnlLogin.setVisible(true);
        painelCriada.setVisible(false);
        painelVIP.setVisible(false);
        try {
            FXMLLoader vipLoader = new FXMLLoader(getClass().getResource("/panels/Login.fxml"));
            Parent rootVIP = vipLoader.load();
            cenaLogin = new Scene(rootVIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lê os usuários registrados a partir de um arquivo JSON.
     *
     * @return Lista de usuários registrados.
     */
    protected List<UserFree> readUsersFromFile() {
        File file = new File("registros.json");

        if (!file.exists()) {
            // Se o arquivo não existe, cria um arquivo vazio
            writeUsersToFile(new ArrayList<>());
        }

        try (Reader reader = new FileReader(file)) {
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

    /**
     * Escreve a lista de usuários no arquivo JSON.
     *
     * @param users Lista de usuários a ser gravada.
     */
    protected void writeUsersToFile(List<UserFree> users) {
        File file = new File("registros.json");

        try {
            // Cria o arquivo se não existir
            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new FileWriter(file)) {
                Gson gson = new Gson();
                gson.toJson(users, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exibe um alerta na interface gráfica.
     *
     * @param titulo   Título do alerta.
     * @param mensagem Mensagem do alerta.
     * @param icon     Ícone do alerta.
     */
    @FXML
    protected void exibirAlerta(String titulo, String mensagem, Image icon) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        alert.getDialogPane().setGraphic(imageView);

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/src/main/css/alerta.css").toExternalForm()); // Substitua pelo seu arquivo de estilo

        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    /**
     * Registra um novo usuário.
     *
     * @param actionEvent Evento de ação que desencadeou o método.
     */
    @FXML
    protected void registrarAgora(ActionEvent actionEvent) {
        if (validar()) {
            boolean already_reg = false;
            List<UserFree> existingUsers = readUsersFromFile();

            UserFree u = new UserFree(regUsuario.getText(),
                    regSenha.getText(),
                    adquiriuVIP,
                    existingUsers.size() + 1,
                    "padrao",
                    regNome.getText(),
                    regEmail.getText()
            );

            for (User p : existingUsers) {
                if (p.getUsername().equals(u.getUsername())) {
                    System.out.println("Usuário já registrado");
                    already_reg = true;
                    Image icon2 = new Image(getClass().getResourceAsStream("/images/hat3.png"));
                    exibirAlerta("Usuário Já Cadastrado", "O usuário já está cadastrado!", icon2);
                }
                if (already_reg) {
                    return;
                }
            }

            if (!already_reg) {
                existingUsers.add(u);

                writeUsersToFile(existingUsers);
                nomeCompleto.setText(regNome.getText());
                usuario.setText(regUsuario.getText());
                senha.setText(regSenha.getText());
                email.setText(regEmail.getText());
                if (adquiriuVIP) {
                    contaVIP.setVisible(true);
                } else {
                    contaVIP.setVisible(false);
                }
                painelCriada.setVisible(true);
                painelVIP.setVisible(false);
                pnlLogin.setVisible(false);
            }

        } else {
            System.out.println("Algum método não foi registrado.");
        }
    }

    /**
     * Valida os campos do formulário de registro.
     *
     * @return true se os campos são válidos, false caso contrário.
     */
    public boolean validar() {
        int teste = 0;
        if (regSenha.getText().isBlank() || regUsuario.getText().isBlank() || regNome.getText().isBlank() || regEmail.getText().isBlank()) {
            // senha está vazia
            if (regSenha.getText().isBlank()) {
                labelSenha.setStyle("-fx-text-fill: #e70000");
                CaixaSenha.setStyle("-fx-border-color: #e70000;");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            labelSenha.setStyle("-fx-text-fill: black");
                            CaixaSenha.setStyle("-fx-border-color: black;");
                        }));
                hideLabel2.setCycleCount(1);
                hideLabel2.play();

                teste = 1;
                System.out.println("Senha em branco.");
            }
            // usuário está vazio
            if (regUsuario.getText().isBlank()) {
                labelUsuario.setStyle("-fx-text-fill: #e70000");
                CaixaUsuario.setStyle("-fx-border-color: #e70000;");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            labelUsuario.setStyle("-fx-text-fill: black");
                            CaixaUsuario.setStyle("-fx-border-color: black;");
                        }));
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
                teste = 1;
            }
            // email está vazio
            if (regEmail.getText().isBlank()) {
                labelEmail.setStyle("-fx-text-fill: #e70000");
                CaixaEmail.setStyle("-fx-border-color: #e70000;");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            labelEmail.setStyle("-fx-text-fill: black");
                            CaixaEmail.setStyle("-fx-border-color: black;");
                        }));
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
                teste = 1;
            }
            // nome está vazio
            if (regNome.getText().isBlank()) {
                labelNome.setStyle("-fx-text-fill: #e70000");
                CaixaNome.setStyle("-fx-border-color: #e70000;");
                Timeline hideLabel2 = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            labelNome.setStyle("-fx-text-fill: black");
                            CaixaNome.setStyle("-fx-border-color: black;");
                        }));
                hideLabel2.setCycleCount(1);
                hideLabel2.play();
                teste = 1;
            }
            // termos estão vazios
            return false;
        } else {
            return true;
        }
    }

    protected Scene cenaLogin;

    /**
     * Alterna para a cena de login.
     *
     * @param event Evento de mouse que desencadeou o método.
     * @throws IOException Exceção de entrada/saída.
     */
    @FXML
    protected void switchScene(MouseEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setScene(cenaLogin);
    }

    /**
     * Ativa o status VIP do usuário.
     *
     * @param actionEvent Evento de ação que desencadeou o método.
     */
    public void adquirirVIP(ActionEvent actionEvent) {
        adquiriuVIP = true;
        labelVIP.setText("  O seu vip foi ativado com sucesso.");
        pnlLogin.setVisible(true);
        painelVIP.setVisible(false);
        painelCriada.setVisible(false);
    }

    /**
     * Volta para a tela de login.
     *
     * @param actionEvent Evento de ação que desencadeou o método.
     */
    public void voltar(ActionEvent actionEvent) {
        pnlLogin.setVisible(true);
        painelVIP.setVisible(false);
        painelCriada.setVisible(false);
    }

    /**
     * Abre a tela VIP.
     *
     * @param actionEvent Evento de ação que desencadeou o método.
     */
    public void abrirVIP(ActionEvent actionEvent) {
        painelVIP.setVisible(true);
        painelCriada.setVisible(false);
        pnlLogin.setVisible(false);
    }

    /**
     * Volta para a tela de login.
     *
     * @param actionEvent Evento de ação que desencadeou o método.
     */
    public void voltarLogin(ActionEvent actionEvent) {
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.setScene(cenaLogin);
    }
}
