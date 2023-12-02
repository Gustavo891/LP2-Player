package br.ufrn.imd.player.ControlMain;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import br.ufrn.imd.player.models.VipUser;

/**
 * Controlador para a criação de playlists.
 */
public class CriadorPlaylist {
    protected VipUser usuarioLogado = new VipUser("", "", false, 0, "", "", "");

    /**
     * Define o usuário logado.
     *
     * @param usuarioLogado Usuário logado.
     */
    public void setLogado(VipUser usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    @FXML
    private TextField playlistNameTextField;

    private Stage dialogStage;

    /**
     * Define o palco do diálogo.
     *
     * @param dialogStage Palco do diálogo.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Cria uma nova playlist.
     */
    @FXML
    private void createPlaylist() {
        if (playlistNameTextField != null) {

            boolean confirmar = usuarioLogado.criarPlaylist(playlistNameTextField);
            if (confirmar) {
                closeDialog();
            } else {
                playlistNameTextField.clear();
                playlistNameTextField.deselect();
                playlistNameTextField.setPromptText("Nome inválido. Escreva novamente.");
                System.out.println("Nome de playlist inválido. Por favor, forneça um nome válido.");
            }
        } else {
            System.out.println("Escreva uma playlist no local.");
        }
    }

    /**
     * Fecha o diálogo.
     */
    private void closeDialog() {
        dialogStage.close();
    }
}
