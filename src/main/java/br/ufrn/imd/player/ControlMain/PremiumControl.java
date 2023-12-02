package br.ufrn.imd.player.ControlMain;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.ufrn.imd.player.models.UserFree;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import br.ufrn.imd.player.models.UserDirectories;
import br.ufrn.imd.player.models.VipUser;

public class PremiumControl {

    protected VipUser usuarioLogado = new VipUser("", "", false, 0, "", "", "");
    protected UserDirectories userDirectories = new UserDirectories(0);
    protected boolean playlist = false;
    protected String caminhoFoto;
    protected Boolean foto = false;
    protected boolean diretorio = false;
    protected List<String> directories = new ArrayList<>();
    protected MediaPlayer mediaPlayer;
    protected boolean ConferirArrasto = false;
    /**
     * Define o usuário logado e inicializa o controle.
     *
     * @param usuarioLogado O usuário logado.
     */
    public void setLogado(VipUser usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        System.out.println("Logado ID" + usuarioLogado.getId());
        userDirectories.setUserId(usuarioLogado.getId());
        initialize();
    }
    @FXML
    protected TextField regNome, regEmail, regSenha, editTextfield;
    @FXML
    protected Button botaoFoto;
    @FXML
    protected MFXComboBox testeDiretorio, testePlaylist;
    @FXML
    protected HBox musicTocando, musicPause;
    @FXML
    protected Slider musicProgress2;
    @FXML
    protected Label tempoAtual, autor, tempoTotal, perfilNome, perfilEmail;
    @FXML
    protected ListView<String> musicListView, playlistMusic;
    @FXML
    protected ProgressBar musicProgress;
    @FXML
    protected Pane painelPerfil, painelConfig;
    @FXML
    protected ImageView imagemPerfil, imagemPerfil1, botaoPlay, botaoPausar, checkbox;
    /**
     * Inicializa o controle.
     */
    public void initialize() {

        testePlaylist.setVisible(true);
        editTextfield.setVisible(false);
        checkbox.setVisible(false);
        musicProgress.setVisible(false);
        musicProgress2.setVisible(false);
        painelConfig.setVisible(false);
        painelPerfil.setVisible(true);

        userDirectories.saveDirectoriesToConfig();
        clearlistViews();
        loadDirectoriesFromConfig();
        usuarioLogado.loadPlaylists(testePlaylist);
        testeDiretorio.setOnAction(event -> updateMusicList());
        testePlaylist.setOnAction(event ->
                usuarioLogado.loadPlaylistSongs((String) testePlaylist.getSelectionModel().getSelectedItem(), playlistMusic));

        configureProgressSlider();
        musicTocando.toBack();
        musicPause.toFront();

        playlistMusic.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                // Obtém o item selecionado e o remove da ListView
                String selectedItem = playlistMusic.getSelectionModel().getSelectedItem();
                playlistMusic.getItems().remove(selectedItem);
                usuarioLogado.removerMusica(testePlaylist.getSelectedItem().toString(), selectedItem, playlistMusic);
                stopMusic();
            }
        });

        testeDiretorio.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                loadDirectoriesFromConfig();
            }
            musicListView.setVisible(!newValue);
        });
        testePlaylist.showingProperty().addListener((observable, oldValue, newValue) -> playlistMusic.setVisible(!newValue));
        loadPerfil();
    }
    /**
     * Lê os usuários do arquivo JSON e retorna a lista de usuários.
     *
     * @return A lista de usuários lida do arquivo JSON.
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
     * @param users A lista de usuários a ser escrita.
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
     * Volta para o painel de perfil a partir do painel de configurações.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    public void voltarConfig(MouseEvent mouseEvent) {
        painelPerfil.setVisible(true);
        painelConfig.setVisible(false);
    }
    /**
     * Abre o painel de configurações a partir do painel de perfil.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    public void abrirConfig(MouseEvent mouseEvent) {
        regEmail.setPromptText(usuarioLogado.getEmail());
        regNome.setPromptText(usuarioLogado.getNomeCompleto());
        regSenha.setPromptText(usuarioLogado.getSenha());
        painelConfig.setVisible(true);
        painelPerfil.setVisible(false);
    }
    @FXML
    protected void salvarConfigRegistro(MouseEvent mouseEvent) {
        String novoNome = regNome.getText();
        String novoEmail = regEmail.getText();
        String novaSenha = regSenha.getText();

        if (!novoNome.isEmpty() || !novoEmail.isEmpty() || !novaSenha.isEmpty() || foto) {
            List<UserFree> users = readUsersFromFile();

            // Find the logged-in user in the list
            for (UserFree user : users) {
                if (user.getId() == usuarioLogado.getId()) {
                    // Update user information if fields are not empty
                    if (!novoNome.isEmpty()) {
                        user.setNomeCompleto(novoNome);
                        usuarioLogado.setUsername(novoNome);
                        regNome.setText("");
                    }
                    if (!novoEmail.isEmpty()) {
                        user.setEmail(novoEmail);
                        usuarioLogado.setEmail(novoEmail);
                        regEmail.setText("");
                    }
                    if (!novaSenha.isEmpty()) {
                        user.setSenha(novaSenha);
                        usuarioLogado.setSenha(novaSenha);
                        regSenha.setText("");
                    }
                    if(foto) {
                        user.setUserIcon(caminhoFoto);
                        usuarioLogado.setUserIcon(caminhoFoto);
                    } else {
                        user.setUserIcon(usuarioLogado.getUserIcon());
                    }
                    painelPerfil.setVisible(true);
                    painelConfig.setVisible(false);
                    break; // Stop searching once the user is found and updated
                }
            }

            writeUsersToFile(users);
            loadPerfil();
            System.out.println("O usuário foi atualizado.");
        } else {
            System.out.println("Não teve nada para ser atualizado.");
        }
    }
    public void escolherFoto(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
        );

        // Abre o diálogo de escolha de arquivo
        File selectedFile = fileChooser.showOpenDialog(null);
        // Verifica se um arquivo foi selecionado
        if (selectedFile != null) {
            caminhoFoto = selectedFile.getAbsolutePath();
            Image image = new Image("file:" + caminhoFoto);
            imagemPerfil1.setImage(image);
            foto = true;
        } else {
            foto = false;
            System.out.println("Nenhum arquivo selecionado.");
        }
    }
    /**
     * Carrega os diretórios do usuário a partir do arquivo de configuração.
     */
    protected void loadPerfil() {
        String icon = usuarioLogado.getUserIcon();
        try {
            if(usuarioLogado.getUserIcon().equals("padrao")) {
                String path = "/images/perfil.jpg";
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);

            } else {
                Image image = new Image("file:" + icon);
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);
                System.out.println(usuarioLogado.getUserIcon());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao carregar a imagem: " + e.getMessage());
        }
        perfilEmail.setText(usuarioLogado.getEmail());
        perfilNome.setText(usuarioLogado.getUsername());
    }
    /**
     * Carrega os diretórios do usuário a partir do arquivo de configuração.
     */
    @FXML
    protected void loadDirectoriesFromConfig() {
        testeDiretorio.getItems().clear();
        directories = userDirectories.loadDirectoriesFromConfig();
        if (directories != null) {
            testeDiretorio.getItems().addAll(directories.stream().map(dir -> new File(dir).getName()).toList());
        }

    }
    /**
     * Limpa as listas de músicas.
     */
    protected void clearlistViews() {
        musicListView.getItems().clear();
        testePlaylist.getItems().clear();
        testeDiretorio.getItems().clear();
    }
    /**
     * Os três metodos abaixam pegam o DragEvent onde você arrasta a música dos diretórios
     * até a playlist desejada.
     */
    @FXML
    protected void playlistMusicDragDropped(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        boolean success = false;

        if (dragboard.hasString()) {
            String musicName = dragboard.getString();
            String selectedDirectoryPath = directories.get(testeDiretorio.getSelectionModel().getSelectedIndex());
            String selectedMusicPath = selectedDirectoryPath + File.separator + musicName + ".mp3";

            usuarioLogado.addMusicToPlaylist((String) testePlaylist.getSelectionModel().getSelectedItem(), selectedMusicPath, playlistMusic);

            success = true;
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }
    @FXML
    protected void playlistMusicDragOver(DragEvent dragEvent) {
        if (dragEvent.getGestureSource() != playlistMusic && dragEvent.getDragboard().hasString()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
        ConferirArrasto = false;
    }
    @FXML
    protected void musiclistViewDetected(MouseEvent mouseEvent) {
        ConferirArrasto = true;

        String selectedMusic = musicListView.getSelectionModel().getSelectedItem();
        if (selectedMusic != null) {
            Dragboard dragboard = musicListView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedMusic);
            dragboard.setContent(content);
        }
        mouseEvent.consume();
    }
    /**
     * Avança para a próxima música na lista.
     */
    @FXML
    protected void proximaMusica() {
        if(!playlist) {
            int selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex < musicListView.getItems().size() - 1) {
                musicListView.getSelectionModel().selectNext();
                selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
                playSelectedMusic(selectedIndex);
            } else {
                musicListView.getSelectionModel().selectFirst();
                playSelectedMusic(0);
            }
        } else {
            int selectedIndex = playlistMusic.getSelectionModel().getSelectedIndex();
            if (selectedIndex < playlistMusic.getItems().size() - 1) {
                playlistMusic.getSelectionModel().selectNext();
                String selectedMusic = playlistMusic.getSelectionModel().getSelectedItem();
                playSelectedPlaylistMusic(selectedMusic);
            } else {
                playlistMusic.getSelectionModel().selectFirst();
                String selectedMusic = playlistMusic.getSelectionModel().getSelectedItem();
                playSelectedPlaylistMusic(selectedMusic);
            }
        }
    }
    /**
     * Reproduz a música anterior na lista.
     */
    @FXML
    protected void musicaAnterior() {
        if(!playlist) {
            int selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex  != 0) {
                musicListView.getSelectionModel().selectPrevious();
            } else {
                musicListView.getSelectionModel().selectLast();
            }
            selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
            playSelectedMusic(selectedIndex);
        } else {
            int selectedIndex = playlistMusic.getSelectionModel().getSelectedIndex();
            if (selectedIndex != 0) {
                playlistMusic.getSelectionModel().selectPrevious();
            } else {
                playlistMusic.getSelectionModel().selectLast();
            }
            String selectedMusic = playlistMusic.getSelectionModel().getSelectedItem();
            playSelectedPlaylistMusic(selectedMusic);
        }
    }
    /**
     * Toda a música selecionada da playlist.
     */
    @FXML
    protected void playSelectedPlaylistMusic(String selectedMusicName) {
        musicListView.getSelectionModel().clearSelection();
        playlist = true;
        diretorio = false;
        int selectedIndex = playlistMusic.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < playlistMusic.getItems().size()) {
            String selectedPlaylist = (String) testePlaylist.getSelectionModel().getSelectedItem();
            String selectedMusicPath = usuarioLogado.getPlaylistMusicPath(selectedPlaylist, selectedMusicName);
            stopMusic();

            try {
                Media media = new Media(new File(selectedMusicPath).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                autor.setText(playlistMusic.getSelectionModel().getSelectedItem());

                configureProgressSlider();
                setupProgressBar(media);

                mediaPlayer.setOnReady(() -> {
                    Duration totalDuration = media.getDuration();
                    int totalMinutes = (int) totalDuration.toMinutes();
                    int totalSeconds = (int) (totalDuration.toSeconds() % 60);
                    String tempoTotalString = String.format("%02d:%02d", totalMinutes, totalSeconds);
                    tempoTotal.setText(tempoTotalString);
                });

                mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
                    mediaPlayer.stop();
                    mediaPlayer.seek(Duration.ZERO);
                    musicProgress2.setValue(0);
                    musicProgress.setProgress(0);
                    tempoAtual.setText("");
                    autor.setText("");
                    mediaPlayer.pause();
                    musicTocando.toBack();
                    musicPause.toFront();
                    // Toca a próxima música quando a atual terminar
                    proximaMusica();
                }));


                mediaPlayer.play();
                musicPause.toBack();
                musicTocando.toFront();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Não foi possível reproduzir a música da playlist: " + selectedMusicName);
            }
        }
    }
    /**
     * Configura o controle do slider do progresso da música.
     */
    @FXML
    protected void configureProgressSlider() {
        if (mediaPlayer != null) {
            musicProgress2.getStyleClass().add("slider");
            musicProgress2.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (musicProgress2.isValueChanging()) {
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    Duration seekTime = totalDuration.multiply(newValue.doubleValue() / 100.0);
                    int minutes = (int) seekTime.toMinutes();
                    int seconds = (int) (seekTime.toSeconds() % 60);
                    String tempo = String.format("%02d:%02d", minutes, seconds);
                    tempoAtual.setText(tempo);
                    mediaPlayer.seek(seekTime);
                }
            });

            musicProgress2.setOnMouseClicked(event -> {
                if (!musicProgress2.isValueChanging()) {
                    double mouseX = event.getX();
                    double sliderWidth = musicProgress2.getWidth();
                    double newValue = (mouseX / sliderWidth) * 100.0;
                    musicProgress2.setValue(newValue);

                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    Duration seekTime = totalDuration.multiply(newValue / 100.0);
                    int minutes = (int) seekTime.toMinutes();
                    int seconds = (int) (seekTime.toSeconds() % 60);
                    String tempo = String.format("%02d:%02d", minutes, seconds);
                    tempoAtual.setText(tempo);
                    mediaPlayer.seek(seekTime);
                }
            });
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!musicProgress2.isValueChanging()) {
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    double progress = newValue.toMillis() / totalDuration.toMillis() * 100.0;
                    musicProgress2.setValue(progress);
                }
            });
        }
    }
    /**
     * Manipula eventos do botão de reprodução/pausa da música.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    @FXML
    protected void botaoPlayer(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();
        if(source.equals(botaoPausar)) {
            mediaPlayer.pause();
            musicTocando.toBack();
            musicPause.toFront();

        } else if(source.equals(botaoPlay)) {
            if (isMusicPlaying()) {
                mediaPlayer.play();
                musicTocando.toFront();
                musicPause.toBack();
            } else {
                System.out.println("nenhuma música tocando");
            }
        }

    }
    /**
     * Verifica se alguma música está sendo reproduzida.
     *
     * @return true se uma música estiver sendo reproduzida, false caso contrário.
     */
    @FXML
    protected boolean isMusicPlaying() {
        return mediaPlayer != null &&
                (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED);
    }
    /**
     * Manipula o clique em uma música na lista de músicas do diretório atual.
     */
    @FXML
    protected void clicouMusica2() {
        String selectedMusic = musicListView.getSelectionModel().getSelectedItem();
        if (selectedMusic != null && !ConferirArrasto) {
            playSelectedMusic(musicListView.getSelectionModel().getSelectedIndex());
        }
    }
    /**
     * Reproduz a música selecionada na lista de músicas do diretório atual.
     */
    @FXML
    protected void playSelectedMusic(int selectedIndex) {
        playlistMusic.getSelectionModel().clearSelection();
        playlist = false;
        diretorio = true;
        if (!ConferirArrasto) {
            if (selectedIndex >= 0 && selectedIndex < musicListView.getItems().size()) {
                String selectedMusicName = musicListView.getItems().get(selectedIndex);
                String selectedDirectoryPath = directories.get(testeDiretorio.getSelectionModel().getSelectedIndex());
                String selectedMusicPath = selectedDirectoryPath + File.separator + selectedMusicName + ".mp3";

                stopMusic();

                try {
                    Media media = new Media(new File(selectedMusicPath).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    autor.setText(selectedMusicName);
                    musicProgress.setVisible(true);
                    musicProgress2.setVisible(true);

                    mediaPlayer.setOnReady(() -> {
                        Duration totalDuration = media.getDuration();
                        int totalMinutes = (int) totalDuration.toMinutes();
                        int totalSeconds = (int) (totalDuration.toSeconds() % 60);
                        String tempoTotalString = String.format("%02d:%02d", totalMinutes, totalSeconds);
                        tempoTotal.setText(tempoTotalString);
                    });

                    configureProgressSlider();
                    setupProgressBar(media);

                    mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
                        stopMusic();
                        // Toca a próxima música quando a atual terminar
                        proximaMusica();
                    }));

                    mediaPlayer.play();
                    musicPause.toBack();
                    musicTocando.toFront();

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Não foi possível reproduzir a música: " + selectedMusicName);
                }
            }
        } else {
            System.out.println("Musica não pode ser tocado pois está sendo arrastada até um destino.");
        }
    }
    /**
     * Reproduz a música selecionada na lista de músicas da playlist atual.
     */
    @FXML
    protected void clicouMusica() {
        String selectedMusic = playlistMusic.getSelectionModel().getSelectedItem();
        if (selectedMusic != null) {
            playSelectedPlaylistMusic(selectedMusic);
        }
    }
    /**
     * Configura a barra de progresso para uma determinada mídia.
     *
     * @param media A mídia a ser configurada.
     */
    @FXML
    protected void setupProgressBar(Media media) {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue.toSeconds() / media.getDuration().toSeconds();
            musicProgress.setProgress(progress);
            int minutes = (int) newValue.toMinutes();
            int seconds = (int) (newValue.toSeconds() % 60);
            String tempoAtualString = String.format("%02d:%02d", minutes, seconds);
            tempoAtual.setText(tempoAtualString);
        });

    }
    /**
     * Para a reprodução da música atual.
     */
    @FXML
    protected void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
            musicProgress2.setValue(0);
            musicProgress.setProgress(0);
            musicProgress.setVisible(false);
            musicProgress2.setVisible(false);
            tempoAtual.setText("");
            autor.setText("");
            mediaPlayer.pause();
            musicTocando.toBack();
            musicPause.toFront();
        }
    }
    /**
     * Abre o seletor de diretórios para escolher um novo diretório de músicas.
     */
    @FXML
    protected void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Escolher diretório de músicas");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            userDirectories.addDirectory(selectedDirectory.getAbsolutePath());
            userDirectories.saveDirectoriesToConfig();
            loadDirectoriesFromConfig();
            testeDiretorio.getSelectionModel().selectIndex(userDirectories.getDirectories().size() - 1);
            updateMusicList();
        }
    }
    /**
     * Atualiza a lista de músicas com base no diretório selecionado.
     */
    @FXML
    protected void updateMusicList() {
        musicListView.getItems().clear();
        int selectedIndex = testeDiretorio.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < directories.size()) {
            String selectedDirectoryPath = directories.get(selectedIndex);
            File selectedDirectory = new File(selectedDirectoryPath);
            File[] mp3Files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (mp3Files != null) {
                for (File mp3File : mp3Files) {
                    musicListView.getItems().add(removeFileExtension(mp3File.getName()));
                }
            }
        }
    }
    /**
     * Remove a extensão de arquivo de um nome de arquivo.
     *
     * @param fileName O nome do arquivo.
     * @return O nome do arquivo sem a extensão.
     */
    @FXML
    protected String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    /**
     * Atualiza a combobox para ver se uma playlist nova foi selecionada.
     */
    public void clicouComboBox() {
        updateMusicList();
    }
    /**
     * Chamao metodo para deletar uma playlist ao clicar no botão da lixeira.
     * @param mouseEvent
     */
    public void deletarPlaylist(MouseEvent mouseEvent) {
        if(testePlaylist.getSelectedItem() != null) {
            System.out.println(testePlaylist.getValue() + " removido");
            usuarioLogado.deletePlaylist(testePlaylist.getSelectedItem().toString());
            testePlaylist.getItems().clear();
            playlistMusic.getItems().clear();
        } else {
            System.out.println("nada selecionado");
        }
    }
    /**
     * Chama a tela para criar uma playlist nova.
     * @param event
     */
    @FXML
    protected void createPlaylist(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/panels/CriarPlaylist.fxml"));
            Parent root = loader.load();

            CriadorPlaylist controller = loader.getController();
            controller.setLogado(usuarioLogado);
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            controller.setDialogStage(dialogStage);

            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png")));
            dialogStage.getIcons().add(appIcon);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setTitle("MusicFX - Crie sua playlist!");
            dialogStage.showAndWait();
            usuarioLogado.loadPlaylists(testePlaylist);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    /**
     * Salva o edit da playlist.
     * @param mouseEvent
     */
    public void salvarEdit(MouseEvent mouseEvent) {
        String playlistAtual = testePlaylist.getSelectedItem().toString();
        String novoNome = editTextfield.getText();

        boolean conferir = usuarioLogado.editarPlaylist(playlistAtual, novoNome);
        if (conferir) {
            testePlaylist.setVisible(true);
            editTextfield.setVisible(false);
            checkbox.setVisible(false);
            testePlaylist.getItems().clear();

            if (!playlistAtual.equals(novoNome)) {
                playlistMusic.getItems().clear();
                usuarioLogado.loadPlaylists(testePlaylist);
            }
        } else {
            System.out.println("Erro");
            editTextfield.clear();
            editTextfield.deselect();
            editTextfield.setPromptText("Nome inválido. Tente novamente.");
        }
    }
    /**
     * Abre o painel para que o usuário digite o novo nome da playlist.
     * @param mouseEvent
     */
    public void editarPlaylist(MouseEvent mouseEvent) {

        if (testePlaylist.getSelectedItem() != null) {
            testePlaylist.setVisible(false);
            editTextfield.setText(testePlaylist.getSelectedItem().toString());
            editTextfield.setVisible(true);
            checkbox.setVisible(true);
        } else {
            System.out.println("nada selecionado");
        }
    }
}
