package br.ufrn.imd.player.ControlMain;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import br.ufrn.imd.player.models.VipUser;

public class CriadorPlaylist {
    protected VipUser usuarioLogado = new VipUser("", "", false, 0, "", "", "");
    public void setLogado(VipUser usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    @FXML
    private TextField playlistNameTextField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void createPlaylist() {
        if(playlistNameTextField != null) {

            boolean confirmar = usuarioLogado.criarPlaylist(playlistNameTextField);
            if (confirmar) {
                closeDialog();
            } else {
                System.out.println("Nome de playlist inválido. Por favor, forneça um nome válido.");
            }
        } else {
            System.out.println("Escreva uma playlist no local.");
        }
    }

    private void closeDialog() {
        dialogStage.close();
    }
}
